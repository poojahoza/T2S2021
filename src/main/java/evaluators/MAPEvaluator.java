package main.java.evaluators;

import java.util.HashMap;

/**
 * Compute the MAP score of a classifier by comparing true labels with predicted labels.
 */
public class MAPEvaluator {

    private HashMap<String, String> correctLabels;

    public MAPEvaluator(HashMap<String, String> trueLabels) {
        correctLabels = trueLabels;
    }

    public double evaluateCalledLabels(HashMap<String, String> calledLabels) {

        double tp = 0;
        double fp = 0;

        /* map must contain id => label */
        double total = 0;
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
                } else if (!isSpam && calledLabel.equals("spam")) {
                    fp++;
                }

                if (tp > 0) {
                    double curPrec = tp / (tp + fp);
                    total += curPrec;
                }


            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        total /= calledLabels.size();

        return total;
    }

}
