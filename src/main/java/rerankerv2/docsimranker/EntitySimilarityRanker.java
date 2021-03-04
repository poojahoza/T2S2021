package main.java.rerankerv2.docsimranker;

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.containers.DBContainer;
import main.java.containers.EntityContainer;
import main.java.database.databaseWrapper;
import main.java.searcher.EntityAbstractSearcher;
import main.java.utils.PreProcessor;
import main.java.utils.RunWriter;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.util.*;

public class EntitySimilarityRanker  extends SimilarityRankerBase
{
    RegisterCommands.CommandSearch searchCommand = null;
    public EntitySimilarityRanker(RegisterCommands.CommandSearch SearchCommand, Map<String,String> query)
    {
        super(SearchCommand,query);
        this.searchCommand =SearchCommand;
    }

    private String[] getEntities(Container input)
    {
            EntityContainer e = input.getEntity();
            if(e.getEntityId().equals(""))
            {
                return null;
            }
            return e.getEntityId().split("[\r\n]+");
    }

    @Override
    INDArray getVector(Container docID)
    {
        String[] entitiesID = getEntities(docID);
        if(entitiesID == null)
        {
            return Nd4j.create(searchCommand.getDimension()).add(0.0000001);
        }
        databaseWrapper dbwrapper = new databaseWrapper();
        Map<String, DBContainer> res = dbwrapper.getRecordLeadTextContainer(entitiesID);
        StringBuilder sb= new StringBuilder();
        for(Map.Entry<String,DBContainer> val:res.entrySet())
        {
            sb.append(val.getValue().getLeadtext());
            sb.append(" ");
        }

        ArrayList<String> temp= null;
        try {
            temp = PreProcessor.processTermsUsingLucene(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buildVector(temp);
    }

    public void doEntityReRank()
    {
        Map<String,Map<String,Container>> res = rerank();
        RunWriter.writeRunFile("Entity_similarity_ranking",res);
    }
}
