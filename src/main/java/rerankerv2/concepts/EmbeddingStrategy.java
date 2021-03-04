package main.java.rerankerv2.concepts;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Embedding strategy class to handle different embedding types
 */
public abstract class EmbeddingStrategy
{
    /*
    All subclassed must implement this method to return the Embedding vector for a given string
    String can be para, individual tokens
     */
    public abstract INDArray getEmbeddingVector(String token);
}
