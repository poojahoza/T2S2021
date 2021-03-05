package main.java.searcher;

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.containers.EntityContainer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/*
This class extends the BaseSearcher and this is used to retrieve the initial set candidate generation using BM25.
What it gives is

            <QID ,  PARA_ID,    Container>> //Container holds all the information for the PARA.
private Map<String, Map<String, Container>> ranks=null;

                                   <QID,   Query>
getRanking ==> Will take in the MAP<String,String> as input and returns the above mentioned map as return value
*/
public class BaseBM25 extends BaseSearcher
{

    private Map<String, Map<String, Container>> ranks=null;
    private int k;

    public BaseBM25(int k,String indexLoc) throws IOException
    {
        super(indexLoc);
        if(ranks==null) this.ranks= new LinkedHashMap<String, Map<String, Container>>();
        this.k=k;
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

    private void parseScoreDocs(ScoreDoc[] scoreDocs, String queryId) throws IOException
    {

        int para_rank=1;

        for(ScoreDoc s:scoreDocs)
        {
            Document rankedDoc = searcher.doc(s.doc);
            String paraId = rankedDoc.getField("Id").stringValue();
            String entity = rankedDoc.getField("EntityLinks").stringValue();
            String entityId = rankedDoc.getField("OutlinkIds").stringValue();

            //Container that holds all the information
            Container c = new Container((double) s.score,s.doc);
            c.addEntityContainer(new EntityContainer(entity, entityId));
            c.setRank(para_rank);
            createRankingQueryDocPair(queryId, paraId,c);
            para_rank++;

        }
    }


    private void runRanking(Map<String,String> out)
    {
        for(Map.Entry<String,String> m:out.entrySet())
        {
            try
            {
                TopDocs topDocuments = null;
                try {
                    topDocuments = this.performSearch(m.getValue(),this.k);
                } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                    e.printStackTrace();
                }
                ScoreDoc[] scoringDocuments = topDocuments.scoreDocs;
                this.parseScoreDocs(scoringDocuments, m.getKey());
            }
            catch (IOException io)
            {
                System.out.println(io.getMessage());
            }

        }
    }

    public Map<String, Map<String, Container>> getRanking(Map<String,String> out)
    {
        if(ranks == null)
        {
            this.runRanking(out);
        }
        else {
            ranks.clear();
            this.runRanking(out);
        }
        return ranks;
    }

    /*
      On fly document retrieval, given the document ID
    */

    public String getDocument(int docID)
    {
        String docString=null;
        try
        {
            Document rankedDoc = searcher.doc(docID);
            docString = rankedDoc.getField("Text").stringValue();
        }
        catch (IOException io)
        {
            System.out.println(io.getMessage());
        }
        return docString;
    }

    public Map<String, Container> getRanking(String query)
    {
         Map<String,Container> temp;

        TopDocs topDocuments = null;
        try {
            topDocuments = this.performSearch(query ,this.k);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ScoreDoc[] scoringDocuments = topDocuments.scoreDocs;

        temp = new LinkedHashMap<>();

        int para_rank=1;


        for(ScoreDoc s:scoringDocuments)
            {
                Document rankedDoc = null;
                try {
                    rankedDoc = searcher.doc(s.doc);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String paraId = rankedDoc.getField("Id").stringValue();
                String entity = rankedDoc.getField("EntityLinks").stringValue();
                String entityId = rankedDoc.getField("OutlinkIds").stringValue();

                //Container that holds all the information
                Container c = new Container((double) s.score,s.doc);
                c.addEntityContainer(new EntityContainer(entity, entityId));
                c.setRank(para_rank);
                temp.put(paraId,c);
                para_rank++;
            }
       return temp;
    }

    public Map<String, Container> getRankingExactNumber(String query)
    {
        // return the same number as k input
        Map<String,Container> temp;
        Map<String,Container> tempSpam = new LinkedHashMap<>();
        int countOfSpam = 0;


        TopDocs topDocuments = null;
        do  {

            try {
                topDocuments = this.performSearch(query, this.k + tempSpam.size());
                countOfSpam = 0;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            ScoreDoc[] scoringDocuments = topDocuments.scoreDocs;

            temp = new LinkedHashMap<>();
            int para_rank = 1;

            for (ScoreDoc s : scoringDocuments) {
                Document rankedDoc = null;
                try {
                    rankedDoc = searcher.doc(s.doc);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String paraId = rankedDoc.getField("Id").stringValue();
                if (tempSpam.containsKey(paraId)){
                    continue;
                }
                String entity = rankedDoc.getField("EntityLinks").stringValue();
                String entityId = rankedDoc.getField("OutlinkIds").stringValue();
                String text = rankedDoc.getField("Text").stringValue();

                //Container that holds all the information
                Container c = new Container((double) s.score, s.doc);

                c.addEntityContainer(new EntityContainer(entity, entityId));
                c.setRank(para_rank);
                temp.put(paraId, c);
                para_rank++;
            }
        } while (countOfSpam > 0);
        return temp;
    }


    public Map<String, Container> getRankingApplyingSpam(String query)
    {
        // return the ham of the first pull it may be less than k
        Map<String,Container> temp;

        TopDocs topDocuments = null;

        try {
            topDocuments = this.performSearch(query, this.k);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ScoreDoc[] scoringDocuments = topDocuments.scoreDocs;

        temp = new LinkedHashMap<>();
        int para_rank=1;

        for (ScoreDoc s : scoringDocuments) {
            Document rankedDoc = null;
            try {
                rankedDoc = searcher.doc(s.doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String paraId = rankedDoc.getField("Id").stringValue();
            String entity = rankedDoc.getField("EntityLinks").stringValue();
            String entityId = rankedDoc.getField("OutlinkIds").stringValue();
            String text = rankedDoc.getField("Text").stringValue();

            //Container that holds all the information
            Container c = new Container((double) s.score, s.doc);

            c.addEntityContainer(new EntityContainer(entity, entityId));
            c.setRank(para_rank);
            temp.put(paraId, c);
            para_rank++;
        }
        return temp;
    }
}
