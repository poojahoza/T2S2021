package main.java.indexer;

/**
 * @author poojaoza
 **/

/*Lucene imports*/
import edu.unh.cs.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexOptions;

/*TREC tools imports*/
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.lucene.TrecCarLuceneConfig;

/*Project specific imports*/
import main.java.utils.IndexUtils;

/*Java specific imports*/
import java.io.*;
import java.util.List;
import java.util.stream.StreamSupport;


public class EntityIndexBuilder
{
    private final IndexWriter indexWriter;
    private static int increment=0;

    public EntityIndexBuilder(String indexDir) throws IOException {
        indexWriter = IndexUtils.createIndexWriter(indexDir);
    }


    private void writePage(Data.Page p,
                           IndexWriter i,
                           TrecCarLuceneConfig.LuceneIndexConfig cfg,
                           TrecCarEntity tce) {


        final List<Document> docs = tce.pageToLuceneDoc(p);
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
        TrecCarLuceneConfig.LuceneIndexConfig cfg = TrecCarLuceneConfig.entityConfig();
        TrecCarPageRepr tcpr = cfg.getTrecCarPageRepr();
        TrecCarEntity tce = new TrecCarEntity();
        StreamSupport.stream(page.spliterator(), true).
                forEach(pages -> {
                    System.out.println(pages.getPageName());
                    writePage(pages, indexWriter, cfg, tce);
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