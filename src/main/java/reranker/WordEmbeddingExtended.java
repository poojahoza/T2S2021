package main.java.reranker;

import main.java.rerankerv2.concepts.EmbeddingStrategy;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/*
    This class implements the standard Analyzer
*/
public class WordEmbeddingExtended extends EmbeddingStrategy
{
    private Map<String, INDArray> word = null;
    private Integer dimension=0;
    private String embeddingFile=null;


    public WordEmbeddingExtended(Integer dimension,String embeddingFile)
    {
        this.dimension =dimension;
        this.embeddingFile=embeddingFile;
        word = readWordVectors();
    }

    private String processedTerm(String content) throws IOException
    {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("Text", new StringReader(content));
        try {
            tokenStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String token=null;
        while (tokenStream.incrementToken()) {
            token = tokenStream.getAttribute(CharTermAttribute.class).toString();
        }
        return token;
    }

    private Map<String, INDArray> readWordVectors()
    {
        if(word==null)
        {
            word = new LinkedHashMap<String,INDArray>();
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(embeddingFile)));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }


        String line=null;
        try {
            while ((line = br.readLine()) != null) {
                String[] vec = line.split(" ");

                //This avoids reading unnecessary line
                if(vec.length < (dimension+1))
                {
                    continue;
                }

                //Allocate the memory to convert the read value in to IND array.
                float[] temp=new float[dimension];
                for(int i=0;i < dimension ;i++)
                {
                    temp[i] = Float.parseFloat(vec[i+1]);
                }

                String term = processedTerm(vec[0]);
                if(term!=null) word.put(processedTerm(vec[0]), Nd4j.create(temp));
            }
        } catch (NullPointerException | IOException n) {
            System.out.println(n.getMessage());
        }

        return word;
    }


    public void DisplayWordEmbeddings(int countval)
    {
        int count=0;
        for(Map.Entry<String,INDArray> outer:word.entrySet())
        {
            count++;
            System.out.print(outer.getKey() +" ");
            for(int i=0;i< dimension;i++)
            {
                INDArray ind = outer.getValue();
                System.out.print(ind.getFloat(i));
                System.out.print(" ");
            }
            System.out.println(" ");
            if(count == countval ) break;
        }
    }


    @Override
    public INDArray getEmbeddingVector(String token) {
        if(word.containsKey(token.toLowerCase()))
        {
            return word.get(token);
        }
        return null;
    }

    /*
        This shoulmd return the score between two string
    */
    public Double getScore(String str1,String str2)
    {
        if(word.containsKey(str1) && word.containsKey(str2))
        {
            return Transforms.cosineSim(word.get(str1),word.get(str2));
        }
        return null;
    }
}
