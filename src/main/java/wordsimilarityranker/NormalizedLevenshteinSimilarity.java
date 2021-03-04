package main.java.wordsimilarityranker;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.utils.RunWriter;

import java.util.Map;

public class NormalizedLevenshteinSimilarity extends SimilarityBase
        {
    public NormalizedLevenshteinSimilarity(RegisterCommands.CommandSearch searchCommand , Map<String,String> query)
    {
        super(searchCommand,query);
    }

    @Override
    public double getScore(String str1, String str2) {

        NormalizedLevenshtein l = new NormalizedLevenshtein();
        return l.similarity(str1,str2);
    }
    public void doNormalizedLevenshtein()
    {
        Map<String,Map<String, Container>> result = performReRank();

        if( result != null)
        {
            RunWriter.writeRunFile("normalized_levenshtein",result);
        }
    }
}
