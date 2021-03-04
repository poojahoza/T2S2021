package main.java.predictors;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class was created to find extra spam training data from the paragraph corpus
 * and is not part of the main pipeline.
 */
public class ClassifyUtils {

    public final HashMap<String, String> pidMap;
    public final HashMap<String, String> dump;

    public ClassifyUtils()
    {
        pidMap = new HashMap<>();
        dump = new HashMap<>();
    }


    /**
     * Desc: Helper function to get the text from a paragraph.
     *
     * @param p a paragraph from the corpus.
     */
    private void extractText(Data.Paragraph p) {

        final String content = p.getTextOnly();
        final String paraId = p.getParaId();

        if (pidMap.get(paraId) != null) {
            System.out.println(paraId + '\t' + content);
            dump.put(paraId, content);
        }
    }


    /**
     * Desc: Iterate through the run file and build a HashMap of paragraph ids.
     */
    public void parseRunfile() {

        BufferedReader reader = null;
        File runFile = new File("/home/rachel/grad_courses/data_science/txt/heavy_water.txt");
        String line = null;

        try {
            reader = new BufferedReader(new FileReader(runFile));

            while ((line = reader.readLine()) != null) {

                String[] curLine = line.split("\\s+");
                if (curLine[2] != null) {
                    pidMap.put(curLine[2], "");
                    //System.out.println(curLine[2]);
                }
            }

        } catch (IOException | NullPointerException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

    }


    /**
     * Desc: Parse the paragraph corpus and get tokens of all documents whose pids were just recorded.
     */
    public void buildTrainTestSets() throws IOException {

        final FileInputStream fileInputStream2 = new FileInputStream(new File("/home/rachel/grad_courses/data_science/paragraphCorpus/dedup.articles-paragraphs.cbor"));
        final Iterator<Data.Paragraph> paragraphIterator = DeserializeData.iterParagraphs(fileInputStream2);
        for (int i = 1; paragraphIterator.hasNext(); i++) {
            extractText(paragraphIterator.next());
        }
        System.out.println("Finished iterating");
        write();
    }


    /**
     * Desc: Write the tokens saved in the previous step to a file for manual inspection.
     */
    private void write() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("/home/rachel/grad_courses/data_science/dump/dump_heavy_water.txt", true));
            for (String key: dump.keySet()) {
                writer.write(key + '\t' + dump.get(key));
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

