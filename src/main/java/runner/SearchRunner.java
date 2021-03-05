package main.java.runner;


import main.java.clustering.ClusteringRanker;
import main.java.commandparser.CommandParser;
import main.java.commandparser.RegisterCommands;
import main.java.commandparser.ValidateCommands;
import main.java.containers.Container;
//import main.java.dbpedia.DBpedia;
import main.java.mrf.MarkovRandomField;
import main.java.queryexp.*;
import main.java.queryexp.rm3.RelevanceModel3;
import main.java.reranker.ReRanker;
import main.java.rerankerv2.docsimranker.DocumentFrequencySimilarity;
import main.java.rerankerv2.docsimranker.EntitySimilarityRanker;
import main.java.searcher.BaseBM25;
import main.java.utils.*;
import main.java.searcher.PageSearcher;
import main.java.searcher.LeadtextSearcher;
import main.java.graph.GraphSimConstructor;
import main.java.graph.GraphDegreeConstructor;
import main.java.wordsimilarityranker.*;
//import main.java.queryexpansion.QueryExpansion;
import main.java.entityrelation.FeatureGenerator;
import main.java.entityrelation.QueryExapansion;
import main.java.wrapper.QueryExpansionReRanking;


import java.io.IOException;
import java.util.Map;

/*
The searchParser object will hold all the information that is passed as the command line argument.
There are helper methods to get the data.
*/
public class SearchRunner implements ProgramRunner
{
    private RegisterCommands.CommandSearch searchParser = null;
    private ValidateCommands.ValidateSearchCommands validate = null;

    public SearchRunner(CommandParser parser)
    {
        searchParser = parser.getSearchCommand();
        validate = new ValidateCommands.ValidateSearchCommands(searchParser);
    }

    @Override
    public void run()  {

        Map<String,String> queryCBOR = null;

        validate.ValidateRetrievalOptions();
        if(searchParser.isArticleEnabled())
        {
            queryCBOR = SearchUtils.readOutline(searchParser.getQueryfile());
        }else
        {
            queryCBOR = SearchUtils.readOutlineSectionPath(searchParser.getQueryfile());
        }

        if(searchParser.isBM25Enabled())
        {
            BaseBM25 bm = null;
            try {
                bm = new BaseBM25(searchParser.getkVAL(),searchParser.getIndexlocation());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Map<String,Map<String, Container>> res = bm.getRanking(queryCBOR);

            String datafile ="";
            if(searchParser.getQueryfile().toLowerCase().contains("test".toLowerCase()))
            {
                datafile = "_test";
            }
            else if(searchParser.getQueryfile().toLowerCase().contains("train".toLowerCase()))
            {
                datafile = "_train";
            }

            String level = searchParser.isArticleEnabled()? "_article": "_section";
            String mname= "BM_25"+level+datafile;

            RunWriter.writeRunFile(mname,res);
        }

        if(searchParser.isReRankEnabled())
        {
            validate.ValidateReRank();
            ReRanker re = new ReRanker(searchParser,queryCBOR);
            re.ReRank();
        }


        if(searchParser.isEntityFreqEnabled()){
            validate.ValidateEntityDegree();
            try{
                BaseBM25 bm25 = new BaseBM25(searchParser.getkVAL(), searchParser.getIndexlocation());
                Map<String, Map<String, Container>> bm25_ranking = bm25.getRanking(queryCBOR);

                Entities e = new Entities();
                //Map<String, Map<String, Integer>> query_ent_list = e.getSortedEntitiesPerQuery(bm25_ranking);
                Map<String, Map<String, Integer>> query_ent_list = e.getSortedEntitiesPerQueryMentionFreq(bm25_ranking);

                WriteFile write_file = new WriteFile();
                String level = searchParser.isArticleEnabled()? "_article": "_section";
                String datafile ="";
                if(searchParser.getQueryfile().toLowerCase().contains("test".toLowerCase()))
                {
                    datafile = "_test";
                }
                else if(searchParser.getQueryfile().toLowerCase().contains("train".toLowerCase()))
                {
                    datafile = "_train";
                }
                write_file.generateEntityRunFile(query_ent_list, "entityBM25Freq"+level+datafile);


            }catch (IOException ioe){
                System.out.println(ioe.getMessage());
            }

        }

        if(searchParser.isEcmExpandEnabled()){
            validate.ValidateEcmExpansion();
            try{
                Entities e = new Entities();
                QueryExapansion qe = new QueryExapansion();
                Map<String, Map<String, Double>> ecm_entities = e.readEntityRunFile(searchParser.getEcmentityfile());
                Map<String, String> expanded_query = qe.expandQueryWithEntities(queryCBOR,
                                                                                ecm_entities,
                                                                                searchParser.getEcmqenum());

                //BaseBM25 bm25 = new BaseBM25(100, searchParser.getIndexlocation());
                BaseBM25 bm25 = new BaseBM25(searchParser.getkVAL(), searchParser.getIndexlocation());
                Map<String, Map<String, Container>> expanded_bm25_ranking = bm25.getRanking(expanded_query);

                WriteFile write_file = new WriteFile();
                String level = searchParser.isArticleEnabled()? "_article": "_section";
                String datafile ="";
                if(searchParser.getQueryfile().toLowerCase().contains("test".toLowerCase()))
                {
                    datafile = "_test";
                }
                else if(searchParser.getQueryfile().toLowerCase().contains("train".toLowerCase()))
                {
                    datafile = "_train";
                }
                write_file.generateBM25RunFile(expanded_bm25_ranking, "EcmX-BM25"+level+datafile);

            }catch (IOException ioe){
                System.out.println(ioe.getMessage());
            }
        }

        if(searchParser.isEntityRelationEnabled()){
            validate.ValidateEntityRelation();

            try {
                //Map<String,String> querysecCBOR = SearchUtils.readOutlineSectionPath(searchParser.getQueryfile());

                BaseBM25 bm25 = new BaseBM25(searchParser.getkVAL(), searchParser.getIndexlocation());
                Map<String, Map<String, Container>> bm25_ranking = bm25.getRanking(queryCBOR);

                Entities e = new Entities();
                Map<String, Map<String, Integer>> query_ent_list = e.getSortedEntitiesPerQuery(bm25_ranking);
                Map<String, Map<String, Double[]>> entity_ranking_list = e.readEntityRunFileDetails(searchParser.getEcmentityfile());

                FeatureGenerator featuregenerator = new FeatureGenerator();
                Map<String, Map<String, Double[]>> featureVectors = featuregenerator.getFeatureVectors(query_ent_list, bm25_ranking, entity_ranking_list);
                //Map<String, Map<String, Double[]>> featureVectors = featuregenerator.getNormalizedFeatureVectors(query_ent_list, bm25_ranking, entity_ranking_list);
                Map<String, Map<String, Double>> hopRelationfeatureVectors = featuregenerator.extractFeatures(featureVectors, 0);
                Map<String, Map<String, Double>> relComentionfeatureVectors = featuregenerator.extractFeatures(featureVectors, 1);
                Map<String, Map<String, Double>> comentionfeatureVectors = featuregenerator.extractFeatures(featureVectors, 2);
                Map<String, Map<String, Double>> cocouplingcountfeatureVectors = featuregenerator.extractFeatures(featureVectors, 3);
                Map<String, Map<String, Double>> cocouplingrelfeatureVectors = featuregenerator.extractFeatures(featureVectors, 4);
                Map<String, Map<String, Double>> biblorelcouplingfeatureVectors = featuregenerator.extractFeatures(featureVectors, 5);
                Map<String, Map<String, Double>> biblocountcouplingfeatureVectors = featuregenerator.extractFeatures(featureVectors, 6);
                Map<String, Map<String, Double>> outlinksDirectlinksfeatureVectors = featuregenerator.extractFeatures(featureVectors, 7);
                Map<String, Map<String, Double>> inlinksDirectlinksfeatureVectors = featuregenerator.extractFeatures(featureVectors, 8);
                Map<String, Map<String, Double>> bidirlinksDirectlinksfeatureVectors = featuregenerator.extractFeatures(featureVectors, 9);
                Map<String, Map<String, Double>> sortedhopRelationFeatureVectors = featuregenerator.sortFeatureVectors(hopRelationfeatureVectors);
                Map<String, Map<String, Double>> sortedrelComentionFeatureVectors = featuregenerator.sortFeatureVectors(relComentionfeatureVectors);
                Map<String, Map<String, Double>> sortedcomentionFeatureVectors = featuregenerator.sortFeatureVectors(comentionfeatureVectors);
                Map<String, Map<String, Double>> sortedcocouplingCountFeatureVectors = featuregenerator.sortFeatureVectors(cocouplingcountfeatureVectors);
                Map<String, Map<String, Double>> sortedcocouplingRelFeatureVectors = featuregenerator.sortFeatureVectors(cocouplingrelfeatureVectors);
                Map<String, Map<String, Double>> sortedbiblorelcouplingFeatureVectors = featuregenerator.sortFeatureVectors(biblorelcouplingfeatureVectors);
                Map<String, Map<String, Double>> sortedbiblocountcouplingFeatureVectors = featuregenerator.sortFeatureVectors(biblocountcouplingfeatureVectors);
                Map<String, Map<String, Double>> sortedoutlinksDirectlinksFeatureVectors = featuregenerator.sortFeatureVectors(outlinksDirectlinksfeatureVectors);
                Map<String, Map<String, Double>> sortedinlinksDirectlinksFeatureVectors = featuregenerator.sortFeatureVectors(inlinksDirectlinksfeatureVectors);
                Map<String, Map<String, Double>> sortedbidirlinksDirectlinksFeatureVectors = featuregenerator.sortFeatureVectors(bidirlinksDirectlinksfeatureVectors);

                WriteFile write_file = new WriteFile();
                String level = searchParser.isArticleEnabled()? "_article": "_section";
                String datafile ="";
                if(searchParser.getQueryfile().toLowerCase().contains("test".toLowerCase()))
                {
                    datafile = "_test";
                }
                else if(searchParser.getQueryfile().toLowerCase().contains("train".toLowerCase()))
                {
                    datafile = "_train";
                }
                write_file.generateEntityRunFile(sortedhopRelationFeatureVectors, "1hoprelation_feature_vector"+level+datafile);
                write_file.generateEntityRunFile(sortedrelComentionFeatureVectors, "rel_comention_feature_vector"+level+datafile);
                write_file.generateEntityRunFile(sortedcomentionFeatureVectors, "count_comention_feature_vector"+level+datafile);
                write_file.generateEntityRunFile(sortedcocouplingCountFeatureVectors, "co_coupling_count_feature_vector"+level+datafile);
                write_file.generateEntityRunFile(sortedcocouplingRelFeatureVectors, "co_coupling_relevance_feature_vector"+level+datafile);
                write_file.generateEntityRunFile(sortedbiblorelcouplingFeatureVectors, "biblo_relevance_coupling_feature_vector"+level+datafile);
                write_file.generateEntityRunFile(sortedbiblocountcouplingFeatureVectors, "biblo_count_coupling_feature_vector"+level+datafile);
                write_file.generateEntityRunFile(sortedoutlinksDirectlinksFeatureVectors, "outlinks_feature_vector"+level+datafile);
                write_file.generateEntityRunFile(sortedinlinksDirectlinksFeatureVectors, "inlinks_feature_vector"+level+datafile);
                write_file.generateEntityRunFile(sortedbidirlinksDirectlinksFeatureVectors, "bidirectional_feature_vector"+level+datafile);
                write_file.generateFeatureVectorRunFile(featureVectors, "feature_vectors"+level+datafile);
                write_file.generateEntityRankLibRunFile(featureVectors, searchParser.getQrelfile(), "rank_lib"+level+datafile);

                Map<String, Map<String, Double>> hop_entities_score = e.getParagraphsScoreDouble(bm25_ranking, sortedhopRelationFeatureVectors);
                hop_entities_score = e.getRerankedParas(hop_entities_score);

                write_file.generateEntityRunFile(hop_entities_score, "paragraph_1hoprelation_feature"+level+datafile);

                Map<String, Map<String, Double>> rel_comention_entities_score = e.getParagraphsScoreDouble(bm25_ranking, sortedrelComentionFeatureVectors);
                rel_comention_entities_score = e.getRerankedParas(rel_comention_entities_score);

                write_file.generateEntityRunFile(rel_comention_entities_score, "paragraph_rel_comention_feature"+level+datafile);


                Map<String, Map<String, Double>> comention_entities_score = e.getParagraphsScoreDouble(bm25_ranking, sortedcomentionFeatureVectors);
                comention_entities_score = e.getRerankedParas(comention_entities_score);

                write_file.generateEntityRunFile(comention_entities_score, "paragraph_count_comention_feature"+level+datafile);

                Map<String, Map<String, Double>> co_coupling_entities_score = e.getParagraphsScoreDouble(bm25_ranking, sortedcocouplingCountFeatureVectors);
                co_coupling_entities_score = e.getRerankedParas(co_coupling_entities_score);

                write_file.generateEntityRunFile(co_coupling_entities_score, "paragraph_co_coupling_feature"+level+datafile);

                Map<String, Map<String, Double>> biblo_co_coupling_entities_score = e.getParagraphsScoreDouble(bm25_ranking, sortedbiblorelcouplingFeatureVectors);
                biblo_co_coupling_entities_score = e.getRerankedParas(biblo_co_coupling_entities_score);

                write_file.generateEntityRunFile(biblo_co_coupling_entities_score, "paragraph_biblo_co_coupling_feature"+level+datafile);


            }catch (Exception ioe){
                ioe.printStackTrace();
            }
        }
        if(searchParser.isEntityRanklibEnabled()){
            validate.ValidateEntityRankLib();

            try {
                FeatureGenerator featureGenerator = new FeatureGenerator();
                Map<String, Map<String, Double>> query_entity_scores = featureGenerator.generateDotProduct(searchParser.getFeaturevectorfile(),
                        searchParser.getRankLibModelFile());
                //System.out.println(query_entity_scores);
                String level = searchParser.isArticleEnabled()? "_article": "_section";
                String datafile ="";
                if(searchParser.getQueryfile().toLowerCase().contains("test".toLowerCase()))
                {
                    datafile = "_test";
                }
                else if(searchParser.getQueryfile().toLowerCase().contains("train".toLowerCase()))
                {
                    datafile = "_train";
                }
                WriteFile write_file = new WriteFile();
                write_file.generateEntityRunFile(query_entity_scores, "entity_ranklib"+level+datafile);

                BaseBM25 bm25 = new BaseBM25(searchParser.getkVAL(), searchParser.getIndexlocation());
                Map<String, Map<String, Container>> bm25_ranking = bm25.getRanking(queryCBOR);

                Entities e = new Entities();
                Map<String, Map<String, Double>> ranked_entities_score = e.getParagraphsScoreDouble(bm25_ranking, query_entity_scores);
                ranked_entities_score = e.getRerankedParas(ranked_entities_score);

                write_file.generateEntityRunFile(ranked_entities_score, "paragraph_ranklib"+level+datafile);

            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }

        if(searchParser.getisVerbose())
        {
            PrintUtils.displayQuery(queryCBOR);
        }


        if(searchParser.isQe_entity_degree_rerankingEnabled()) {
            validate.ValidateEntityDegree();
            validate.ValidateReRank();

            try {
                BaseBM25 bm25 = new BaseBM25(searchParser.getkVAL(), searchParser.getIndexlocation());
                Map<String, Map<String, Container>> bm25_ranking = bm25.getRanking(queryCBOR);

                Entities e = new Entities();
                Map<String, Map<String, String>> query_ent_list = e.getEntitiesPerQuery(bm25_ranking);

                PageSearcher pgs = new PageSearcher(searchParser.getEntityIndLoc());
                Map<String, Map<String, String>> query_entities = pgs.getRanking(query_ent_list);

                GraphDegreeConstructor gdc = new GraphDegreeConstructor();
                Map<String, Map<String, Integer>> ranked_entities = gdc.getGraphDegree(query_entities);

                Map<String, Map<String, Double>> ranked_entities_score = e.getParagraphsScore(bm25_ranking, ranked_entities);
                ranked_entities_score = e.getRerankedParas(ranked_entities_score);

                Map<String, String> expanded_query = e.expandQuery(queryCBOR, ranked_entities_score);

                //BaseBM25 bm25 = new BaseBM25(100, searchParser.getIndexlocation());
                Map<String, Map<String, Container>> expanded_bm25_ranking = bm25.getRanking(expanded_query);


                QueryExpansionReRanking geReRank = new QueryExpansionReRanking(searchParser, queryCBOR, expanded_bm25_ranking);
                geReRank.getDocumentFrequencyReRanking("QE_Entity_degree");

            } catch (IOException io) {
                        io.getStackTrace();
            }

         }
       }
    }
