package org.protempa;

/*
 * #%L
 * Protempa Framework
 * %%
 * Copyright (C) 2012 - 2015 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import org.protempa.query.QueryValidationException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.arp.javautil.arrays.Arrays;
import org.protempa.backend.dsb.filter.Filter;
import org.protempa.dest.Destination;
import org.protempa.dest.QueryResultsHandler;
import org.protempa.dest.QueryResultsHandlerCloseException;
import org.protempa.dest.QueryResultsHandlerInitException;
import org.protempa.dest.QueryResultsHandlerProcessingException;
import org.protempa.dest.QueryResultsHandlerValidationFailedException;
import org.protempa.proposition.Proposition;
import org.protempa.query.Query;

/**
 *
 * @author Andrew Post
 */
final class Executor implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(Executor.class.getName());
    private final Set<String> propIds;
    private final Filter filters;
    private final PropositionDefinition[] propDefs;
    private final KnowledgeSource ks;
    private final Query query;
    private Collection<PropositionDefinition> propositionDefinitionCache;
    private final AbstractionFinder abstractionFinder;
    private ExecutionStrategy executionStrategy;
    private final Destination destination;
    private QueryResultsHandler resultsHandler;
    private boolean failed;
    private final MessageFormat logMessageFormat;
    private HandleQueryResultThread handleQueryResultThread;
    private boolean canceled;
    private final List<QueryException> exceptions;

    Executor(Query query, Destination resultsHandlerFactory, AbstractionFinder abstractionFinder) throws QueryException {
        this.abstractionFinder = abstractionFinder;
        assert query != null : "query cannot be null";
        assert resultsHandlerFactory != null : "resultsHandlerFactory cannot be null";
        assert abstractionFinder != null : "abstractionFinder cannot be null";
        if (abstractionFinder.isClosed()) {
            throw new QueryException(query.getName(), new ProtempaAlreadyClosedException());
        }
        this.propIds = Arrays.asSet(query.getPropositionIds());
        this.filters = query.getFilters();
        this.propDefs = query.getPropositionDefinitions();
        if (propDefs != null && propDefs.length > 0) {
            ks = new KnowledgeSourceImplWrapper(abstractionFinder.getKnowledgeSource(), propDefs);
        } else {
            ks = abstractionFinder.getKnowledgeSource();
        }
        this.query = query;
        this.destination = resultsHandlerFactory;
        this.logMessageFormat = new MessageFormat("Query " + this.query.getName() + ": {0}");
        this.exceptions = new ArrayList<>();
    }

    void init() throws QueryException {
        try {
            createQueryResultsHandler();

            if (isLoggable(Level.FINE)) {
                log(Level.FINE, "Propositions to be queried are {0}", StringUtils.join(this.propIds, ", "));
            }
            extractPropositionDefinitionCache();

            try {
                if (hasSomethingToAbstract(query) || this.query.getDatabasePath() != null) {
                    selectExecutionStrategy();
                    initializeExecutionStrategy();
                }
            } catch (QueryValidationException ex) {
                throw new QueryException(query.getName(), ex);
            }

            startQueryResultsHandler();
        } catch (KnowledgeSourceReadException | QueryResultsHandlerValidationFailedException | QueryResultsHandlerInitException | QueryResultsHandlerProcessingException | Error | RuntimeException ex) {
            this.failed = true;
            throw new QueryException(this.query.getName(), ex);
        }
    }

    void cancel() {
        synchronized (this) {
            if (this.handleQueryResultThread != null) {
                this.handleQueryResultThread.interrupt();
            }
            this.canceled = true;
        }
        log(Level.INFO, "Canceled");
    }

    void execute() throws QueryException {
        try {
            RetrieveDataThread retrieveDataThread;
            DoProcessThread doProcessThread;
            synchronized (this) {
                if (this.canceled) {
                    return;
                }
                log(Level.INFO, "Processing data");
                DataStreamingEvent doProcessPoisonPill = new DataStreamingEvent("poison", Collections.emptyList());
                QueueObject hqrPoisonPill = new QueueObject();
                BlockingQueue<DataStreamingEvent<Proposition>> doProcessQueue
                        = new ArrayBlockingQueue<>(1000);
                BlockingQueue<QueueObject> hqrQueue
                        = new ArrayBlockingQueue<>(1000);
                retrieveDataThread = new RetrieveDataThread(doProcessQueue,
                        doProcessPoisonPill, this.query, 
                        this.abstractionFinder.getDataSource(), 
                        this.propositionDefinitionCache,
                        this.filters, this.resultsHandler);
                doProcessThread = new DoProcessThread(doProcessQueue, hqrQueue,
                        doProcessPoisonPill, hqrPoisonPill, this.query,
                        this.executionStrategy, retrieveDataThread);
                this.handleQueryResultThread
                        = new HandleQueryResultThread(hqrQueue, hqrPoisonPill, doProcessThread, this.query, this.resultsHandler);
                retrieveDataThread.start();
                doProcessThread.start();
                this.handleQueryResultThread.start();
            }

            try {
                retrieveDataThread.join();
                this.exceptions.addAll(retrieveDataThread.getExceptions());
                log(Level.INFO, "Done retrieving data");
            } catch (InterruptedException ex) {
                log(Level.FINER, "Protempa producer thread join interrupted", ex);
            }
            try {
                doProcessThread.join();
                int count = doProcessThread.getCount();
                log(Level.INFO, "Processed {0}", count);
                this.exceptions.addAll(doProcessThread.getExceptions());
                log(Level.INFO, "Done processing data");
            } catch (InterruptedException ex) {
                log(Level.FINER, "Protempa consumer thread join interrupted", ex);
            }
            try {
                this.handleQueryResultThread.join();
                this.exceptions.addAll(this.handleQueryResultThread.getExceptions());
                log(Level.INFO, "Done outputting results");
            } catch (InterruptedException ex) {
                log(Level.FINER, "Protempa consumer thread join interrupted", ex);
            }

            if (!exceptions.isEmpty()) {
                throw exceptions.get(0);
            }
        } catch (QueryException ex) {
            this.failed = true;
            throw ex;
        }
    }

    @Override
    public void close() throws CloseException {
        try {
            if (executionStrategy != null) {
                executionStrategy.shutdown();
            }
            // Might be null if init() fails.
            if (this.resultsHandler != null) {
                if (!this.failed) {
                    this.resultsHandler.finish();
                }
                this.resultsHandler.close();
                this.resultsHandler = null;
            }
        } catch (QueryResultsHandlerProcessingException
                | QueryResultsHandlerCloseException
                | ExecutionStrategyShutdownException ex) {
            throw new CloseException(ex);
        } finally {
            if (this.resultsHandler != null) {
                try {
                    this.resultsHandler.close();
                } catch (QueryResultsHandlerCloseException ignore) {

                }
            }
        }
    }

    boolean isLoggable(Level level) {
        return LOGGER.isLoggable(level);
    }

    void log(Level level, String msg, Object[] params) {
        if (isLoggable(level)) {
            LOGGER.log(level, this.logMessageFormat.format(new Object[]{msg}), params);
        }
    }

    void log(Level level, String msg, Object param) {
        if (isLoggable(level)) {
            LOGGER.log(level, this.logMessageFormat.format(new Object[]{msg}), param);
        }
    }

    void log(Level level, String msg, Throwable throwable) {
        if (isLoggable(level)) {
            LOGGER.log(level, this.logMessageFormat.format(new Object[]{msg}), throwable);
        }
    }

    void log(Level level, String msg) {
        if (isLoggable(level)) {
            LOGGER.log(level, this.logMessageFormat.format(new Object[]{msg}));
        }
    }

    private boolean hasSomethingToAbstract(Query query) throws QueryValidationException {
        try {
            KnowledgeSource ks = this.abstractionFinder.getKnowledgeSource();
            if (!ks.readAbstractionDefinitions(query.getPropositionIds()).isEmpty()
                    || !ks.readContextDefinitions(query.getPropositionIds()).isEmpty()) {
                return true;
            }
            for (PropositionDefinition propDef : query.getPropositionDefinitions()) {
                if (propDef instanceof AbstractionDefinition || propDef instanceof ContextDefinition) {
                    return true;
                }
            }
            return false;
        } catch (KnowledgeSourceReadException ex) {
            throw new QueryValidationException("Invalid proposition id(s) " + StringUtils.join(query.getPropositionIds(), ", "), ex);
        }
    }

    private void selectExecutionStrategy() {
        if (this.query.getDatabasePath() != null) {
            log(Level.FINER, "Chosen stateful execution strategy");
            this.executionStrategy = new StatefulExecutionStrategy(
                    this.abstractionFinder.getAlgorithmSource(),
                    this.query);
        } else {
            log(Level.FINER, "Chosen stateless execution strategy");
            this.executionStrategy = new StatelessExecutionStrategy(
                    this.abstractionFinder.getAlgorithmSource());
        }
    }

    private void initializeExecutionStrategy() throws QueryException {
        try {
            this.executionStrategy.initialize(this.propositionDefinitionCache);
        } catch (ExecutionStrategyInitializationException ex) {
            throw new QueryException(query.getName(), ex);
        }
    }

    private void extractPropositionDefinitionCache() throws KnowledgeSourceReadException {
        this.propositionDefinitionCache = this.ks.collectPropDefDescendantsUsingAllNarrower(false, this.propIds.toArray(new String[this.propIds.size()]));

        if (isLoggable(Level.FINE)) {
            Set<String> allNarrowerDescendantsPropIds = new HashSet<>();
            for (PropositionDefinition pd : this.propositionDefinitionCache) {
                allNarrowerDescendantsPropIds.add(pd.getId());
            }
            log(Level.FINE, "Proposition details: {0}", StringUtils.join(allNarrowerDescendantsPropIds, ", "));
        }
    }

    private void startQueryResultsHandler() throws QueryResultsHandlerProcessingException {
        log(Level.FINE, "Calling query results handler start...");
        this.resultsHandler.start(this.propositionDefinitionCache);
        log(Level.FINE, "Query results handler started");
        log(Level.FINE, "Query results handler waiting for results...");
    }

    private void createQueryResultsHandler() throws QueryResultsHandlerValidationFailedException, QueryResultsHandlerInitException {
        log(Level.FINE, "Initializing query results handler...");
        this.resultsHandler = this.destination.getQueryResultsHandler(this.query, this.abstractionFinder.getDataSource(), this.ks, this.abstractionFinder.getEventListeners());
        log(Level.FINE, "Got query results handler {0}", this.resultsHandler.getId());
        log(Level.FINE, "Validating query results handler");
        this.resultsHandler.validate();
        log(Level.FINE, "Query results handler validated successfully");
    }

}
