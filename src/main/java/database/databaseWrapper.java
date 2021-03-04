package main.java.database;

/**
 * @author poojaoza
 **/

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


import java.util.LinkedHashMap;
import java.util.Map;

import main.java.utils.DBSettings;
import main.java.containers.DBContainer;

public class databaseWrapper {

    public DBCursor queryDBWithID(String[] entities_ids){

        if(DBSettings.mongoCollection == null){
            return null;
        }
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("Id",new BasicDBObject("$in", entities_ids));
        DBCursor cursor = DBSettings.mongoCollection.find(whereQuery);
        return cursor;
    }

    public Map<String, String[]> getRecordLeadText(String[] entities_ids){
        Map<String, String[]> records = new LinkedHashMap<>();
        DBCursor entity_cursor = queryDBWithID(entities_ids);
        DBObject record;
        while (entity_cursor.hasNext()){
            String[] entity_records = new String[1];
            record = entity_cursor.next();
            entity_records[0] = record.get("LeadText").toString();
            records.put(record.get("Id").toString(), entity_records);
        }
        return records;
    }

    public Map<String, String[]> getRecordDetails(String[] entities_ids){
        Map<String, String[]> records = new LinkedHashMap<>();
        DBCursor entity_cursor = queryDBWithID(entities_ids);
        DBObject record;
        while (entity_cursor.hasNext()){
            String[] entity_records = new String[3];
            record = entity_cursor.next();
            entity_records[0] = record.get("LeadText").toString();
            entity_records[1] = record.get("OutlinkIds").toString();
            entity_records[2] = record.get("InlinkIds").toString();
            records.put(record.get("Id").toString(), entity_records);
        }
        return records;
    }

    public Map<String, DBContainer> getRecordLeadTextContainer(String[] entities_ids){
        Map<String, DBContainer> records = new LinkedHashMap<>();
        DBCursor entity_cursor = queryDBWithID(entities_ids);
        DBObject record;
        while (entity_cursor.hasNext()){
            record = entity_cursor.next();
            records.put(record.get("Id").toString(), new DBContainer(record.get("LeadText").toString(),
                    record.get("OutlinkIds").toString(),
                    record.get("InlinkIds").toString(),
                    record.get("Id").toString()));
        }
        return records;
    }

    public Map<String, DBContainer> getRecordEntityTextContainer(String[] entities_ids){
        Map<String, DBContainer> records = new LinkedHashMap<>();
        DBCursor entity_cursor = queryDBWithID(entities_ids);
        DBObject record;
        while (entity_cursor.hasNext()){
            record = entity_cursor.next();
            records.put(record.get("Id").toString(), new DBContainer(record.get("Title").toString()));
        }
        return records;
    }

}
