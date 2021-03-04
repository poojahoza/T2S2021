package main.java.svm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class to demonstrate the use of the PythonSVM.
 */
public class DemoPythonSVM {

    public static void main (String[] args) throws IOException {

        HashMap<String, HashMap<String, Integer>> freqMap = new HashMap<>();
        ArrayList<String> words = new ArrayList<>();
        PythonSVM pythonSVM = new PythonSVM(freqMap, words);

        /* Data preprocessing stage. Data mush be in the form pid -> text, and you'll have to change the paths
            below as appropriate. The ham and spam train sets from the Naive Bayes classifier can be used, and
            are still in the same location on the server.
            IMPORTANT NOTE: Once you have your train and test csvs, you'll have to change the paths
            in svm.py to match them.
         */
        ArrayList<HashMap<Integer, Double>> spamTrain = pythonSVM.readIndex("/home/rachel/grad_courses/data_science/spamTrain");
        ArrayList<HashMap<Integer, Double>> hamTrain = pythonSVM.readIndex("/home/rachel/grad_courses/data_science/hamTrain");
        ArrayList<HashMap<Integer, Double>> test = pythonSVM.readIndex("/home/rachel/grad_courses/data_science/hamSpamTest");

        String trainPath = "/home/rachel/grad_courses/data_science/train_data.csv";
        String testPath = "/home/rachel/grad_courses/data_science/test_data.csv";
        pythonSVM.prepareData(spamTrain, hamTrain, test, trainPath, testPath);

        /* After prepreoceesing the data, you can call the following to train your model and return
           class labels. 1 means the document is a ham, and -1 means it is a spam.
         */

        ArrayList<String> linearLabels = pythonSVM.execLinearSVC();
        ArrayList<String> polynomialLabels = pythonSVM.execPolynomialSVC();
        ArrayList<String> sigmoidLabels = pythonSVM.execSigmoidSVC();
        ArrayList<String> rbfLabels = pythonSVM.execRbfSVC();

        System.out.println("Finished classifying, printing labels");
        for (int i = 0; i < linearLabels.size(); i++) {
            System.out.println(linearLabels.get(i));
        }
        for (int i = 0; i < polynomialLabels.size(); i++) {
            System.out.println(polynomialLabels.get(i));
        }
        for (int i = 0; i < sigmoidLabels.size(); i++) {
            System.out.println(sigmoidLabels.get(i));
        }
        for (int i = 0; i < rbfLabels.size(); i++) {
            System.out.println(rbfLabels.get(i));
        }

    }

}
