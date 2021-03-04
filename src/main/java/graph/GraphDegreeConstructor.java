package main.java.graph;

/**
 * @author poojaoza
 **/

import main.java.utils.SortUtils;
import org.jgrapht.Graph;

import java.util.LinkedHashMap;
import java.util.Map;

public class GraphDegreeConstructor {

    public Map<String, Map<String, Integer>> getGraphDegree(Map<String, Map<String, String>> input)
    {
        GraphDegreeSearcher graph = new GraphDegreeSearcher();
        Map<String, Map<String, Integer>> query_entity_degree = new LinkedHashMap<>();

        for(Map.Entry<String, Map<String, String>> m:input.entrySet()) {
            Graph g = graph.generateGraph(m.getValue());
            Map<String, Integer> degree_list = graph.getNodeDegree(g);
            degree_list = SortUtils.sortByValue(degree_list);

            query_entity_degree.put(m.getKey(), degree_list);
        }
        return query_entity_degree;
    }
}
