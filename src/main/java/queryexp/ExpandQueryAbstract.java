package main.java.queryexp;

import main.java.commandparser.RegisterCommands;
import main.java.containers.Container;
import main.java.searcher.BaseBM25;
import main.java.utils.CorpusStats;
import main.java.utils.RunWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

public class ExpandQueryAbstract extends ExpandQueryBase implements ExpandQuery {

    private RegisterCommands.CommandSearch SearchCommand = null;
    private Map<String, String> query = null;

    /**
     * Constructor calls super class to initialize the value
     *
     * @param searchCommand
     * @param query
     */
    public ExpandQueryAbstract(RegisterCommands.CommandSearch searchCommand, Map<String, String> query) {
        super(searchCommand, query,true);
        this.SearchCommand = searchCommand;
        this.query = query;
    }

    @Override
    public void doQueryExpansion() {
        Map<String, Map<String, Container>> res = performQueryExpansion();
        String fname = getFileSuffix("Entity_Abstract");
        RunWriter.writeRunFile(fname, res);

    }

    @Override
    public void doQueryExpansion(Map<String, Map<String, Container>> input) {
        Map<String, Map<String, Container>> res = new LinkedHashMap<>();

        StreamSupport.stream(input.entrySet().spliterator(), SearchCommand.isParallelEnabled())
                .forEach(q -> {
                    try {
                        BaseBM25 bm = new BaseBM25(SearchCommand.getkVAL(),SearchCommand.getIndexlocation());
                        Map<String, Container> retvalue = q.getValue();
                        String expandedTerms = getExpandedTerms(getQuery(q.getKey()), retvalue);
                        Map<String, Container> expanded = bm.getRanking(expandedTerms);
                        res.put(q.getKey(), expanded);
                        System.out.print(".");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        String fname = getFileSuffix("IDF_ENTITY_ABSTRACT_RE_RANK");
        RunWriter.writeRunFile(fname, res);

    }

    @Override
    public Map<String, Map<String, Container>> getExpandedQuery() {
        return performQueryExpansion();
    }

    @Override
    public String getExpandedTerms(String originalQuery, Map<String, Container> retrievedList) {
        ArrayList<String> candidates = getCandidateTermsWithEntitiesAbstract(retrievedList);
        ArrayList<String> top = getSemanticTerms(originalQuery, candidates);
        CorpusStats cs = new CorpusStats(SearchCommand.getIndexlocation());
        ArrayList<String> topDF = null;
        try {
            topDF = cs.getIDFStandardAnalyzer(top);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> finalTerms = getTopK(topDF, originalQuery);
        return ArrayListInToString(finalTerms);
    }
}
