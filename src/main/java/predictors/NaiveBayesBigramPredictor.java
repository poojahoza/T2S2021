package main.java.predictors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NaiveBayesBigramPredictor extends LabelPredictor {

    private BayesCounter bc = new BayesCounter();

    public NaiveBayesBigramPredictor() {
        super();
    }

    /**
     * Desc: Train classifier on ham documents.
     *
     * @param tokens List of tokens in the document
     */
    public void trainHamTokens(List<String> tokens) {
        bc.buildBigramsHashMap("ham", tokens);
    }

    /**
     * Desc: Train classifier on spam documents.
     *
     * @param tokens List of tokens in the document
     */
    public void trainSpamTokens(List<String> tokens) {
        bc.buildBigramsHashMap("spam", tokens);
    }

    /**
     * Desc: Predict whether a document is a ham or spam.
     *
     * @param tokens List of tokens in the document
     * @return String The label ("spam" or "ham") that is predicted given the document tokens
     */
    @Override
    public String predict(List<String> tokens) {
        return bc.classifyWithBigrams(tokens);
    }

    /**
     * Desc: Get the ham and spam scores for the test data.
     *
     * @param tokens List of tokens in the document
     * @return ArrayList The ham and spam scores of the given document tokens
     */
    @Override
    public ArrayList<Double> score(List<String> tokens) {
        return bc.getBigramScores(tokens);
    }

    /**
     * Desc: Get the F1 and MAP scores of the classifier.
     *
     * @param spam, a hash map of the ham test data by itself.
     * @param ham, a hash map of the spam test data by itself.
     * @param docs of mixed ham and spam documents mapping their pids to their text.
     */
    @Override
    public void evaluate(HashMap<String, String> spam, HashMap<String, String> ham, HashMap<String, String> docs) {
        bc.evaluateBigramPredictor(spam, ham, docs);
    }

}