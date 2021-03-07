package main.java.searcher;

import main.java.containers.Container;
import main.java.containers.EntityContainer;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParagraphSearcher{

    private IndexSearcher searcher = null;
    private QueryParser parser = null;
    private Query queryObj = null;
    private Map<String, Map<String, Container>> ranks=null;

    public ParagraphSearcher(String indexLoc) throws IOException{
        searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(indexLoc))));
        parser = new QueryParser("Id", new EnglishAnalyzer());
    }

    public TopDocs performSearch(String paraid, int n)
            throws IOException, ParseException, NullPointerException {

        queryObj = parser.parse(QueryParser.escape(paraid));
        return searcher.search(queryObj, n);
    }

    protected void parseScoreDocs(ScoreDoc[] scoreDocs, String paraid, String rank, String score, String queryId)
            throws IOException {
        for(int ind=0; ind<scoreDocs.length; ind++){

            //Get the scoring document
            ScoreDoc scoringDoc = scoreDocs[ind];

            //Create the rank document from searcher
            Document rankedDoc = searcher.doc(scoringDoc.doc);

            //Print out the results from the rank document
            String rankedDocparaId = rankedDoc.getField("Id").stringValue();
            if(rankedDocparaId.equals(paraid)){
                String paraId = rankedDoc.getField("Id").stringValue();
                String entity = rankedDoc.getField("EntityLinks").stringValue();
                String entityId = rankedDoc.getField("OutlinkIds").stringValue();

                Container c = new Container(Double.parseDouble(score),scoringDoc.doc);
                c.addEntityContainer(new EntityContainer(entity, entityId));
                c.setRank(Integer.parseInt(rank));
                createRankingQueryDocPair(queryId, paraId,c);
                break;
            }
        }
    }

    private void createRankingQueryDocPair(String outer_key, String inner_key, Container rank)
    {
        if(ranks.containsKey(outer_key))
        {
            Map<String, Container> extract = ranks.get(outer_key);
            extract.put(inner_key, rank);
        }
        else
        {
            Map<String,Container> temp = new LinkedHashMap<>();
            temp.put(inner_key, rank);
            ranks.put(outer_key,temp);
        }
    }

    private void runRanking(String paraId, String queryId, String rank, String score)
    {
        try
        {
            TopDocs topDocuments = null;
            try {
                topDocuments = this.performSearch(paraId,25);
                } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                    e.printStackTrace();
                }
                ScoreDoc[] scoringDocuments = topDocuments.scoreDocs;
                this.parseScoreDocs(scoringDocuments, paraId, rank, score, queryId);
        }
                catch (IOException io)
        {
            System.out.println(io.getMessage());
        }
    }

    public Map<String, Map<String, Container>> getRanking(Map<String, Map<String, Object>> rankings)
    {
        if(ranks != null){
            ranks.clear();
        }
        for(Map.Entry<String, Map<String, Object>> q:rankings.entrySet()) {
            for(Map.Entry<String,Object> p:q.getValue().entrySet())
            {
                System.out.println(p.getValue().getClass().getName());
                List<String> para_details = (List<String>) p.getValue();
                this.runRanking(p.getKey(), q.getKey(), para_details.get(0), para_details.get(1));
            }
        }
        return ranks;
    }
}
