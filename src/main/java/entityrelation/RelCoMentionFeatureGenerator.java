package main.java.entityrelation;

/**
 * @author poojaoza
 **/

import com.mongodb.MongoClient;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import main.java.containers.Container;
import main.java.utils.DBSettings;
import main.java.utils.Entities;
import main.java.utils.Stats;
import main.java.utils.SortUtils;
import main.java.database.databaseWrapper;

public class RelCoMentionFeatureGenerator {

    public RelCoMentionFeatureGenerator(){

        if(DBSettings.mongoClient == null){
            DBSettings.mongoClient = new MongoClient(DBSettings.HOST_NAME, DBSettings.PORT);
            DBSettings.mongoDB = DBSettings.mongoClient.getDB("test");
            DBSettings.mongoCollection = DBSettings.mongoDB.getCollection("entityIndex");
        }
    }

    /*
    first_entity: the main entity
    second_entity: against which the main entity is compared
     *//**/
    private Double get1HopRelation(String first_entity,
                                   String second_entity,
                                   Map<String, String[]> entities_details){
        if(entities_details.containsKey(second_entity)){
            String[] details = entities_details.get(second_entity);
            String[] outlinkIds = details[1].split("[\r\n]+");
            String[] inlinkIds = details[2].split("[\r\n]+");
            if(Arrays.stream(outlinkIds).anyMatch(first_entity::equals) || Arrays.stream(inlinkIds).anyMatch(first_entity::equals)){
                return 1.0;
            }
            /*if(Entities.searchArrayElement(outlinkIds, first_entity) || Entities.searchArrayElement(inlinkIds, first_entity)){
                return 1.0;
            }*/
        }
        return 0.0;
    }

    private Double[] getHopRelations(String first_entity,
                                     String second_entity,
                                     Map<String, String[]> entities_details,
                                     Double[] relations) {
        if(entities_details.containsKey(first_entity)){
            String[] details = entities_details.get(first_entity);
            String[] outlinkIds = details[1].split("[\r\n]+");
            String[] inlinkIds = details[2].split("[\r\n]+");

            if(Arrays.stream(outlinkIds).anyMatch(second_entity::equals)) {
                relations[0] = relations[0] + 1.0;
                relations[2] = relations[2] + 1.0;
            }

            if(Arrays.stream(inlinkIds).anyMatch(second_entity::equals)){
                relations[0] = relations[0] + 1.0;
                relations[3] = relations[3] + 1.0;
            }
            if (Arrays.stream(outlinkIds).anyMatch(second_entity::equals) && Arrays.stream(inlinkIds).anyMatch(second_entity::equals)) {
                relations[4] = relations[4] + 1.0;
            }
            /*if(first_entity.equals("enwiki:Linearity")){
              System.out.println("========"+relations[0]+" "+relations[1]+" "+relations[2]+" "+relations[3]+" "+relations[4]+"========");
            }*/

            /*for(int out = 0; out < outlinkIds.length; out++){

                if(entities_details.containsKey(outlinkIds[out])) {
                    String[] out_entity_details = entities_details.get(outlinkIds[out]);
                    String[] out_entity_outlinkIds = out_entity_details[1].split("[\r\n]+");
                    String[] out_entity_inlinkIds = out_entity_details[2].split("[\r\n]+");

                    //System.out.println("outlink id : "+outlinkIds[out]);
                    if (Arrays.stream(out_entity_outlinkIds).anyMatch(first_entity::equals)) {
                        relations[1] = relations[1] + 1.0;
                    }
                    if(Arrays.stream(out_entity_inlinkIds).anyMatch(first_entity::equals)){
                        relations[1] = relations[1] + 1.0;
                    }
                }
            }
            for(int in = 0; in < inlinkIds.length; in++){
                if(entities_details.containsKey(inlinkIds[in])) {
                    String[] in_entity_details = entities_details.get(inlinkIds[in]);
                    String[] in_entity_outlinkIds = in_entity_details[1].split("[\r\n]+");
                    String[] in_entity_inlinkIds = in_entity_details[2].split("[\r\n]+");

                    if (Arrays.stream(in_entity_outlinkIds).anyMatch(first_entity::equals)) {
                        relations[1] = relations[1] + 1.0;
                    }
                    if(Arrays.stream(in_entity_inlinkIds).anyMatch(first_entity::equals)){
                        relations[1] = relations[1] + 1.0;
                    }
                }
            }*/
        }
        return relations;
    }

    private double[] getBibloCouplingFeatures(String first_entity,
                                              String second_entity,
                                              Map<String, String[]> entities_details,
                                              Map<String, Double[]> entity_ranking){
        double biblo_rel_score = 0.0;
        double biblo_count_score = 0.0;

        if(entities_details.containsKey(first_entity) && entities_details.containsKey(second_entity)) {
            String[] first_ent_details = entities_details.get(first_entity);
            String[] first_ent_outlinkIds = first_ent_details[1].split("[\r\n]+");

            String[] second_ent_details = entities_details.get(second_entity);
            String[] second_ent_outlinkIds = second_ent_details[1].split("[\r\n]+");

            Set<String> first_ent_set = new HashSet<>(Arrays.asList(first_ent_outlinkIds));
            Set<String> second_ent_set = new HashSet<>(Arrays.asList(second_ent_outlinkIds));
            first_ent_set.retainAll(second_ent_set);
            //Set<String> common_outlinks = Sets.intersection(first_ent_set, second_ent_set);
            biblo_count_score = first_ent_set.size();

            for(String temp_ent_set: first_ent_set){
                if(entity_ranking != null) {
                    if (entity_ranking.containsKey(temp_ent_set)) {
                        Double[] entity_ranking_details = entity_ranking.get(temp_ent_set);
                        biblo_rel_score += entity_ranking_details[1];
                        //biblo_rel_score += (double)1/entity_ranking_details[0];
                        //biblo_count_score += 1.0;
                        /*if(first_entity.equals("enwiki:Ernst%20Antevs") || first_entity.equals("enwiki:Holocene") || first_entity.equals("enwiki:Paleobotany") ||
                                first_entity.equals("enwiki:Pleistocene") || first_entity.equals("enwiki:Two%20Creeks%20Buried%20Forest%20State%20Natural%20Area") ||
                                first_entity.equals("enwiki:Two%20Creeks,%20Wisconsin") || first_entity.equals("enwiki:Wisconsin%20glaciation")) {
                            if (second_entity.equals("enwiki:Ernst%20Antevs") || second_entity.equals("enwiki:Holocene") || second_entity.equals("enwiki:Paleobotany") ||
                                    second_entity.equals("enwiki:Pleistocene") || second_entity.equals("enwiki:Two%20Creeks%20Buried%20Forest%20State%20Natural%20Area") ||
                                    second_entity.equals("enwiki:Two%20Creeks,%20Wisconsin") || second_entity.equals("enwiki:Wisconsin%20glaciation")) {
                                System.out.println("####################"+first_entity+" "+second_entity+" "+String.valueOf(entity_ranking_details[0])+" "+biblo_rel_score+" "+biblo_count_score);
                        for(int pe = 0; pe < para_entities.length; pe++){
                            System.out.println(para_entities[pe]);
                        }

                            }
                        }*/
                    }
                }
            }

        }
        return new double[]{biblo_rel_score, biblo_count_score};

    }

    private double[] getCoCouplingFeatures(String first_entity,
                                           String second_entity,
                                           Map<String, String[]> entities_details,
                                           Map<String, Double[]> entity_ranking){
        double co_rel_score = 0.0;
        double co_count_score = 0.0;

        if(entities_details.containsKey(first_entity) && entities_details.containsKey(second_entity)) {
            String[] first_ent_details = entities_details.get(first_entity);
            String[] first_ent_inlinkIds = first_ent_details[2].split("[\r\n]+");

            String[] second_ent_details = entities_details.get(second_entity);
            String[] second_ent_inlinkIds = second_ent_details[2].split("[\r\n]+");

            Set<String> first_ent_set = new HashSet<>(Arrays.asList(first_ent_inlinkIds));
            Set<String> second_ent_set = new HashSet<>(Arrays.asList(second_ent_inlinkIds));
            first_ent_set.retainAll(second_ent_set);
            //Set<String> common_inlinks = Sets.intersection(first_ent_set, second_ent_set);
            co_count_score = first_ent_set.size();

            for(String temp_ent_set: first_ent_set){
                if(entity_ranking != null) {
                    if (entity_ranking.containsKey(temp_ent_set)) {
                        Double[] entity_ranking_details = entity_ranking.get(temp_ent_set);
                        co_rel_score += entity_ranking_details[1];
                        //co_rel_score += (double)1/entity_ranking_details[0];
                        //co_count_score += 1.0;
                        /*if(first_entity.equals("enwiki:Ernst%20Antevs") || first_entity.equals("enwiki:Holocene") || first_entity.equals("enwiki:Paleobotany") ||
                                first_entity.equals("enwiki:Pleistocene") || first_entity.equals("enwiki:Two%20Creeks%20Buried%20Forest%20State%20Natural%20Area") ||
                                first_entity.equals("enwiki:Two%20Creeks,%20Wisconsin") || first_entity.equals("enwiki:Wisconsin%20glaciation")) {
                            if (second_entity.equals("enwiki:Ernst%20Antevs") || second_entity.equals("enwiki:Holocene") || second_entity.equals("enwiki:Paleobotany") ||
                                    second_entity.equals("enwiki:Pleistocene") || second_entity.equals("enwiki:Two%20Creeks%20Buried%20Forest%20State%20Natural%20Area") ||
                                    second_entity.equals("enwiki:Two%20Creeks,%20Wisconsin") || second_entity.equals("enwiki:Wisconsin%20glaciation")) {
                                System.out.println("******"+first_entity+" "+second_entity+" "+String.valueOf(entity_ranking_details[0])+" "+String.valueOf(co_rel_score)+" "+co_count_score);
                        for(int pe = 0; pe < para_entities.length; pe++){
                            System.out.println(para_entities[pe]);
                        }

                            }
                        }*/
                    }
                }
            }

        }
        //return new double[]{co_rel_score, co_count_score};
        return new double[]{co_count_score, co_rel_score};

    }


    private double[] entityCoMentions(String first_entity,
                                      String second_entity,
                                      Map<String, Container> bm25_ranking){
        double rel_score = 0.0;
        double counter = 0.0;
        for(Map.Entry<String, Container> c:bm25_ranking.entrySet()){
            String[] para_entities = c.getValue().getEntity().getEntityId().split("[\r\n]+");
            if (Arrays.stream(para_entities).anyMatch(first_entity::equals) && Arrays.stream(para_entities).anyMatch(second_entity::equals)) {
                /*if(first_entity.equals("enwiki:Ernst%20Antevs") || first_entity.equals("enwiki:Holocene") || first_entity.equals("enwiki:Paleobotany") ||
                        first_entity.equals("enwiki:Pleistocene") || first_entity.equals("enwiki:Two%20Creeks%20Buried%20Forest%20State%20Natural%20Area") ||
                        first_entity.equals("enwiki:Two%20Creeks,%20Wisconsin") || first_entity.equals("enwiki:Wisconsin%20glaciation")) {
                    if (second_entity.equals("enwiki:Ernst%20Antevs") || second_entity.equals("enwiki:Holocene") || second_entity.equals("enwiki:Paleobotany") ||
                            second_entity.equals("enwiki:Pleistocene") || second_entity.equals("enwiki:Two%20Creeks%20Buried%20Forest%20State%20Natural%20Area") ||
                            second_entity.equals("enwiki:Two%20Creeks,%20Wisconsin") || second_entity.equals("enwiki:Wisconsin%20glaciation")) {
                        System.out.println("====="+first_entity+" "+second_entity+" "+c.getValue().getRank()+" "+String.valueOf((double)1 / c.getValue().getRank())+" "+counter+" "+para_entities.length);
                        for(int pe = 0; pe < para_entities.length; pe++){
                            System.out.println(para_entities[pe]);
                        }

                    }
                }*/
                /*if(first_entity.equals("enwiki:Acatalasia") || first_entity.equals("enwiki:Active%20site") || first_entity.equals("enwiki:Catalase") ||
                        first_entity.equals("enwiki:Chloroplast") || first_entity.equals("enwiki:Cytosol") || first_entity.equals("enwiki:Enzyme%20kinetics") ||
                        first_entity.equals("enwiki:Eukaryote") || first_entity.equals("enwiki:Extracellular%20fluid") || first_entity.equals("enwiki:Genetic%20engineering") ||
                        first_entity.equals("Hemolytic%20anemia") || first_entity.equals("enwiki:Hyperoxia") || first_entity.equals("enwiki:Manganese") ||
                        first_entity.equals("enwiki:Eukaryote") || first_entity.equals("enwiki:Eukaryote") || first_entity.equals("enwiki:Eukaryote") ||
                        first_entity.equals("enwiki:Eukaryote") || first_entity.equals("enwiki:Eukaryote") || first_entity.equals("enwiki:Eukaryote") ||
                        first_entity.equals("enwiki:Eukaryote") || first_entity.equals("enwiki:Eukaryote") || first_entity.equals("enwiki:Eukaryote") ||
                        first_entity.equals("enwiki:Eukaryote") || first_entity.equals("enwiki:Eukaryote") || first_entity.equals("enwiki:Eukaryote")) {
                    if (second_entity.equals("enwiki:Ernst%20Antevs") || second_entity.equals("enwiki:Holocene") || second_entity.equals("enwiki:Paleobotany") ||
                            second_entity.equals("enwiki:Pleistocene") || second_entity.equals("enwiki:Two%20Creeks%20Buried%20Forest%20State%20Natural%20Area") ||
                            second_entity.equals("enwiki:Two%20Creeks,%20Wisconsin") || second_entity.equals("enwiki:Wisconsin%20glaciation")) {
                        System.out.println("====="+first_entity+" "+second_entity+" "+c.getValue().getRank()+" "+String.valueOf((double)1 / c.getValue().getRank())+" "+counter);

                    }
                }*/

                rel_score += (double)1 / c.getValue().getRank();
                counter += 1;
            }
        }
        return new double[]{rel_score, counter};
    }

    private Map<String, Double[]> generateFeatureVectors(Map<String, Integer> entities_list,
                                                         String query_id,
                                                         Map<String, Map<String, Container>> bm25_ranking,
                                                         Map<String, Map<String, Double[]>> entity_ranking){
        Map<String, Map<String, Double[]>> entities_features = new LinkedHashMap<>();
        Map<String, Double[]> entities_normalized_features = new LinkedHashMap<>();
        Entities entities_utils = new Entities();
        List<String> entities_array = entities_utils.getEntityIdsList(entities_list);

        String[] entities_ids = new String[entities_array.size()];

        for(int i = 0; i < entities_array.size(); i++){
            entities_ids[i] = entities_array.get(i);
        }

        databaseWrapper dbwrapper = new databaseWrapper();
        Map<String, String[]> entities_details = dbwrapper.getRecordDetails(entities_ids);

        //System.out.println(cursor.length());
        int entity_length = entities_ids.length;
        //System.out.println(entity_length);
        Double hop_relations[] = new Double[] {0.0, 0.0, 0.0, 0.0, 0.0};
        //double co_mention[] = new double[]{0.0, 0.0};

        //Map<String, Double[]> other_entities = new LinkedHashMap<>();
        for(int c = 0; c < entity_length; c++){
            //System.out.println(entities_features);
            Map<String, Double[]> other_entities = new LinkedHashMap<>();
            double get1hoprelation_calc = 0.0;
            double get2hoprelation_calc = 0.0;
            double comention_calc = 0.0;
            double rel_comention_cal = 0.0;
            double getoutlinksrelation_calc = 0.0;
            double getinlinksrelation_calc = 0.0;
            double getbidirlinksrelation_calc = 0.0;
            double getbiblorelev_calc = 0.0;
            double getbiblocount_calc = 0.0;
            double getcocoupcount_calc = 0.0;
            double getcocouprel_calc = 0.0;

            for(int e = 0; e < entity_length; e++){
                //System.out.println("Inside e "+entities_features.containsKey(entities_array.get(e))+ " "+c+" "+e+" "+entities_array.get(c));
                if(e == c){
                    continue;
                }
                double[] features_list = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

                if(entities_features.containsKey(entities_array.get(e))){

                    Map<String, Double[]> other_entities_val = entities_features.get(entities_array.get(e));

                    if(other_entities_val.containsKey(entities_array.get(c))){

                        Double[] val = other_entities_val.get(entities_array.get(c));
                        get1hoprelation_calc += val[0].doubleValue();
                        get2hoprelation_calc += val[1].doubleValue();
                        rel_comention_cal += val[2].doubleValue();
                        comention_calc += val[3].doubleValue();
                        //getoutlinksrelation_calc += val[4].doubleValue();
                        //getinlinksrelation_calc += val[5].doubleValue();
                        getoutlinksrelation_calc += val[5].doubleValue();
                        getinlinksrelation_calc += val[4].doubleValue();
                        getbidirlinksrelation_calc += val[6].doubleValue();
                        getbidirlinksrelation_calc += val[7].doubleValue();
                        getbiblorelev_calc += val[8].doubleValue();
                        getcocoupcount_calc += val[9].doubleValue();
                        getcocouprel_calc += val[10].doubleValue();

                        features_list[0] = val[0].doubleValue();
                        features_list[1] = val[1].doubleValue();
                        features_list[2] = val[2].doubleValue();
                        features_list[3] = val[3].doubleValue();
                        //features_list[4] += val[4].doubleValue();
                        //features_list[5] += val[5].doubleValue();
                        features_list[4] += val[5].doubleValue();
                        features_list[5] += val[4].doubleValue();
                        features_list[6] += val[6].doubleValue();
                        features_list[7] += val[7].doubleValue();
                        features_list[8] += val[8].doubleValue();
                        features_list[9] += val[9].doubleValue();
                        features_list[10] += val[10].doubleValue();

                        //System.out.println("=="+entities_array.get(e)+" "+entities_array.get(c)+" "+features_list[0]+" "+features_list[1]+" "+get1hoprelation_calc+" "+get2hoprelation_calc+" "+comention_calc);
                        /*if(entities_array.get(c).equals("enwiki:Ernst%20Antevs") || entities_array.get(c).equals("enwiki:Holocene") || entities_array.get(c).equals("enwiki:Paleobotany") ||
                                entities_array.get(c).equals("enwiki:Pleistocene") || entities_array.get(c).equals("enwiki:Two%20Creeks%20Buried%20Forest%20State%20Natural%20Area") ||
                                entities_array.get(c).equals("enwiki:Two%20Creeks,%20Wisconsin") || entities_array.get(c).equals("enwiki:Wisconsin%20glaciation")) {
                            if (entities_array.get(e).equals("enwiki:Ernst%20Antevs") || entities_array.get(e).equals("enwiki:Holocene") || entities_array.get(e).equals("enwiki:Paleobotany") ||
                                    entities_array.get(e).equals("enwiki:Pleistocene") || entities_array.get(e).equals("enwiki:Two%20Creeks%20Buried%20Forest%20State%20Natural%20Area") ||
                                    entities_array.get(e).equals("enwiki:Two%20Creeks,%20Wisconsin") || entities_array.get(e).equals("enwiki:Wisconsin%20glaciation")) {
                                System.out.println(entities_array.get(c)+" "+entities_array.get(e)+" "+features_list[0]+" "+features_list[2]+" "+features_list[3]+" "+features_list[4]+" "+features_list[5]+" "+features_list[6]
                                +" "+features_list[9]+" "+features_list[10]);

                            }
                        }*/

                    }
                    other_entities.put(entities_array.get(e), ArrayUtils.toObject(features_list));
                }
                else{
                    hop_relations[0] = 0.0;
                    hop_relations[1] = 0.0;
                    hop_relations[2] = 0.0;
                    hop_relations[3] = 0.0;
                    hop_relations[4] = 0.0;

                    /*co_mention[0] = 0.0;
                    co_mention[1] = 0.0;*/

                    //hop_relations = getHopRelations(entities_array.get(c), entities_array.get(e), entities_details, hop_relations);
                    //features_list[2] = entityCoMentions(entities_array.get(c), entities_array.get(e), bm25_ranking.get(query_id));
                    double co_mention[] = entityCoMentions(entities_array.get(c), entities_array.get(e), bm25_ranking.get(query_id));
                    //double biblo_relations[] = getBibloCouplingFeatures(entities_array.get(c), entities_array.get(e), entities_details, entity_ranking.get(query_id));
                    //double cocoupling_relations[] = getCoCouplingFeatures(entities_array.get(c), entities_array.get(e), entities_details, entity_ranking.get(query_id));
                    double biblo_relations[] = new double[]{0, 0};
                    double cocoupling_relations[] = new double[]{0, 0};
                    features_list[0] = hop_relations[0]; //undirected direct links
                    features_list[1] = hop_relations[1];
                    features_list[2] = co_mention[0]; //co-occurrence relevance
                    features_list[3] = co_mention[1]; //co-occurrence count
                    features_list[4] = hop_relations[2]; //outlinks direct links
                    features_list[5] = hop_relations[3]; //inlinks direct links
                    features_list[6] = hop_relations[4]; //bidirectional direct links
                    features_list[7] = biblo_relations[0];
                    features_list[8] = biblo_relations[1];
                    features_list[9] = cocoupling_relations[0];
                    features_list[10] = cocoupling_relations[1];

                    get1hoprelation_calc += features_list[0];
                    get2hoprelation_calc += features_list[1];
                    rel_comention_cal += features_list[2];
                    comention_calc += features_list[3];
                    getoutlinksrelation_calc += features_list[4];
                    getinlinksrelation_calc += features_list[5];
                    getbidirlinksrelation_calc += features_list[6];
                    getbiblorelev_calc += features_list[7];
                    getbiblocount_calc += features_list[8];
                    getcocoupcount_calc += features_list[9];
                    getcocouprel_calc += features_list[10];
                    //System.out.println(entities_array.get(c)+" "+entities_array.get(e)+" "+features_list[0]+" "+features_list[1]+" "+get1hoprelation_calc+" "+get2hoprelation_calc+" "+comention_calc);
                    other_entities.put(entities_array.get(e), ArrayUtils.toObject(features_list));
                    /*if(entities_array.get(c).equals("enwiki:Ernst%20Antevs") || entities_array.get(c).equals("enwiki:Holocene") || entities_array.get(c).equals("enwiki:Paleobotany") ||
                            entities_array.get(c).equals("enwiki:Pleistocene") || entities_array.get(c).equals("enwiki:Two%20Creeks%20Buried%20Forest%20State%20Natural%20Area") ||
                            entities_array.get(c).equals("enwiki:Two%20Creeks,%20Wisconsin") || entities_array.get(c).equals("enwiki:Wisconsin%20glaciation")) {
                        if (entities_array.get(e).equals("enwiki:Ernst%20Antevs") || entities_array.get(e).equals("enwiki:Holocene") || entities_array.get(e).equals("enwiki:Paleobotany") ||
                                entities_array.get(e).equals("enwiki:Pleistocene") || entities_array.get(e).equals("enwiki:Two%20Creeks%20Buried%20Forest%20State%20Natural%20Area") ||
                                entities_array.get(e).equals("enwiki:Two%20Creeks,%20Wisconsin") || entities_array.get(e).equals("enwiki:Wisconsin%20glaciation")) {
                            System.out.println(entities_array.get(c)+" "+entities_array.get(e)+" "+features_list[0]+" "+features_list[2]+" "+features_list[3]+" "+features_list[4]+" "+features_list[5]+" "+features_list[6]
                            +" "+features_list[9]+" "+features_list[10]);

                        }
                    }*/

                }

            }
            entities_features.put(entities_array.get(c), other_entities);
            /*entities_normalized_features.put(entities_array.get(c), new Double[] {get1hoprelation_calc/entity_length,
                    get2hoprelation_calc/entity_length,
                    comention_calc/entity_length});*/
            System.out.println("Features : "+query_id+" "+entities_array.get(c)+" "+entity_length+" "+get1hoprelation_calc+" "+rel_comention_cal+" "+comention_calc+" "+getbiblorelev_calc+" "+
                    getbiblocount_calc+" "+getoutlinksrelation_calc+" "+getinlinksrelation_calc+" "+getbidirlinksrelation_calc);

            entities_normalized_features.put(entities_array.get(c), new Double[] {get1hoprelation_calc/entity_length,
                    //get2hoprelation_calc/entity_length,
                    rel_comention_cal/entity_length,
                    comention_calc/entity_length,
                    getcocoupcount_calc/entity_length,
                    getcocouprel_calc/entity_length,
                    getbiblorelev_calc/entity_length,
                    getbiblocount_calc/entity_length,
                    getoutlinksrelation_calc/entity_length,
                    getinlinksrelation_calc/entity_length,
                    getbidirlinksrelation_calc/entity_length});
            //System.out.println("Features : "+query_id+" "+c+" "+entity_length+" "+get1hoprelation_calc/entity_length+" "+get2hoprelation_calc/entity_length+" "+rel_comention_cal/entity_length+" "+comention_calc/entity_length);
        }

        return entities_normalized_features;
    }


    public Map<String, Map<String, Double[]>> getFeatureVectors(Map<String, Map<String, Integer>> query_entity_list,
                                                                Map<String, Map<String, Container>> bm25_ranking,
                                                                Map<String, Map<String, Double[]>> entity_ranking){
        System.out.println(query_entity_list.size());
        int p = 0;
        Map<String, Map<String, Double[]>> query_entity_feature_vec = new ConcurrentHashMap<>();

        query_entity_list.entrySet().parallelStream().forEach(m ->
        { query_entity_feature_vec.put(m.getKey(), generateFeatureVectors(m.getValue(), m.getKey(), bm25_ranking, entity_ranking));});

        /*query_entity_list.entrySet().parallelStream().forEach(m ->
        {
            if(m.getKey().equals("enwiki:Radiocarbon%20dating/Use%20in%20archaeology/Notable%20applications/Pleistocene/Holocene%20boundary%20in%20Two%20Creeks%20Fossil%20Forest")){
                query_entity_feature_vec.put(m.getKey(), generateFeatureVectors(m.getValue(), m.getKey(), bm25_ranking, entity_ranking));
            } });*/

       /*Map.Entry<String, Map<String, Integer>> m = query_entity_list.entrySet().iterator().next();
            p++;
            System.out.println(p);
            System.out.println(m.getKey());
            query_entity_feature_vec.put(m.getKey(), generateFeatureVectors(m.getValue(), m.getKey(), bm25_ranking, entity_ranking));*/

        return query_entity_feature_vec;
    }

    public Map<String, Map<String, Double[]>> getNormalizedFeatureVectors(Map<String, Map<String, Integer>> query_entity_list,
                                                                          Map<String, Map<String, Container>> bm25_ranking,
                                                                          Map<String, Map<String, Double[]>> entity_ranking){

        Map<String, Map<String, Double[]>> query_entity_normalized_vec;
        query_entity_normalized_vec = getFeatureVectors(query_entity_list, bm25_ranking, entity_ranking);
        query_entity_normalized_vec = new Stats().normalizeData(query_entity_normalized_vec);
        return query_entity_normalized_vec;
    }

    public Map<String, Map<String, Double>> extractFeatures(Map<String, Map<String, Double[]>> feature_vec_list, int featureNum){
        Map<String, Map<String, Double>> features = new LinkedHashMap<>();

        for(Map.Entry<String, Map<String, Double[]>> mp : feature_vec_list.entrySet()){
            for(Map.Entry<String, Double[]> np: mp.getValue().entrySet()){
                if (features.containsKey(mp.getKey())) {
                    Map<String, Double> extract = features.get(mp.getKey());
                    //Double[] features = new Double[] {Double.parseDouble(words[3]), Double.parseDouble(words[4])};
                    extract.put(np.getKey(), np.getValue()[featureNum]);
                } else {

                    Map<String, Double> temp = new LinkedHashMap<>();
                    //Double[] features = new Double[] {Double.parseDouble(words[3]), Double.parseDouble(words[4])};
                    temp.put(np.getKey(), np.getValue()[featureNum]);
                    features.put(mp.getKey(), temp);
                }
            }
        }
        return features;
    }

    public Map<String, Map<String, Double>> sortFeatureVectors(Map<String, Map<String, Double>> feature_vec_list){
        Map<String, Map<String, Double>> sorted_entity_list = new LinkedHashMap<>();
        for(Map.Entry<String, Map<String, Double>> m : feature_vec_list.entrySet()) {
            sorted_entity_list.put(m.getKey(), Entities.sortByValueWithLimit(m.getValue()));
        }
        return sorted_entity_list;
    }

    public Map<String, Map<String, Double>> generateDotProduct(String feature_vectors, String ranklib_model ){

        Map<String, Map<String, Double>> query_entity_score = new LinkedHashMap<>();
        Entities e = new Entities();
        Map<String, Map<String, Double[]>> features = e.readEntityFeatureVectorFile(feature_vectors);
        Double[] weights = e.readRankLibModelFile(ranklib_model);

        Stats s = new Stats();

        for(Map.Entry<String, Map<String, Double[]>> m : features.entrySet()){
            Map<String, Double> score = new LinkedHashMap<>();
            for(Map.Entry<String, Double[]> n: m.getValue().entrySet()) {
                score.put(n.getKey(), s.getDotProduct(n.getValue(), weights));
            }
            query_entity_score.put(m.getKey(), SortUtils.sortByValue(score));
        }
        return query_entity_score;
    }

    public Map<String, Map<String, Double>> generateAverageCentroidVector(String feature_vectors){

        Map<String, Map<String, Double>> query_entity_score = new LinkedHashMap<>();
        Entities e = new Entities();
        Map<String, Map<String, Double[]>> features = e.readEntityFeatureVectorFile(feature_vectors);

        Stats s = new Stats();

        for(Map.Entry<String, Map<String, Double[]>> m : features.entrySet()){
            Map<String, Double> score = new LinkedHashMap<>();
            int entities_size = m.getValue().size();
            Double[] centroid = new Double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            for(Map.Entry<String, Double[]> n: m.getValue().entrySet()) {
                try {
                    //System.out.println(n.getValue());
                    centroid[0] += n.getValue()[0];
                    centroid[1] += n.getValue()[1];
                    centroid[2] += n.getValue()[2];
                    centroid[3] += n.getValue()[3];
                    centroid[4] += n.getValue()[4];
                    centroid[5] += n.getValue()[5];
                }catch (NullPointerException npe){
                    System.out.println(npe.getMessage());
                }
            }
            System.out.println("centroid vector before: "+entities_size+" "+centroid[0].toString()+" "+centroid[1].toString()+" "+centroid[2].toString()+" "+centroid[3].toString()+" "+centroid[4].toString()+" "+centroid[5].toString());

            centroid[0] = centroid[0]/entities_size;
            centroid[1] = centroid[1]/entities_size;
            centroid[2] = centroid[2]/entities_size;
            centroid[3] = centroid[3]/entities_size;
            centroid[4] = centroid[4]/entities_size;
            centroid[5] = centroid[5]/entities_size;

            System.out.println("centroid vector : "+entities_size+" "+centroid[0].toString()+" "+centroid[1].toString()+" "+centroid[2].toString()+" "+centroid[3].toString()+" "+centroid[4].toString()+" "+centroid[5].toString());


            for(Map.Entry<String, Double[]> n: m.getValue().entrySet()) {
                //score.put(n.getKey(), s.getDotProduct(n.getValue(), centroid));
                score.put(n.getKey(), s.getEuclideanDistance(n.getValue(), centroid));
            }
            query_entity_score.put(m.getKey(), SortUtils.sortByValue(score));

        }
        return query_entity_score;

    }
}
