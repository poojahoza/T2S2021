package main.java.utils;

import main.java.containers.Container;

import java.util.ArrayList;
import java.util.Map;

public class PrintUtils
{
    public static void displayMap(Map<String,Map<String,Container>> result)
    {
        for(Map.Entry<String,Map<String, Container>> outer: result.entrySet())
        {
            System.out.println(outer.getKey());
            for(Map.Entry<String,Container> inner:outer.getValue().entrySet())
            {
                System.out.println(inner.getKey()+" "+inner.getValue().getScore()+" "+inner.getValue().getDocID());
            }
            System.out.println("***********************************************************************************************************************");

        }
    }

    public static void  displayMapContainer(Map<String,Container> result)
    {
        for(Map.Entry<String,Container> val: result.entrySet())
        {
            String paraID = val.getKey();
            double score = val.getValue().getScore();
            int docID= val.getValue().getDocID();
            System.out.println(paraID +" "+ score +" "+docID);
        }

    }

    public static void  displayQuery(Map<String,String> result)
    {
        int count=0;
        for(Map.Entry<String,String> val: result.entrySet())
        {
            count++;
           System.out.println("QNO: "+count+" QueryID :"+ val.getKey() + " -----"+"QueryString :"+ val.getValue());
        }
    }

    public static void  displayScores(Map<String,Double> result)
    {
        int count=0;
        for(Map.Entry<String,Double> val: result.entrySet())
        {
            count++;
            System.out.println("PNO: "+count+" PID :"+ val.getKey() + " -----"+" Score  :"+ val.getValue());
        }
    }

    public static void  displayScores(ArrayList<Double> result)
    {
        int count=0;
        for(Double val: result)
        {
            count++;
            System.out.println("Score : "+val);
        }
    }

    public static void  displayMapContainerList(Map<String,Container> result)
    {
        for(Map.Entry<String,Container> val: result.entrySet())
        {
            String paraID = val.getKey();
            double score = val.getValue().getScore();
            System.out.println(paraID +" "+ score +" "+val.getValue().getScoresList());
        }

    }




}
