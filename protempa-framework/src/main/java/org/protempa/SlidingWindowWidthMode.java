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
package org.protempa;

/**
 * For specifying whether a low-level abstraction definition's or aggregation
 * definition's sliding window width should be the algorithm's default
 * (DEFAULT), the entire width of a time series (ALL), or a specified minimum
 * and maximum number of values (RANGE).
 * 
 * @author Andrew Post
 */
public enum SlidingWindowWidthMode {
	DEFAULT, ALL, RANGE
}
