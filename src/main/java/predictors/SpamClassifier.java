package main.java.predictors;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static main.java.utils.SearchUtils.createTokenList;

public class SpamClassifier {

    private HashMap<String, String> labels;

    public SpamClassifier() {

        labels = new HashMap<>();
    }

    /**
     * Desc: Read and store the training or test data. The user should call this before any of other methods to make
     *       ham and spam train sets, and a test set.
     *
     * @param path to the training/test set.
     * @return HashMap of train or test data in the form pid => tokens
     */
    public HashMap<String, String> readIndex(String path) {

        BufferedReader reader = null;
        HashMap<String, String> lines = new HashMap<>();
        File f = new File(path);

        String line = null;

        try {
            reader = new BufferedReader(new FileReader(f));
            while ((line = reader.readLine()) != null) {
                String[] curLine = line.split("\\t+");
                String pid = curLine[0].trim();
                String test = curLine[1].trim();
                lines.put(pid, test);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return lines;
    }


    /**
     * Desc: Train a NaiveBayesPredictor (unigrams) using the HashMaps made from the previous method.
     *
     * @param spamCorpus the training set of spam documents.
     * @param hamCorpus the training set of ham documents.
     *
     * @return LabelPredictor that is trained.
     */
    public LabelPredictor classifyWithUnigrams(HashMap<String, String>spamCorpus, HashMap<String, String> hamCorpus) {

        LabelPredictor unigramsPredictor = new NaiveBayesPredictor();

        for (String key : spamCorpus.keySet()) {
            String text = spamCorpus.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            unigramsPredictor.trainSpamTokens(tokens);
        }

        for (String key: hamCorpus.keySet()) {
            String text = hamCorpus.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            unigramsPredictor.trainHamTokens(tokens);
        }

        return unigramsPredictor;

    }


    /**
     * Desc: Train a NaiveBayesBigramPredictor.
     *
     * @param spamCorpus the training set of spam documents.
     * @param hamCorpus the training set of ham documents.
     *
     * @return LabelPredictor that is trained.
     */
    public LabelPredictor classifyWithBigrams(HashMap<String, String> spamCorpus, HashMap<String, String> hamCorpus) {

        LabelPredictor bigramsPredictor = new NaiveBayesBigramPredictor();

        for (String key : spamCorpus.keySet()) {
            String text = spamCorpus.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            bigramsPredictor.trainSpamTokens(tokens);
        }

        for (String key: hamCorpus.keySet()) {
            String text = hamCorpus.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            bigramsPredictor.trainHamTokens(tokens);
        }

        return bigramsPredictor;
    }


    /**
     * Desc: Train a NaiveBayesTrigramPredictor.
     *
     * @param spamCorpus the training set of spam documents.
     * @param hamCorpus the training set of ham documents.
     *
     * @return LabelPredictor that is trained.
     */
    public LabelPredictor classifyWithTrigrams(HashMap<String, String> spamCorpus, HashMap<String, String> hamCorpus) {

        LabelPredictor trigramsPredictor = new NaiveBayesTrigramPredictor();
        HashMap<String, String> test = null;

        for (String key : spamCorpus.keySet()) {
            String text = spamCorpus.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            trigramsPredictor.trainSpamTokens(tokens);
        }

        for (String key: hamCorpus.keySet()) {
            String text = hamCorpus.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            trigramsPredictor.trainHamTokens(tokens);
        }

        return trigramsPredictor;

    }


    /**
     * Desc: Train a NaiveBayesQuadgramPredictor.
     *
     * @param spamCorpus the training set of spam documents.
     * @param hamCorpus the training set of ham documents.
     *
     * @return LabelPredictor that is trained.
     */
    public LabelPredictor classifyWithQuadgrams(HashMap<String, String> spamCorpus, HashMap<String, String> hamCorpus) {

        LabelPredictor quadgramsPredictor = new NaiveBayesQuadgramPredictor();
        HashMap<String, String> test = null;

        for (String key : spamCorpus.keySet()) {
            String text = spamCorpus.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            quadgramsPredictor.trainSpamTokens(tokens);
        }

        for (String key: hamCorpus.keySet()) {
            String text = hamCorpus.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            quadgramsPredictor.trainHamTokens(tokens);
        }

        return quadgramsPredictor;

    }


    /**
     * Desc: Train a SpecialCharPredictor.
     *
     * @param spamCorpus the training set of spam documents.
     * @param hamCorpus the training set of ham documents.
     *
     * @return StopWordLabelPredictor that is trained.
     */
    public StopWordLabelPredictor classifyWithSpecialChars(HashMap<String, String> spamCorpus, HashMap<String, String> hamCorpus) {

        StopWordLabelPredictor specialCharPredictor = new SpecialCharPredictor();
        HashMap<String, String> test = null;

        for (String key : spamCorpus.keySet()) {
            String text = spamCorpus.get(key);
            //System.out.println(text);
            List<String> tokens = createTokenList(text, new WhitespaceAnalyzer());
            specialCharPredictor.trainSpamTokens(tokens, key);
        }

        for (String key: hamCorpus.keySet()) {
            String text = hamCorpus.get(key);
            //System.out.println(text);
            List<String> tokens = createTokenList(text, new WhitespaceAnalyzer());
            specialCharPredictor.trainHamTokens(tokens, key);
        }

        return specialCharPredictor;

    }


    /**
     * Desc: Train a StopCoverPredictor.
     *
     * @param spamCorpus the training set of spam documents.
     * @param hamCorpus the training set of ham documents.
     *
     * @return StopWordLabelPredictor that is trained.
     */
    public StopWordLabelPredictor classifyWithStopCover(HashMap<String, String> spamCorpus, HashMap<String, String> hamCorpus) {

        StopWordLabelPredictor stopCoverPredictor = new StopCoveragePredictor();
        HashMap<String, String> test = null;

        for (String key : spamCorpus.keySet()) {
            String text = spamCorpus.get(key);
            List<String> tokens = createTokenList(text, new WhitespaceAnalyzer());
            stopCoverPredictor.trainSpamTokens(tokens, key);
        }

        for (String key: hamCorpus.keySet()) {
            String text = hamCorpus.get(key);
            List<String> tokens = createTokenList(text, new WhitespaceAnalyzer());
            stopCoverPredictor.trainHamTokens(tokens, key);
        }

        return stopCoverPredictor;

    }

    /**
     * Desc: Train a FracStopsPredictor.
     *
     * @param spamCorpus the training set of spam documents.
     * @param hamCorpus the training set of ham documents.
     *
     * @return StopWordLabelPredictor that is trained.
     */
    public StopWordLabelPredictor classifyWithFracStops(HashMap<String, String> spamCorpus, HashMap<String, String> hamCorpus) {

        StopWordLabelPredictor fracStopPredictor = new FracStopPredictor();
        HashMap<String, String> test = null;

        for (String key : spamCorpus.keySet()) {
            String text = spamCorpus.get(key);
            List<String> tokens = createTokenList(text, new WhitespaceAnalyzer());
            fracStopPredictor.trainSpamTokens(tokens, key);
        }

        for (String key: hamCorpus.keySet()) {
            String text = hamCorpus.get(key);
            List<String> tokens = createTokenList(text, new WhitespaceAnalyzer());
            fracStopPredictor.trainHamTokens(tokens, key);
        }

        return fracStopPredictor;

    }


    /**
     * Desc: Test the trained predictor.
     *
     * @param predictor the LabelPredictor that was previously trained.
     * @param test the test data that was held out in the form pid => tokens.
     *
     * @return HashMap containing pid => label of "spam" or "ham".
     */
    public HashMap<String, String> predict(LabelPredictor predictor, HashMap<String, String> test) {

        for (String key : test.keySet()) {
            String text = test.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            String prediction = predictor.predict(tokens);
            labels.put(key, prediction);
        }

        return labels;
    }


    /**
     * Desc: Test the trained predictor.
     *
     * @param predictor the StopWordsLabelPredictor that was previously trained.
     * @param test the test data that was held out in the form pid => tokens.
     *
     * @return HashMap containing pid => label of "spam" or "ham".
     */
    public HashMap<String, String> predict(StopWordLabelPredictor predictor, HashMap<String, String> test) {

        for (String key : test.keySet()) {
            String text = test.get(key);
            List<String> tokens = createTokenList(text, new WhitespaceAnalyzer());
            String prediction = predictor.predict(tokens);
            labels.put(key, prediction);
        }

        return labels;
    }

    /**
     * Desc: Get ham and spam scores for the trained LabelPredictor.
     *
     * @param predictor the LabelPredictor that was previously trained.
     * @param test the test data that was held out in the form pid => tokens.
     */
    public HashMap<String, ArrayList<Double>> getScores(LabelPredictor predictor, HashMap<String, String> test) {

        HashMap<String, ArrayList<Double>> allScores = new HashMap<>();
        for (String key : test.keySet()) {
            String text = test.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            ArrayList<Double> scores = predictor.score(tokens);
            allScores.put(key, scores);
        }

        return allScores;
    }


    /**
     * Desc: Get the F1 and MAP scores of the classifier.
     *
     * @param spamTest, a hash map of the ham test data by itself.
     * @param hamTest, a hash map of the spam test data by itself.
     * @param testDocs of mixed ham and spam documents mapping their pids to their text.
     */
    public void evaluate(HashMap<String, String> spamTest, LabelPredictor predictor, HashMap<String, String> hamTest, HashMap<String, String> testDocs) {
        predictor.evaluate(spamTest, hamTest, testDocs);
    }


    /**
     * Desc: Helper method to check whether a document is spam or not.
     *
     * @param predictor, a trained LabelPredictor
     * @param text, to be classified as ham or spam
     */
    public boolean isSpam(LabelPredictor predictor,  String text ) {

        List<String> tokens = createTokenList(text, new EnglishAnalyzer());
        String prediction = predictor.predict(tokens);
        switch (prediction.toLowerCase()){
            case "ham": { return false; }
            case "spam": { return true; }
            default: { return true; }
        }
    }

}

