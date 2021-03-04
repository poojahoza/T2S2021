package main.java.mrf;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;

public class Normalize
{

    private static double getMean(ArrayList<Double> vec)
    {
        int n = vec.size() > 1 ? vec.size() : 1;
        Double meanscore = 0.0;
        for(Double d:vec)
        {
            meanscore+=d;
        }
        return (meanscore/n);
    }

    static double  getVariance(ArrayList<Double> vec) {

        double mean = getMean(vec);
        double temp = 0;
        for(double a :vec)
            temp += (a-mean)*(a-mean);
        return temp/(vec.size()-1);
    }

    static double  getStdDev(ArrayList<Double> vec) {
        return Math.sqrt(getVariance(vec));
    }


    static ArrayList<Double> getZScoreNormalized(ArrayList<Double> unnorm)
    {
        if(unnorm.size()<2) return unnorm;


        ArrayList<Double> norm = new ArrayList<>();
        Double mean = getMean(unnorm);
        Double std = getStdDev(unnorm);

        for(double d:unnorm)
        {
            double zscore = (d - mean)/std;
            norm.add(zscore);
        }
        return norm;
    }
}
