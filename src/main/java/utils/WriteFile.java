package main.java.utils;

/**
 * @author poojaoza
 **/

import main.java.containers.Container;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class WriteFile {

    private void checkFileExistence(String output_file_name)
    {
        File e = new File(output_file_name);
        System.out.println(output_file_name);
        if(e.exists())
        {
            e.delete();
            //System.out.println(output_file_name+" deleted");
        }
    }

    public <T> void generateEntityRunFile(Map<String, Map<String, T>> results, String methodname)
    {
        String output_file = "output_ranking_"+methodname+".txt";
        List<String> rankings = new ArrayList<String>();
        String result_dir = "result";
        File directory = new File(result_dir);
        if (! directory.exists()){
            directory.mkdir();
        }
        Path file = Paths.get(System.getProperty("user.dir")+"/"+result_dir, output_file);
        checkFileExistence(System.getProperty("user.dir")+"/"+result_dir+"/"+output_file);
        try {
            Files.createFile(file);
        }catch(IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
        for(Map.Entry<String, Map<String, T>> m: results.entrySet())
        {
            int rank = 0;
            rankings.clear();
            for(Map.Entry<String, T> n: m.getValue().entrySet())
            {
                rank += 1;
                rankings.add(m.getKey() + " Q0 " + n.getKey() +" "+String.valueOf(rank)+" "+n.getValue().toString()+ " team1 "+methodname);
            }
            try {
                Files.write(file, rankings, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            }
            catch (IOException io)
            {
                System.out.println("Error writing in file");
            }
        }
    }

    public void generateBM25RunFile(Map<String, Map<String, Container>> results, String methodname)
    {
        String output_file = "output_ranking_"+methodname+".txt";
        List<String> rankings = new ArrayList<String>();
        String result_dir = "result";
        File directory = new File(result_dir);
        if (! directory.exists()){
            directory.mkdir();
        }
        Path file = Paths.get(System.getProperty("user.dir")+"/"+result_dir, output_file);
        checkFileExistence(System.getProperty("user.dir")+"/"+result_dir+"/"+output_file);
        try {
            Files.createFile(file);
        }catch(IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
        for(Map.Entry<String, Map<String, Container>> m: results.entrySet())
        {
            int rank = 0;
            rankings.clear();
            for(Map.Entry<String, Container> n: m.getValue().entrySet())
            {
                rank += 1;
                rankings.add(m.getKey() + " Q0 " + n.getKey() +" "+String.valueOf(rank)+" "+String.valueOf(n.getValue().getScore())+ " team1 "+methodname);
            }
            try {
                Files.write(file, rankings, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            }
            catch (IOException io)
            {
                System.out.println("Error writing in file");
            }
        }
    }

    public void generateFeatureVectorRunFile(Map<String, Map<String, Double[]>> results, String methodname)
    {
        String output_file = "output_ranking_"+methodname+".txt";
        List<String> rankings = new ArrayList<String>();
        NumberFormat formatter = new DecimalFormat("#0.000000000000");
        String result_dir = "result";
        File directory = new File(result_dir);
        if (! directory.exists()){
            directory.mkdir();
        }
        Path file = Paths.get(System.getProperty("user.dir")+"/"+result_dir, output_file);
        checkFileExistence(System.getProperty("user.dir")+"/"+result_dir+"/"+output_file);
        try {
            Files.createFile(file);
        }catch(IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
        for(Map.Entry<String, Map<String, Double[]>> m: results.entrySet())
        {
            int rank = 1;
            rankings.clear();
            for(Map.Entry<String, Double[]> n: m.getValue().entrySet())
            {
                //rank += 1;
                StringBuilder builder = new StringBuilder();
                builder.append(m.getKey()+" ");
                builder.append(n.getKey()+" ");
                for(Double d: n.getValue()){
                    builder.append(" ");
                    builder.append(formatter.format(d)); //d.toString()
                }
                String final_data = builder.toString();
                rankings.add(final_data);

            }
            try {
                Files.write(file, rankings, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            }
            catch (IOException io)
            {
                System.out.println("Error writing in file");
            }
            rank++;
        }
    }


    public void generateEntityRankLibRunFile(Map<String, Map<String, Double[]>> results, String qrelfilepath, String methodname)
    {
        String output_file = "output_ranking_"+methodname+".txt";
        List<String> rankings = new ArrayList<String>();
        String result_dir = "result";
        File directory = new File(result_dir);
        if (! directory.exists()){
            directory.mkdir();
        }
        Path file = Paths.get(System.getProperty("user.dir")+"/"+result_dir, output_file);
        Entities entities = new Entities();
        Map<String, Map<String, String>> mp = entities.readEntityQrelFile(qrelfilepath);
        checkFileExistence(System.getProperty("user.dir")+"/"+result_dir+"/"+output_file);
        try {
            Files.createFile(file);
        }catch(IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
        for(Map.Entry<String, Map<String, Double[]>> m: results.entrySet())
        {
            int rank = 1;
            rankings.clear();
            Map<String, String> qrel_entities = mp.get(m.getKey());
            if(qrel_entities == null){
                continue;
            }
            for(Map.Entry<String, Double[]> n: m.getValue().entrySet())
            {
                String relevancy = "0";
                /*
                * Handle this patch at the time of feature vectors generation
                * */
                if(n.getKey().equals("")){
                    continue;
                }
                if(qrel_entities.containsKey(n.getKey())){
                    relevancy = "1";
                }
                StringBuilder builder = new StringBuilder();
                builder.append(relevancy);
                builder.append(" ");
                builder.append("qid:"+rank);
                int query = 0;
                for(Double d: n.getValue()){
                    query++;
                    builder.append(" ");
                    builder.append(query+":"+d.toString());
                }
                builder.append(" ");
                builder.append("#"+m.getKey()+"_"+n.getKey());
                String final_data = builder.toString();
                rankings.add(final_data);

            }
            try {
                Files.write(file, rankings, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            }
            catch (IOException io)
            {
                System.out.println("Error writing in file");
            }
            rank++;
        }
    }

    public void generateFile(Map<String,String> results, String methodname)
    {
        String output_file = "output_ranking_"+methodname+".txt";
        List<String> rankings = new ArrayList<String>();
        String result_dir = "result";
        Path file = Paths.get(System.getProperty("user.dir")+"/"+result_dir, output_file);
        checkFileExistence(output_file);
        try {
            Files.createFile(file);
        }catch(IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
        rankings.clear();
        int index = 0;
        for(Map.Entry<String,String> m: results.entrySet())
        {

            index ++ ;
            rankings.add( index + "\t" + m.getKey() + "\tCount\t" + m.getValue() + "\tMethodName:\t" + methodname);

        }
        try {
            Files.write(file, rankings, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        }
        catch (IOException io)
        {
            System.out.println("Error writing in file");
        }
    }


}
