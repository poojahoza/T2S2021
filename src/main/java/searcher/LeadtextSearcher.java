package main.java.searcher;

/**
 * @author poojaoza
 **/

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.Map;

public class LeadtextSearcher extends PageSearcher{

    public LeadtextSearcher(String indexLoc) throws IOException
    {
        super(indexLoc);
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

                String outlinkIds = rankedDoc.getField("LeadText").stringValue();
                entity_outlinks.put(entity_id, outlinkIds);
            }
            ranking++;
        }
        return entity_outlinks;
    }

}
