package main.java.utils;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class PreProcessor
{
    private final  List<String> STOP_WORDS = StopWord.getStopWords();

    List<String> getSTOP_WORDS()
    {
        return STOP_WORDS;
    }

    @Deprecated
    private static String[] processQuery(String query)
    {
        return query.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
    }

    public static ArrayList<String> processTermsUsingLucene(String content) throws IOException
    {
        PreProcessor p = new PreProcessor();

        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        ArrayList<String> data = new ArrayList<>();
            TokenStream tokenStream = analyzer.tokenStream("Text", new StringReader(content));
        try {
            tokenStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (tokenStream.incrementToken()) {
            final String token = tokenStream.getAttribute(CharTermAttribute.class).toString();

            if(!p.STOP_WORDS.contains(token)) {
                if (!data.contains(token)) {
                    data.add(token);
                }
            }
        }
        return data;
    }

    public static ArrayList<String> processTermsUsingLuceneStandard(String content) throws IOException
    {
        PreProcessor p = new PreProcessor();

        StandardAnalyzer analyzer = new StandardAnalyzer();
        ArrayList<String> data = new ArrayList<>();
        TokenStream tokenStream = analyzer.tokenStream("Text", new StringReader(content));
        try {
            tokenStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (tokenStream.incrementToken()) {
            final String token = tokenStream.getAttribute(CharTermAttribute.class).toString();

            if(!p.STOP_WORDS.contains(token)) {
                if (!data.contains(token)) {
                    data.add(token);
                }
            }
        }
        return data;
    }


    @Deprecated
    public static ArrayList<String> processDocument(String sb)
    {
        PreProcessor p = new PreProcessor();

        String[] data = processQuery(sb);
        ArrayList<String> processedData= new ArrayList<>();
        for(String s:data)
        {
            if(!p.STOP_WORDS.contains(s))
            {
                if(!processedData.contains(s))
                    processedData.add(s);
            }
        }
        return processedData;
    }

    @Deprecated
    public static ArrayList<String> processDocumentWithStemming(String sb)
    {
        PreProcessor p = new PreProcessor();
        String[] data = processQuery(sb);
        ArrayList<String> processedData= new ArrayList<>();
        for(String s:data)
        {
            if(!p.STOP_WORDS.contains(s))
            {
                if(!processedData.contains(s))
                    processedData.add(stemTerm(s));
            }
        }
        return processedData;
    }

    public static String stemTerm(String term)
    {
        PorterStemmer stem= new PorterStemmer();
        stem.setCurrent(term);
        stem.stem();
        return stem.getCurrent();
    }

}
