package main.java.svm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Abstract class to instantiate a PythonSVM
 */
abstract public class SVM {

    private HashMap<String, HashMap<String, Integer>> freqMap;
    private ArrayList<String> words;

    public SVM(HashMap<String, HashMap<String, Integer>> freqMap, ArrayList<String> words) {
        freqMap = new HashMap<>();
        words = new ArrayList<>();
    }

}
