package main.java.commandparser;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

/*
All the necessary commands should be registered here first for your implementation.
*/

public class RegisterCommands
{
    public enum qeType
    {
        entityText , entityID , entityTextID,entityIDInEntityField;
    }
     /*
      All the commands as part of the index should be registered here
      */
     @Parameters(separators = "=",commandDescription = "Command to Index the Corpus")
     public static class CommandIndex
     {

          @Parameter(names = {"-i","--corpus-file"},description = "Corpus file to index. In case of Entity Abstract, please specify Entity Index location",required=true)
          private String IndexPath;

          @Parameter(names = {"-d","--dest-location"},description = "Location to save the index file")
          private String destpath = System.getProperty("user.dir") + System.getProperty("file.separator") + "indexed_file";

         @Parameter(names = {"--para-index"}, description = "Perform Paragraph Index")
         private Boolean isParaIndex = false;

         @Parameter(names = {"--entity-index"}, description = "Perform Entity Index")
         private Boolean isEntity = false;

         @Parameter(names = {"--abstract-index"}, description = "Perform Entity Abstract Index")
         private Boolean isEntityAbstract = false;


          @Parameter(names = "--help", help = true)
          private boolean help;

          boolean isHelp()
          {
               return help;
          }

          public String getIndexPath()
          {
               return IndexPath;
          }

          public String getDestpath()
          {
               return destpath;
          }

          public Boolean getIsParaIndex() {return isParaIndex;}

          public Boolean getIsEntity() {return isEntity;}

         public Boolean getIsEntityAbstract() {return isEntityAbstract;}


     }

     /*
     All the commands as part of the search should be registered here
     The required parameter is set to true because its mandatory

     Example command for your search implementation.
     --rerank one of the method implementation requires word embeddings file, whatever data you need, you can accept it.

     There are some helper functions that needs to be implemented to validate the inputs for your method, if the user missed any
     data, the program should exit with a status message

     */

     @Parameters(separators = "=",commandDescription = "Command to search")
     public static class CommandSearch
     {
          @Parameter(names = {"-i", "--index-loc"}, description = "Indexed directory to search", required = true)
          private String indexlocation = null;

          @Parameter(names = {"-q", "--query-cbor"}, description = "Query file (CBOR file)", required = true)
          private String queryfile = null;

         @Parameter(names = {"-qrel", "--entity-qrel"}, description = "Entity qrel file")
         private String qrelfile = null;

        @Parameter(names = "--help", help = true)
        private boolean help;
         @Parameter(names = {"-entity-run", "--entity-run"}, description = "Entity run file")
         private String ecmentityfile = null;


         @Parameter(names = {"-ecm-qe-num", "--ecm-query-expansion-terms-num"}, description = "ECM  Query Expansion Terms Number")
         private Integer ecmqenum = 20;


          @Parameter(names = {"-k","--candidate-set-val"}, description = "How many candidate set to retrieve using BM25")
          private Integer kVAL=100;



         @Parameter(names = {"-we","--word-embedding"},description ="Pass the word embedding file GloVe/ Word2Vec")
         private String word_embedding_file = null;


         @Parameter(names = {"-dim","--word-dimension"},description ="Dimension of the Word embeddings")
         private Integer dimension=0;

         @Parameter(names = {"-model","--ranklib-model"},description ="Pass the file location of ranklib model file")
         private String ranklib_model=null;

         @Parameter(names = {"-f", "--entity-feature"}, description = "Entity feature vector file")
         private String featurevectorfile = null;

         @Parameter(names = {"-bm25","--default-bm25"},description ="Rerank the initial retrieved cluster using document similarity")
         private boolean isBM25 =false;

         @Parameter(names = {"-V","--verbose"},description ="Print out  some of the results  into stdout")
         private boolean isVerbose =false;

         @Parameter(names = "--rerank",description ="Rerank the initial retrieved document using document similarity")
         private boolean isReRank =false;

         @Parameter(names = "--bias-fact",description ="Bias factor to get the document representation")
         private Integer biasFactor = 1;

         @Parameter(names = {"--rerank-idf"},description ="Rerank the document based on the IDF")
         private boolean isIDFReRank =false;

         @Parameter(names = {"--rerank-df"},description ="Rerank the document based on the DF")
         private boolean isDFReRank =false;

         @Parameter(names = {"--cosine-sim"},description ="Rerank the document based on the cosine similarity between two strings")
         private boolean isCosineSimilarity =false;

         @Parameter(names = {"--jaccard-sim"},description ="Rerank the document based on the Jaccard similarity between two strings")
         private boolean isJaccardSimilarity = false;

         @Parameter(names = {"--jaro-sim"},description ="Rerank the document based on the Jaro Winkler similarity between two strings")
         private boolean isJaroEnabled = false;

         @Parameter(names = {"--dice-sim"},description ="Rerank the document based on the Sorensen Dice coefficient similarity between two strings")
         private boolean isDiceEnabled = false;

         @Parameter(names = {"--leven-sim"},description ="Rerank the document based on the NormalizedLevenshtein similarity between two strings")
         private boolean isLevenSim = false;

         @Parameter(names = {"-dbpedia","--exist-dbpedia"},description ="Find out if an entity exist in dbpedia")
         private boolean isExistDBpedia =false;

         @Parameter(names = {"-dbpcontain","--dbpedia-contain"},description ="Change the search to contain")
         private boolean isDBpediaContain =false;

         @Parameter(names = {"-qe","--query-expansion"},description ="Rerank the document using Query expansion")
         private boolean isQE =false;

         @Parameter(names = {"-qe-type","--query-expansion-type"},description ="Select type of Query expansion (entityText, entityID , entityTextID) ")
         private qeType qeTypeValue = qeType.entityText;

         @Parameter(names = {"-top"},description ="specify the top number of selected entity to used in the Query expansion")
         private int numberOfReturnedEntity = 3;


         @Parameter(names = "--entity-default-freq",description ="Rerank the initial retrieved document using entity frequency")
         private boolean isEntityFreq =false;

         @Parameter(names = "--entity-degree",description ="Rerank the initial retrieved document using entity degree")
         private boolean isEntityDegree =false;

         @Parameter(names = "--entity-index",description ="Pass the index location of entity index")
         private String entityIndLoc = null;

         @Parameter(names = "--entity-sim",description ="Rerank the initial retrieved document using entity abstract similarity")
         private boolean isEntitySim =false;

         @Parameter(names = "--entity-expand",description ="Rerank the initial retrieved document using expanded query")
         private boolean isQueryExpand =false;

         @Parameter(names = "--entity-ecm-expand",description ="Rerank the initial retrieved document using ecm entities to expand query")
         private boolean isEcmExpandEnabled =false;


         @Parameter(names = "--entity-relation",description ="Generate the feature vectors and ranklib model")
         private boolean isEntityRelationEnabled =false;

         @Parameter(names = "--entity-ranklib",description ="Rerank the passages using entity ranklib")
         private boolean isEntityRanklibEnabled =false;

         @Parameter(names = "--entity-centroid",description ="Rerank the passages using entity average centroid")
         private boolean isEntityCentroidEnabled =false;

         @Parameter(names = "article",description ="Article level retrieval")
         private boolean isArticleEnabled =false;

         @Parameter(names = "section",description ="Section level retrieval")
         private boolean isSectionEnabled =false;

         @Parameter(names = "--entity-doc-sim",description ="Rerank the initial retrieved document using entity abstract similarity")
         private boolean isEntityDocSimEnabled = false;

         @Parameter(names = "--parallel",description ="Uses the parallel stream for the reranker methods")
         private boolean isParallelEnabled = false;

         @Parameter(names = "--qrel-path",description ="Pass the absolute path of the Qrel")
         private String qrelPath = null;

         @Parameter(names = "--rank-lib",description ="Provide path to the Ranklib")
         private String ranklibpath = null;

         @Parameter(names = "--qe-reranking",description ="Query expansion method based on the Entities and reranking")
         private boolean qe_reranking = false;

         @Parameter(names = "--qe-entity-degree",description ="Query expansion based on the entity degree and reranking")
         private boolean qe_entity_degree_reranking = false;

        @Parameter(names = "--qe-exp-df", description = "Query expansion using PRF and the terms selected using DF")
        private boolean isQueryExDF = false;

        @Parameter(names = "--qe-exp-idf", description = "Query expansion using PRF and the terms selected using IDF")
        private boolean isQueryExIDF = false;

        @Parameter(names = "--qe-exp-entity", description = "Query expansion using PRF, also considering entity abstract")
        private boolean isQueryExpEntity = false;


        @Parameter(names = "--test", description = "Only for testing purposes")
        private boolean isTestEnabled = false;

        public boolean isQueryExpRm3() {
            return isQueryExpRm3;
        }

        public boolean isTestEnabled() {
            return isTestEnabled;
        }

        public boolean isQueryExDFEnabled() {
            return isQueryExDF;
        }

        public boolean isQueryExIDFEnabled() {
            return isQueryExIDF;
        }

        public boolean isQueryExpEntityEnabled() {
            return isQueryExpEntity;
        }

        public boolean is_qe_reranking() {
            return qe_reranking;
        }

        public boolean isQe_entity_degree_rerankingEnabled() {
            return qe_entity_degree_reranking;
        }

        public String getRanklibPath() {
            return ranklibpath;
        }

         public boolean isLevenSimEnabled() {return isLevenSim;}
         public String getQrelPath()
         {
             return qrelPath;
         }
         public boolean isParallelEnabled(){return isParallelEnabled;}
         public boolean isArticleEnabled()
         {
             return isArticleEnabled;
         }
         public boolean isSectionEnabled()
         {
             return isSectionEnabled;
         }
         public boolean isExistinDBpedia(){return isExistDBpedia;}
         public boolean isDBpediaContain() {return isDBpediaContain;}
         public boolean isQEEnabled(){return isQE;}
         public qeType getQEType () {return qeTypeValue;}
         public int getNumberOfReturnedEntity() {return numberOfReturnedEntity;}

         public boolean isDiceEnabled() { return isDiceEnabled;}
         public boolean isJaroSimilarityEnabled(){return isJaroEnabled;}
         public boolean isJaccardSimilarityEnabled(){return isJaccardSimilarity;}

         public boolean isIDFReRankEnabled()
         {
             return isIDFReRank;
         }
         public boolean isDFReRankEnabled()
         {
             return isDFReRank;
         }

        public String getIndexlocation() {
            return indexlocation;
        }

        public String getQueryfile() {
            return queryfile;
        }

        public String getQrelfile() {
            return qrelfile;
        }


         public String getEcmentityfile()
         {
             return ecmentityfile;
         }

         public String getFeaturevectorfile()
         {
             return featurevectorfile;
         }

          public boolean isReRankEnabled()
          {
               return isReRank;
          }

          public Integer getkVAL()
          {
               return kVAL;
          }

          public String getWordEmbeddingFile()
          {
              return word_embedding_file;
          }

          public Integer getDimension()
          {
              return dimension;
          }

          public Boolean getisVerbose()
          {
              return isVerbose;
          }

          public boolean isBM25Enabled()
          {
              return isBM25;
          }

          public boolean isCosineSimilarityEnabled() {return isCosineSimilarity;}


            public boolean isEntityDocSimEnabled()
            {
                return isEntityDocSimEnabled;
            }

          public boolean isEntityDegreeEnabled()
         {
             return isEntityDegree;
         }

         public boolean isEntityFreqEnabled() { return isEntityFreq;}

         public Integer getEcmqenum() { return  ecmqenum; }

         public boolean isEntityRelationEnabled()
         {
             return isEntityRelationEnabled;
         }

         public boolean isEntityRanklibEnabled()
         {
             return isEntityRanklibEnabled;
         }

         public boolean isEntityCentroidEnabled()
         {
             return isEntityCentroidEnabled;
         }

         public boolean isEcmExpandEnabled(){ return isEcmExpandEnabled;}

         public String getRankLibModelFile()
         {
             return ranklib_model;
         }

        public String getEntityIndLoc() {
            return entityIndLoc;
        }

        public boolean isEntitySimEnabled() {
            return isEntitySim;
        }

        public boolean isQueryExpand() {
            return isQueryExpand;
        }

        boolean isHelp() {
            return help;
        }

        public static boolean isSpecialCharSpamFilterEnabled()
        {
            return isSpecialCharSpamFilterEnabled;
        }
    }

        @Parameters(separators = "=", commandDescription = "Help Information")
    public static class CommandHelp {

    }

    @Parameters(separators = "=", commandDescription = "Ranker")
    public static class Ranker {
        @Parameter(names = {"--model-file"}, description = "Location of the model file", required = true)
        private String modelFile = null;

        @Parameter(names = {"--run-file"}, description = "Location of the run file", required = true)
        private String runfile = null;

        @Parameter(names = {"--mname"}, description = "Method name suffix")
        private String mname = "mrfupdated";

        public String getModelFile() {
            return modelFile;
        }

        public String getRunfile() {
            return runfile;
        }

        public String getMname() {
            return mname;
        }

    }

}
