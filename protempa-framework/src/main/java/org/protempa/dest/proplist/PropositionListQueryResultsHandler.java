/*
 * #%L
 * Protempa Framework
 * %%
 * Copyright (C) 2012 - 2013 Emory University
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
package org.protempa.dest.proplist;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.arp.javautil.string.StringUtil;
import org.protempa.ProtempaException;
import org.protempa.dest.AbstractQueryResultsHandler;
import org.protempa.dest.QueryResultsHandlerProcessingException;
import org.protempa.proposition.AbstractParameter;
import org.protempa.proposition.Constant;
import org.protempa.proposition.Event;
import org.protempa.proposition.Parameter;
import org.protempa.proposition.PrimitiveParameter;
import org.protempa.proposition.Proposition;
import org.protempa.proposition.TemporalProposition;
import org.protempa.proposition.UniqueId;
import org.protempa.proposition.visitor.AbstractPropositionCheckedVisitor;

/**
 * An implementation of QueryResultsHandler providing functionality for writing
 * the results to an output stream in a tab-delimited format.
 *
 * @author Michel Mansour
 *
 */
public final class PropositionListQueryResultsHandler extends AbstractQueryResultsHandler {
    private static final char COLUMN_DELIMITER = '\t';
    private final List<? extends Comparator<Proposition>> comparator;
    private final boolean includeDerived;
    private final TabDelimHandlerPropositionVisitor visitor;

    /**
     * Instantiates this handler to write to a {@link Writer}. No sorting will
     * be performed.
     *
     * @param out a {@link Writer}.
     */
    public PropositionListQueryResultsHandler(BufferedWriter out) {
        this(out, null);
    }

    public PropositionListQueryResultsHandler(BufferedWriter out, boolean includeDerived) {
        this(out, null, includeDerived);
    }

    /**
     * Instantiates this handler to write to a {@link Writer} with optional
     * sorting of propositions.
     *
     * @param out a {@link Writer}.
     * @param comparator a {@link List<? extends Comparator<Proposition>>}. Every key's
     * propositions will be sorted by the provided comparators in the order they
     * are given. A value of <code>null</code> or an empty array means no
     * sorting will be performed.
     */
    public PropositionListQueryResultsHandler(BufferedWriter out,
            List<? extends Comparator<Proposition>> comparator) {
        this(out, comparator, false);
    }

    public PropositionListQueryResultsHandler(BufferedWriter writer,
            List<? extends Comparator<Proposition>> comparator,
            boolean includeDerived) {
        this.visitor = new TabDelimHandlerPropositionVisitor(writer);
        this.includeDerived = includeDerived;
        if (comparator == null) {
            this.comparator = Collections.emptyList();
        } else {
            this.comparator
                    = new ArrayList<>(comparator);
        }
    }

    /**
     * Writes a keys worth of data in tab delimited format optionally
     * sorted.
     *
     * @param key a key id {@link String}.
     * @param propositions a {@link List<Proposition>}.
     * @throws QueryResultsHandlerProcessingException if an error
     * occurred writing to the specified file, output stream or writer.
     */
    @Override
    public void handleQueryResult(String key, List<Proposition> propositions, 
            Map<Proposition, Set<Proposition>> forwardDerivations, 
            Map<Proposition, Set<Proposition>> backwardDerivations,
            Map<UniqueId, Proposition> references) throws QueryResultsHandlerProcessingException {
        Set<Proposition> propositionsAsSet = new HashSet<>();
        addDerived(propositions, forwardDerivations, backwardDerivations, propositionsAsSet);
        List<Proposition> propositionsCopy = new ArrayList<>(propositionsAsSet);
        for (Comparator<Proposition> c : this.comparator) {
            Collections.sort(propositionsCopy, c);
        }
        this.visitor.setKeyId(key);
        try {
            this.visitor.visit(propositionsCopy);
        } catch (TabDelimHandlerProtempaException pe) {
            throw new QueryResultsHandlerProcessingException(pe);
        } catch (ProtempaException pe) {
            throw new AssertionError(pe);
        }
    }

    private void addDerived(List<Proposition> propositions, 
            Map<? extends Proposition, ? extends Set<? extends Proposition>> forwardDerivations, 
            Map<? extends Proposition, ? extends Set<? extends Proposition>> backwardDerivations, 
            Set<Proposition> propositionsAsSet) {
        List<Proposition> derivedProps = new ArrayList<>();
        for (Proposition prop : propositions) {
            boolean added = propositionsAsSet.add(prop);
            if (added && this.includeDerived) {
                derivedProps.addAll(forwardDerivations.get(prop));
                derivedProps.addAll(backwardDerivations.get(prop));
                addDerived(derivedProps, forwardDerivations, backwardDerivations, propositionsAsSet);
                derivedProps.clear();
            }
        }
    }
    
    private final static class TabDelimHandlerProtempaException
            extends ProtempaException {

        private static final long serialVersionUID = 2008992530872178708L;

        TabDelimHandlerProtempaException(IOException cause) {
            super(cause);
            assert cause != null : "cause cannot be null";
        }
    }

    private final static class TabDelimHandlerPropositionVisitor
            extends AbstractPropositionCheckedVisitor {

        private String keyId;
        private final BufferedWriter writer;

        TabDelimHandlerPropositionVisitor(
                BufferedWriter writer) {
            this.writer = writer;
        }

        void setKeyId(String keyId) {
            this.keyId = keyId;
        }

        @Override
        public void visit(AbstractParameter abstractParameter)
                throws TabDelimHandlerProtempaException {
            try {
                doWriteKeyId();
                doWritePropId(abstractParameter);
                doWriteValue(abstractParameter);
                doWriteTime(abstractParameter);
                this.writer.newLine();
            } catch (IOException ioe) {
                throw new TabDelimHandlerProtempaException(ioe);
            }
        }

        @Override
        public void visit(Event event)
                throws TabDelimHandlerProtempaException {
            try {
                doWriteKeyId();
                doWritePropId(event);
                this.writer.write(COLUMN_DELIMITER);
                doWriteTime(event);
                this.writer.newLine();
            } catch (IOException ioe) {
                throw new TabDelimHandlerProtempaException(ioe);
            }
        }

        @Override
        public void visit(PrimitiveParameter primitiveParameter)
                throws TabDelimHandlerProtempaException {
            try {
                doWriteKeyId();
                doWritePropId(primitiveParameter);
                doWriteValue(primitiveParameter);
                doWriteTime(primitiveParameter);
                this.writer.newLine();
            } catch (IOException ioe) {
                throw new TabDelimHandlerProtempaException(ioe);
            }
        }

        @Override
        public void visit(Constant constant)
                throws TabDelimHandlerProtempaException {
            try {
                doWriteKeyId();
                doWritePropId(constant);
                this.writer.write(COLUMN_DELIMITER);
                this.writer.newLine();
            } catch (IOException ioe) {
                throw new TabDelimHandlerProtempaException(ioe);
            }
        }

        private void doWriteKeyId() throws IOException {
            StringUtil.escapeAndWriteDelimitedColumn(this.keyId,
                    COLUMN_DELIMITER, this.writer);
            this.writer.write(COLUMN_DELIMITER);
        }

        private void doWritePropId(Proposition proposition)
                throws IOException {
            StringUtil.escapeAndWriteDelimitedColumn(proposition.getId(),
                    COLUMN_DELIMITER, this.writer);
            this.writer.write(COLUMN_DELIMITER);
        }

        private void doWriteValue(Parameter parameter) throws IOException {
            StringUtil.escapeAndWriteDelimitedColumn(
                    parameter.getValueFormatted(), COLUMN_DELIMITER,
                    this.writer);
            this.writer.write(COLUMN_DELIMITER);
        }

        private void doWriteTime(TemporalProposition proposition)
                throws IOException {
            StringUtil.escapeAndWriteDelimitedColumn(
                    proposition.getStartFormattedShort(),
                    COLUMN_DELIMITER, this.writer);
            this.writer.write(COLUMN_DELIMITER);
            String finish = proposition.getFinishFormattedShort();
            if (!finish.isEmpty()) {
                StringUtil.escapeAndWriteDelimitedColumn(finish, COLUMN_DELIMITER,
                        this.writer);
            }
        }
    }

    
}
