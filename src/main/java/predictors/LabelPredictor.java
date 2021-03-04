package main.java.predictors;

import main.java.utils.SearchUtils;
//import main.kotlin.evaluation.KotlinEvaluator;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract class to predict labels.
 */
abstract public class LabelPredictor {

    public LabelPredictor() {
    }

    /**
     * Desc: Train classifier on ham documents.
     *
     * @param tokens List of tokens in the document
     */
    public void trainHamTokens(List<String> tokens) {}

    /**
     * Desc: Train classifier on ham documents.
     *
     * @param tokens List of tokens in the document
     */
    public void trainSpamTokens(List<String> tokens) {}

    /**
     * Desc: Predict whether a document is ham or spam.
     *
     * @param tokens List of tokens in the document
     * @return String The label ("spam" or "ham") that is predicted given the document tokens
     */
    abstract public String predict(List<String> tokens);

    /**
     * Desc: Get the ham and spam scores for the test data.
     *
     * @param tokens List of tokens in the document
     * @return ArrayList The ham and spam scores of the given document tokens
     */
    abstract public ArrayList<Double> score(List<String> tokens);

    /**
     * Desc: When called, the LabelPredictor will be handed unlabelled documents to classify (using predict method)
     *       The labels will be used to compute the F1 Score of your label prediction method.
     * @param spam test set in form of pid => tokens
     * @param ham test set in form of pid => tokens
     * @param docs containing spam and ham documents in form of pid => tokens
     */
    abstract public void evaluate(HashMap<String, String> spam, HashMap<String, String> ham, HashMap<String, String> docs);

}

