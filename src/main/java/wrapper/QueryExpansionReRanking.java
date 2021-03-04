package main.java.wrapper;


/*
A wrapper class to all the query expansion method to use the re-ranking implementation
*/

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.reranker.ReRanker;
import main.java.rerankerv2.docsimranker.DocumentFrequencySimilarity;
import main.java.utils.RunWriter;

import java.util.Map;

public class QueryExpansionReRanking
{

    private RegisterCommands.CommandSearch searchCommand = null;
    private Map<String,String> query = null;
    private Map<String,Map<String, Container>> result = null;

    public QueryExpansionReRanking(RegisterCommands.CommandSearch searchCommand, Map<String,String> query, Map<String,Map<String, Container>> result)
    {
        this.searchCommand=searchCommand;
        this.query=query;
        this.result =result;
    }

    public void getDocumentFrequencyReRanking(String mname)
    {
        DocumentFrequencySimilarity dfs = new DocumentFrequencySimilarity(searchCommand,query);
        Map<String,Map<String, Container>> res = dfs.doDocumentFrequency(result);
        RunWriter.writeRunFile(mname,res);
    }

    public void getInverseDocumentFrequencyReRanking(String mname)
    {
        ReRanker re = new ReRanker(searchCommand,query);
        Map<String,Map<String, Container>> res = re.getReRankSimilarityIDF(result);
        RunWriter.writeRunFile(mname,res);
    }

    public void getReRanking(String mname)
    {
        ReRanker re = new ReRanker(searchCommand,query);
        Map<String,Map<String, Container>> res = re.getReRankSimilarity(result);
        RunWriter.writeRunFile(mname,res);
    }


}
