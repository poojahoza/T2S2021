package main.java.commandparser;

import com.beust.jcommander.JCommander;


/*
This class creates the Commands such as index and search.
index and search is added as sub commands.
*/
public class CommandParser
{
    private  JCommander  parse = null;
    private RegisterCommands.CommandIndex index = null;
    private RegisterCommands.CommandSearch search = null;
    private RegisterCommands.CommandHelp helpc = null;
    private String[] argslist = null;

    public CommandParser(String  ... args)
    {
        index = new RegisterCommands.CommandIndex();
        search =new RegisterCommands.CommandSearch();
        helpc = new RegisterCommands.CommandHelp();
        argslist = args;
        parse = createParser();
    }

    private JCommander createParser()
    {
        if(parse == null)
        {
            parse = JCommander.newBuilder().addCommand("index",index).addCommand("search",search).addCommand("--help",helpc).build();
            parse.parse(argslist);
        }
        return parse;
    }

    public JCommander getParser()
    {
        return parse;
    }

    public RegisterCommands.CommandIndex getIndexCommand() { return index; }

    public RegisterCommands.CommandSearch getSearchCommand()
    {
        return search;
    }

}