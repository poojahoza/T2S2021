package main.java.graph;

/**
 * @author poojaoza
 **/

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;

import main.java.reranker.WordEmbedding;

import java.util.LinkedHashMap;
import java.util.Map;

public class GraphSimilaritySearcher extends GraphDegreeSearcher {

    WordEmbedding we = null;

    public Graph generateGraph(Map<String, String> entity_list,
                               Integer dim,
                               String embedding_file,
                               Map<String, Double> entity_weight){
        entity_graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        we = new WordEmbedding(dim, embedding_file);
        //Map<String, Double> entity_weight = new LinkedHashMap<>();
        createVertexList(entity_list);
        this.createEdgeList(entity_list, entity_weight);

        return entity_graph;
    }

    void createEdgeList(Map<String, String> entity_list,
                        Map<String, Double> entity_weight){
        String[][] edge_array = new String[entity_list.size()][2];
        int counter = 0;
        for(Map.Entry<String, String> m: entity_list.entrySet())
        {
            edge_array[counter][0] = m.getKey();
            edge_array[counter][1] = m.getValue();
            counter++;
        }
        for(int i = 0; i < edge_array.length; i++)
        {
            for(int j = i+1; j < edge_array.length; j++)
            {
                Double cos_sim = we.getSimilarity(edge_array[i][1], edge_array[j][1]);
                if(cos_sim >= 0.5)
                {
                    entity_graph.addEdge(edge_array[i][0], edge_array[j][0]);
                    entity_graph.setEdgeWeight(edge_array[i][0], edge_array[j][0], cos_sim);
                    if(entity_weight.containsKey(edge_array[i][0]))
                    {
                        Double score = entity_weight.get(edge_array[i][0]);
                        entity_weight.put(edge_array[i][0], score+cos_sim);
                    }else{
                        entity_weight.put(edge_array[i][0], cos_sim);
                    }

                    if(entity_weight.containsKey(edge_array[j][0]))
                    {
                        Double score = entity_weight.get(edge_array[j][0]);
                        entity_weight.put(edge_array[j][0], score+cos_sim);
                    }else{
                        entity_weight.put(edge_array[j][0], cos_sim);
                    }
                }
            }
        }
    }
}
