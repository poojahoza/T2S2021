package main.java.mrf;

import main.java.containers.Container;
import main.java.rerankerv2.concepts.EmbeddingStrategy;
import main.java.searcher.BaseBM25;
import main.java.utils.PreProcessor;
import main.java.utils.SortUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;


import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static main.java.utils.RunWriter.checkDir;
import static main.java.utils.RunWriter.createFile;

public  class MrfHelper
{
    public static INDArray getVector(String query, EmbeddingStrategy embedding,Integer Dimension)
    {
        INDArray res = Nd4j.create(Dimension).add(0.000001);
        int _number_of_terms=1;
        try {
            for(String val:PreProcessor.processTermsUsingLucene(query))
            {
                    if(embedding.getEmbeddingVector(val)!=null)
                    {
                        _number_of_terms++;
                        INDArray temp = embedding.getEmbeddingVector(val);
                        res = res.add(temp);
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res.div(_number_of_terms);
    }



    static void updatescores(Map<String,Container> res, ArrayList<Double> fet)
    {
        int index =0;
        if(res.size()!= fet.size())
        {
            System.out.println("Mismatch in the size, something wrong");
            System.exit(-1);
        }

        for(Map.Entry<String,Container> val: res.entrySet())
        {
            val.getValue().addScores(fet.get(index));
            index++;
        }
    }

    static void updateCollectiveScores(Map<String,Container> res, List<ArrayList<Double>> fet)
    {
        int index =0;
        if(res.size()!= fet.get(0).size())
        {
            System.out.println("Mismatch in the size, something wrong");
            System.exit(-1);
        }

        for(Map.Entry<String,Container> val: res.entrySet())
        {
            val.getValue().addScores(fet.get(0).get(index));
            val.getValue().addScores(fet.get(1).get(index));
            index++;
        }
    }

    private static void closeStream(BufferedReader br)
    {
        if(br!=null) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, Map<String, Integer>> readQrel(String filename) {
        Map<String, Map<String, Integer>> mp = new LinkedHashMap<String, Map<String, Integer>>();

        File fp = new File(filename);
        FileReader fr;
        BufferedReader br = null;


        try {
            fr = new FileReader(fp);
            br = new BufferedReader(fr);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        while (true) {
            try {
                String line = br.readLine();

                if (line == null) {
                    break;
                }

                String[] words = line.split(" ");
                String outKey = words[0];

                if (mp.containsKey(outKey)) {
                    Map<String, Integer> extract = mp.get(outKey);
                    String inner_key = words[2];
                    Integer is_relevant = new Integer(words[3]);
                    extract.put(inner_key, is_relevant);
                } else {

                    String inner_key = words[2];
                    Integer is_relevant = new Integer(words[3]);
                    Map<String, Integer> temp = new LinkedHashMap<String, Integer>();
                    temp.put(inner_key, is_relevant);
                    mp.put(outKey, temp);
                }
            } catch (NullPointerException n) {
                System.out.println(n.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }
        return mp;
    }

        private static int isRelevant( Map<String, Map<String, Integer>> qrel,String qID, String docID){
        if(qrel.containsKey(qID))
        {
            if(qrel.get(qID).containsKey(docID))
            {
                return 1;
            }
        }
        return 0;
        }

     static void writeRunFile(Map<String, Map<String, Container>> res,String mname)
     {
        String dirname="result";
        boolean success = checkDir(dirname);

        if (!success) {
            System.out.println("Unable to create the directory");
            System.exit(-1);
        }

        FileWriter ptr = createFile(dirname,mname);
        String newLine = System.getProperty("line.separator");
        String runFileLine;

        for(Map.Entry<String,Map<String, Container>> outer: res.entrySet())
        {
            int ranking=0;
            for(Map.Entry<String,Container> inner:outer.getValue().entrySet())
            {
                ranking++;
                String queryID = outer.getKey();
                String pID = inner.getKey();
                //String score = String.valueOf(inner.getValue().getScore());
                StringBuilder sb =new StringBuilder();
                for(Double d:inner.getValue().getScoresList())
                {
                    sb.append(d);
                    sb.append(" ");
                }
                runFileLine = queryID+" Q0 "+pID+" "+ranking+" "+sb.toString()+newLine;
                try {
                    ptr.write(runFileLine);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        if(ptr !=null)
        {
            try {
                ptr.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    static void writeFeatureFile(Map<String, Map<String, Container>> res,String mname,String qrelpath)
    {
        String dirname="feature";
        boolean success = checkDir(dirname);

        if (!success) {
            System.out.println("Unable to create the directory");
            System.exit(-1);
        }

        Map<String,Map<String,Integer>> qrel = readQrel(qrelpath);

        FileWriter ptr = createFile(dirname,mname);
        String newLine = System.getProperty("line.separator");
        String runFileLine;

        int qidindex=0;
        for(Map.Entry<String,Map<String, Container>> outer: res.entrySet())
        {
            qidindex++;
            for(Map.Entry<String,Container> inner:outer.getValue().entrySet())
            {
                String isRel = String.valueOf(isRelevant(qrel,outer.getKey(),inner.getKey()));
                String qid = "qid:"+String.valueOf(qidindex);
                String queryid = outer.getKey()+"_";

                int count=0;
                StringBuilder sb= new StringBuilder();
                for(Double d:inner.getValue().getScoresList())
                {
                    count++;
                    sb.append(count+":"+d);
                    sb.append(" ");
                }
                String info = "#"+queryid+inner.getKey();

                runFileLine = isRel+" "+qid+" "+sb.toString()+info+newLine;
                try {
                    ptr.write(runFileLine);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        if(ptr!=null)
        {
            try {
                ptr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    /*
       This is the helper method for the evidence 3
    */

    static Double getClusterScore(Map<String, Container> unsorted,EmbeddingStrategy embedding,Integer Dimension,String Query,String indexLoc,Integer kval)
    {
        /*
            Sort the Map, choose the top k value as the cluster to the document in one question
        */

        int clustercount = 5;
        Map<String,Container> sorted = SortUtils.sortByValue(unsorted);
        int count=0;
        BaseBM25 bm25 = null;

        try {
             bm25 = new BaseBM25(kval,indexLoc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        INDArray queryVec = getVector(Query,embedding, Dimension);
        INDArray clusterCentroid = Nd4j.create(Dimension).add(0.000001);

        for(Map.Entry<String,Container> val: sorted.entrySet())
        {
            count++;
            Integer docID = val.getValue().getDocID();
            INDArray temp = getVector(bm25.getDocument(docID),embedding,Dimension);
            clusterCentroid = clusterCentroid.add(temp);
            if(clustercount == count) break;
        }

        //Compute the centroid by dividing by the number of points in the cluster
        if(clusterCentroid != null)
        {
            clusterCentroid = clusterCentroid.div(clustercount);
        }
        return Transforms.cosineSim(queryVec,clusterCentroid);
    }

    static public void getWeights(String jarPath,String fetFile,String qrelfile)
    {
        try {
            String s = null;
            String command = "java -jar "+jarPath+" -train "+ fetFile+" -ranker 4 -metric2t MAP -save feature\\model.txt";
            System.out.println("Executing the command \n");
            System.out.println(command);

            Process p = Runtime.getRuntime().exec(command);

             int code = p.waitFor();

            System.out.println("Code "+ code);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static double getWeightedScore(ArrayList<Double> weights,ArrayList<Double> run)
    {
        if(weights.size()!= run.size())
        {
            System.out.println("Cannot perform Dot product");
            System.exit(-1);
        }

        double score = 0.0;
        for(int i=0;i<weights.size();i++)
        {
            score += weights.get(i)*run.get(i);
        }
        return score;
    }

    private static ArrayList<Double> getWeights(String model)
    {
            BufferedReader br = getFileReader(model);
            ArrayList<Double> modelw = new ArrayList<>();
            while (true) {
                try {
                    String line = br.readLine();

                    if (line == null) {
                        break;
                    }

                    if(line.startsWith("#"))
                    {
                        continue;
                    }
                    else
                    {
                        String[] weights = line.split(" ");
                        for(String s:weights)
                        {
                            String temp[] = s.split(":");
                            modelw.add(Double.valueOf(temp[1]));
                        }
                    }
                } catch (IOException e) {

                }
            }
            return modelw;
    }

    static private BufferedReader getFileReader(String fname)
    {
        File fp = new File(fname);
        FileReader fr=null;
        BufferedReader br = null;

        try {
            fr = new FileReader(fp);
            br = new BufferedReader(fr);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return br;
    }

    public static void createRunFileFromWeights(String model,String run,String mname) {
        String dirname = "result";
        boolean success = checkDir(dirname);

        if (!success) {
            System.out.println("Unable to create the directory");
            System.exit(-1);
        }
        String newLine = System.getProperty("line.separator");
        ArrayList<Double> modelw= getWeights(model);

        FileWriter fw = createFile(dirname, mname);
        BufferedReader br = getFileReader(run);
        System.out.println("Creating run files from the model file.................");
        while (true) {
            try {
                String line = br.readLine();

                if (line == null) {
                    break;
                }


                String[] words = line.split(" ");
                ArrayList<Double> runval =new ArrayList<>();
                int count=0;

                for(String s: words)
                {
                    count++;
                    if(count <= 4) continue;
                    runval.add(Double.valueOf(s));
                }
                Double score = getWeightedScore(modelw,runval);

                String runline = words[0]+" "+words[1]+" "+words[2]+" "+words[3]+" "+String.valueOf(score)+" "+mname+newLine;
                fw.write(runline);

            }catch (IOException e)
            {
                e.getStackTrace();
            }
        }
        if(fw!=null)
        {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Created file in the directory "+ dirname + " the file has a substring of "+ mname);
    }
}
