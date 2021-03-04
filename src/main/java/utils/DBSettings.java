package main.java.utils;

/**
 * @author poojaoza
 **/

import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class DBSettings {

    public static final String HOST_NAME = "localhost";
    public static final Integer PORT = 27017;
    public static MongoClient mongoClient = new MongoClient(HOST_NAME, PORT);
    public static DB mongoDB = mongoClient.getDB("test");
    public static DBCollection mongoCollection = mongoDB.getCollection("entityIndex");
}
