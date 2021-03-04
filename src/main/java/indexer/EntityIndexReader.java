package main.java.indexer;

/**
 * @author poojaoza
 **/

import com.mongodb.MongoClient;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.mongodb.BasicDBObject;

import main.java.utils.DBSettings;

public class EntityIndexReader {

    private IndexReader indexReader;

    public EntityIndexReader(String indexLoc) throws IOException{
        Path indexPath = Paths.get(indexLoc);
        FSDirectory indexDir = FSDirectory.open(indexPath);
        IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
        conf.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
        indexReader = DirectoryReader.open(new IndexWriter(indexDir, conf));

        if(DBSettings.mongoClient == null){
            DBSettings.mongoClient = new MongoClient(DBSettings.HOST_NAME, DBSettings.PORT);
            DBSettings.mongoDB = DBSettings.mongoClient.getDB("test");
            DBSettings.mongoCollection = DBSettings.mongoDB.getCollection("entityIndex");

        }
    }

    public void totalIndexDocs(){
        System.out.println(indexReader.numDocs());
        System.out.println(indexReader.maxDoc());

        for(int i = 0; i < indexReader.maxDoc(); i++){
            try {
                Document doc = indexReader.document(i);
                System.out.println(doc.getField("LeadText").stringValue());
                System.out.println(doc.getField("OutlinkIds").stringValue());

                BasicDBObject mongoDocument = new BasicDBObject();

                mongoDocument.put("Id" , doc.getField("Id").stringValue());
                mongoDocument.put("LeadText" , doc.getField("LeadText").stringValue());
                mongoDocument.put("OutlinkIds" , doc.getField("OutlinkIds").stringValue());
                mongoDocument.put("InlinkIds" , doc.getField("InlinkIds").stringValue());
                mongoDocument.put("AnchorNames" , doc.getField("AnchorNames").stringValue());
                mongoDocument.put("Title" , doc.getField("Title").stringValue());
                mongoDocument.put("CategoryNames" , doc.getField("CategoryNames").stringValue());
                mongoDocument.put("DisambiguationNames" , doc.getField("DisambiguationNames").stringValue());

                DBSettings.mongoCollection.insert(mongoDocument);
            }catch (IOException ioe){
                System.out.println("document not present : " + String.valueOf(i));
            }
        }
        DBSettings.mongoCollection.createIndex(new BasicDBObject("Id", 1));
    }

}
