package edu.duke.ra.core;

/**
 * An interface to get the result of an RA query
 *
 */
public interface IQueryResult {

    /**
     * Get the raw string representation of the result of the query 
     * 
     * @return The raw, unstructured string result
     */
    String toRawString();

    /**
     * Return a JSON representation of the result as a string
     * 
     * @return A JSON string representing the result
     */
    String toJsonString();
}