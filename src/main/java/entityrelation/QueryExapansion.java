package main.java.entityrelation;

/**
 * @author poojaoza
 **/

import com.mongodb.MongoClient;
import main.java.containers.DBContainer;
import main.java.database.databaseWrapper;
import main.java.utils.DBSettings;
import main.java.utils.Entities;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryExapansion {

    public QueryExapansion(){

        if(DBSettings.mongoClient == null){
            DBSettings.mongoClient = new MongoClient(DBSettings.HOST_NAME, DBSettings.PORT);
            DBSettings.mongoDB = DBSettings.mongoClient.getDB("test");
            DBSettings.mongoCollection = DBSettings.mongoDB.getCollection("entityIndex");
        }
    }

    public Map<String, String> expandQueryWithEntities(Map<String, String> queryCbor,
                                           Map<String, Map<String, Double>> ranked_entities,
                                           int expand_number)
    {
        Map<String, String> expanded_query = new LinkedHashMap<>();
        databaseWrapper dbwrapper = new databaseWrapper();
        Entities entities_utils = new Entities();

        for(Map.Entry<String, String> m: queryCbor.entrySet())
        {
            int counter = 0;
            if(ranked_entities.containsKey(m.getKey()))
            {
                Map<String, Double> ranked_entity_details = ranked_entities.get(m.getKey());
                List<String> entities_array = entities_utils.getEntityIdsList(ranked_entity_details);

                String[] entities_ids = new String[entities_array.size()];

                for(int i = 0; i < entities_array.size(); i++){
                    entities_ids[i] = entities_array.get(i);
                }

                Map<String, DBContainer> entities_details = dbwrapper.getRecordEntityTextContainer(entities_ids);
                StringBuilder expanded_terms = new StringBuilder();

                for(Map.Entry<String, Double> r: ranked_entity_details.entrySet()){
                    if(counter == expand_number){
                        break;
                    }
                    if(entities_details.containsKey(r.getKey())) {
                        expanded_terms.append(" ");
                        expanded_terms.append(entities_details.get(r.getKey()).getEntitiesTitle());
                        counter++;
                    }
                }


                expanded_query.put(m.getKey(), expanded_terms.toString());
            }else{
                expanded_query.put(m.getKey(), m.getValue());
            }

        }
        return expanded_query;
    }
}
