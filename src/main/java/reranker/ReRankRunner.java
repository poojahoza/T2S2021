package main.java.reranker;

import main.java.containers.Container;
import main.java.searcher.BaseBM25;
import main.java.utils.PreProcessor;

import main.java.utils.SortUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/*
@author: Amith
Helper class for ReRanker
*/

class ReRankRunner
{
    protected BaseBM25 bm25 = null ;
    protected String embeddingFile = null;
    protected Integer Dimension;
    protected WordEmbedding word = null;
    protected int biasFactor=0;

    ReRankRunner(BaseBM25 bm25, String embeddingFile,Integer Dimension)
    {
        this.bm25 = bm25;
        this.embeddingFile=embeddingFile;
        this.Dimension =Dimension;
        word = new WordEmbedding(this.Dimension,this.embeddingFile);
    }

    protected ArrayList<String> getTopK(ArrayList<String> sorted)
    {
        ArrayList<String> res = new ArrayList<>();
        int k =sorted.size()/2;
        int count=0;
        for(String ss: sorted)
        {
            count++;
            res.add(ss);
            if(k==count) break;
        }
        return res;
    }

    protected void setBiasFactor(Integer biasFactor)
    {
        this.biasFactor =biasFactor;
    }


    protected INDArray buildVector(ArrayList<String> processed)
    {
        int _number_of_terms=1;
        INDArray res = Nd4j.create(Dimension).add(0.000001); //Create the Dimension vector
        for(String str:processed)
        {
            if(word.getWordEmbeddingVector(str)!= null)
            {
                _number_of_terms++;
                INDArray temp = word.getWordEmbeddingVector(str);
                res = res.add(temp);
            }
        }
        return res.div(_number_of_terms); //Taking the mean of the vector
    }


    protected INDArray getVector(Integer docID)
    {

        ArrayList<String> processed = null;
        try {
            processed = PreProcessor.processTermsUsingLucene(bm25.getDocument(docID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  buildVector(processed);
    }

    /*
    A new implementation based on the bias factor
    */
    protected Map<String,Container> getReRank(Map<String, Container> unranked) {
        if (unranked.size() < 3) return unranked;

        INDArray biased_vector = null;
        INDArray res = Nd4j.create(Dimension);

        int count = 0;

        //If more biases are used than the candidate set, half the size of the candidate set will be used
        int actualBias = unranked.size() > biasFactor ? biasFactor : unranked.size() / 2;

        double biasedScore = 0.0;
        /*
        Compute the document representation based on the Bias Factors, if the user pass in 10, the first document of the retrieved set will be used as bias
        */
        for (Map.Entry<String, Container> val : unranked.entrySet()) {
            count++;
            int docID = val.getValue().getDocID();
            biasedScore += val.getValue().getScore();
            INDArray temp = getVector(docID);
            res = res.add(temp);
            if (count == actualBias) break;
        }

        //If the Bias factor is 1, we do not need to divide the vector component.

        biased_vector = biasFactor == 1 ? res : res.div(actualBias);
        biasedScore = biasFactor == 1 ? biasedScore : (biasedScore / actualBias);


        Map<String, Container> unsorted = new LinkedHashMap<String, Container>();

        for (Map.Entry<String, Container> val : unranked.entrySet())
        {
            int docID = val.getValue().getDocID();
            INDArray _other_doc = getVector(docID);
            double cosineScore = Transforms.cosineSim(biased_vector, _other_doc);
            double newScore = ((val.getValue().getScore() * cosineScore) + biasedScore);

            Container temp = new Container(newScore, val.getValue().getDocID());
            //temp.addEntityContainer(val.getValue().getEntity());
            unsorted.put(val.getKey(), temp);
        }
        return SortUtils.sortByValue(unsorted);
    }
}
