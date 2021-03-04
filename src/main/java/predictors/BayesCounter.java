package main.java.predictors;

import main.java.evaluators.F1Evaluator;
import main.java.evaluators.MAPEvaluator;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static main.java.utils.SearchUtils.createTokenList;
import static weka.core.Stopwords.isStopword;

public class BayesCounter {

    /**
     * Makes a new BayesCounter with empty hash maps.
     */
    public final HashMap<String, HashMap<String, Integer>> bayesMap;
    public final HashMap<String, HashMap<String, Double>> stopWordMap;
    public final HashMap<String, HashMap<String, Double>> specialCharMap;

    public BayesCounter() {
        bayesMap = new HashMap<>();
        stopWordMap = new HashMap<>();
        specialCharMap = new HashMap<>();
    }


    /**
     * Evaluate the trained NaiveBayesUnigramPredictor by computing F1 and MAP scores.
     *
     * @param spamTest HashMap in the form pid => tokens
     * @param hamTest HashMap in the form pid => tokens
     * @param docs HashMap in the form pid => tokens
     */
    public void evaluateUnigramPredictor(HashMap<String, String> spamTest, HashMap<String, String> hamTest, HashMap<String, String> docs) {

        HashMap <String, String> calledLabels = new HashMap<>();
        HashMap <String, String> trueLabels = new HashMap<>();

        // For each document, call the predict method. Store the pid with its prediction in the calledLabels map
        for (String key: docs.keySet()) {
            String text = docs.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            String label = this.classify(tokens);
            calledLabels.put(key, label);
        }

        // For each document, get the real label. Store the pid with its real label in the trueLabels map
        for (String key: docs.keySet()) {
            if (hamTest.get(key) != null) {
                trueLabels.put(key, "ham");
            }
            else if (spamTest.get(key) != null) {
                trueLabels.put(key, "spam");
            }
        }

        F1Evaluator f1 = new F1Evaluator(trueLabels);
        double f1Score = f1.evaluateCalledLabels(calledLabels);
        System.out.println("F1 score: " + f1Score);

        MAPEvaluator map = new MAPEvaluator(trueLabels);
        double MAPScore = map.evaluateCalledLabels(calledLabels);
        System.out.println("MAP: " + MAPScore);
    }


    /**
     * Evaluate the trained NaiveBayesBigramPredictor by computing F1 and MAP scores.
     *
     * @param spamTest HashMap in the form pid => tokens
     * @param hamTest HashMap in the form pid => tokens
     * @param docs HashMap in the form pid => tokens
     */
    public void evaluateBigramPredictor(HashMap<String, String> spamTest, HashMap<String, String> hamTest, HashMap<String, String> docs) {

        HashMap <String, String> calledLabels = new HashMap<>();
        HashMap <String, String> trueLabels = new HashMap<>();

        // For each document, call the predict method. Store the pid with its prediction in the calledLabels map
        for (String key: docs.keySet()) {
            String text = docs.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            String label = this.classifyWithBigrams(tokens);
            calledLabels.put(key, label);
        }

        // For each document, get the real label. Store the pid with its real label in the trueLabels map
        for (String key: docs.keySet()) {
            if (hamTest.get(key) != null) {
                trueLabels.put(key, "ham");
            }
            else if (spamTest.get(key) != null) {
                trueLabels.put(key, "spam");
            }
        }

        F1Evaluator f1 = new F1Evaluator(trueLabels);
        double f1Score = f1.evaluateCalledLabels(calledLabels);
        System.out.println("F1 score: " + f1Score);

        MAPEvaluator map = new MAPEvaluator(trueLabels);
        double MAPScore = map.evaluateCalledLabels(calledLabels);
        System.out.println("MAP: " + MAPScore);
    }


    /**
     * Evaluate the trained NaiveBayesTrigramPredictor by computing F1 and MAP scores.
     *
     * @param spamTest HashMap in the form pid => tokens
     * @param hamTest HashMap in the form pid => tokens
     * @param docs HashMap in the form pid => tokens
     */
    public void evaluateTrgramPredictor(HashMap<String, String> spamTest, HashMap<String, String> hamTest, HashMap<String, String> docs) {

        HashMap <String, String> calledLabels = new HashMap<>();
        HashMap <String, String> trueLabels = new HashMap<>();

        // For each document, call the predict method. Store the pid with its prediction in the calledLabels map
        for (String key: docs.keySet()) {
            String text = docs.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            String label = this.classifyWithTrigrams(tokens);
            calledLabels.put(key, label);
        }

        // For each document, get the real label. Store the pid with its real label in the trueLabels map
        for (String key: docs.keySet()) {
            if (hamTest.get(key) != null) {
                trueLabels.put(key, "ham");
            }
            else if (spamTest.get(key) != null) {
                trueLabels.put(key, "spam");
            }
        }

        F1Evaluator f1 = new F1Evaluator(trueLabels);
        double f1Score = f1.evaluateCalledLabels(calledLabels);
        System.out.println("F1 score: " + f1Score);

        MAPEvaluator map = new MAPEvaluator(trueLabels);
        double MAPScore = map.evaluateCalledLabels(calledLabels);
        System.out.println("MAP: " + MAPScore);
    }


    /**
     * Evaluate the trained NaiveBayesQuadgramPredictor by computing F1 and MAP scores.
     *
     * @param spamTest HashMap in the form pid => tokens
     * @param hamTest HashMap in the form pid => tokens
     * @param docs HashMap in the form pid => tokens
     */
    public void evaluateQuadgramPredictor(HashMap<String, String> spamTest, HashMap<String, String> hamTest, HashMap<String, String> docs) {

        HashMap <String, String> calledLabels = new HashMap<>();
        HashMap <String, String> trueLabels = new HashMap<>();

        // For each document, call the predict method. Store the pid with its prediction in the calledLabels map
        for (String key: docs.keySet()) {
            String text = docs.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            String label = this.classifyWithQuadgrams(tokens);
            calledLabels.put(key, label);
        }

        // For each document, get the real label. Store the pid with its real label in the trueLabels map
        for (String key: docs.keySet()) {
            if (hamTest.get(key) != null) {
                trueLabels.put(key, "ham");
            }
            else if (spamTest.get(key) != null) {
                trueLabels.put(key, "spam");
            }
        }

        F1Evaluator f1 = new F1Evaluator(trueLabels);
        double f1Score = f1.evaluateCalledLabels(calledLabels);
        System.out.println("F1 score: " + f1Score);

        MAPEvaluator map = new MAPEvaluator(trueLabels);
        double MAPScore = map.evaluateCalledLabels(calledLabels);
        System.out.println("MAP: " + MAPScore);
    }


    /**
     * Evaluate the trained StopCoverPredictor by computing F1 and MAP scores.
     *
     * @param spamTest HashMap in the form pid => tokens
     * @param hamTest HashMap in the form pid => tokens
     * @param docs HashMap in the form pid => tokens
     */
    public void evaluateStopCoverPredictor(HashMap<String, String> spamTest, HashMap<String, String> hamTest, HashMap<String, String> docs) {

        HashMap <String, String> calledLabels = new HashMap<>();
        HashMap <String, String> trueLabels = new HashMap<>();

        // For each document, call the predict method. Store the pid with its prediction in the calledLabels map
        for (String key: docs.keySet()) {
            String text = docs.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            String label = this.classifyWithStopCover(tokens);
            calledLabels.put(key, label);
        }

        // For each document, get the real label. Store the pid with its real label in the trueLabels map
        for (String key: docs.keySet()) {
            if (hamTest.get(key) != null) {
                trueLabels.put(key, "ham");
            }
            else if (spamTest.get(key) != null) {
                trueLabels.put(key, "spam");
            }
        }

        F1Evaluator f1 = new F1Evaluator(trueLabels);
        double f1Score = f1.evaluateCalledLabels(calledLabels);
        System.out.println("F1 score: " + f1Score);

        MAPEvaluator map = new MAPEvaluator(trueLabels);
        double MAPScore = map.evaluateCalledLabels(calledLabels);
        System.out.println("MAP: " + MAPScore);
    }


    /**
     * Evaluate the trained FracStopPredictor by computing F1 and MAP scores.
     *
     * @param spamTest HashMap in the form pid => tokens
     * @param hamTest HashMap in the form pid => tokens
     * @param docs HashMap in the form pid => tokens
     */
    public void evaluateFracStopPredictor(HashMap<String, String> spamTest, HashMap<String, String> hamTest, HashMap<String, String> docs) throws FileNotFoundException {

        HashMap <String, String> calledLabels = new HashMap<>();
        HashMap <String, String> trueLabels = new HashMap<>();

        // For each document, call the predict method. Store the pid with its prediction in the calledLabels map
        for (String key: docs.keySet()) {
            String text = docs.get(key);
            List<String> tokens = createTokenList(text, new EnglishAnalyzer());
            String label = this.classifyWithFracStops(tokens);
            calledLabels.put(key, label);
        }

        // For each document, get the real label. Store the pid with its real label in the trueLabels map
        for (String key: docs.keySet()) {
            if (hamTest.get(key) != null) {
                trueLabels.put(key, "ham");
            }
            else if (spamTest.get(key) != null) {
                trueLabels.put(key, "spam");
            }
        }

        F1Evaluator f1 = new F1Evaluator(trueLabels);
        double f1Score = f1.evaluateCalledLabels(calledLabels);
        System.out.println("F1 score: " + f1Score);

        MAPEvaluator map = new MAPEvaluator(trueLabels);
        double MAPScore = map.evaluateCalledLabels(calledLabels);
        System.out.println("MAP: " + MAPScore);
    }


    /**
     * Evaluate the trained SpecialCharPredictor by computing F1 and MAP scores.
     *
     * @param spamTest HashMap in the form pid => tokens
     * @param hamTest HashMap in the form pid => tokens
     * @param docs HashMap in the form pid => tokens
     */
    public void evaluateSpecialCharPredictor(HashMap<String, String> spamTest, HashMap<String, String> hamTest, HashMap<String, String> docs) {

        HashMap <String, String> calledLabels = new HashMap<>();
        HashMap <String, String> trueLabels = new HashMap<>();

        // For each document, call the predict method. Store the pid with its prediction in the calledLabels map
        for (String key: docs.keySet()) {
            String text = docs.get(key);
            List<String> tokens = createTokenList(text, new WhitespaceAnalyzer());
            String label = this.classifyWithSpecialChars(tokens);
            calledLabels.put(key, label);
        }

        // For each document, get the real label. Store the pid with its real label in the trueLabels map
        for (String key: docs.keySet()) {
            if (hamTest.get(key) != null) {
                trueLabels.put(key, "ham");
            }
            else if (spamTest.get(key) != null) {
                trueLabels.put(key, "spam");
            }
        }

        F1Evaluator f1 = new F1Evaluator(trueLabels);
        double f1Score = f1.evaluateCalledLabels(calledLabels);
        System.out.println("F1 score: " + f1Score);

        MAPEvaluator map = new MAPEvaluator(trueLabels);
        double MAPScore = map.evaluateCalledLabels(calledLabels);
        System.out.println("MAP: " + MAPScore);
    }


    /**
     * Desc: Parse the document tokens and make a hash map of hash maps for the class passed as a parameter for the
     *       NaiveBayesUnigramPredictor.
     *
     * @param docClass the document label of "ham" or "spam"
     * @param tokens List of tokens in the document
     */
    public void buildHashMap(String docClass, List<String> tokens) {

        if (bayesMap.get(docClass) == null) {
            HashMap<String, Integer> classMap = new HashMap<>();
            bayesMap.put(docClass, classMap);
        }

        HashMap<String, Integer> curMap = bayesMap.get(docClass);

        for (String token : tokens) {
            if (!curMap.containsKey(token)) {
                curMap.put(token, 0);
            }

            int curCount = curMap.get(token);
            curMap.put(token, curCount + 1);
        }
    }


    /**
     * Desc: For the NaiveBayesUnigramPredictor, parse the tokens of the document passed as a parameter and sum the
     *       counts of each word.
     *
     * @param tokens List of tokens in the document.
     * @return The document class with the larger count.
     */
    public String classify(List<String> tokens) {

        HashMap<String, Integer> spamDist = bayesMap.get("spam");
        HashMap<String, Integer> hamDist = bayesMap.get("ham");

        double hamScore = 0;
        double spamScore = 0;

        for (String token : tokens) {
            spamScore += Math.log(spamDist.getOrDefault(token, 1));
            hamScore += Math.log(hamDist.getOrDefault(token, 1));
        }

        if (hamScore > spamScore) {
            return "ham";
        }
        else {
            return "spam";
        }
    }


    /**
     * Desc: For the NaiveBayesUnigramPredictor, parse the tokens for a document and sum the counts of each word.
     *
     * @param tokens List of tokens in the document.
     * @return scores a list containing the hamScore, followed by the spamScore.
     */
    public ArrayList<Double> getScores(List<String> tokens) {

        HashMap<String, Integer> spamDist = bayesMap.get("spam");
        HashMap<String, Integer> hamDist = bayesMap.get("ham");
        ArrayList<Double> scores = new ArrayList<>();

        double hamScore = 0;
        double spamScore = 0;

        for (String token : tokens) {
            spamScore += Math.log(spamDist.getOrDefault(token, 1));
            hamScore += Math.log(hamDist.getOrDefault(token, 1));
        }

        scores.add(hamScore);
        scores.add(spamScore);
        return scores;
    }


    /**
     * Desc: Parse the document tokens and make a hash map of hash maps for the class passed as a parameter for the
     *       NaiveBayesBigramPredictor.
     *
     * @param docClass the document label of "ham" or "spam"
     * @param tokens List of tokens in the document
     */
    public void buildBigramsHashMap(String docClass, List<String> tokens) {

        if (bayesMap.get(docClass) == null) {
            HashMap<String, Integer> classMap = new HashMap();
            bayesMap.put(docClass, classMap);
        }

        HashMap<String, Integer> curMap = bayesMap.get(docClass);

        for (int i = 0; i < tokens.size() - 1; i++) {
            String bigram = tokens.get(i) + tokens.get(i + 1);
            if (!curMap.containsKey(bigram)) {
                curMap.put(bigram, 0);
            }

            int curCount = curMap.get(bigram);
            curMap.put(bigram, curCount + 1);

        }

    }


    /**
     * Desc: For the NaiveBayesBigramPredictor, parse the tokens of the document passed as a parameter and sum the
     *       counts of each word.
     *
     * @param tokens List of tokens in the document.
     * @return The document class with the larger count.
     */
    public String classifyWithBigrams(List<String> tokens) {

        HashMap<String, Integer> spamDist = bayesMap.get("spam");
        HashMap<String, Integer> hamDist = bayesMap.get("ham");

        double hamScore = 0;
        double spamScore = 0;

        for (int i = 0; i < tokens.size() - 1; i++) {
            String bigram = tokens.get(i) + tokens.get(i + 1);
            spamScore += Math.log(spamDist.getOrDefault(bigram, 1));
            hamScore += Math.log(hamDist.getOrDefault(bigram, 1));
        }

        if (hamScore > spamScore) {
            return "ham";
        }
        else {
            return "spam";
        }
    }


    /**
     * Desc: For the NaiveBayesBigramPredictor, parse the tokens for a document and sum the counts of each word.
     *
     * @param tokens List of tokens in the document.
     * @return scores a list containing the hamScore, followed by the spamScore.
     */
    public ArrayList<Double> getBigramScores(List<String> tokens) {

        HashMap<String, Integer> spamDist = bayesMap.get("spam");
        HashMap<String, Integer> hamDist = bayesMap.get("ham");
        ArrayList<Double> scores = new ArrayList<>();

        double hamScore = 0;
        double spamScore = 0;

        for (int i = 0; i < tokens.size() - 1; i++) {
            String bigram = tokens.get(i) + tokens.get(i + 1);
            spamScore += Math.log(spamDist.getOrDefault(bigram, 1));
            hamScore += Math.log(hamDist.getOrDefault(bigram, 1));
        }

        scores.add(hamScore);
        scores.add(spamScore);
        return scores;
    }


    /**
     * Desc: Parse the document tokens and make a hash map of hash maps for the class passed as a parameter for the
     *       NaiveBayesTrigramPredictor.
     *
     * @param docClass the document label of "ham" or "spam"
     * @param tokens List of tokens in the document
     */
    public void buildTrigramsHashMap(String docClass, List<String> tokens) {

        if (bayesMap.get(docClass) == null) {
            HashMap<String, Integer> classMap = new HashMap();
            bayesMap.put(docClass, classMap);
        }

        HashMap<String, Integer> curMap = bayesMap.get(docClass);

        for (int i = 0; i < tokens.size() - 2; i++) {
            String trigram = tokens.get(i) + tokens.get(i + 1) + tokens.get(i + 2);
            if (!curMap.containsKey(trigram)) {
                curMap.put(trigram, 0);
            }

            int curCount = curMap.get(trigram);
            curMap.put(trigram, curCount + 1);
        }
    }

    /**
     * Desc: For the NaiveBayesTrigramredictor, parse the tokens of the document passed as a parameter and sum the
     *       counts of each word.
     *
     * @param tokens List of tokens in the document.
     * @return The document class with the larger count.
     */
    public String classifyWithTrigrams(List<String> tokens) {

        HashMap<String, Integer> spamDist = bayesMap.get("spam");
        HashMap<String, Integer> hamDist = bayesMap.get("ham");

        double hamScore = 0;
        double spamScore = 0;

        for (int i = 0; i < tokens.size() - 2; i++) {
            String trigram = tokens.get(i) + tokens.get(i + 1) + tokens.get(i + 2);
            spamScore += Math.log(spamDist.getOrDefault(trigram, 1));
            hamScore += Math.log(hamDist.getOrDefault(trigram, 1));
        }

        if (hamScore > spamScore) {
            return "ham";
        }
        else {
            return "spam";
        }
    }


    /**
     * Desc: For the NaiveBayesTrigramPredictor, parse the tokens for a document and sum the counts of each word.
     *
     * @param tokens List of tokens in the document.
     * @return scores a list containing the hamScore, followed by the spamScore.
     */
    public ArrayList<Double> getTrigramScores(List<String> tokens) {

        HashMap<String, Integer> spamDist = bayesMap.get("spam");
        HashMap<String, Integer> hamDist = bayesMap.get("ham");
        ArrayList<Double> scores = new ArrayList<>();

        double hamScore = 0;
        double spamScore = 0;

        for (int i = 0; i < tokens.size() - 2; i++) {
            String trigram = tokens.get(i) + tokens.get(i + 1) + tokens.get(i + 2);
            spamScore += Math.log(spamDist.getOrDefault(trigram, 1));
            hamScore += Math.log(hamDist.getOrDefault(trigram, 1));
        }

        scores.add(hamScore);
        scores.add(spamScore);
        return scores;
    }


    /**
     * Desc: Parse the document tokens and make a hash map of hash maps for the class passed as a parameter for the
     *       NaiveBayesQuadgramPredictor.
     *
     * @param docClass the document label of "ham" or "spam"
     * @param tokens List of tokens in the document
     */
    public void buildQuadgramsHashMap(String docClass, List<String> tokens) {

        if (bayesMap.get(docClass) == null) {
            HashMap<String, Integer> classMap = new HashMap();
            bayesMap.put(docClass, classMap);
        }

        HashMap<String, Integer> curMap = bayesMap.get(docClass);

        for (int i = 0; i < tokens.size() - 3; i++) {
            String quadgram = tokens.get(i) + tokens.get(i + 1) + tokens.get(i + 2) + tokens.get(i + 3);
            if (!curMap.containsKey(quadgram)) {
                curMap.put(quadgram, 0);
            }

            int curCount = curMap.get(quadgram);
            curMap.put(quadgram, curCount + 1);

        }

    }


    /**
     * Desc: For the NaiveBayesQuadgramPredictor, parse the tokens of the document passed as a parameter and sum the
     *       counts of each word.
     *
     * @param tokens List of tokens in the document.
     * @return The document class with the larger count.
     */
    public String classifyWithQuadgrams(List<String> tokens) {

        HashMap<String, Integer> spamDist = bayesMap.get("spam");
        HashMap<String, Integer> hamDist = bayesMap.get("ham");

        double hamScore = 0;
        double spamScore = 0;

        for (int i = 0; i < tokens.size() - 3; i++) {
            String quadgram = tokens.get(i) + tokens.get(i + 1) + tokens.get(i + 2) + tokens.get(i + 3);
            spamScore += Math.log(spamDist.getOrDefault(quadgram, 1));
            hamScore += Math.log(hamDist.getOrDefault(quadgram, 1));
        }

        if (hamScore > spamScore) {
            return "ham";
        }
        else {
            return "spam";
        }
    }


    /**
     * Desc: For the NaiveBayesQuadgramPredictor, parse the tokens for a document and sum the counts of each word.
     *
     * @param tokens List of tokens in the document.
     * @return scores a list containing the hamScore, followed by the spamScore.
     */
    public ArrayList<Double> getQuadgramScores(List<String> tokens) {

        HashMap<String, Integer> spamDist = bayesMap.get("spam");
        HashMap<String, Integer> hamDist = bayesMap.get("ham");
        ArrayList<Double> scores = new ArrayList<>();

        double hamScore = 0;
        double spamScore = 0;

        for (int i = 0; i < tokens.size() - 3; i++) {
            String quadgram = tokens.get(i) + tokens.get(i + 1) + tokens.get(i + 2) + tokens.get(i + 3);
            spamScore += Math.log(spamDist.getOrDefault(quadgram, 1));
            hamScore += Math.log(hamDist.getOrDefault(quadgram, 1));
        }

        scores.add(hamScore);
        scores.add(spamScore);
        return scores;
    }


    /**
     * Desc: Parse the document tokens and make a hash map of hash maps for the class passed as a parameter for the
     *       StopWordsPredictor.
     *
     * @param docClass the document label of "ham" or "spam"
     * @param tokens List of tokens in the document
     */
    public void buildStopWordHashMap(String docClass, List<String> tokens, String pid) {

        if (stopWordMap.get(docClass) == null) {
            HashMap<String, Double> classMap = new HashMap<>();
            stopWordMap.put(docClass, classMap);
        }

        HashMap<String, Double> curMap = stopWordMap.get(docClass);
        // Get the stop coverage for each document in the ham and spam sets.
        double stop = 0;
        double total = 0;

        for (String token : tokens) {
            if (isStopword(token)) {
                stop++;
            }
            total++;
        }

        double result = (stop / total) * 100;
        curMap.put(pid, result);
    }


    /**
     * Desc: For the StopCoverPredictor, parse the tokens of the document passed as a parameter and sum the
     *       counts of each word.
     *
     * @param tokens List of tokens in the document.
     * @return The document class with the larger count.
     */
    public String classifyWithStopCover(List<String> tokens) {

        HashMap<String, Double> spamDist = stopWordMap.get("spam");
        HashMap<String, Double> hamDist = stopWordMap.get("ham");

        // Get the average number of stopwords for spam and ham training
        double sum = 0;
        for (int i = 0; i < spamDist.size(); i++) {
            sum += spamDist.getOrDefault(i, 0.0);
        }
        double spamAverage = sum/spamDist.size();

        sum = 0;
        for (int i = 0; i < hamDist.size(); i++) {
            sum += hamDist.getOrDefault(i, 0.0);
        }
        double hamAverage = sum/hamDist.size();

        double stop = 0;
        double total = 0;

        for (String token : tokens) {
            if (isStopword(token)) {
                stop++;
            }
            total++;
        }

        double result = (stop / total) * 100;

        if (Math.abs(spamAverage - result) < Math.abs(hamAverage - result)) {
            return "ham";
        }
        else {
            return "spam";
        }
    }


    /**
     * Desc: Parse the document tokens and make a hash map of hash maps for the class passed as a parameter for the
     *       FracStopPredictor.
     *
     * @param docClass the document label of "ham" or "spam"
     * @param tokens List of tokens in the document
     */
    public void buildFracStopHashMap(String docClass, List<String> tokens, String pid) throws FileNotFoundException {

        // Get list of the most common stopwords
        ArrayList<String> stopwords = new ArrayList<>();
        Scanner s = new Scanner(new File("/home/rachel/grad_courses/data_science/cs953-team1/stopwords"));
        while (s.hasNext()) {
            stopwords.add(s.next());
        }
        s.close();

        if (stopWordMap.get(docClass) == null) {
            HashMap<String, Double> classMap = new HashMap<>();
            stopWordMap.put(docClass, classMap);
        }

        HashMap<String, Double> curMap = stopWordMap.get(docClass);
        // Get the stop coverage for each document in the ham and spam sets.
        double stop = 0;
        double total = 0;

        for (String token : tokens) {
            if (stopwords.contains(token)) {
                stop++;
            }
            total++;
        }

        double result = (stop / total) * 100;
        curMap.put(pid, result);
    }


    /**
     * Desc: For the FracStopPredictor, parse the tokens of the document passed as a parameter and sum the
     *       counts of each word.
     *
     * @param tokens List of tokens in the document.
     * @return The document class with the larger count.
     */
    public String classifyWithFracStops(List<String> tokens) throws FileNotFoundException {

        // Get list of the most common stopwords
        ArrayList<String> stopwords = new ArrayList<>();
        Scanner s = new Scanner(new File("stopwords"));
        while (s.hasNext()) {
            stopwords.add(s.next());
        }
        s.close();

        HashMap<String,  Double> spamDist = stopWordMap.get("spam");
        HashMap<String, Double> hamDist = stopWordMap.get("ham");

        // Get the average number of stopwords for spam and ham training
        double sum = 0;
        for (int i = 0; i < spamDist.size(); i++) {
            sum += spamDist.getOrDefault(i, 0.0);
        }
        double spamAverage = sum/spamDist.size();

        sum = 0;
        for (int i = 0; i < hamDist.size(); i++) {
            sum += hamDist.getOrDefault(i, 0.0);
        }
        double hamAverage = sum/hamDist.size();

        double stop = 0;
        double total = 0;

        for (String token : tokens) {
            if (stopwords.contains(token)) {
                stop++;
            }
            total++;
        }

        double result = (stop / total) * 100;

        if (Math.abs(spamAverage - result) < Math.abs(hamAverage - result)) {
            return "ham";
        }
        else {
            return "spam";
        }
    }


    /**
     * Desc: Parse the document tokens and make a hash map of hash maps for the class passed as a parameter for the
     *       SpecialCharPredictor.
     *
     * @param docClass the document label of "ham" or "spam"
     * @param tokens List of tokens in the document
     */
    public void buildSpecialCharHashMap(String docClass, List<String> tokens, String pid) {

        if (specialCharMap.get(docClass) == null) {
            HashMap<String, Double> classMap = new HashMap<>();
            specialCharMap.put(docClass, classMap);
        }
        HashMap<String, Double> curMap = specialCharMap.get(docClass);

        // Record all ratios of special characters to total tokens in the appropriate hash map for the current document class.
        double specialCharRatio = getSpecialCharRatio(tokens);
        curMap.put(pid, specialCharRatio);
    }


    /**
     * Desc: For the SpecialCharPredictor, parse the tokens of the document passed as a parameter and sum the
     *       counts of each word.
     *
     * @param tokens List of tokens in the document.
     * @return The document class with the larger count.
     */
    public String classifyWithSpecialChars(List<String> tokens) {

        HashMap<String,  Double> spamDist = specialCharMap.get("spam");
        HashMap<String, Double> hamDist = specialCharMap.get("ham");

        // Get the average ratio of special chars for spam and ham training
        double sum = 0;
        for (String key: spamDist.keySet()) {
            sum += spamDist.get(key);
        }
        double spamAverage = sum/spamDist.size();

        sum = 0;
        for (String key: hamDist.keySet()) {
            sum += hamDist.get(key);
        }
        double hamAverage = sum/hamDist.size();

        double specialCharRatio = getSpecialCharRatio(tokens);

        // check whether the specialCharRatio is closer to the hamAverage or the spamAverage
        if (Math.abs(spamAverage - specialCharRatio) < Math.abs(hamAverage - specialCharRatio)) {
            return "spam";
        }
        else {
            return "ham";
        }
    }


    /**
     * Desc: For the SpecialCharPredictor, parse the tokens for a document and get the ratio of special charracters to
     *       normal characters.
     *
     * @param tokens List of tokens in the document.
     * @return scores a list containing the final score of the document. The higher the score, the spammier the document.
     */
    public ArrayList<Double> getSpecialCharScores(List<String> tokens) {

        ArrayList<Double> score = new ArrayList<>();
        score.add(getSpecialCharRatio(tokens));
        return score;
    }


    /**
     * Desc: Helper function to compute the special char ratio for each document.
     *
     * @param tokens List of tokens in the document.
     * @return score of the document. The higher the score, the spammier the document.
     */
    public double getSpecialCharRatio (List<String> tokens) {

        double numSpecial = 0;
        double numTokens = 0;
        for (String token: tokens) {
            for (int i = 0; i < token.length(); i++) {
                if (Character.toString(token.charAt(i)).matches("[^a-z]")) {
                    numSpecial++;
                }
            }
            numTokens++;
        }

        // Is the first token a special token? If so, multiply the final ratio by a weight
        String first = tokens.get(0);
        String last = tokens.get(tokens.size() - 1);
        double weight = 1;
        if (Character.toString(first.charAt(0)).matches("[^A-Za-z]") || (numTokens < 10 && Character.toString(last.charAt(last.length() -1)).matches("[:]"))) {
            weight = 1.5;
        }
        return weight*(numSpecial/numTokens);
    }
}
