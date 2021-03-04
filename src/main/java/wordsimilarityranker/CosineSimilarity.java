package main.java.wordsimilarityranker;

//project imports
import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.searcher.BaseBM25;

//Dependency imports
import  info.debatty.java.stringsimilarity.*;
import main.java.utils.RunWriter;

//Java imports
import java.util.Map;

/*
A class implements the abstract class SimilarityBase and returns the CosineSimilarity score
*/
public class CosineSimilarity extends SimilarityBase
{
    public CosineSimilarity(RegisterCommands.CommandSearch searchCommand, Map<String,String> query)
    {
        super(searchCommand,query);
    }

    @Override
    public double getScore(String str1 , String str2) {
        Cosine cosine = new Cosine();
        return cosine.similarity(str1,str2);
    }

    public void doCosine()
    {
        Map<String,Map<String, Container>> result = performReRank();
        if( result != null)
        {
            RunWriter.writeRunFile("cosine_similarity",result);
        }
    }
}
