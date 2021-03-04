package main.java.evaluators;

import java.util.HashMap;

/**
 * Compute the F1 score of a classifier by comparing true labels to predicted labels.
 */
public class F1Evaluator {

    private HashMap<String, String> correctLabels;

    public F1Evaluator(HashMap<String, String> trueLabels) {
        correctLabels = trueLabels;
    }

    public double evaluateCalledLabels(HashMap<String, String> calledLabels) {

        double tn = 0;
        double tp = 0;
        double fn = 0;
        double fp = 0;

        /* map must contain id => label */
        for (String id : calledLabels.keySet()) {
            String calledLabel = calledLabels.get(id);
            String correctLabel = null;
            if (correctLabels.containsKey((id))) {
                correctLabel = correctLabels.get(id); // Will be "ham" or "spam"
            }
            boolean isSpam = false;
            try {
                assert correctLabel != null;
                if (correctLabel.equals("spam")) {
                    isSpam = true;
                }

                if (isSpam && calledLabel.equals("spam")) {
                    tp++;
                } else if (isSpam && !calledLabel.equals("spam")) {
                    fn++;
                } else if (!isSpam && !calledLabel.equals("spam")) {
                    tn++;
                } else {
                    fp++;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        System.out.println("true positives: " + tp);
        System.out.println("false positives: " + fp);
        System.out.println("true negatives: " + tn);
        System.out.println("false negatives: " + fn);

        double precision = tp / (tp + fp);
        double recall = tp / (tp + fn);
        double f1 = 2 * (precision * recall) / (precision + recall);
        double precision2 = tn / (tn + fn);
        double recall2 = tn / (tn + fp);
        double f2 = 2 * (precision2 * recall2) / (precision2 + recall2);
        return (f1 + f2)/2;
    }

}
