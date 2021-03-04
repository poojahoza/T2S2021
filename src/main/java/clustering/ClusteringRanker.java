package main.java.clustering;

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.mrf.MrfHelper;
import main.java.reranker.WordEmbedding;
import main.java.rerankerv2.concepts.EmbeddingStrategy;
import main.java.searcher.BaseBM25;
import org.apache.commons.lang3.time.StopWatch;
import org.deeplearning4j.clustering.cluster.Cluster;
import org.deeplearning4j.clustering.cluster.ClusterSet;
import org.deeplearning4j.clustering.kmeans.KMeansClustering;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.clustering.cluster.Point;
import org.nd4j.linalg.exception.ND4JIllegalStateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/*
Perform the clustering ReRanking
*/
public class ClusteringRanker
{
    private RegisterCommands.CommandSearch SearchCommand = null;
    private Map<String,String> query = null;
    private EmbeddingStrategy embedding =null;
    public ClusteringRanker(RegisterCommands.CommandSearch SearchCommand, Map<String,String> query)
    {
        this.SearchCommand = SearchCommand;
        this.query = query;
        embedding = new WordEmbedding(SearchCommand.getDimension(),SearchCommand.getWordEmbeddingFile());
    }


    private List<Point> getPoints(Map<String, Container> uncluster,BaseBM25 bm,EmbeddingStrategy embed,Integer Dimenion)
    {
        StopWatch sw = new StopWatch();
        List<Point> points = new ArrayList<>();

            for(Map.Entry<String,Container> val:uncluster.entrySet())
            {
                try
                {
                    int docID = val.getValue().getDocID();
                    INDArray docVector = MrfHelper.getVector(bm.getDocument(docID),embed,Dimenion);
                    if(docVector.columns()!=Dimenion)
                    {
                        System.out.println("Vector size did not match");
                        System.exit(-1);
                    }
                    points.add(new Point(val.getKey(),docVector));
                    System.out.println(docVector);

                } catch (ND4JIllegalStateException ne)
                {
                    System.out.println("Caught on "+ val.getKey());
                }
            }
        return points;
    }

    private void getClusters(List<Point> pointlist)
    {
        int maxIterationCount = 10;
        int clusterCount = 5;
        String distanceFunction = "cosinesimilarity";

        StopWatch sw = new StopWatch();

        sw.reset();
        sw.start();
        KMeansClustering kmc = KMeansClustering.setup(clusterCount, maxIterationCount, distanceFunction);
        ClusterSet cs = kmc.applyTo(pointlist);
        System.out.println("Time taken to run clustering on " +pointlist.size()+" Time :" + sw.getTime());

        List<Cluster> clsterLst = cs.getClusters();
        System.out.println("Number of cluster = "+ clsterLst.size());
        System.out.println("\nCluster Centers:");
        for(Cluster c: clsterLst) {
            Point center = c.getCenter();
            System.out.println(center.getId() +"Points in each cluster = "+ c.getPoints().size());
        }
    }
    public  void doCluster()
    {
        Map<String,Map<String,Container>> res = new LinkedHashMap<String,Map<String,Container>>();

        long start= System.currentTimeMillis();
        StreamSupport.stream(query.entrySet().spliterator(),SearchCommand.isParallelEnabled())
                .forEach(q -> {
                    try {
                        BaseBM25 bm = new BaseBM25(SearchCommand.getkVAL(),SearchCommand.getIndexlocation());
                        Map<String, Container> retDoc = bm.getRanking(q.getValue());
                        List<Point> pointlist = getPoints(retDoc,bm,embedding,SearchCommand.getDimension());
                        getClusters(pointlist);
                        System.out.print(".");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        long end = System.currentTimeMillis();
        long timeElapsed = end-start;
        System.out.println("Time took :"+ (double)timeElapsed/1000 +" sec "+ ((double)timeElapsed/1000)/60 +" min");
    }
}
