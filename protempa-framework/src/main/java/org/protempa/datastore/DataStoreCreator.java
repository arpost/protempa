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

import java.io.IOException;
import org.eurekaclinical.datastore.DataStore;

/**
 * An interface for defining classes that return external data stores.
 * 
 * @author Michel Mansour
 * 
 * @param <K>
 *            the key type to store
 * @param <V>
 *            the value type to store
 */
public interface DataStoreCreator<K, V> {

    /**
     * Returns a permanent store with the given name. If a store with the name
     * exists, it may be returned. Otherwise, a new store with the name will be
     * created.
     * 
     * @param name
     *            the name of the store
     * @return a {@link DataStore} backed by a permanent store implementation
     */
    DataStore<K, V> getPersistentStore();

}
