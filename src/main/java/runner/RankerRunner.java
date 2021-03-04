package main.java.runner;

import main.java.commandparser.CommandParser;
import main.java.commandparser.RegisterCommands;
import main.java.mrf.MrfHelper;

public class RankerRunner implements ProgramRunner {

    private RegisterCommands.Ranker ranker = null;

    public RankerRunner(CommandParser cmd)
    {
        ranker = cmd.getRankerCommand();
    }

    @Override
    public void run() {
        MrfHelper.createRunFileFromWeights(ranker.getModelFile(),ranker.getRunfile(),ranker.getMname());
    }
}
