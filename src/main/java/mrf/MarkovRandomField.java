package main.java.mrf;

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.reranker.WordEmbedding;
import main.java.rerankerv2.concepts.EmbeddingStrategy;
import main.java.searcher.BaseBM25;
import main.java.utils.PrintUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class MarkovRandomField
{
    private RegisterCommands.CommandSearch SearchCommand = null;
    private Map<String,String> query = null;
    private EmbeddingStrategy embedding = null;

    public MarkovRandomField(RegisterCommands.CommandSearch SearchCommand, Map<String,String> query)
    {
        this.SearchCommand =SearchCommand;
        this.query = query;
        embedding = new WordEmbedding(SearchCommand.getDimension(),SearchCommand.getWordEmbeddingFile());
    }

    /*
    Collects the different kind of evidences for each document. Each evidence returns the score for a document
    The scores returned is normalized.
    */
    private Map<String,Container> collectEvidences(Map<String, Container> unranked,String queryVal)
    {

        ArrayList<Double> ans = null;

        Map<String,Container> result = new LinkedHashMap<String,Container>();
        result.putAll(unranked);

//        ans = Evidences.evidence1(unranked);
//        MrfHelper.updatescores(result,ans);
//
//
//        ans = Evidences.evidence2(unranked,embedding,SearchCommand.getDimension(),queryVal,SearchCommand.getIndexlocation(),SearchCommand.getkVAL());
//        MrfHelper.updatescores(result,ans);

        List<ArrayList<Double>> collective = Evidences.collectiveEvidence(unranked,embedding,SearchCommand.getDimension(),queryVal,SearchCommand.getIndexlocation(),
                SearchCommand.getkVAL());

//        for(ArrayList<Double> val:collective)
//        {
//            MrfHelper.updatescores(result,Normalize.getZScoreNormalized(val));
//        }

        MrfHelper.updateCollectiveScores(unranked,collective);

        ans = Evidences.evidence3(unranked,embedding,SearchCommand.getDimension(),queryVal,SearchCommand.getIndexlocation(),
                SearchCommand.getkVAL());
        MrfHelper.updatescores(result,ans);


        if(SearchCommand.getisVerbose())
        {
            PrintUtils.displayMapContainerList(result);
        }
       return result;
    }

    /*
        Runs the results in parallel if the --parallel is enabled in the command line and writes the result to file
    */
    public void doMarkovRandomField()
    {
        Map<String,Map<String,Container>> res = new LinkedHashMap<String,Map<String,Container>>();


        long start= System.currentTimeMillis();
        StreamSupport.stream(query.entrySet().spliterator(),SearchCommand.isParallelEnabled())
                .forEach(q -> {
                    try {
                        BaseBM25 bm = new BaseBM25(SearchCommand.getkVAL(),SearchCommand.getIndexlocation());
                        Map<String, Container> retDoc = bm.getRanking(q.getValue());
                        Map<String,Container> reranked = collectEvidences(retDoc,q.getValue());
                        res.put(q.getKey(),reranked);
                        System.out.print(".");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
        System.out.println("\n");
        long end = System.currentTimeMillis();
        long timeElapsed = end-start;
        System.out.println("Time took :"+ (double)timeElapsed/1000 +" sec "+ ((double)timeElapsed/1000)/60 +" min");
        MrfHelper.writeRunFile(res,"mrf");
        MrfHelper.writeFeatureFile(res,"mrfrank",SearchCommand.getQrelPath());
    }
}
