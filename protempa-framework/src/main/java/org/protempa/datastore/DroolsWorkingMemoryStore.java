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
package org.protempa.datastore;

import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.eurekaclinical.datastore.bdb.BdbPersistentStoreFactory;

import org.eurekaclinical.datastore.DataStore;
import org.drools.RuleBase;
import org.drools.WorkingMemory;

/**
 * A data store mapping key IDs to Drools working memory objects. This
 * specialized class is necessary because of the way Drools and BerkeleyDB
 * handle object serialization. Drools expects to deserialize its objects in a
 * custom way that BerkeleyDB breaks. The solution is to convert the working
 * memory to a byte array and have BDB serialize that instead. The byte array
 * can then be deserialized into a Drools rule base, which can generate the
 * original working memory.
 *
 * @author Michel Mansour
 *
 */
final class DroolsWorkingMemoryStore implements
        DataStore<String, WorkingMemory> {

    private final DataStore<String, byte[]> store;
    private boolean isClosed;

    /*
     * Drools rule base. Required to recreate the original working memory from a
     * byte array.
     */
    private final RuleBase ruleBase;

    DroolsWorkingMemoryStore(BdbPersistentStoreFactory storeFactory, String dbName, RuleBase ruleBase) {
        store = storeFactory.newInstance(dbName);
        this.isClosed = false;
        this.ruleBase = ruleBase;
    }

    @Override
    public void shutdown() {
        this.store.shutdown();
        this.isClosed = true;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Converts a byte array to the original Drools working memory it
     * represents. This is done using a Drools rule base, which can generate a
     * stateful session from a byte array.
     *
     * @param barr a byte array containing the working memory.
     * @return the working memory.
     *
     * @throws IOError if an error occurs reading the working memory.
     */
    private WorkingMemory readWorkingMemory(byte[] barr) {
        try {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(barr)) {
                return ruleBase.newStatefulSession(bais, false);
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new IOError(ex);
        }
    }

    @Override
    public WorkingMemory get(Object key) {
        WorkingMemory retval;
        if (key instanceof String) {
            retval = readWorkingMemory(this.store.get(key));
        } else {
            retval = null;
        }
        return retval;
    }

    /**
     * Puts the working memory into the data store.
     *
     * @param key the keyId.
     * @param value the working memory.
     * @return the working memory.
     *
     * @throws IOError if an error occurs writing the key and working memory to
     * the data store.
     */
    @Override
    public WorkingMemory put(String key, WorkingMemory value) {
        /*
         * Converts a Drools working memory into a byte array for storage by
         * BerkeleyDB. This is necessary because the BDB serializes objects
         * interferes with the custom way in which Drools expects to deserialize
         * them.
         */
        try {
            this.store.put(key, SerializationUtils.serialize(value));
            return value;
        } catch (SerializationException ex) {
            throw new IOError(ex);
        }
    }

    @Override
    public int size() {
        return this.store.size();
    }

    @Override
    public boolean isEmpty() {
        return this.store.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.store.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.store.containsValue(value);
    }

    @Override
    public WorkingMemory remove(Object key) {
        return readWorkingMemory(this.store.remove(key));
    }

    /**
     * Writes all of the keyId-working memory pairs in the given map to the data
     * store.
     *
     * @param m a map of keyId-working memory pairs.
     *
     * @throws IOError if an error occurs writing a working memory to the data
     * store.
     */
    @Override
    public void putAll(Map<? extends String, ? extends WorkingMemory> m) {
        for (Entry<? extends String, ? extends WorkingMemory> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        this.store.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.store.keySet();
    }

    /**
     * Gets the working memories from this data store. Depending on how many
     * working memories that have been saved, this operation could exhaust
     * memory!
     *
     * @return a collection of working memories.
     * @throws IOError if an error occurred while reading the working memories.
     */
    @Override
    public Collection<WorkingMemory> values() {
        Collection<WorkingMemory> values = new ArrayList<>();
        for (byte[] barr : this.store.values()) {
            values.add(readWorkingMemory(barr));
        }

        return Collections.unmodifiableCollection(values);
    }

    /**
     * Returns a collection view of the map. It reads the working memories
     * lazily. Each call to {@link Map.Entry#getValue() } could throw an IOError
     * if an error occurs while reading the entry's working memory from the data
     * store.
     *
     * @return a collection view of the map.
     */
    @Override
    public Set<java.util.Map.Entry<String, WorkingMemory>> entrySet() {
        Set<java.util.Map.Entry<String, WorkingMemory>> entrySet = new HashSet<>();

        for (String key : this.store.keySet()) {
            entrySet.add(new LazyEntry(key));
        }

        return entrySet;
    }

    private class LazyEntry implements Map.Entry<String, WorkingMemory> {

        private final String key;

        public LazyEntry(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        /**
         * Gets this map entry's working memory.
         *
         * @return the working memory.
         *
         * @throws IOError if there was an error reading the working memory.
         */
        @Override
        public WorkingMemory getValue() {
            return readWorkingMemory(store.get(this.key));
        }

        @Override
        public WorkingMemory setValue(WorkingMemory value) {
            throw new UnsupportedOperationException("setValue not supported");
        }

    }

}
