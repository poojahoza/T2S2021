package main.java.wordsimilarityranker;

import info.debatty.java.stringsimilarity.Jaccard;
import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.searcher.BaseBM25;
import main.java.utils.RunWriter;

import java.util.Map;

public class JaccardSimilarity extends SimilarityBase
{
    public JaccardSimilarity(RegisterCommands.CommandSearch searchCommand, Map<String,String> query)
    {
        super(searchCommand,query);
    }

    @Override
    public double getScore(String str1, String str2) {
        Jaccard jaccard = new Jaccard();
        return jaccard.similarity(str1,str2);
    }

    public void doJaccard()
    {
        Map<String,Map<String, Container>> result = performReRank();
        if( result != null)
        {
            RunWriter.writeRunFile("jaccard_similarity",result);
        }
    }

}
