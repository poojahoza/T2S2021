package main.java.indexer;

/*Lucene imports*/
import edu.unh.cs.TrecCarParagraph;
import edu.unh.cs.lucene.TrecCarLuceneConfig;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexOptions;

/*TREC tools imports*/
import edu.unh.cs.treccar_v2.Data;

/*Project specific imports*/
import main.java.utils.IndexUtils;

/*Java specific imports*/
import java.io.*;
import java.util.List;
import java.util.stream.StreamSupport;


public class IndexBuilder
{
    private final IndexWriter indexWriter;
    private static int increment=0;

    public IndexBuilder(String indexDir) throws IOException {
        indexWriter = IndexUtils.createIndexWriter(indexDir);
    }


    private void writePara(Data.Paragraph p,
                           IndexWriter i,
                           TrecCarLuceneConfig.LuceneIndexConfig cfg,
                           TrecCarParagraph trecCarParaRepr){
        final Document doc = trecCarParaRepr.paragraphToLuceneDoc(p);
        try {
            int incrementFactor=10000;
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

    /**
     * Create an index of the cbor file passed as a parameter.
     */

    public void performIndex(String cborLoc) throws IOException
    {
        Iterable<Data.Paragraph> para = IndexUtils.createParagraphIterator(cborLoc);
        TrecCarLuceneConfig.LuceneIndexConfig cfg = TrecCarLuceneConfig.paragraphConfig();
        TrecCarParagraph tcpr = cfg.getTrecCarParaRepr();
        StreamSupport.stream(para.spliterator(), true)
                .forEach(paragraph ->
                {
                    writePara(paragraph,indexWriter, cfg, tcpr);

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