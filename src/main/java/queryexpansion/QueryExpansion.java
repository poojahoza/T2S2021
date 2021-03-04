//package main.java.queryexpansion;
//
//import main.java.commandparser.RegisterCommands;
//import main.java.containers.Container;
//import main.java.searcher.BaseBM25;
//import main.java.containers.EntityContainer;
//import main.java.utils.PrintUtils;
//import main.java.utils.RunWriter;
//import main.java.utils.SortUtils;
//import java.io.IOException;
//import java.util.*;
//import org.apache.jena.query.*;
//
//public class QueryExpansion {
//
//    private BaseBM25 bm25 = null;
//    private RegisterCommands.CommandSearch searchcommand = null;
//    private Map<String, String> query = null;
//    private HashMap<String, EntityContainer> myEntityMap = null;
//    private HashMap<String, HashMap<String, EntityContainer>> myGlobalMap = null;
//
//    public QueryExpansion(RegisterCommands.CommandSearch searchcommand, Map<String, String> query) {
//        this.searchcommand = searchcommand;
//        this.query = query;
//
//        try {
//            bm25 = new BaseBM25(searchcommand.getkVAL(), searchcommand.getIndexlocation());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public Map<String,Map<String,Container >> doQueryExpansion() {
//
//        myEntityMap = new HashMap<>();
//        myGlobalMap = new HashMap<>();
//        Map<String, Map<String, Container>> tempBM25 = new HashMap<>();
//        String qID = "";
//        EntityContainer entity = null;
//        int i = 0;
//        for (Map.Entry<String, String> qMain : query.entrySet()) {
//
//            if (searchcommand.getisVerbose()) {
//                System.out.println(i + " of " + query.entrySet().size());
//            }
//
//            i++;
//            String Query = qMain.getValue();
//
//            Map<String, Container> temp = bm25.getRanking(Query);
//
//            // Get all the entity from the baseline BM25 and save it to entity map
//            //          with count represent existing of an Entity in the Query result from BM25
//            // them add the query with the entities in Global Map.
//            myEntityMap = new HashMap<>();
//
//            for (Map.Entry<String, Container> q3 : temp.entrySet()) {
//
//                EntityContainer e = q3.getValue().getEntity();
//
//                if (qMain.getKey() != qID) {
//                    // new Query
//                    if (e.getEntityId() != " " ) {
//
//                        entity = e;
//                        entity.setCount(1);
//                        myEntityMap.put(e.getEntityId(), entity);
//                        myGlobalMap.put(qMain.getKey(), myEntityMap);
//                    }
//                } else {
//                    // old Query
//                    if (e.getEntityId() != " " ) {
//                        entity = myEntityMap.get(e.getEntityId());
//                        if (entity == null) {
//                            entity = e;
//                            entity.setCount(1);
//                            myEntityMap.put(e.getEntityId(), entity);
//                        } else {
//                            entity.setCount(entity.getCount() + 1);
//                            myEntityMap.replace(e.getEntityId(), entity);
//                        }
//                        myGlobalMap.replace(qMain.getKey(), myEntityMap);
//                    }
//                }
//                qID = qMain.getKey();
//                tempBM25.put(qMain.getKey(),temp);
//            }
//
//        }
//
//        String mname = "doc_reranking_BM25"+"_k"+this.searchcommand.getkVAL() + "_Spam_filter_enable_" + RegisterCommands.CommandSearch.isSpamFilterEnabled() + "_ISspecialCharSpamEnabled_" + RegisterCommands.CommandSearch.isSpecialCharSpamFilterEnabled() + "_top"+searchcommand.getNumberOfReturnedEntity();
//        RunWriter.writeRunFile(mname,tempBM25);
//
//        // issue Query expansion based on the selection method
//        switch (searchcommand.getQEType()) {
//            case  entityText:
//                return QEWithEntityText();
//            case   entityID:
//                return QEWithEntityID();
//            case  entityTextID :
//                return QEWithEntityTextAndID();
//            case   entityIDInEntityField:
//                return QEOnlyEntityIDInEntityIDField();
//            default:
//                return QEWithEntityText();
//        }
//    }
//
//    private Map<String,Map<String,Container >> QEWithEntityText () {
//        // Query text + Entity Text.
//        int i2 = 1;
//        Map<String, String> expandedQuery = query;
//        for (Map.Entry<String, HashMap<String, EntityContainer>> globalQMap : myGlobalMap.entrySet()) {
//
//
//            int numberOfReturnedEntity = searchcommand.getNumberOfReturnedEntity();
//
//            // return max entity (the top entity)
//            List<EntityContainer> candidateEntityList = getMaxEntityCount(globalQMap.getValue(), numberOfReturnedEntity + 1);
//
//            int iq = 0;
//            String additionalQuery = "";
//            for (EntityContainer candidateEntity : candidateEntityList) {
//                if (iq != 0) { // do not take the first entity which is ""
//
//                    additionalQuery = additionalQuery + " " + candidateEntity.getEntityVal();
//
//                    if (searchcommand.getisVerbose()) {
//                        System.out.println("\n" + i2 + " query " + globalQMap.getKey() + " the top Entity " + candidateEntity.getEntityId() + " --- Value ---- " + candidateEntity.getEntityVal() + " with count " + candidateEntity.getCount());
//                    }
//                }
//                iq++;
//            }
//            i2++;
//
//            expandedQuery.replace(globalQMap.getKey(), expandedQuery.get(globalQMap.getKey()) + " " + additionalQuery);
//        }
//
//        return ReRank(expandedQuery);
//
//    }
//
//    private Map<String,Map<String,Container >> QEWithEntityID () {
//        // Query text + Entity ID.
//
//        int i2 = 1;
//        Map<String, String> expandedQuery = query;
//        for (Map.Entry<String, HashMap<String, EntityContainer>> globalQMap : myGlobalMap.entrySet()) {
//
//
//            int numberOfReturnedEntity = searchcommand.getNumberOfReturnedEntity();
//
//            // return max entity (the top entity)
//            List<EntityContainer> candidateEntityList = getMaxEntityCount(globalQMap.getValue(), numberOfReturnedEntity + 1);
//
//            int iq = 0;
//            String additionalQuery = "";
//            for (EntityContainer candidateEntity : candidateEntityList) {
//                if (iq != 0) { // do not take the first entity which is ""
//
//                    additionalQuery = additionalQuery + " " + candidateEntity.getEntityId() ;
//
//                    additionalQuery = additionalQuery.replaceAll("enwiki:" , "QQQQ").replaceAll(":","").replaceAll("QQQQ","enwiki:" );
//
//                    if (searchcommand.getisVerbose()) {
//                        System.out.println("\n" + i2 + " query " + globalQMap.getKey() + " the top Entity " + candidateEntity.getEntityId() + " --- Value ---- " + candidateEntity.getEntityVal() + " with count " + candidateEntity.getCount());
//                    }
//                }
//                iq++;
//            }
//            i2++;
//
//            expandedQuery.replace(globalQMap.getKey(), expandedQuery.get(globalQMap.getKey()) + " " + additionalQuery);
//        }
//
//        return   ReRank(expandedQuery);
//    }
//
//    private Map<String,Map<String,Container >> QEWithEntityTextAndID() {
//        // Query text + Entity Text + Entity ID
//
//        int i2 = 1;
//        Map<String, String> expandedQuery = query;
//        for (Map.Entry<String, HashMap<String, EntityContainer>> globalQMap : myGlobalMap.entrySet()) {
//
//
//            int numberOfReturnedEntity = searchcommand.getNumberOfReturnedEntity();
//
//            // return max entity (the top entity)
//            List<EntityContainer> candidateEntityList = getMaxEntityCount(globalQMap.getValue(), numberOfReturnedEntity + 1);
//
//            int iq = 0;
//            String additionalQuery = "";
//            for (EntityContainer candidateEntity : candidateEntityList) {
//                if (iq != 0) { // do not take the first entity which is ""
//
//                    additionalQuery = additionalQuery + " " + candidateEntity.getEntityVal() + " " + candidateEntity.getEntityId() ;
//
//                    additionalQuery = additionalQuery.replaceAll("enwiki:" , "QQQQ").replaceAll(":","").replaceAll("QQQQ","enwiki:" );
//
//                    if (searchcommand.getisVerbose()) {
//                        System.out.println("\n" + i2 + " query " + globalQMap.getKey() + " the top Entity " + candidateEntity.getEntityId() + " --- Value ---- " + candidateEntity.getEntityVal() + " with count " + candidateEntity.getCount());
//                    }
//                }
//                iq++;
//            }
//            i2++;
//
//            expandedQuery.replace(globalQMap.getKey(), expandedQuery.get(globalQMap.getKey()) + " " + additionalQuery);
//        }
//        return ReRank(expandedQuery);
//    }
//
//    private Map<String,Map<String,Container >> QEOnlyEntityIDInEntityIDField() {
//        //Only Entity ID In Entity ID Field
//        int i2 = 1;
//        Map<String, String> expandedQuery = query;
//        for (Map.Entry<String, HashMap<String, EntityContainer>> globalQMap : myGlobalMap.entrySet()) {
//
//
//            int numberOfReturnedEntity = searchcommand.getNumberOfReturnedEntity();
//
//            // return max entity (the top entity)
//            List<EntityContainer> candidateEntityList = getMaxEntityCount(globalQMap.getValue(), numberOfReturnedEntity + 1);
//
//            int iq = 0;
//            String additionalQuery = "";
//            for (EntityContainer candidateEntity : candidateEntityList) {
//                if (iq != 0) { // do not take the first entity which is ""
//
//                    additionalQuery =  candidateEntity.getEntityId() ;
//
//                    additionalQuery = additionalQuery.replaceAll("enwiki:" , "QQQQ").replaceAll(":","").replaceAll("QQQQ","enwiki:" );
//
//                    if (searchcommand.getisVerbose()) {
//                        System.out.println("\n" + i2 + " query " + globalQMap.getKey() + " the top Entity " + candidateEntity.getEntityId() + " --- Value ---- " + candidateEntity.getEntityVal() + " with count " + candidateEntity.getCount());
//                    }
//                }
//                iq++;
//            }
//            i2++;
//
//            expandedQuery.replace(globalQMap.getKey(), expandedQuery.get(globalQMap.getKey()) + " " + additionalQuery);
//        }
//        return ReRank(expandedQuery);
//    }
//
//    private String prepareEntityText (String entityText){
//        // if take out \n will get exception so there is new line in the entity
//        if (entityText.indexOf('\n') > 0) {
//            entityText = entityText.substring(0, entityText.indexOf('\n'));
//        }
//        return entityText.replaceAll("'s", "").replaceAll("\\'","").replaceAll("'","").replaceAll("\n"," ");
//    }
//
//    public int returnDBPediaEntityCount(String s1,String s2) {
//        int count = 0;
//        try {
//
//            if (s1 !="" && s2 !="" ) {
//                String sparqlQueryString1 =
//                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
//                                "PREFIX owl: <http://dbpedia.org/ontology/>" +
//                                "SELECT ?data ?name ?abstract " +
//                                "WHERE {{  ?data foaf:name ?name. ?data owl:abstract ?abstract." +
//                                "FILTER(langMatches(lang(?abstract),'en')). " +
//                                "FILTER(contains(lcase(?abstract),lcase('" + s1 + "'))). " +
//                                "FILTER(contains(lcase(?abstract),lcase('" + s2 + "'))).} " +
//                                "UNION { ?data foaf:name ?name.  FILTER(langMatches(lang(?name),'en')). " +
//                                "FILTER(contains(lcase(?name),lcase('" + s1 + "'))). " +
//                                "FILTER(contains(lcase(?name),lcase('" + s2 + "'))).}}" ;
//
//
//                Query query = QueryFactory.create(sparqlQueryString1, Syntax.syntaxSPARQL_11);
//                QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
//                ResultSet results = qexec.execSelect();
//                ResultSetFormatter.out(System.out, results, query);
//                count = results.getRowNumber();
//                if (count > 0) {
//                    int a = 0;
//                }
//                qexec.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return count;
//    }
//
//    public Map<String, Map<String, Container>> doQueryExpansionWithDBpedia() {
//        //PrintUtils.displayMap(bm25.getRanking(query));
//
//        myEntityMap = new HashMap<>();
//        myGlobalMap = new HashMap<>();
//
//        String qID = "";
//        EntityContainer entity = null;
//        int i = 0;
//        for (Map.Entry<String, String> qMain : query.entrySet()) {
//
//            if (searchcommand.getisVerbose()) {
//                System.out.println(i + " of " + query.entrySet().size());
//            }
//
//            i++;
//            String Query = qMain.getValue();
//
//            // step 1 Get the first paragraph by BM25
//            Map<String, Container> temp = bm25.getRanking(Query);
//            myEntityMap = new HashMap<>();
//
//            // create sparql Query and return the result count
//            for (Map.Entry<String, Container> q3 : temp.entrySet()) {
//
//                EntityContainer e = q3.getValue().getEntity();
//                if (qMain.getKey() != qID) {
//                    // new Query
//                    if (e.getEntityId() != " ") {
//                        entity = e;
//                        int a = 0;
//                        if (e.getEntityVal() != "" ) {
//                            a += returnDBPediaEntityCount(prepareEntityText(Query) , prepareEntityText( e.getEntityVal()));
//                        }
//                        entity.setCount(a);
//                        myEntityMap.put(e.getEntityId(), entity);
//                        myGlobalMap.put(qMain.getKey(), myEntityMap);
//                    }
//                } else {
//                    // old Query
//                    if (e.getEntityId() != " ") {
//                        entity = myEntityMap.get(e.getEntityId());
//                        if (entity == null) {
//                            entity = e;
//                            int a = 0;
//                            if (e.getEntityVal() != "" ) {
//                                a += returnDBPediaEntityCount(prepareEntityText(Query) , prepareEntityText( e.getEntityVal()));
//                            }
//
//                            entity.setCount(a);
//                            myEntityMap.put(e.getEntityId(), entity);
//                        } else {
//                            int a = 0;
//                            if (e.getEntityVal()!="") {
//                                a += returnDBPediaEntityCount(prepareEntityText(Query) , prepareEntityText( e.getEntityVal()));
//                            }
//                            entity.setCount(entity.getCount() + a);
//                            myEntityMap.replace(e.getEntityId(), entity);
//                        }
//                        myGlobalMap.replace(qMain.getKey(), myEntityMap);
//                    }
//                }
//                qID = qMain.getKey();
//            }
//        }
//        int i2 = 1;
//
//        // step 3-	expand the original query with top (n) appeared entity in the returned dbpedia result.
//        Map<String, String> expandedQuery = query;
//        for (Map.Entry<String, HashMap<String, EntityContainer>> globalQMap : myGlobalMap.entrySet()) {
//
//
//            int numberOfReturnedEntity = searchcommand.getNumberOfReturnedEntity();
//
//            // return max entity (the top entity) top (n) appeared entity in the returned dbpedia result
//            List<EntityContainer> candidateEntityList = getMaxEntityCount(globalQMap.getValue(), numberOfReturnedEntity + 1);
//
//            int iq = 0;
//            String additionalQuery = "";
//            for (EntityContainer candidateEntity : candidateEntityList) {
//                if (iq != 0) { // do not take the first entity which is ""
//
//                    //expand the original query with top (n)
//                    additionalQuery = additionalQuery + " " + candidateEntity.getEntityVal();
//
//                    if (searchcommand.getisVerbose()) {
//                        System.out.println("\n" + i2 + " query " + globalQMap.getKey() + " the top Entity " + candidateEntity.getEntityId() + " --- Value ---- " + candidateEntity.getEntityVal() + " with count " + candidateEntity.getCount());
//                    }
//                }
//                iq++;
//            }
//            i2++;
//
//            expandedQuery.replace(globalQMap.getKey(), expandedQuery.get(globalQMap.getKey()) + " " + additionalQuery);
//        }
//
//        // step 4 use BM25 is run on the expanded query.
//        return ReRank(expandedQuery);
//
//    }
//
//    private List<EntityContainer> getMaxEntityCount(Map<String, EntityContainer> entityMap, int numberOfEntityReturn) {
//        // do not care of the entity that appear one time.
//        int max = 1;
//        List<EntityContainer> tempMaxEntity = new ArrayList<EntityContainer>();
//        EntityContainer tempEntity = null;
//
//        for (int i = 0; i < numberOfEntityReturn ; i++) {
//            for (Map.Entry<String, EntityContainer> iEntity : entityMap.entrySet()) {
//                if (tempMaxEntity.size() > 0) {
//                    if (iEntity.getValue().getCount() > max && iEntity.getValue().getCount() < tempMaxEntity.get(tempMaxEntity.size() - 1).getCount()) {
//                        if (iEntity.getValue().getEntityVal() != " " && iEntity.getValue().getCount() != 1) {
//                            max = iEntity.getValue().getCount();
//                            tempEntity = iEntity.getValue();
//                        }
//                    }
//                }else {
//                    if (iEntity.getValue().getCount() > max) {
//                        if (iEntity.getValue().getEntityVal() != " " && iEntity.getValue().getCount() != 1) {
//                            max = iEntity.getValue().getCount();
//                            tempEntity = iEntity.getValue();
//                        }
//                    }
//                }
//            }
//            if (max > 1) {
//                max = 1;
//                tempMaxEntity.add(tempEntity);
//                tempEntity = null;
//            }
//        }
//        return tempMaxEntity;
//    }
//
//    public Map<String,Map<String,Container >> ReRank(Map<String,String> inputQuery){
//
//        Map<String,Map<String,Container >> result = new LinkedHashMap<String,Map<String,Container>>();
//        for(Map.Entry<String,String> q: inputQuery.entrySet())
//        {
//            String Query = q.getValue();
//            Map<String, Container> BM25Val = bm25.getRanking(Query);
//            Map<String, Container> sortedMap = SortUtils.sortByValue(BM25Val);
//            result.put(q.getKey(),sortedMap);
//        }
//
//        String mname = "doc_reranking_With_QueryExpansion"+"_k"+this.searchcommand.getkVAL()+"_top"+searchcommand.getNumberOfReturnedEntity() +"_ExpansionType_"+searchcommand.getQEType() + "_Spam_filter_enable" + RegisterCommands.CommandSearch.isSpamFilterEnabled() + "_ISspecialCharSpamEnabled_" +  main.java.commandparser.RegisterCommands.CommandSearch.isSpecialCharSpamFilterEnabled() ;
//
//        RunWriter.writeRunFile(mname,result);
//
//        if(searchcommand.getisVerbose())
//        {
//            PrintUtils.displayMap(result);
//        }
//        return result;
//    }
//}
