/*
 * #%L
 * Protempa Framework
 * %%
 * Copyright (C) 2012 Emory University
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
package org.protempa;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.arp.javautil.datastore.DataStore;
import org.drools.WorkingMemory;
import org.protempa.datastore.WorkingMemoryStoreCreator;
import org.protempa.proposition.Proposition;

/**
 * This is a helper class for testing {@link AbstractionFinder}. It provides
 * intermediate results of Protempa's execution phases to classes implementing
 * tests of Protempa's functionality.
 * 
 * @author Michel Mansour
 * 
 */
public final class AbstractionFinderTestHelper {

    private DataStore<String, DerivationsBuilder> dbStore;
    private final String wmStoreName;

    /**
     * Initializes a new instance using a working memory store name.
     * 
     * @param workingMemoryStoreName
     *            the name of the persistent store that will hold the working
     *            memories
     */
    public AbstractionFinderTestHelper(String workingMemoryStoreName) {
        this.wmStoreName = workingMemoryStoreName;
    }

    /**
     * Runs the rule processing phase of Protempa and returns the persisted
     * working memories generated during the execution for testing purposes.
     * 
     * @param p
     *            the Protempa instance to get the various sources to pass to
     *            the AbstractionFinder
     * @param keyIds
     *            the key IDs to process
     * @param propositionIds
     *            the propositions to process
     * @param qs
     *            the query session
     * @param propositionStoreName
     *            the name of persistent store where the retrieved data is
     * @return a {@link DataStore} mapping key IDs to working memories as a
     *         result of the rules engine processing
     * @throws FinderException
     *             if something goes wrong in AbstractionFinder
     * @throws KnowledgeSourceReadException
     *             if the {@link KnowledgeSource} cannot be read
     * @throws ProtempaException
     *             if a general problem occurs within Protempa
     */
    public DataStore<String, WorkingMemory> processStoredResults(Protempa p,
            Set<String> keyIds, Set<String> propositionIds, QuerySession qs,
            String propositionStoreName) throws FinderException,
            KnowledgeSourceReadException, ProtempaException {
        AbstractionFinder af = new AbstractionFinder(p.getDataSource(),
                p.getKnowledgeSource(), p.getAlgorithmSource(),
                p.getTermSource(), true);
        af.processStoredResults(keyIds, propositionIds, qs,
                propositionStoreName, this.wmStoreName);
        StatefulExecutionStrategy strategy = new StatefulExecutionStrategy(af);
        strategy.createRuleBase(propositionIds, new DerivationsBuilder(), qs);

        // The derivations builder store needs to be acquired here instead of in
        // the constructor because AbstractionFinder.processStoredResults() uses
        // a data store with the same name and closes the store when it's done.
        // Getting the store here forces it to reopen.
        this.dbStore = DerivationsBuilderStoreCreator.getInstance()
                .getPersistentStore(this.wmStoreName);

        return WorkingMemoryStoreCreator.getInstance(strategy.ruleBase)
                .getPersistentStore(this.wmStoreName);
    }

    /**
     * Gets the forward derivations associated with a key ID
     * 
     * @param keyId
     *            the key ID to look up
     * @return the forward derivations as a {@link Map} from {@link Proposition}
     *         to a {@link List} of <tt>Proposition</tt>s.
     */
    public Map<Proposition, List<Proposition>> getForwardDerivations(
            String keyId) {
        return getDerivationsBuilder(keyId).toForwardDerivations();
    }

    /**
     * Gets the backward derivations associated with a key ID
     * 
     * @param keyId
     *            the key ID to look up
     * @return the backward derivations as a {@link Map} from
     *         {@link Proposition} to a {@link List} of <tt>Proposition</tt>s.
     */
    public Map<Proposition, List<Proposition>> getBackwardDerivations(
            String keyId) {
        return getDerivationsBuilder(keyId).toBackwardDerivations();
    }

    private DerivationsBuilder getDerivationsBuilder(String keyId) {
        return this.dbStore.get(keyId);
    }

    /**
     * Performs any clean-up tasks. Should be called when this object is no
     * longer needed.
     */
    public void cleanUp() {
        this.dbStore.shutdown();
    }
}
