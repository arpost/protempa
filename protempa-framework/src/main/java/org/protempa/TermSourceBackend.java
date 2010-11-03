package org.protempa;

import java.util.Map;

/**
 * Interface for term source backends
 * 
 * @author Michel Mansour
 */
public interface TermSourceBackend extends
        Backend<TermSourceBackendUpdatedEvent, TermSource> {

    /**
     * Reads a term from the given terminology
     * 
     * @param id
     *            the term to find
     * @return a {@link Term} matching the given term in the given terminology
     * @throws TermSourceReadException
     *             if the {@link TermSource} is unreadable
     */
    Term readTerm(String id) throws TermSourceReadException;

    /**
     * Reads an array of terms from the given terminology
     * 
     * @param ids
     *            the terms to find
     * @return an array of {@link Term}s matching the given terms in the given
     *         terminology
     * @throws TermSourceReadException
     *             if the {@link TermSource} is unreadable
     */
    Map<String, Term> readTerms(String[] ids) throws TermSourceReadException;
}