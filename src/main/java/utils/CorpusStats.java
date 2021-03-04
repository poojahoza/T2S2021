package main.java.utils;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CorpusStats
{
    private IndexReader indexReader  = null;
    private QueryParser parser = null;
    private Query queryObj = null;
    private  PorterStemmer stem=null;

    public CorpusStats(String indexLocation)
    {
        indexReader = getIndexReader(indexLocation);
        parser = new QueryParser("Text", new EnglishAnalyzer());
        stem = new PorterStemmer();
    }

    /*
        Creates the Index reader
    */
    private IndexReader getIndexReader(String indexLocation)
    {
        IndexReader index =null;
        try
        {
            index = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
        }catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
        return index;
    }

    /*
    Helper class to get the Corpus statistics
     */
    private BasicStats getBasicStats(Term myTerm) throws IOException
    {
        String fieldName = myTerm.field();

        CollectionStatistics collectionStats = new CollectionStatistics(
                "Text",
                indexReader.maxDoc(),
                indexReader.getDocCount(fieldName),
                indexReader.getSumTotalTermFreq(fieldName),
                indexReader.getSumDocFreq(fieldName)
        );

        TermStatistics termStats = new TermStatistics(
                myTerm.bytes(),
                indexReader.docFreq(myTerm),
                indexReader.totalTermFreq(myTerm)
        );

        BasicStats myStats = new BasicStats(fieldName, 1);
        assert collectionStats.sumTotalTermFreq() == -1 || collectionStats.sumTotalTermFreq() >= termStats.totalTermFreq();
        long numberOfDocuments = collectionStats.maxDoc();

        long docFreq = termStats.docFreq();
        long totalTermFreq = termStats.totalTermFreq();

        if (totalTermFreq == -1) {
            totalTermFreq = docFreq;
        }

        final long numberOfFieldTokens;
        final float avgFieldLength;

        long sumTotalTermFreq = collectionStats.sumTotalTermFreq();

        if (sumTotalTermFreq <= 0) {
            numberOfFieldTokens = docFreq;
            avgFieldLength = 1;
        } else {
            numberOfFieldTokens = sumTotalTermFreq;
            avgFieldLength = (float)numberOfFieldTokens / numberOfDocuments;
        }

        myStats.setNumberOfDocuments(numberOfDocuments);
        myStats.setNumberOfFieldTokens(numberOfFieldTokens);
        myStats.setAvgFieldLength(avgFieldLength);
        myStats.setDocFreq(docFreq);
        myStats.setTotalTermFreq(totalTermFreq);
        return myStats;
    }

    /*
    Get the document frequency for each word passed
    */
    public long getDF(String s) throws  IOException
    {
        Term t= new Term("Text",s);
        BasicStats b = getBasicStats(t);
        long d = 0;

        if(b.getDocFreq()!=0)
        {
            d = b.getDocFreq();
            return d;
        }
        return d;
    }

     /*
       Not fully implemented, Do not use this.
     */

    private String analyzeWord(String word)
    {
        QueryParser parser = new QueryParser("Text", new EnglishAnalyzer());
        String str =null;

        try {
            queryObj =  parser.parse(word);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return queryObj.toString();
    }

    /*
       perform the porter stem
    */
    private String stemTerm(String term) {
        stem.setCurrent(term);
        stem.stem();
        return stem.getCurrent();
    }

    /*
        Gets the Terms IDF value
    */
    public double getIDF(String s) throws  IOException
    {
        Term t= new Term("Text",s);
        BasicStats b = getBasicStats(t);
        double d = 0.0;
        try
        {
            d = Math.log(b.getNumberOfDocuments()/b.getDocFreq());
        } catch (ArithmeticException e)
        {
            return 0.0;
        }
        return d;
    }

    /*
        Returns the sorted list of IDFs, Highest to lowest
    */

    public ArrayList<String> getIDF(ArrayList<String> strList) throws  IOException
    {
        Map<String,Double> unsorted = new LinkedHashMap<String,Double>();
        for(String s:strList)
        {
            Double val = getIDF(s);
            unsorted.put(s,val);
        }
        ArrayList<String> sortedList = new ArrayList<>();
        for(Map.Entry<String,Double> un:SortUtils.sortByValue(unsorted).entrySet())
        {
                    sortedList.add(un.getKey());
        }
        return sortedList;
    }
    /*
    Over loaded function of
    */
    public ArrayList<String> getDF(ArrayList<String> strList) throws  IOException
    {
        Map<String,Long> unsorted = new LinkedHashMap<String,Long>();
        for(String s:strList)
        {
            long val = getDF(s);
            unsorted.put(s,val);
        }
        ArrayList<String> sortedList = new ArrayList<>();
        for(Map.Entry<String,Long> un:SortUtils.sortByValue(unsorted).entrySet())
        {
            sortedList.add(un.getKey());
        }
        return sortedList;
    }

    private String processedTerm(String content) throws IOException {
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

    public ArrayList<String> getDFStandardAnalyzer(ArrayList<String> strList) throws  IOException
    {
        Map<String,Long> unsorted = new LinkedHashMap<String,Long>();
        for(String s:strList)
        {
            long val = getDF(processedTerm(s));
            unsorted.put(s,val);
        }
        ArrayList<String> sortedList = new ArrayList<>();
        for(Map.Entry<String,Long> un:SortUtils.sortByValue(unsorted).entrySet())
        {
            sortedList.add(un.getKey());
        }
        return sortedList;
    }

    public ArrayList<String> getIDFStandardAnalyzer(ArrayList<String> strList) throws  IOException
    {
        Map<String,Double> unsorted = new LinkedHashMap<String,Double>();
        for(String s:strList)
        {
            Double val = getIDF(processedTerm(s));
            if(!unsorted.containsKey(processedTerm(s)))
            {
                unsorted.put(s,val);
            }
        }
        ArrayList<String> sortedList = new ArrayList<>();
        for(Map.Entry<String,Double> un:SortUtils.sortByValue(unsorted).entrySet())
        {
            sortedList.add(un.getKey());
        }
        return sortedList;
    }


}
