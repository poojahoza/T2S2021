package main.java.reranker;

import com.sun.jna.platform.win32.OaIdl;
import main.java.containers.Container;
import main.java.searcher.BaseBM25;
import main.java.utils.CorpusStats;
import main.java.utils.PreProcessor;

import org.nd4j.linalg.api.ndarray.INDArray;


import java.io.IOException;

import java.util.ArrayList;


public class ReRankIDFRunner extends ReRankRunner
{
    private CorpusStats cs = null;

    public ReRankIDFRunner(BaseBM25 bm25, String embeddingFile, Integer Dimension,String indexLoc)
    {
            super(bm25,embeddingFile,Dimension);
            cs = new CorpusStats(indexLoc);
    }

    protected INDArray getVector(Integer docID)
    {
        ArrayList<String> processed = null;
        try {
            processed = PreProcessor.processTermsUsingLucene(bm25.getDocument(docID));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> highestIDF = null;
        try {
            highestIDF = cs.getIDF(processed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  buildVector(getTopK(highestIDF));
    }
}
