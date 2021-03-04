package main.java.predictors;

import java.io.IOException;

public class ClassifyUtilsDemo {

    public static void main(String [] args) throws IOException {
        ClassifyUtils classifyUtils = new ClassifyUtils();
        classifyUtils.parseRunfile();
        classifyUtils.buildTrainTestSets();

    }
}
