package main.java.queryexp.rm3;

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.containers.DocStats;
import main.java.queryexp.ExpandQuery;
import main.java.queryexp.ExpandQueryBase;
import main.java.searcher.BaseBM25;
import main.java.utils.PreProcessor;
import main.java.utils.RunWriter;
import main.java.utils.SortUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class RelevanceModel3 extends ExpandQueryBase implements ExpandQuery {


    private RegisterCommands.CommandSearch SearchCommand = null;
    private Map<String, String> query = null;


    public RelevanceModel3(RegisterCommands.CommandSearch searchCommand, Map<String, String> query) {
        super(searchCommand, query, false);
        this.SearchCommand = searchCommand;
        this.query = query;
    }


    @Override
    public void doQueryExpansion() {
        Map<String, Map<String, Container>> res = performQueryExpansion();
        String fname = getFileSuffix("RM3");
        RunWriter.writeRunFile(fname, res);
    }

    @Override
    public void doQueryExpansion(Map<String, Map<String, Container>> input) {

    }


    @Override
    public Map<String, Map<String, Container>> getExpandedQuery() {
        return performQueryExpansion();
    }

    private Double getDistributionScore(ArrayList<DocStats> docStats) {
        Double score = 0.0;

        for (DocStats stats : docStats) {
            score += stats.getScore();
        }
        return score;
    }

    /**
     * This builds the document stats , each DocStats holds the all the information about the Document
     *
     * @param retrievedList
     * @return
     */
    private ArrayList<DocStats> collectDocumentStats(Map<String, Container> retrievedList) {
        ArrayList<DocStats> docStats = new ArrayList<>();
        BaseBM25 bm = null;
        try {
            bm = new BaseBM25(SearchCommand.getkVAL(), SearchCommand.getIndexlocation());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int count = 0;
        for (Map.Entry<String, Container> val : retrievedList.entrySet()) {
            count++;
            String id = val.getKey();
            Container c = val.getValue();
            String content = bm.getDocument(c.getDocID());
            docStats.add(new DocStats(id, c, content));

            if (count == SearchCommand.getPrfVAL()) {
                break;
            }
        }
        return docStats;
    }

    private double getProbabilityPerTerm(String term, ArrayList<DocStats> docStats, Double distributeScore) {

        double score = 0.0;

        for (DocStats stats : docStats) {

            Double termgivendoc = stats.getProbabilityTerm(term);
            Double docDis = (stats.getScore() / distributeScore);
            //score += stats.getProbabilityTerm(term) * (stats.getScore() / distributeScore);
            score += termgivendoc * docDis;
        }
        return score;
    }

    private Map<String, Double> getProbability(ArrayList<DocStats> docStats, ArrayList<String> candidateTerms) {
        Map<String, Double> relevantTerms = new LinkedHashMap<>();
        Double distributeScore = getDistributionScore(docStats);
        for (String term : candidateTerms) {
            relevantTerms.put(term, getProbabilityPerTerm(term, docStats, distributeScore));
        }
        return relevantTerms;
    }

    private ArrayList<String> getRm3ExpansionTerms(Map<String, Double> unsorted, String orig) {
        ArrayList<String> res = null;

        try {
            res = PreProcessor.processTermsUsingLuceneStandard(orig);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int counter = 0;

        for (Map.Entry<String, Double> val : SortUtils.sortByValue(unsorted).entrySet()) {
            counter++;

            res.add(val.getKey());
            if (counter == SearchCommand.getPrfValTermsKterms()) {
                break;
            }
        }
        return res;
    }

    @Override
    public String getExpandedTerms(String originalQuery, Map<String, Container> retrievedList) {
        ArrayList<DocStats> docStats = collectDocumentStats(retrievedList);
        ArrayList<String> candidates = getCandidateTerms(retrievedList);
        Map<String, Double> ans = getProbability(docStats, candidates);
        ArrayList<String> expansionterms = getRm3ExpansionTerms(ans, originalQuery);

        return ArrayListInToString(expansionterms);
    }
}
