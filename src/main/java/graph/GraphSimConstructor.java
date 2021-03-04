package main.java.graph;

/**
 * @author poojaoza
 **/

import main.java.utils.SortUtils;
import org.jgrapht.Graph;

import java.util.LinkedHashMap;
import java.util.Map;

public class GraphSimConstructor {

    public Map<String, Map<String, Double>> getGraphDegree(Map<String, Map<String, String>> input,
                                                            Integer dim,
                                                            String embedding_file_loc)
    {
        GraphSimilaritySearcher graph = new GraphSimilaritySearcher();
        Map<String, Map<String, Double>> query_entity_degree = new LinkedHashMap<>();

        for(Map.Entry<String, Map<String, String>> m:input.entrySet()) {
            Map<String, Double> query_entity_score = new LinkedHashMap<>();
            Graph g = graph.generateGraph(m.getValue(), dim, embedding_file_loc, query_entity_score);
            Map<String, Integer> degree_list = graph.getNodeDegree(g);
            query_entity_score = SortUtils.sortByValue(query_entity_score);

            query_entity_degree.put(m.getKey(), query_entity_score);
        }
        return query_entity_degree;
    }

}
