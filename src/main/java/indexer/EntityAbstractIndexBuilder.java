package main.java.indexer;

import main.java.utils.IndexUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EntityAbstractIndexBuilder {

    private IndexWriter indexWriter;
    private static int increment=0;

    public EntityAbstractIndexBuilder(String indexDir) throws IOException {
        indexWriter = IndexUtils.createIndexWriter(indexDir);
    }

    private void writeAbstract(String entityId,
                               String entityAbstract){
        try {

            int incrementFactor=10000;

            Document doc = new Document();
            //Add the entity id and entity abstract for searching
            doc.add(new StringField("Id", entityId, Field.Store.YES));
            doc.add(new TextField("Abstract", entityAbstract, Field.Store.YES));

            indexWriter.addDocument(doc);
            increment++;

            //commit the Data after incrementFactorVariable paragraph

            if(increment % incrementFactor ==0)
            {
                indexWriter.commit();
            }
        }catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }

    }

    public void performAbstractIndex(String entityIndexLoc) throws IOException{
        Path indexPath = Paths.get(entityIndexLoc);
        FSDirectory indexReaderDir = FSDirectory.open(indexPath);
        IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
        conf.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
        IndexReader indexReader = DirectoryReader.open(new IndexWriter(indexReaderDir, conf));

        for(int i = 0; i < indexReader.maxDoc(); i++) {
            Document doc = indexReader.document(i);
            writeAbstract(doc.getField("Id").stringValue(), doc.getField("LeadText").stringValue());
        }

    }
}
