package main.java.mrf;

import main.java.containers.Container;
import main.java.rerankerv2.concepts.EmbeddingStrategy;
import main.java.searcher.BaseBM25;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;


import java.io.IOException;
import java.util.ArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



public class Evidences
{
    /*
        Evidence is to return the BM25 score of query-document pair
    */
     static ArrayList<Double> evidence1(Map<String, Container> list)
    {
        ArrayList<Double> res = new ArrayList<Double>();

        for(Map.Entry<String,Container> val: list.entrySet())
        {
            res.add(val.getValue().getScore());
        }
        return Normalize.getZScoreNormalized(res);
    }

    /*
        Evidence 2 is to return the query-document cosine score.
        Each document is build using embedding vector of some dimension

    */
     static ArrayList<Double> evidence2(Map<String, Container> list, EmbeddingStrategy embedding,Integer Dimension,String Query,String indexLoc,Integer kval)
    {
        ArrayList<Double> res = new ArrayList<Double>();
        BaseBM25 bm = null;
        try {
           bm = new BaseBM25(kval,indexLoc);
        } catch (IOException e) {
            e.printStackTrace();
        }

        INDArray queryVector = MrfHelper.getVector(Query,embedding,Dimension);

        for(Map.Entry<String,Container> val: list.entrySet())
        {

            int DocID = val.getValue().getDocID();
            INDArray doc = MrfHelper.getVector(bm.getDocument(DocID),embedding,Dimension);
            res.add(Transforms.cosineDistance(queryVector,doc));
        }
        return Normalize.getZScoreNormalized(res);
    }

    static  List<ArrayList<Double>> collectiveEvidence(Map<String, Container> list, EmbeddingStrategy embedding,Integer Dimension,String Query,String indexLoc,Integer kval)
    {
        List<ArrayList<Double>> collective = new ArrayList<ArrayList<Double>>(2);

        ArrayList<Double> bmScore = new ArrayList<>();
        ArrayList<Double> qdScore = new ArrayList<>();



        BaseBM25 bm = null;
        try {
            bm = new BaseBM25(kval,indexLoc);
        } catch (IOException e) {
            e.printStackTrace();
        }

        INDArray queryVector = MrfHelper.getVector(Query,embedding,Dimension);

        for(Map.Entry<String,Container> val: list.entrySet())
        {
            bmScore.add(val.getValue().getScore());
            int DocID = val.getValue().getDocID();
            INDArray doc = MrfHelper.getVector(bm.getDocument(DocID),embedding,Dimension);
            Double cosScore = Transforms.cosineDistance(queryVector,doc);
            qdScore.add(cosScore);
        }

        collective.add(Normalize.getZScoreNormalized(bmScore));
        collective.add(Normalize.getZScoreNormalized(qdScore));

        return collective;
    }

    /*
        Finds the cluster score, for each document, it finds the k nearest documents
        Computes the centroid of the K nearest document and find the cosine similarity with the query
    */
    static ArrayList<Double> evidence3(Map<String, Container> list, EmbeddingStrategy embedding,Integer Dimension,String Query,String indexLoc,Integer kval)
    {
        ArrayList<Double> res = new ArrayList<>();

        for(Map.Entry<String,Container> outer:list.entrySet())
        {
            Map<String,Container> unsorted = new LinkedHashMap<String, Container>();
            BaseBM25 bm25 = null;
            try {
                 bm25 = new BaseBM25(kval,indexLoc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Integer docIDOuter = outer.getValue().getDocID();
            INDArray docOuter = MrfHelper.getVector(bm25.getDocument(docIDOuter),embedding,Dimension);


            for(Map.Entry<String,Container> inner:list.entrySet())
            {
                if(!outer.getKey().equals(inner.getKey()))
                {
                    Integer docIDInner = inner.getValue().getDocID();
                    INDArray docInner= MrfHelper.getVector(bm25.getDocument(docIDInner),embedding,Dimension);
                    Double simscore = Transforms.cosineSim(docOuter,docInner);
                    Container tempc = new Container(simscore, inner.getValue().getDocID());
                    unsorted.put(inner.getKey(),tempc);

                }
            }

            /*
                call to getClusterScore returns the score between centroid and the query vector
            */
            Double d = MrfHelper.getClusterScore(unsorted,embedding,Dimension,Query,indexLoc,kval);
            res.add(d);
        }
        return Normalize.getZScoreNormalized(res);
    }
}
