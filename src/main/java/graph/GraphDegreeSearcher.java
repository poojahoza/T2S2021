package main.java.graph;

/**
 * @author poojaoza
 **/

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.LinkedHashMap;
import java.util.Map;


public class GraphDegreeSearcher {

    Graph<String, DefaultEdge> entity_graph = null;

    public Graph generateGraph(Map<String, String> entity_list){
        entity_graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        createVertexList(entity_list);
        createEdgeList(entity_list);
        return entity_graph;
    }

    public Map<String, Integer> getNodeDegree(Graph g){

        Map<String, Integer> entity_degree = new LinkedHashMap<>();
        for(Object v:g.vertexSet()){
            entity_degree.put(v.toString(),entity_graph.degreeOf(v.toString()));
        }
        return entity_degree;
    }

    void createVertexList(Map<String, String> entity_list)
    {
        for(String v:entity_list.keySet()){
            entity_graph.addVertex(v);
        }
    }

    void createEdgeList(Map<String, String> entity_list)
    {
        for (Map.Entry<String, String> e: entity_list.entrySet()){
            String [] outlinks_list = e.getValue().split("[\r\n]+");
            for(int l = 0; l < outlinks_list.length; l++){
                for(String eid:entity_list.keySet()){
                    if(eid.equals(outlinks_list[l])){
                        entity_graph.addEdge(outlinks_list[l], eid);
                    }
                }
            }
        }
    }
}


