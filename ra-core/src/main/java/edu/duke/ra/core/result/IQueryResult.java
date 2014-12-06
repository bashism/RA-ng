package edu.duke.ra.core.result;

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
    public String toRawString();

    /**
     * Return a JSON representation of the result as a string
     * 
     * @return A JSON string representing the result
     */
    public String toJsonString();
    
    /**
     * Check whether the command requested the client to quit
     * 
     * @return Whether to quit
     */
    public boolean quit(); 
}