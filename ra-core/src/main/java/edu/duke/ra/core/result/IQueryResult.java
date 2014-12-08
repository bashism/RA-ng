package edu.duke.ra.core.result;

/**
 * An interface to get the result of an RA query
 *
 */
public interface IQueryResult {

    /**
     * Return a JSON representation of the result as a string, including the
     * results and any error messages
     * 
     * @return A JSON string representing the result
     */
    public String toJsonString();

}