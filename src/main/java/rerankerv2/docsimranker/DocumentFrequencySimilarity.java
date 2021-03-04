package main.java.rerankerv2.docsimranker;

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.searcher.BaseBM25;
import main.java.utils.PreProcessor;
import main.java.utils.RunWriter;

import org.nd4j.linalg.api.ndarray.INDArray;


import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

public class DocumentFrequencySimilarity extends SimilarityRankerBase
{
    private RegisterCommands.CommandSearch SearchCommand = null;

    public DocumentFrequencySimilarity(RegisterCommands.CommandSearch SearchCommand, Map<String,String> query)
    {
        super(SearchCommand,query);
        this.SearchCommand =SearchCommand;
    }

    @Override
    INDArray getVector(Container c) {
        ArrayList<String> processed = null;
        try {
            processed = PreProcessor.processTermsUsingLucene(bm25.getDocument(c.getDocID()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> highestDF = null;
        try {
            highestDF = cs.getDF(processed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  buildVector(getTopK(highestDF));
    }

    public void doDocumentFrequency()
    {
        Map<String,Map<String,Container>> res = rerank();
        RunWriter.writeRunFile("DOC_FRE_RANK",res);
    }

    public Map<String,Map<String,Container>> getDocumentFRequencyReRanker()
    {
        return rerank();
    }

    public Map<String,Map<String,Container>> doDocumentFrequency(Map<String,Map<String,Container>> result)
    {
        System.out.println("Executing the Document Frequency Similarity");
        Map<String,Map<String,Container>> res = new LinkedHashMap<String,Map<String,Container>>();
        long start= System.currentTimeMillis();

        StreamSupport.stream(result.entrySet().spliterator(),SearchCommand.isParallelEnabled())
                .forEach(q -> {
                        Map<String, Container> retDoc = q.getValue();
                        Map<String,Container> reranked = getReRank(retDoc);
                        res.put(q.getKey(),reranked);
                        System.out.print(".");
                 });
        long end = System.currentTimeMillis();
        long timeElapsed = end-start;
        System.out.println("Time took :"+ (double)timeElapsed/1000 +"Minutes : "+ ((double)timeElapsed/1000)/60);
        return res;
    }
}
