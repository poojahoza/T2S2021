package main.java.utils;

import main.java.containers.Container;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class RunWriter
{

     public static boolean checkDir(String dirname)
    {
        boolean isSuccess = false;
        File dir = new File(dirname);
        if (dir.exists())
        {
            isSuccess =true;
        }
        else
        {
            try {
                isSuccess = dir.mkdir();
            } catch (SecurityException se) {
                System.out.println(se.getMessage());
            }

        }
    return isSuccess;
    }

     public static FileWriter createFile(String dirname,String mname) {

        String dest = dirname + System.getProperty("file.separator") + "output_" + mname + "_ranking.txt";
        FileWriter ptr = null;
        if (dest != null) {
            File e = new File(dest);
            if (e.exists()) {
                e.delete();
            }


            try {
                ptr = new FileWriter(dest);
            } catch (IOException ee) {
                ee.printStackTrace();
            }
        }
        return ptr;
    }


    public static void writeRunFile(String mname, Map<String, Map<String, Container>> res){

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
                    String score = String.valueOf(inner.getValue().getScore());
                    runFileLine = queryID+" Q0 "+pID+" "+ranking+" "+score+" "+mname+newLine;
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
}

