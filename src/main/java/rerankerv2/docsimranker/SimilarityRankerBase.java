package main.java.rerankerv2.docsimranker;

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;



import main.java.rerankerv2.concepts.EmbeddingStrategy;
import main.java.reranker.WordEmbedding;
import main.java.searcher.BaseBM25;
import main.java.utils.CorpusStats;
import main.java.utils.RunWriter;
import main.java.utils.SortUtils;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;


public abstract class SimilarityRankerBase {

    private RegisterCommands.CommandSearch SearchCommand=null;
    protected BaseBM25 bm25 = null;
    protected Map<String,String> query = null;
    private EmbeddingStrategy embedding = null;
    protected CorpusStats cs = null;

    SimilarityRankerBase(RegisterCommands.CommandSearch SearchCommand,Map<String,String> query)
    {
        this.SearchCommand = SearchCommand;
        this.query = query;
        try {
            this.bm25 = new BaseBM25(SearchCommand.getkVAL(),SearchCommand.getIndexlocation());
        } catch (IOException e) {
            e.printStackTrace();
        }
        embedding = new WordEmbedding(SearchCommand.getDimension(),SearchCommand.getWordEmbeddingFile());
        cs = new CorpusStats(SearchCommand.getIndexlocation());
    }
        /*
         All class needs to implement the below function which describes how to build vector (Document rep)
         Takes the Lucene document as input and returns the document vector representation
         */
        abstract INDArray getVector(Container c);

        protected boolean checkForSpam(Container c)
        {
            return false;
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

        protected INDArray buildVector(ArrayList<String> processed)
        {
            int _number_of_terms=1;
            INDArray res = Nd4j.create(SearchCommand.getDimension()).add(0.000001); //Create the Dimension vector
            for(String str:processed)
            {
                if(embedding.getEmbeddingVector(str)!= null)
                {
                    _number_of_terms++;
                    INDArray temp = embedding.getEmbeddingVector(str);
                    res = res.add(temp);
                }
            }
            return res.div(_number_of_terms);
        }

        protected Map<String,Container> getReRank(Map<String, Container> unranked) {
            if (unranked.size() < 3) return unranked;

            Integer Dimension = SearchCommand.getDimension();
            Integer biasFactor = SearchCommand.getBiasFactor();

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
                biasedScore += val.getValue().getScore();
                INDArray temp = getVector(val.getValue());
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
                INDArray _other_doc = getVector(val.getValue());
                double cosineScore = Transforms.cosineSim(biased_vector, _other_doc);
                double newScore = ((val.getValue().getScore() * cosineScore) + biasedScore);

                Container temp = new Container(newScore, val.getValue().getDocID());
                temp.addEntityContainer(val.getValue().getEntity());
                unsorted.put(val.getKey(), temp);
            }
            return SortUtils.sortByValue(unsorted);
    }


    protected Map<String,Map<String,Container>> rerank()
    {
        Map<String,Map<String,Container>> res = new LinkedHashMap<String,Map<String,Container>>();
        long start= System.currentTimeMillis();


        StreamSupport.stream(query.entrySet().spliterator(),SearchCommand.isParallelEnabled())
                .forEach(q -> {
                    try {
                        BaseBM25 bm = new BaseBM25(SearchCommand.getkVAL(),SearchCommand.getIndexlocation());
                        Map<String, Container> retDoc = bm.getRanking(q.getValue());
                        Map<String,Container> reranked = getReRank(retDoc);
                        res.put(q.getKey(),reranked);
                        System.out.print(".");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        long end = System.currentTimeMillis();
        long timeElapsed = end-start;
        System.out.println("Time took :"+ (double)timeElapsed/1000 +"Minutes : "+ ((double)timeElapsed/1000)/60);
        return res;
    }
}
