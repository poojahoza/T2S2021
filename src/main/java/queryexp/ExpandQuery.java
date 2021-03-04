package main.java.queryexp;

import main.java.containers.Container;

import java.util.Map;

/**
 * All query expansion class implements this interface
 */
public interface ExpandQuery {

    /**
     * This is the public method can be called, this performs the query expansion
     * and writes to the run files
     */
    public void doQueryExpansion();

    public void doQueryExpansion(Map<String, Map<String, Container>> input);
    /**
     * Method should run for all the queries and return the expanded results.
     * This is the interface for anyone who wants to access it the results
     *
     * @return returns the result of the query expansion
     */
    public Map<String, Map<String, Container>> getExpandedQuery();

}
