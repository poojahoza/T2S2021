package main.java.searcher;

/**
 * @author poojaoza
 **/

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class EntityAbstractSearcher {

    private IndexSearcher searcher = null;
    private QueryParser parser = null;
    private Query queryObj = null;


    public EntityAbstractSearcher(String indexLoc)
    {
        try {
            searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(indexLoc))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        parser = new QueryParser("Title", new EnglishAnalyzer());
    }

    public TopDocs performSearch(String queryString, int n)
            throws IOException, ParseException, NullPointerException {

        queryObj = parser.parse(QueryParser.escape(queryString));
        return searcher.search(queryObj, n);
    }

     public String parseScoreDocs(ScoreDoc[] scoreDocs,String entity_id) throws IOException
    {
        String text =null;
        for(ScoreDoc s:scoreDocs)
        {

            Document rankedDoc = searcher.doc(s.doc);
            String entityId = rankedDoc.getField("Id").stringValue();
            if(entity_id.equals(entityId)) {
                 text = rankedDoc.getField("LeadText").stringValue();
            }
        }
        return text;
    }



    public String getDocs(String entityMention) {
        try {
            TopDocs topDocuments = null;
            try {
                if(!entityMention.equals("")) {
                    topDocuments = this.performSearch(entityMention,100);
                }
            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                System.out.println("The query was not parsed");

            }
            try {
                ScoreDoc[] scoringDocuments = topDocuments.scoreDocs;
                return parseScoreDocs(scoringDocuments, entityMention);
            }catch (NullPointerException npe){
                System.out.println("No matching documents found " + entityMention);
            }
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }

        return null;
    }


    public String getAbstract(String entityMention)
    {
        return getDocs(entityMention);
    }

}
