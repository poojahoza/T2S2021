package main.java.containers;

import main.java.utils.PreProcessor;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the Document information such as Term frequencies
 * Takes the Document as input and builds the statistics.
 */
public class DocStats {

    private String id = null;
    private String content = null;
    private Container container = null;
    private Map<String, Integer> termFreq = null;
    private Double score = 0.0;

    /**
     * Constructor to init only the container without having the document ID
     *
     * @param container
     */

    private DocStats(Container container) {
        termFreq = new LinkedHashMap<>();
        this.container = container;
        score = container.getScore();
    }

    /**
     * Constructor to save the Document ID.
     *
     * @param id
     * @param container
     */

    public DocStats(String id, Container container, String content) {
        this(container);
        this.id = id;
        this.content = content;
        this.createTermFreq();
    }

    /**
     * Uses the English Analyzer to save the term frequencies
     * Each term is tokenized and saved
     *
     * @param content
     * @return
     * @throws IOException
     */
    private String processTerm(String content) throws IOException {
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("Text", new StringReader(content));
        try {
            tokenStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String token = null;
        while (tokenStream.incrementToken()) {
            token = tokenStream.getAttribute(CharTermAttribute.class).toString();
        }
        return token;
    }

    /**
     * This function is called from the Constructor
     * This creates the Term frequency vector
     */
    private void createTermFreq() {

        ArrayList<String> processedContent = null;

        try {
            processedContent = PreProcessor.processTermsUsingLuceneStandard(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String term : processedContent) {
            String pterm = null;
            try {
                pterm = processTerm(term);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (termFreq.containsKey(pterm)) {
                termFreq.put(pterm, termFreq.get(pterm) + 1);
            } else {
                termFreq.put(pterm, 1);
            }
        }
    }

    /**
     * Returns the terms frequency of the term, else returns -1;
     *
     * @param term
     * @return
     */
    private Integer getTermFreq(String term) {
        String processedTerm = null;
        try {
            processedTerm = processTerm(term);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (termFreq.containsKey(processedTerm)) {
            return termFreq.get(processedTerm);
        }

        return -1;
    }

    public Double getScore() {
        return score;
    }

    public Double getProbabilityTerm(String term) {
        String processedTerm = null;
        try {
            processedTerm = processTerm(term);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!termFreq.containsKey(processedTerm)) {
            return 0.0;
        }
        return (termFreq.get(processedTerm)) / (double) termFreq.size();
    }
}
