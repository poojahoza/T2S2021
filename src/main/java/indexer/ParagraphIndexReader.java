package main.java.indexer;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author poojaoza
 **/
public class ParagraphIndexReader {

    private IndexReader indexReader;

    public ParagraphIndexReader(String indexLoc) throws IOException {
        Path indexPath = Paths.get(indexLoc);
        FSDirectory indexDir = FSDirectory.open(indexPath);
        IndexWriterConfig conf = new IndexWriterConfig(new EnglishAnalyzer());
        conf.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
        indexReader = DirectoryReader.open(new IndexWriter(indexDir, conf));
    }

    public String getContent(int i, String fieldname){
        String content;
        try {
            Document doc = indexReader.document(i);
            content = doc.getField(fieldname).stringValue();
        }
        catch (IOException ioe){
            content = ioe.getMessage();
            System.out.println(content);
        }
        return content;
    }
}
