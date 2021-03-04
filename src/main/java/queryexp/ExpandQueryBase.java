package main.java.queryexp;

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.containers.DBContainer;
import main.java.containers.EntityContainer;
import main.java.database.databaseWrapper;
import main.java.reranker.WordEmbeddingExtended;
import main.java.rerankerv2.concepts.EmbeddingStrategy;
import main.java.searcher.BaseBM25;

import main.java.utils.CorpusStats;
import main.java.utils.PreProcessor;

import main.java.utils.SortUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.StreamSupport;


/**
 * All query expansion methods extends the abstract class
 * and implement how to get new terms for the queries
 * This base class provides the common functions for all query
 * expansion method
 */

abstract public class ExpandQueryBase {

    private RegisterCommands.CommandSearch SearchCommand = null;
    private Map<String, String> query = null;
    protected EmbeddingStrategy embedding = null;

    /**
     * All the query expansion methods should implement this method
     * to find new terms for the given query
     *
     * @param originalQuery
     * @param retrievedList
     * @return Expanded query
     */
    abstract public String getExpandedTerms(String originalQuery, Map<String, Container> retrievedList);


    /**
     * Constructor, this will be called by the sub class
     *
     * @param searchCommand
     * @param query
     */
    public ExpandQueryBase(RegisterCommands.CommandSearch searchCommand, Map<String, String> query, boolean isLoad) {
        this.SearchCommand = searchCommand;
        this.query = query;
        if (isLoad) {
            embedding = new WordEmbeddingExtended(SearchCommand.getDimension(), SearchCommand.getWordEmbeddingFile());
        }

    }

    /**
     * Helper function to display all candidates
     *
     * @param candidates
     */
    protected void dumpCandidates(ArrayList<String> candidates) {
        for (String str : candidates) {
            System.out.println(str);
        }
    }

    protected String getFileSuffix(String mname) {
        return mname + "_" + (SearchCommand.isSectionEnabled() ? "section_" : "article_") + "d" + SearchCommand.getDimension()
                + "_" + "prf" + SearchCommand.getPrfVAL() + "_" + "prfval" + SearchCommand.getPrfValTermsKterms() + "_" + "k" + SearchCommand.getkVAL()+"_"
                +(RegisterCommands.CommandSearch.isSpecialCharSpamFilterEnabled() ? "spam":"_no_spam");

    }

    private ArrayList<String> checkForNumerics(ArrayList<String> candidates) {
        ArrayList<String> terms = new ArrayList<>();
        for (String candidate : candidates) {
            if (!(candidate.matches("[0-9]+") && candidate.length() >= 1)) {
                terms.add(candidate);
            }
        }
        return terms;
    }

    protected String getQuery(String qid) {
        return query.get(qid);
    }


    /**
     * Get the candidate terms from the K dodcuments, remove the stop words and process with standard analyzer.
     *
     * @param retrievedList
     * @return ArrayList<String>
     */
    public ArrayList<String> getCandidateTerms(Map<String, Container> retrievedList) {
        int PRF_VAL = SearchCommand.getPrfVAL();
        int counter = 0;

        BaseBM25 bm25 = null;
        try {
            bm25 = new BaseBM25(SearchCommand.getkVAL(), SearchCommand.getIndexlocation());
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Container> doc : retrievedList.entrySet()) {
            counter++;
            int docID = doc.getValue().getDocID();
            String content = bm25.getDocument(docID);
            sb.append(content);
            sb.append(" ");
            if (counter == PRF_VAL) break;
        }

        ArrayList<String> candidates = null;
        try {
            candidates = PreProcessor.processTermsUsingLuceneStandard(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return checkForNumerics(candidates);
        return candidates;
    }

    private String[] getEntities(Container input) {
        EntityContainer e = input.getEntity();
        if (e.getEntityId().equals("")) {
            return null;
        }
        return e.getEntityId().split("[\r\n]+");
    }

    private String getEntitiesAbstract(Container container) {
        String[] entitiesID = getEntities(container);
        if (entitiesID == null) {
            return null;
        }
        databaseWrapper dbwrapper = new databaseWrapper();
        Map<String, DBContainer> res = dbwrapper.getRecordLeadTextContainer(entitiesID);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, DBContainer> val : res.entrySet()) {
            sb.append(val.getValue().getLeadtext());
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Returns the candidate terms considering each entity abstract
     *
     * @param retrievedList
     * @return
     */
    public ArrayList<String> getCandidateTermsWithEntitiesAbstract(Map<String, Container> retrievedList) {
        int PRF_VAL = SearchCommand.getPrfVAL();
        int counter = 0;

        BaseBM25 bm25 = null;
        try {
            bm25 = new BaseBM25(SearchCommand.getkVAL(), SearchCommand.getIndexlocation());
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Container> doc : retrievedList.entrySet()) {
            counter++;
            int docID = doc.getValue().getDocID();
            String content = bm25.getDocument(docID);
            sb.append(content);
            sb.append(" ");
            String abs = getEntitiesAbstract(doc.getValue());
            // System.out.println(abs);
            if (abs != null) {
                sb.append(abs);
            }

            if (counter == PRF_VAL) break;
        }

        ArrayList<String> candidates = null;
        try {
            candidates = PreProcessor.processTermsUsingLuceneStandard(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return checkForNumerics(candidates);
        return candidates;
    }

    /**
     * Returns the top K workds from the list along with the original query terms
     *
     * @param q
     * @param OriginalQueryTerms
     * @return
     */
    protected ArrayList<String> getTopK(ArrayList<String> q, String OriginalQueryTerms) {
        ArrayList<String> terms = null;
        try {
            terms = PreProcessor.processTermsUsingLuceneStandard(OriginalQueryTerms);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int count = 0;

        for (String str : q) {
            count++;
            terms.add(str);
            if (count == SearchCommand.getPrfValTermsKterms()) {
                break;
            }
        }
        return terms;
    }

    protected ArrayList<String> getTopK(Map<String, Double> q) {
        Map<String, Double> sorted = SortUtils.sortByValue(q);

        int c = 0;
        ArrayList<String> res = new ArrayList<>();

        for (Map.Entry<String, Double> s : sorted.entrySet()) {
            c++;
            res.add(s.getKey());
            if (c == SearchCommand.getPrfValTerms()) break;
        }
        return res;
    }

    /**
     * Returns the some of the nearest terms that are near to the query per query term
     * Based on the --prf-val-term option, it it returns that many nearest terms per query term
     *
     * @param orignalquery
     * @param candidates
     * @return
     */
    protected ArrayList<String> getSemanticTerms(String orignalquery, ArrayList<String> candidates) {

        /*
         * Process the original query using the standard Analyzer
         */
        ArrayList<String> qterms = null;
        CorpusStats cs = new CorpusStats(SearchCommand.getIndexlocation());
        try {
            qterms = PreProcessor.processTermsUsingLuceneStandard(orignalquery);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> topTerms = new ArrayList<>();

        /*
         * If the query terms is less than one and if there is no word embedding,
         * Trying to use some of the important candidate terms for query expansion
         */
        if (qterms.size() < 2 && embedding.getEmbeddingVector(qterms.get(0)) == null) {
            for (String str : candidates) {
                topTerms.add(str);
            }
            return topTerms;
        }

        /*
        Find all the new terms
         */

        for (String qterm : qterms) {
            Map<String, Double> perEachQuery = new LinkedHashMap<>();
            INDArray v1 = null;
            if (embedding.getEmbeddingVector(qterm) != null) {
                v1 = embedding.getEmbeddingVector(qterm);
            } else {
                continue;
            }

            for (String cterm : candidates) {
                if (embedding.getEmbeddingVector(cterm) != null && (!qterms.contains(cterm))) {
                    INDArray v2 = embedding.getEmbeddingVector(cterm);
                    double score = Transforms.cosineSim(v1, v2);
                    perEachQuery.put(cterm, score);
                }
            }

            if (!perEachQuery.isEmpty()) {
                ArrayList<String> temp = getTopK(perEachQuery);
                for (String s : temp) {
                    topTerms.add(s);
                }
            }
        }
        return topTerms;
    }

    protected String ArrayListInToString(ArrayList<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString();
    }


    protected String processedTerm(String content) throws IOException {
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("Text", new StringReader(content));
        try {
            tokenStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String token = null;
        while (tokenStream.incrementToken()) {
            token = tokenStream.getAttribute(CharTermAttribute.class).toString();
        }
        return token;
    }


    /**
     * Common method for all query expansion methods, this performs the query expansion and
     * returns the results. The same function will be called by all query expansion methods
     *
     * @return For Each query, its expanded results
     */

    protected Map<String, Map<String, Container>> performQueryExpansion() {
        Map<String, Map<String, Container>> res = new LinkedHashMap<>();
        StreamSupport.stream(query.entrySet().spliterator(), SearchCommand.isParallelEnabled())
                .forEach(q -> {
                    try {
                        BaseBM25 bm = new BaseBM25(SearchCommand.getkVAL(), SearchCommand.getIndexlocation());
                        Map<String, Container> bm25init = bm.getRanking(q.getValue());
                        String expandedTerms = getExpandedTerms(q.getValue(), bm25init);
                        Map<String, Container> expanded = bm.getRanking(expandedTerms);
                        res.put(q.getKey(), expanded);
                        System.out.print(".");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return res;
    }
}

