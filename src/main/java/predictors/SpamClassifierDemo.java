package main.java.predictors;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class to demonstrate the use of the classes in the predictors package.
 */
public class SpamClassifierDemo {

    public static void main (String [] args) throws IOException, ParseException {

        SpamClassifier sc = new SpamClassifier();
        HashMap<String, String> hamTrain = sc.readIndex("/home/rachel/grad_courses/data_science/hamTrain");
        HashMap<String, String> spamTrain = sc.readIndex("/home/rachel/grad_courses/data_science/spamTrain");
        HashMap<String, String> test = sc.readIndex("/home/rachel/grad_courses/data_science/hamSpamTest");

        HashMap<String, String> spamTest = sc.readIndex("/home/rachel/grad_courses/data_science/spamTest");
        HashMap<String, String> hamTest = sc.readIndex("/home/rachel/grad_courses/data_science/hamTest");

        LabelPredictor unigramsPredictor = sc.classifyWithUnigrams(spamTrain, hamTrain);
        LabelPredictor bigramsPredictor = sc.classifyWithBigrams(spamTrain, hamTrain);
        LabelPredictor trigramsPredictor = sc.classifyWithTrigrams(spamTrain, hamTrain);
        LabelPredictor quadgramsPredictor = sc.classifyWithQuadgrams(spamTrain, hamTrain);

        StopWordLabelPredictor stopCoverPredictor = sc.classifyWithStopCover(spamTrain, hamTrain);
        StopWordLabelPredictor fracStopsPredictor = sc.classifyWithFracStops(spamTrain, hamTrain);
        StopWordLabelPredictor specialCharPredictor = sc.classifyWithSpecialChars(spamTrain, hamTrain);

        HashMap<String, ArrayList<Double>> unigramScores = sc.getScores(unigramsPredictor, test);
        HashMap<String, ArrayList<Double>> bigramScores = sc.getScores(bigramsPredictor, test);
        HashMap<String, ArrayList<Double>> trigramScores = sc.getScores(trigramsPredictor, test);
        HashMap<String, ArrayList<Double>> quadgramScores = sc.getScores(quadgramsPredictor, test);

        for (String key: unigramScores.keySet()) {
            ArrayList<Double> curList = unigramScores.get(key);
            System.out.println("Pid: " + key + "  Ham score: " + curList.get(0) + "  Spam score; " + curList.get(1));
        }

        HashMap<String, String> labels = sc.predict(unigramsPredictor, test);

        for (String key : labels.keySet()) {
           System.out.println(key + '\t' + labels.get(key));
        }

        System.out.println("\n++++++++ UNIGRAMS ++++++++");
        unigramsPredictor.evaluate(spamTest, hamTest, test);

        System.out.println("\n++++++++ BIGRAMS ++++++++");
        bigramsPredictor.evaluate(spamTest, hamTest, test);

        System.out.println("\n++++++++ TRIGRAMS ++++++++");
        trigramsPredictor.evaluate(spamTest, hamTest, test);

        System.out.println("\n++++++++ QUADGRAMS ++++++++");
        quadgramsPredictor.evaluate(spamTest, hamTest, test);

        System.out.println("\n++++++++ STOP COVERAGE ++++++++");
        stopCoverPredictor.evaluate(spamTest, hamTest, test);

        System.out.println("\n++++++++ FRAC STOPS ++++++++");
        fracStopsPredictor.evaluate(spamTest, hamTest, test);

        System.out.println("\n++++++++ SPECIAL CHARS ++++++++");
        specialCharPredictor.evaluate(spamTest, hamTest, test);

    }
}
