package main.java.wordsimilarityranker;

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.searcher.BaseBM25;
import main.java.utils.PreProcessor;
import main.java.utils.SortUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

/*
A Base class which implements all functionality.
*/
abstract  public class SimilarityBase
{
    private  RegisterCommands.CommandSearch Searchcommand = null;
    private Map<String,String> query = null;

    SimilarityBase(RegisterCommands.CommandSearch Searchcommand, Map<String,String> query)
    {
        this.Searchcommand = Searchcommand;
        this.query = query;
    }

    /*
        Abstract methods needs to be implemented by other methods
    */
    abstract  public double getScore(String str1,String str2);

    /*
    This will perform the re-rank for each query candidate set.
    */

    private Map<String, Container> performRankerOnCandidate(String Query, Map<String, Container> candidate,BaseBM25 bm) {

        //if the map has only one value, return as-is
        if(candidate.size()<2) return candidate;

        //Preprocessed query terms
        ArrayList<String> querylist = null;
        try {
            querylist = PreProcessor.processTermsUsingLucene(Query);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String,Container> result = new LinkedHashMap<>();

        for(Map.Entry<String,Container> candid : candidate.entrySet())
        {
            double score = 0.0;
            int docID = candid.getValue().getDocID();
            ArrayList<String> canidateterms = null;
            try {
                canidateterms = PreProcessor.processTermsUsingLucene(bm.getDocument(docID));
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(String qterm: querylist)
            {
                for(String cterm: canidateterms)
                {
                    score+= getScore(qterm,cterm);
                }
            }
            Container temp = new Container(score,candid.getValue().getDocID());
            result.put(candid.getKey(),temp);
        }
        return SortUtils.sortByValue(result);
    }


    /*
        For each query, it retrieves the initial BM25 candidate sets and calls performRankerOnCandidate to rerank
        based on the String similarity.
    */
//    protected Map<String,Map<String,Container>>  performReRank()
//    {
//        Map<String,Container> candidate = null;
//        Map<String,Container> candidateRanked = null;
//
//        Map<String,Map<String,Container>> result = new LinkedHashMap<>();
//
//        for(Map.Entry<String,String> Q: query.entrySet())
//        {
//            String query = Q.getValue();
//            candidate = bm.getRanking(query);
//            candidateRanked = performRankerOnCandidate(query,candidate);
//            result.put(Q.getKey(),candidateRanked);
//        }
//        return result;
//    }

    protected Map<String,Map<String,Container>>  performReRank()
    {

        Map<String,Map<String,Container>> result = new LinkedHashMap<>();

        StreamSupport.stream(query.entrySet().spliterator(),Searchcommand.isParallelEnabled())
                .forEach(q -> {
                    try {
                        BaseBM25 bm = new BaseBM25(Searchcommand.getkVAL(),Searchcommand.getIndexlocation());
                        Map<String,Container> candidateRanked = performRankerOnCandidate(q.getValue(),bm.getRanking(q.getValue()),bm);
                        result.put(q.getKey(),candidateRanked);
                        System.out.print(".");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return result;
    }



}
