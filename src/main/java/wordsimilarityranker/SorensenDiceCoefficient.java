package main.java.wordsimilarityranker;

import info.debatty.java.stringsimilarity.SorensenDice;
import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.searcher.BaseBM25;
import main.java.utils.RunWriter;

import java.util.Map;

public class SorensenDiceCoefficient  extends  SimilarityBase {

    public SorensenDiceCoefficient(RegisterCommands.CommandSearch searchCommand, Map<String,String> query)
    {
        super(searchCommand,query);
    }

    @Override
    public double getScore(String str1, String str2) {
        SorensenDice sorensenDice = new SorensenDice();
        return sorensenDice.similarity(str1,str2);
    }

    public void doSorsenCoff()
    {
        Map<String,Map<String, Container>> result = performReRank();
        if( result != null)
        {
            RunWriter.writeRunFile("dice_coff_similarity",result);
        }
    }


}
