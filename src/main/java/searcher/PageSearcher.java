package main.java.searcher;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

import java.util.LinkedHashMap;
import java.util.Map;

public class PageSearcher extends BaseSearcher {


    public PageSearcher(String indexLoc) throws IOException {

        super(indexLoc);
        searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(indexLoc))));
        parser = new QueryParser("Title", new EnglishAnalyzer());
   }

    Map<String, String> parseScoreDocs(ScoreDoc[] scoreDocs,
                                String entity_id,
                                Map<String, String> entity_outlinks) throws IOException
    {
        int ranking=1;
        for(ScoreDoc s:scoreDocs)
        {

            Document rankedDoc = searcher.doc(s.doc);
            String entityId = rankedDoc.getField("Id").stringValue();
            if(entity_id.equals(entityId)) {
                String outlinkIds = rankedDoc.getField("OutlinkIds").stringValue();

                entity_outlinks.put(entity_id, outlinkIds);
            }
            ranking++;
        }
        return entity_outlinks;
    }


    private Map<String, Map<String, String>> runRanking(Map<String, Map<String, String>> out)
    {
        Map<String, Map<String, String>> query_entity_pair = new LinkedHashMap<>();

        for(Map.Entry<String, Map<String, String>> m:out.entrySet())
        {
            Map<String, String> entity_outlinks = new LinkedHashMap<>();
            for(Map.Entry<String, String> n:m.getValue().entrySet()) {
                try {
                    TopDocs topDocuments = null;
                    try {
                        if(!n.getValue().equals("")) {
                            topDocuments = this.performSearch(n.getValue(), 100);
                        }
                    } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                        System.out.println("The query was not parsed");
                        //e.printStackTrace();
                    }
                    try {
                        ScoreDoc[] scoringDocuments = topDocuments.scoreDocs;
                        entity_outlinks = parseScoreDocs(scoringDocuments, n.getKey(), entity_outlinks);
                    }catch (NullPointerException npe){
                        System.out.println("No matching documents found "+n.getKey());
                        //npe.printStackTrace();
                    }
                } catch (IOException io) {
                    System.out.println(io.getMessage());
                }
            }

            query_entity_pair.put(m.getKey(), entity_outlinks);

        }
        return query_entity_pair;
    }

    public Map<String, Map<String, String>> getRanking(Map<String, Map<String, String>> out)
    {
        Map<String, Map<String, String>> ranked_entities = this.runRanking(out);
        return ranked_entities;
    }
}
