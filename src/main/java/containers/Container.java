package main.java.containers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Container implements Comparable
{
    private int  docID;
    private Double scoreVal;
    private EntityContainer entity;
    private ArrayList<Double> scores;
    private int rank;

    public Container(Double scoreVal,int docID)
    {
        this.docID=docID;
        this.scoreVal=scoreVal;
        this.scores = new ArrayList<Double>();
        this.rank=rank;
    }

    public void addEntityContainer(EntityContainer e)
    {
        this.entity = e;
    }

    public int  getDocID() {return docID;}
    public Double getScore(){return scoreVal;}
    public int getRank() {return rank;}

    public EntityContainer getEntity() { return entity;}
    public void setScoreVal(Double scoreVal)
    {
        this.scoreVal=scoreVal;
    }
    public void setRank(int rank){this.rank=rank;}

    public void addScores(Double d)
    {
        scores.add(d);
    }

    public ArrayList<Double> getScoresList()
    {
        return scores;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        return this.scoreVal.compareTo(((Container)o).scoreVal);
    }
}
