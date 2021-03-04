package main.java.runner;

import main.java.commandparser.CommandParser;
import main.java.commandparser.RegisterCommands;
import main.java.commandparser.ValidateCommands;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import java.awt.*;
import java.io.*;
import java.util.*;


/**
 * Creates training and test data for ham and spam documents from the manual qrels file.
 */
public class IndexHamSpamRunner implements ProgramRunner {

    private RegisterCommands.IndexHamSpam indexHamSpamParser = null;
    private ValidateCommands.ValidateIndexHamSpamCommands validate = null;
    public final HashMap<String, HashMap<String, String>> classifyMap;
    public final HashMap<String, HashMap<String, String>> corpus;

    public IndexHamSpamRunner(CommandParser parser)
    {
        indexHamSpamParser = parser.getIndexHamSpamCommand();
        validate = new ValidateCommands.ValidateIndexHamSpamCommands(indexHamSpamParser);
        classifyMap = new HashMap<>();
        corpus = new HashMap<>();
    }


    @Override
    public void run() {

        parseQrels();

        /*HashMap<String, String> curMap = classifyMap.get("spam");
        for (String key : curMap.keySet()) {
            System.out.println(key);
        }*/

        try {
            buildTrainTestSets();
        } catch (IOException e) {
            e.printStackTrace();
        }
        write();
    }

    private void extractText(Data.Paragraph p) throws IOException {

        HashMap<String, String> spamMap = classifyMap.get("spam");
        HashMap<String, String> hamMap = classifyMap.get("ham");

         /*for (String key : spamMap.keySet()) {
            System.out.println(key);
        }*/
        //BufferedWriter writer = new BufferedWriter(new FileWriter("/home/rachel/Desktop/test", true));

        final String content = p.getTextOnly();
        final String paraId = p.getParaId();

        if (spamMap.get(paraId) != null) {
            //System.out.println("spam detected: " + paraId + '\t' + content);
            //writer.write("spam detected: " + paraId + '\t' + content);
            //writer.newLine();
            HashMap <String, String> curMap = corpus.get("spam");
            curMap.put(paraId, content);

        }
        else if (hamMap.get(paraId) != null) {
            //writer.write("ham detected: " + paraId + '\t' + content);
            //writer.newLine();
            HashMap <String, String> curMap = corpus.get("ham");
            curMap.put(paraId, content);
        }
    }


    /**
     * Desc: Iterate through the qrels file and map pids to ham/spam labels according to their annotations.
     * A pid of 2 indicates that a document is ham, whereas a pid of -1, -2, or -0 indicates that a
     * document is spam.
     *
     */
    private void parseQrels() {

        HashMap<String, String> spamMap = new HashMap<>();
        HashMap<String, String> hamMap = new HashMap<>();
        classifyMap.put("ham", hamMap);
        classifyMap.put("spam", spamMap);

        BufferedReader reader = null;
        File qrelsFile = new File(indexHamSpamParser.getQrelPath());
        String line = null;

        try {
            reader = new BufferedReader(new FileReader(qrelsFile));

            while ((line = reader.readLine()) != null) {

                String[] curLine = line.split("\\s+");
                HashMap<String, String> curMap;

                if (curLine[3].equals("-2")) {
                    curMap = classifyMap.get("spam");
                    curMap.put(curLine[2], curLine[0]);
                   // System.out.println("spam found: " + curLine[2] + '\t' + curLine[0]);
                }
                else if (curLine[3].equals("2")) {
                    curMap = classifyMap.get("ham");
                    curMap.put(curLine[2], curLine[0]);
                    //System.out.println("ham found: " + curLine[2] + '\t' + curLine[0]);
                }
            }

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

    }


    /**
     * Desc: Iterate through the Lucene index and store all ids that are also found in the qrels.
     *
     */
    private void buildTrainTestSets() throws IOException {

        HashMap<String, String> spamMap = classifyMap.get("spam");
        HashMap<String, String> hamMap = classifyMap.get("ham");


        /*for (String key : hamMap.keySet()) {
            System.out.println(key);
        }*/

        corpus.put("ham", new HashMap<>());
        corpus.put("spam", new HashMap<>());

        // Get the doc id and check if it is in the spam/ham map. If it is, then add it to the corpus.
        final FileInputStream fileInputStream2 = new FileInputStream(new File(indexHamSpamParser.getParagraphPath()));
        final Iterator<Data.Paragraph> paragraphIterator = DeserializeData.iterParagraphs(fileInputStream2);
        for (int i = 1; paragraphIterator.hasNext(); i++) {
            extractText(paragraphIterator.next());
        }
        System.out.println("Finished iterating");
    }


    /**
     * Desc: Create separate training and test sets.
     *
     */
    private void write() {

        HashMap<String, String> spamCorpus = corpus.get("spam");
        HashMap<String, String> hamCorpus = corpus.get("ham");

        // Divide the ham and spam maps in half.
        HashMap<String, String> spamTrain = new HashMap<>();
        HashMap<String, String> spamTest =  new HashMap<>();

        HashMap<String, String> hamTrain = new HashMap<>();
        HashMap<String, String> hamTest =  new HashMap<>();

        Random random = new Random(50);
        for (String key : spamCorpus.keySet()) {
            if (random.nextInt() > 25) {
                //System.out.println("spam test: " + key + '\t' + spamCorpus.get(key));
                spamTest.put(key, spamCorpus.get(key));
            }
            else {
                //System.out.println("spam train: " + key + '\t' + spamCorpus.get(key));
                spamTrain.put(key, spamCorpus.get(key));
            }
        }
        for (String key : hamCorpus.keySet()) {
            if (random.nextInt() > 25) {
                //System.out.println("ham test: " + key + '\t' +  hamCorpus.get(key));
                hamTest.put(key, hamCorpus.get(key));
            }
            else {
                //System.out.println("ham train: " + key + '\t' +  hamCorpus.get(key));
                hamTrain.put(key, hamCorpus.get(key));
            }
        }

        // Write out to separate files
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(indexHamSpamParser.getSpamTrainPath(), true));
            for (String key: spamTrain.keySet()) {
                writer.write(key + '\t' + spamTrain.get(key));
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(indexHamSpamParser.getHamTrainPath(), true));
            for (String key: hamTrain.keySet()) {
                writer.write(key + '\t' + hamTrain.get(key));
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Write out to a third file a combination of the remaining halves for testing.
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(indexHamSpamParser.getHamSpamTestPath(), true));
            for (String key: spamTest.keySet()) {
                writer.write(key + '\t' + spamTest.get(key));
                writer.newLine();
            }
            for (String key: hamTest.keySet()) {
                writer.write(key + '\t' + hamTest.get(key));
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



        // Write the test data sets into their own files too, so we can evaluate the performance of the classifiers on test data.
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(indexHamSpamParser.getSpamTestPath(), true));
            for (String key: spamTest.keySet()) {
                writer.write(key + '\t' + spamTest.get(key));
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(indexHamSpamParser.getHamTestPath(), true));
            for (String key: hamTest.keySet()) {
                writer.write(key + '\t' + hamTest.get(key));
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
