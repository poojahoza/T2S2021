package main.java.utils;

/**
 * @author poojaoza
 **/

/*
The below code is taken from the Stack Overflow:
https://stackoverflow.com/questions/51772174/zscore-and-p-value-in-java
 */

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Stats {

    private void computeZScoreAndSurvivalFunctions(
            DescriptiveStatistics ds,
            RealDistribution dist,
            BiConsumer<Double, Double> consumer
    ) {
        double variance = ds.getPopulationVariance();
        double sd = Math.sqrt(variance);
        double mean = ds.getMean();
        for (int index = 0; index < ds.getN(); ++index) {
            double zscore = (ds.getElement(index) - mean) / sd;
            double sf = 1.0 - dist.cumulativeProbability(Math.abs(zscore));
            consumer.accept(zscore, sf);
        }
    }

    /*public void getZScore(){
        computeZScoreAndSurvivalFunctions();
    }*/

    private Double calculateNormalize(Double val, Double min, Double max){
        System.out.println("min , max "+min+" "+max+" "+val+" "+((val - min)/(max - min)));
        if((max - min) == 0.0) {
            System.out.println("in if min, max");
            return 0.0;
        }
        return (val - min)/(max - min);
    }

    public Map<String, Map<String, Double[]>> normalizeData(Map<String, Map<String, Double[]>> query_entities_list){
        Map<String, Double[]> query_min_max = new LinkedHashMap<>();
        for(Map.Entry<String, Map<String, Double[]>> m: query_entities_list.entrySet()){
            /*Double[] min = new Double[] {9999999.0000000, 9999999.0000000, 9999999.0000000};
            Double[] max = new Double[] {0.0, 0.0, 0.0};*/

            Double[] min = new Double[] {9999999.0000000, 9999999.0000000};
            Double[] max = new Double[] {0.0, 0.0};

            for(Map.Entry<String, Double[]> m_value: m.getValue().entrySet()){
                Double[] val = m_value.getValue();

                if (val[0] < min[0]){
                    min[0] = val[0];
                }
                if (val[1] < min[1]){
                    min[1] = val[1];
                }
     /*           if (val[2] < min[2]){
                    min[2] = val[2];
                }*/

                if (val[0] > max[0]){
                    max[0] = val[0];
                }
                if (val[1] > max[1]){
                    max[1] = val[1];
                }
                /*if (val[2] > max[2]){
                    max[2] = val[2];
                }*/
            }
            //query_min_max.put(m.getKey(), new Double[]{min[0], max[0], min[1], max[1], min[2], max[2]});
            query_min_max.put(m.getKey(), new Double[]{min[0], max[0], min[1], max[1]});
        }

        for(Map.Entry<String, Map<String, Double[]>> n: query_entities_list.entrySet()){
            Map<String, Double[]> temp = new LinkedHashMap<>();
            for(Map.Entry<String, Double[]> n_value: n.getValue().entrySet()){
                Double[] val = n_value.getValue();
                Double[] min_max = query_min_max.get(n.getKey());
                val[0] = calculateNormalize(val[0], min_max[0], min_max[1]);
                System.out.println(val[1]+" "+min_max[2]+" "+min_max[3]);
                val[1] = calculateNormalize(val[1], min_max[2], min_max[3]);
                //val[2] = calculateNormalize(val[2], min_max[4], min_max[5]);
                temp.put(n_value.getKey(), val);
                //System.out.println("normalized features : "+n.getKey()+" "+n_value.getKey()+" "+val[0]+" "+val[1]+" "+val[2]);
                System.out.println("normalized features : "+n.getKey()+" "+n_value.getKey()+" "+val[0]+" "+val[1]);
            }
            query_entities_list.put(n.getKey(), temp);
        }
        return query_entities_list;
    }

    public double getDotProduct(Double[] vec1, Double[] vec2){

        double dotproduct = 0.0;

        if(vec1.length != vec2.length){
            return dotproduct;
        }
        for(int i = 0; i < vec1.length; i++){
            dotproduct += vec1[i]*vec2[i];
        }
        return dotproduct;
    }

    public double getEuclideanDistance(Double[] vec1, Double[] vec2){

        double dotproduct = 0.0;

        if(vec1.length != vec2.length){
            return dotproduct;
        }
        for(int i = 0; i < vec1.length; i++){
            dotproduct += Math.pow(vec2[i]-vec1[i], 2);
        }
        dotproduct = Math.sqrt(dotproduct);
        return dotproduct;

    }

}
