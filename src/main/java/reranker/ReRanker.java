package main.java.reranker;

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.searcher.BaseBM25;
import main.java.utils.PrintUtils;
import main.java.utils.RunWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/*
@author: Amith
This class performs the Re-Ranking based on the document similarity
*/

public class ReRanker
{
    private BaseBM25 bm25 = null;

    private RegisterCommands.CommandSearch SearchCommand= null;
    private Map<String,String> query =null;
    private  ReRankRunner runnerReRank = null;
    private  ReRankIDFRunner runnerIDFReRank = null;
    private  ReRankDFRunner runnerDFReRank = null;


    public ReRanker(RegisterCommands.CommandSearch SearchCommand, Map<String,String> query)
    {
        this.SearchCommand = SearchCommand;
        this.query = query;
        try {
            this.bm25 = new BaseBM25(SearchCommand.getkVAL(),SearchCommand.getIndexlocation());
        } catch (IOException e) {
            e.printStackTrace();
        }
        runnerReRank = new ReRankRunner(bm25,SearchCommand.getWordEmbeddingFile(),SearchCommand.getDimension());
        runnerIDFReRank= new ReRankIDFRunner(bm25,SearchCommand.getWordEmbeddingFile(),SearchCommand.getDimension(),SearchCommand.getIndexlocation());
        runnerDFReRank=new ReRankDFRunner(bm25,SearchCommand.getWordEmbeddingFile(),SearchCommand.getDimension(),SearchCommand.getIndexlocation());
    }


    public void ReRank()
    {
        runnerReRank.setBiasFactor(SearchCommand.getBiasFactor());
        Map<String,Map<String,Container >> result = new LinkedHashMap<String,Map<String,Container>>();
//        for(Map.Entry<String,String> q: query.entrySet())
//        {
//            String Query = q.getValue();
//            Map<String, Container> BM25Val = bm25.getRanking(Query);
//            Map<String, Container> reOrdered = runnerReRank.getReRank(BM25Val);
//            result.put(q.getKey(),reOrdered);
//        }

        StreamSupport.stream(query.entrySet().spliterator(),true)
                .forEach(q -> {
                    try {
                        BaseBM25 bm = new BaseBM25(SearchCommand.getkVAL(),SearchCommand.getIndexlocation());
                        Map<String, Container> BM25Val = bm.getRanking(q.getValue());
                        Map<String, Container> reOrdered = runnerReRank.getReRank(BM25Val);
                        result.put(q.getKey(),reOrdered);
                        System.out.print(".");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        String datafile ="";
        if(SearchCommand.getQueryfile().toLowerCase().contains("test".toLowerCase()))
        {
            datafile = "_test";
        }
        else if(SearchCommand.getQueryfile().toLowerCase().contains("train".toLowerCase()))
        {
            datafile = "_train";
        }
        String level = SearchCommand.isArticleEnabled()? "_article": "_section";
        String mname = "doc_sim_reranking"+"_k"+SearchCommand.getkVAL()+"_b"+SearchCommand.getBiasFactor()+"_d"+SearchCommand.getDimension()+level+datafile;

        RunWriter.writeRunFile(mname,result);

        if(SearchCommand.getisVerbose())
        {
            PrintUtils.displayMap(result);
        }
    }

    public void ReRankIDF()
    {
        runnerIDFReRank.setBiasFactor(SearchCommand.getBiasFactor());

        LinkedHashMap<String,Map<String,Container >> result = new LinkedHashMap<>();

        for(Map.Entry<String,String> q: query.entrySet())
        {
            String Query = q.getValue();
            Map<String, Container> BM25Val = bm25.getRanking(Query);
            Map<String, Container> reOrdered = runnerIDFReRank.getReRank(BM25Val);
            result.put(q.getKey(),reOrdered);
        }

//        StreamSupport.stream(query.entrySet().spliterator(), true)
//                .parallel()
//                .forEach(q -> {
//                    String Query = q.getValue();
//                    Map<String, Container> BM25Val = bm25.getRanking(Query);
//                    Map<String, Container> reOrdered = runnerIDFReRank.getReRank(BM25Val);
//                    result.put(q.getKey(), reOrdered);
//                });



        String datafile ="";
        if(SearchCommand.getQueryfile().toLowerCase().contains("test".toLowerCase()))
        {
            datafile = "_test";
        }
        else if(SearchCommand.getQueryfile().toLowerCase().contains("train".toLowerCase()))
        {
            datafile = "_train";
        }

        String level = SearchCommand.isArticleEnabled()? "_article": "_section";
        String mname = "doc_sim_IDF_reranking"+"_k"+SearchCommand.getkVAL()+"_b"+SearchCommand.getBiasFactor()+"_d"+SearchCommand.getDimension()+level+datafile;
        RunWriter.writeRunFile(mname,result);
        if(SearchCommand.getisVerbose())
        {
            PrintUtils.displayMap(result);
        }
    }

    public void ReRankDF()
    {
        runnerDFReRank.setBiasFactor(SearchCommand.getBiasFactor());
        Map<String,Map<String,Container >> result = new LinkedHashMap<String,Map<String,Container>>();
        for(Map.Entry<String,String> q: query.entrySet())
        {
            String Query = q.getValue();
            Map<String, Container> BM25Val = bm25.getRanking(Query);
            Map<String, Container> reOrdered = runnerDFReRank.getReRank(BM25Val);
            result.put(q.getKey(),reOrdered);
        }
        String datafile ="";
        if(SearchCommand.getQueryfile().toLowerCase().contains("test".toLowerCase()))
        {
            datafile = "_test";
        }
        else if(SearchCommand.getQueryfile().toLowerCase().contains("train".toLowerCase()))
        {
            datafile = "_train";
        }

        String level = SearchCommand.isArticleEnabled()? "_article": "_section";
        String mname = "doc_sim_DF_reranking"+"_k"+SearchCommand.getkVAL()+"_b"+SearchCommand.getBiasFactor()+"_d"+SearchCommand.getDimension()+level+datafile;
        RunWriter.writeRunFile(mname,result);

        if(SearchCommand.getisVerbose())
        {
            PrintUtils.displayMap(result);
        }
    }

    public Map<String,Map<String,Container>> getReRankSimilarity(Map<String,Map<String,Container>> res)
    {
        System.out.println("Running the ReRank Similarity.....");
        runnerReRank.setBiasFactor(SearchCommand.getBiasFactor());
        long start= System.currentTimeMillis();
        Map<String,Map<String,Container >> result = new LinkedHashMap<String,Map<String,Container>>();

        StreamSupport.stream(res.entrySet().spliterator(),SearchCommand.isParallelEnabled())
                .forEach(q -> {
                        Map<String, Container> reOrdered = runnerReRank.getReRank(q.getValue());
                        result.put(q.getKey(),reOrdered);
                        System.out.print(".");
                });
        long end = System.currentTimeMillis();
        long timeElapsed = end-start;
        System.out.println("\nTime took :"+ (double)timeElapsed/1000 +"Minutes : "+ ((double)timeElapsed/1000)/60);

        return result;
    }

    public Map<String,Map<String,Container>> getReRankSimilarityIDF(Map<String,Map<String,Container>> res)
    {
        System.out.println("Running the ReRank IDF Similarity.....");
        runnerIDFReRank.setBiasFactor(SearchCommand.getBiasFactor());
        Map<String,Map<String,Container >> result = new LinkedHashMap<String,Map<String,Container>>();
        long start= System.currentTimeMillis();
        StreamSupport.stream(res.entrySet().spliterator(),SearchCommand.isParallelEnabled())
                .forEach(q -> {
                    Map<String, Container> reOrdered = runnerIDFReRank.getReRank(q.getValue());
                    result.put(q.getKey(),reOrdered);
                    System.out.print(".");
                });
        long end = System.currentTimeMillis();
        long timeElapsed = end-start;
        System.out.println("\nTime took :"+ (double)timeElapsed/1000 +"Minutes : "+ ((double)timeElapsed/1000)/60);
        return result;
    }

}
