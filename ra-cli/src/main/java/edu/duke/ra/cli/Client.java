package edu.duke.ra.cli;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.sql.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.args4j.CmdLineException;

import edu.duke.ra.core.RA;
import edu.duke.ra.core.RAConfig;
import edu.duke.ra.core.RAConfigException;
import edu.duke.ra.core.result.Column;
import edu.duke.ra.core.result.IQueryResult;
import edu.duke.ra.core.util.PrettyPrinter;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

public class Client {
    public static final String welcomeMessage = ""
            + "\n"
            + "RA: an interactive relational algebra interpreter" + "\n"
            + "Version " + "3.0" + " by Jun Yang (junyang@cs.duke.edu)" + "\n"
            + "http://www.cs.duke.edu/~junyang/ra/" + "\n"
            + "Type \"\\help;\" for help" + "\n"
            + "\n";

    protected PrintStream out;
    protected PrintStream err;
    protected PrintStream prompt;
    protected ConsoleReader reader;
    protected RAConfig raConfig;
    protected RA ra;
    protected PrintStream log;

    public Client(ClientConfig config) throws IOException, SQLException{
        out = config.out();
        err = config.err();
        prompt = System.out;
        reader = config.reader();
        setCompletions();
        raConfig = config.raConfig();
        ra = new RA(raConfig);
        if (config.log() != null) {
            log = new PrintStream(config.log());
        }
        else {
            log = null;
        }
    }

    void setCompletions() {
        reader.addCompleter(new StringsCompleter(new String [] {
                "\\help;", "\\quit;", "\\list;", "\\sqlexec_{",
                "\\select_{", "\\project_{", "\\join", "\\join_{", "\\rename_{",
                "\\cross", "\\union", "\\diff", "\\intersect"
        }));
    }

    protected String getPrompt(int line){
        return (line == 1)? "ra> " : "" + line + "> ";
    }
 
    protected void exit() {
        prompt.println("Bye!");
        prompt.println();
        exit(0);
        return;
    }

    protected void exit(int code) {
        ra.closeDBConnection();
        System.exit(code);
    }

    /*
    protected void skipInput() {
        try {
            (new BufferedReader(new InputStreamReader(in))).readLine();
        } catch (IOException e) {
            err.println("Unexpected I/O error:");
            err.println(e.toString());
            err.println();
            exit(1);
        }
    }
    */

    public void start(){
        prompt.print(welcomeMessage);
        while (true) {
            String line = getLine();
            if (log != null) {
                log.print(line);
            }
            IQueryResult result = ra.query(line);
            String resultString = result.toJsonString();
            JSONObject resultJson = new JSONObject(resultString);
            JSONArray errors = resultJson.getJSONArray("errors");
            if (errors.length() != 0) {
                printErrors(errors);
            }
            else {
                printResult(resultString);
            }
            if (resultJson.getBoolean("quit")) {
                break;
            }
        }
    }

    String getLine(){
        int lineNumber = 1;
        String line = "";
        while (!line.contains(";")) {
            reader.setPrompt(getPrompt(lineNumber));
            try {
                // .readLine() returns null if EOF has been reached
                String toAdd = reader.readLine();
                if (toAdd == null){
                    break;
                }
                line += toAdd;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            lineNumber++;
        }
        line += "\n";
        return line;
    }

    void printErrors(JSONArray errors) {
        for (int i = 0; i < errors.length(); i++){
            JSONObject error = errors.getJSONObject(i);
            String errorMessage = error.getString("description") + ":\n";
            if (error.has("message")) {
                errorMessage += error.getString("message") + "\n";
            }
            if (error.has("details")) {
                errorMessage += error.getString("details") + "\n";
            }
            err.print(errorMessage);
        }
    }

    void printResult(String resultString) {
        JSONObject resultJson = new JSONObject(resultString);
        JSONObject dataSection = resultJson.getJSONObject("data");
        String resultType = dataSection.keys().next();
        switch (resultType) {
            case "tuples":
                List<Column> schema = PrettyPrinter.createSchemaFromJson(resultString);
                List<List<String>> entries = PrettyPrinter.createTuplesFromJson(resultString);
                out.print(PrettyPrinter.printTuples(schema, entries));
                break;
            case "relations":
                List<String> relations = PrettyPrinter.createRelationsFromJson(resultString);
                out.print(PrettyPrinter.printRelations(relations));
                break;
            case "text":
                out.print(dataSection.getString("text"));
                break;
        }
    }

    public static void main(String[] args) {
        ClientConfig config = new ClientConfig(args);
        if (config.help() || ! config.isValid()) {
            config.usage();
            return;
        }
        try {
            Client client = new Client(config);
            client.start();
        } catch (IOException | SQLException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
