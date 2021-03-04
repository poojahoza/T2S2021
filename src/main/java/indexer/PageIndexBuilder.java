package main.java.indexer;

/**
 * @author poojaoza
 **/

/*Lucene imports*/
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;


/*TREC tools imports*/
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.TrecCarPageRepr;
import edu.unh.cs.lucene.TrecCarLuceneConfig;

/*Project specific imports*/
import main.java.utils.IndexUtils;

/*Java specific imports*/
import java.io.*;
import java.util.List;
import java.util.stream.StreamSupport;


public class PageIndexBuilder
{
    private final IndexWriter indexWriter;
    private static int increment=0;

    public PageIndexBuilder(String indexDir) throws IOException {
        indexWriter = IndexUtils.createIndexWriter(indexDir);
    }


    private void writePage(Data.Page p,
                           IndexWriter i,
                           TrecCarLuceneConfig.LuceneIndexConfig cfg,
                           TrecCarPageRepr trecCarPageRepr) {

        final List<Document> docs = trecCarPageRepr.pageToLuceneDoc(p);
        try {
            int incrementFactor=2000;
            indexWriter.addDocuments(docs);
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

    /**
     * Create an index of the cbor file passed as a parameter.
     */


    public void performPageIndex(String cborLoc) throws IOException{
        Iterable<Data.Page> page = IndexUtils.createPageIterator(cborLoc);
        TrecCarLuceneConfig.LuceneIndexConfig cfg = TrecCarLuceneConfig.pageConfig();
        TrecCarPageRepr tcpr = cfg.getTrecCarPageRepr();
        StreamSupport.stream(page.spliterator(), true).
                forEach(pages -> {
                    writePage(pages, indexWriter, cfg, tcpr);
                });
        closeIndexWriter();
    }



    /**
     * Closes the indexwriter so that we can use it in searching.
     * @throws IOException
     */
    private void closeIndexWriter()
    {
        if (indexWriter != null)
        {
            try
            {
                indexWriter.close();
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
            }

        }
    }

}