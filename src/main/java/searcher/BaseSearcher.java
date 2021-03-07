package main.java.searcher;

import edu.unh.cs.lucene.TrecCarLuceneConfig;
import main.java.utils.SearchUtils;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.nio.file.Paths;
import main.java.utils.Constants;
import java.util.Map;

import java.nio.file.Path;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class BaseSearcher {

    protected IndexSearcher searcher = null;
    protected QueryParser parser = null;
    protected Query queryObj = null;


    public BaseSearcher(String indexLocation) throws IOException
    {
        searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation))));
        parser = new QueryParser("Text", new EnglishAnalyzer());
        //parser = new QueryParser("text", new EnglishAnalyzer());
    }

    /**
     *
     * @param queryString
     * @param n
     * @return Top documents for this search
     * @throws IOException
     * @throws ParseException
     */
    public TopDocs performSearch(String queryString, int n)
            throws IOException, ParseException, NullPointerException {

        queryObj = parser.parse(QueryParser.escape(queryString));
        return searcher.search(queryObj, n);
    }



    protected List<String> getRankings(ScoreDoc[] scoreDocs, String queryId)
            throws IOException {

        List<String> rankings = new ArrayList<String>();
        for(int ind=0; ind<scoreDocs.length; ind++){

            //Get the scoring document
            ScoreDoc scoringDoc = scoreDocs[ind];

            //Create the rank document from searcher
            Document rankedDoc = searcher.doc(scoringDoc.doc);

            //Print out the results from the rank document

            String docScore = String.valueOf(scoringDoc.score);
            String paraId = rankedDoc.getField("Id").stringValue();
            String paraRank = String.valueOf(ind+1);
            rankings.add(queryId + " Q0 " + paraId + " " + paraRank + " " + docScore + " "+"team1" + "-" + "BM25");
        }
        return rankings;
    }

    public void writeRankings(Map<String,String> p, String output_file_name)
    {
        Path file = Paths.get(output_file_name);

        try {
            if(output_file_name != null){
                File e = new File(output_file_name);
                if (e.exists()) {
                    e.delete();
                }
                Files.createFile(file);
            }
            else{
                System.out.println("Output file name is null. Please check");
                System.exit(1);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        for(Map.Entry<String,String> m:p.entrySet())
        {
            try {
                TopDocs searchDocs = this.performSearch(m.getValue(), 100);
                ScoreDoc[] scoringDocuments = searchDocs.scoreDocs;
                List<String> formattedRankings = this.getRankings(scoringDocuments, m.getKey());
                Files.write(file, formattedRankings, Charset.forName("UTF-8"), StandardOpenOption.APPEND);

            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }

    /**
     * Function: query
     * Desc: Queries Lucene paragraph corpus using a standard similarity function.
     *       Note that this uses the StandardAnalyzer.
     * @param queryString: The query string that will be turned into a boolean query.
     * @param nResults: How many search results should be returned
     * @return TopDocs (ranked results matching query)
     */
    public TopDocs query(String queryString, Integer nResults) {
        Query q = SearchUtils.createStandardBooleanQuery(queryString, "text");
        try {
            return searcher.search(q, nResults);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}