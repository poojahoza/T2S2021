//package main.java.dbpedia;
//
//import main.java.commandparser.RegisterCommands;
//import main.java.containers.Container;
//import main.java.containers.EntityContainer;
//import main.java.searcher.BaseBM25;
//import main.java.utils.WriteFile;
//import org.apache.jena.query.*;
//import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class DBpedia {
//
//    private BaseBM25 bm25 = null;
//    private RegisterCommands.CommandSearch searchcommand = null;
//    private Map<String, String> query = null;
//    private HashMap<String, EntityContainer> myEntityMap = null;
//    private HashMap<String, HashMap<String, EntityContainer>> myGlobalMap = null;
//
//    public DBpedia(RegisterCommands.CommandSearch searchcommand, Map<String, String> query) {
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
//    private String prepareEntityText (String entityText){
//        // if take out \n will get exception so there is new line in the entity
//        if (entityText.indexOf('\n') > 0) {
//            entityText = entityText.substring(0, entityText.indexOf('\n'));
//        }
//        return entityText.replaceAll("'s", "").replaceAll("\\'","").replaceAll("'","").replaceAll("\n"," ");
//    }
//
//    public Map<String,Map<String,Container>> doRetriveRowsFromDBpedia() {
//        //PrintUtils.displayMap(bm25.getRanking(query));
//
//        HashMap<String,Map<String,Container>> QDBpedia = new HashMap<>();
//        int i =0 ;
//        for (Map.Entry<String, String> qMain : query.entrySet()) {
//
//            if (searchcommand.getisVerbose()) {
//                System.out.println(i + " of " + query.entrySet().size());
//            }
//            i++;
//            String Query = qMain.getValue();
//            Map<String,Container> a  = new HashMap<>();
//            if (Query!= "" ) {
//                a = getListOfRelatedRow(prepareEntityText(Query));
//            }
//
//            System.out.println(i + " of " + Query + " found " + ((Container) a.values()).getEntity().getCount() );
//
//            QDBpedia.put(Query, a);
//
//        }
//
//        WriteFile write_file = new WriteFile();
//        write_file.generateBM25RunFile(QDBpedia, "TracDBpediaequalExactName");
//        // step 4 use BM25 is run on the expanded query.
//
//        return QDBpedia;
//
//    }
//
//    public void doRetriveRelationFromDBpedia() {
//        //PrintUtils.displayMap(bm25.getRanking(query));
//
//        HashMap<String,String> QDBpedia = new HashMap<>();
//        int i =0 ;
//        for (Map.Entry<String, String> qMain : query.entrySet()) {
//
//            if (searchcommand.getisVerbose()) {
//                System.out.println(i + " of " + query.entrySet().size());
//            }
//            i++;
//            String queryValue = qMain.getValue();
//            ArrayList<String> a = new ArrayList<>();
//            if (queryValue!= "" ) {
//                a = returnDBPediaRelationBetweenEntities(prepareEntityText(queryValue),prepareEntityText(queryValue));
//                // a = returnDBPediaRelationBetweenEntities(prepareEntityText("Ketolide"),prepareEntityText("Aminocoumarin"));
//
//            }
//            for (i=0;i<a.size();i++) {
//                System.out.println(queryValue + " found " + i+1 + " -- relation  " + a.get(i) );
//            }
//
//            QDBpedia.put(queryValue,""+ a.size());
//
//        }
//
//        WriteFile write_file = new WriteFile();
//        write_file.generateFile(QDBpedia, "TracDBpediaequalExactName");
//
//
//    }
//
//    public ArrayList<String> returnDBPediaRelationBetweenEntities(String s1,String s2) {
//        ArrayList<String> sResult =new ArrayList<String>();
//
//        try {
//            if (s1 !="" && s2 !="" ) {
//                String sparqlQueryString1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
//                        "SELECT  distinct ?type1 " +
//                        "WHERE { ?data  rdfs:label ?label1. ?data rdf:type ?type1.   FILTER contains(lcase(str(?label1)),'" + s1.toLowerCase()  + "'). }";
//
//                Query query = QueryFactory.create(sparqlQueryString1);
//                //QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
//
//
//                QueryEngineHTTP objectToExec = QueryExecutionFactory.createServiceRequest("http://dbpedia.org/sparql", query);
//
//                objectToExec.addParam("timeout","3000");
//                ResultSet results = objectToExec.execSelect();
//                List<QuerySolution> s = ResultSetFormatter.toList(results);
//                ResultSetFormatter.out(System.out, results, query);
//
//                sparqlQueryString1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
//                        "SELECT distinct ?type1 " +
//                        "WHERE {?data  rdfs:label ?label1. ?data rdf:type ?type1.  FILTER contains(lcase(str(?label1)),'" + s2.toLowerCase()  + "'). }";
//
//                query = QueryFactory.create(sparqlQueryString1);
//
//                objectToExec = QueryExecutionFactory.createServiceRequest("http://dbpedia.org/sparql", query);
//                objectToExec.addParam("timeout","3000");
//                results = objectToExec.execSelect();
//                List<QuerySolution> s22 = ResultSetFormatter.toList(results);
//                ResultSetFormatter.out(System.out, results, query);
//
//                for (int i = 0; i< s.size()  ; i++){
//                    String relation  = s.get(i).get("type1").toString();
//                    if (sResult.contains(relation)==false ){
//                        for (int y = 0;y < s22.size()  ; y++) {
//                            if (s22.get(y).get("type1").toString().equals(relation)) {
//                                sResult.add(relation);
//                                continue;
//                            }
//                        }
//                    }
//                }
//                objectToExec.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return sResult;
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
//    public int returnDBPediaEntityCount(String s1) {
//        int count = 0;
//        try {
//
//            if (s1 !="") {
//                String sparqlQueryString1 =
//                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
//                                "PREFIX owl: <http://dbpedia.org/ontology/>" +
//                                "SELECT ?data ?name ?abstract " +
//                                "WHERE {{  ?data foaf:name ?name. ?data owl:abstract ?abstract." +
//                                "FILTER(langMatches(lang(?abstract),'en')). " +
//                                "FILTER(contains(lcase(?abstract),lcase('" + s1 + "'))).} " +
//                                "UNION { ?data foaf:name ?name.  FILTER(langMatches(lang(?name),'en')). " +
//                                "FILTER(contains(lcase(?name),lcase('" + s1 + "'))).}}" ;
//
//
//                Query query = QueryFactory.create(sparqlQueryString1, Syntax.syntaxSPARQL_11);
//                QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
//                ResultSet results = qexec.execSelect();
//                ResultSetFormatter.out(System.out, results, query);
//                //           System.out.println(s);
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
//    public HashMap<String,Container>  getListOfRelatedRow(String s1) {
//        HashMap<String,Container> tds = new HashMap<>();
//        try {
//            if (s1 !="") {
//                String sparqlQueryString1 =
//                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
//                                "PREFIX owl: <http://dbpedia.org/ontology/>" +
//                                "SELECT ?data ?name " +
//                                "WHERE {{  ?data foaf:name ?name.  FILTER(langMatches(lang(?name),'en')). " +
//                                "FILTER(contains(lcase(str(?name)), '" + s1.toLowerCase()  + "')).}}" ;
//
//                Query query = QueryFactory.create(sparqlQueryString1, Syntax.syntaxSPARQL_11);
//                QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
//                ResultSet results = qexec.execSelect();
//                List<QuerySolution> s = ResultSetFormatter.toList(results);
//                ResultSetFormatter.out(System.out, results, query);
//                for (int i = 0; i< s.size()  ; i++){
//                    EntityContainer ec = new EntityContainer(s.get(i).get("name").toString(),s.get(i).get("data").toString());
//                    ec.setCount(s.size());
//                    Container c = new Container(0.0,0 );
//                    c.addEntityContainer(ec);
//                    tds.put(s1 + "-" + i,c);
//                }
//                qexec.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return tds;
//    }
//
//    public int returnDBPediaEntityCountNameContain(String s1) {
//        HashMap<String,EntityContainer> tds = new HashMap<>();
//        int count = 0;
//        try {
//            if (s1 !="") {
//
//                String sparqlQueryString1 =
//                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
//                                "PREFIX owl: <http://dbpedia.org/ontology/>" +
//                                "SELECT ?data ?name ?abstract " +
//                                "WHERE {{  ?data foaf:name ?name.  FILTER(langMatches(lang(?name),'en')). " +
//                                "FILTER(contains(lcase(str(?name)), '" + s1.toLowerCase()  + "')).}}" ;
//                Query query = QueryFactory.create(sparqlQueryString1, Syntax.syntaxSPARQL_11);
//                QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
//                ResultSet results = qexec.execSelect();
//
//                ResultSetFormatter.out(System.out, results, query);
//                count = results.getRowNumber();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return count;
//    }
//
//    public int returnDBPediaEntityCountName(String s1) {
//        HashMap<String,EntityContainer> tds = new HashMap<>();
//        int count = 0;
//        try {
//            if (s1 !="") {
//
//                String sparqlQueryString1 =
//                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
//                                "PREFIX owl: <http://dbpedia.org/ontology/>" +
//                                "SELECT ?data ?name  " +
//                                "WHERE {{   ?data  foaf:name  ?name. " +
//                                "filter( lcase(str(?name)) =   '" + s1.toLowerCase() + "').  FILTER(langMatches(lang(?name),'en')).}}" ;
//                Query query = QueryFactory.create(sparqlQueryString1, Syntax.syntaxSPARQL_11);
////                QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
////                ResultSet results = qexec.execSelect();
////                ResultSetFormatter.out(System.out, results, query);
//
//
//
//                QueryEngineHTTP objectToExec = QueryExecutionFactory.createServiceRequest("http://dbpedia.org/sparql", query);
//
//                objectToExec.addParam("timeout","3000");
//                ResultSet results = objectToExec.execSelect();
//                ResultSetFormatter.out(System.out, results, query);
//
//                count = results.getRowNumber();
//                // qexec.close();
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return count;
//    }
//
//    public void retriveExistanceinDBpeda(boolean conltinName) {
//
//        HashMap<String,String> QDBpedia = new HashMap<>();
//        int i =0 ;
//        for (Map.Entry<String, String> qMain : query.entrySet()) {
//
//            if (searchcommand.getisVerbose()) {
//                System.out.println(i + " of " + query.entrySet().size());
//            }
//            i++;
//            String queryValue = qMain.getValue();
//            int a = 0;
//            if (queryValue!= "" ) {
//                if (conltinName) {
//                    a= returnDBPediaEntityCountNameContain(prepareEntityText(queryValue));
//                }else
//                {
//                    a = returnDBPediaEntityCountName(prepareEntityText(queryValue));
//                }
//                System.out.println(i + " of " + queryValue + " found " + a);
//            }
//
//
//            QDBpedia.put(queryValue,""+a);
//
//       }
//
//        String mname = "entity_Exist_in_DBpedia"+ "_Contain_" + conltinName + "_Spam_filter_enable_" + RegisterCommands.CommandSearch.isSpamFilterEnabled() + "_k"+this.searchcommand.getkVAL() + "_top"+searchcommand.getNumberOfReturnedEntity();
//        WriteFile write_file = new WriteFile();
//        write_file.generateFile(QDBpedia, mname);
//    }
//}
