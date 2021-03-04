package main.java.predictors;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FracStopPredictor extends StopWordLabelPredictor{

    private BayesCounter bc = new BayesCounter();

    public FracStopPredictor() {
        super();
    }

    /**
     * Desc: Train classifier on ham documents.
     *
     * @param tokens List of tokens in the document
     */
    public void trainHamTokens(List<String> tokens, String pid) {
        try {
            bc.buildFracStopHashMap("ham", tokens, pid);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Desc: Train classifier on spam documents.
     *
     * @param tokens List of tokens in the document
     */
    public void trainSpamTokens(List<String> tokens, String pid) {
        try {
            bc.buildFracStopHashMap("spam", tokens, pid);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Desc: Predict whether a document is a ham or spam.
     *
     * @param tokens List of tokens in the document
     * @return String The label ("spam" or "ham") that is predicted given the document tokens
     */
    public String predict(List<String> tokens) {
        String label = null;
        try {
            label =  bc.classifyWithFracStops(tokens);
        } catch (FileNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }
        return label;
    }

    /**
     * Desc: Get the ham and spam scores for the test data.
     *
     * @param tokens of the document
     * @return ArrayList The ham and spam scores of the given document tokens
     */
    public ArrayList<Double> score(List<String> tokens) throws NullPointerException {

        return null;
    }

    /**
     * Desc: Get the F1 and MAP scores of the classifier.
     *
     * @param spam, a hash map of the ham test data by itself.
     * @param ham, a hash map of the spam test data by itself.
     * @param docs of mixed ham and spam documents mapping their pids to their text.
     */
    public void evaluate(HashMap<String, String> spam, HashMap<String, String> ham, HashMap<String, String> docs) {
        try {
            bc.evaluateFracStopPredictor(spam, ham, docs);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
