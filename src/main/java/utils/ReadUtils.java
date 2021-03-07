package main.java.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReadUtils {

    public static Map<String, Map<String, Object>> readRunFile(String filepath){
        Map<String, Map<String, Object>> file_data = new LinkedHashMap<>();

        File fp = new File(filepath);
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

                if (file_data.containsKey(outKey)) {
                    Map<String, Object> extract = file_data.get(outKey);
                    String inner_key = words[2];
                    extract.put(inner_key, Arrays.asList(words[3], words[4]));
                } else {

                    String inner_key = words[2];
                    Map<String, Object> temp = new LinkedHashMap<>();
                    temp.put(inner_key, Arrays.asList(words[3], words[4]));
                    file_data.put(outKey, temp);
                }
            } catch (NullPointerException n) {
                System.out.println(n.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }
        return file_data;
    }
}
