package edu.duke.ra.cli;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.sql.*;

import org.json.JSONObject;

import edu.duke.ra.core.RA;
import edu.duke.ra.core.RAConfig;
import edu.duke.ra.core.RAConfigException;
import edu.duke.ra.core.result.Column;
import edu.duke.ra.core.result.IQueryResult;
import edu.duke.ra.core.util.PrettyPrinter;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.CommonAST;
import antlr.collections.AST;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

public class Client {

    protected PrintStream out = System.out;
    protected PrintStream err = System.err;
    protected InputStream in = null;
    protected ConsoleReader reader = null;
    protected RA ra;

    protected void welcome() {
        out.println();
        out.println("RA: an interactive relational algebra interpreter");
        out.println("Version " + "3.0" +
                    " by Jun Yang (junyang@cs.duke.edu)");
        out.println("http://www.cs.duke.edu/~junyang/ra/");
        out.println("Type \"\\help;\" for help");
        out.println();
        return;
    }

    protected void usage() {
        out.println("Usage: ra [Options] [PROPS_FILE]");
        out.println("Options:");
        out.println("  -h: print this message, and exit");
        out.println("  -i FILE: read commands from FILE instead of standard input");
        out.println("  -o FILE: save a transcript of the session in FILE");
        out.println("  -v: turn on verbose output");
        out.println("  -l URL: use URL for JDBC database connection");
        out.println("    (overriding the URL in PROPS_FILE)");
        out.println("  -p PASSWD: use PASSWD to connect to the database");
        out.println("    (overriding any password in PROPS_FILE)");
        out.println("  -P: prompt for database password");
        out.println("    (overriding any password in PROPS_FILE)");
        out.println("  -u USER: connect to the database as USER");
        out.println("    (overriding any user in PROPS_FILE)");
        out.println("PROPS_FILE: specifies the JDBC connection URL and properties");
        out.println("    (defaults to /ra/ra.properties packaged in ra.jar)");
        out.println();
        return;
    }

    protected void prompt(int line) {
        if (reader == null) return;
        reader.setPrompt(getPrompt(line));
        return;
    }

    protected String getPrompt(int line){
        return (line == 1)? "ra> " : "" + line + "> ";
    }
 
    protected void exit() {
        out.println("Bye!");
        out.println();
        exit(0);
        return;
    }

    protected void exit(int code) {
        ra.closeDBConnection();
        System.exit(code);
    }

    protected String getPassword(ConsoleReader reader) {
        String password = null;
        try {
            password = reader.readLine("Password: ", new Character((char)0));
        } catch (IOException e) {
        }
        if (password == null) {
            err.println("Error reading password input");
            err.println();
            exit(1);
        }
        return password;
    }

    protected void skipInput() {
        try {
            (new BufferedReader(new InputStreamReader(in))).readLine();
        } catch (IOException e) {
            err.println("Unexceptected I/O error:");
            err.println(e.toString());
            err.println();
            exit(1);
        }
    }
    private RAConfig parseCommandLineArguments() throws IOException, RAConfigException{
        return new RAConfig.Builder().build();
    }
/*
    private RAConfig parseCommandLineArguments() {
        
        CmdLineParser cmdLineParser = new CmdLineParser();
        CmdLineParser.Option helpO = cmdLineParser.addBooleanOption('h', "help");
        CmdLineParser.Option inputO = cmdLineParser.addStringOption('i', "input");
        CmdLineParser.Option outputO = cmdLineParser.addStringOption('o', "output");
        CmdLineParser.Option passwordO = cmdLineParser.addStringOption('p', "password");
        CmdLineParser.Option promptPasswordO = cmdLineParser.addBooleanOption('P', "prompt-password");
        CmdLineParser.Option schemaO = cmdLineParser.addStringOption('s', "schema");
        CmdLineParser.Option urlO = cmdLineParser.addStringOption('l', "url");
        CmdLineParser.Option userO = cmdLineParser.addStringOption('u', "user");
        CmdLineParser.Option verboseO = cmdLineParser.addBooleanOption('v', "verbose");
        try {
            cmdLineParser.parse(args);
        } catch (CmdLineParser.OptionException e) {
            err.println(e.getMessage());
            usage();
            exit(1);
        }
        boolean help = ((Boolean)cmdLineParser.getOptionValue(helpO, Boolean.FALSE)).booleanValue();
        String inFileName = (String)cmdLineParser.getOptionValue(inputO);
        String outFileName = (String)cmdLineParser.getOptionValue(outputO);
        String password = (String)cmdLineParser.getOptionValue(passwordO);
        boolean promptPassword = ((Boolean)cmdLineParser.getOptionValue(promptPasswordO, Boolean.FALSE)).booleanValue();
        String schema = (String)cmdLineParser.getOptionValue(schemaO);
        String url = (String)cmdLineParser.getOptionValue(urlO);
        String user = (String)cmdLineParser.getOptionValue(userO);
        boolean verbose = ((Boolean)cmdLineParser.getOptionValue(verboseO, Boolean.FALSE)).booleanValue();
        if (help) {
            usage();
            exit(1);
        }
        String propsFileName = null;
        String[] otherArgs = cmdLineParser.getRemainingArgs();
        if (otherArgs.length > 1) {
            usage();
            exit(1);
        } else if (otherArgs.length == 1) {
            propsFileName = otherArgs[0];
        }
        if (inFileName != null) {
            try {
                in = new FileInputStream(inFileName);
            } catch (FileNotFoundException e) {
                err.println("Error opening input file '" + inFileName + "'");
                err.println();
                exit(1);
            }
        } else {
            try {
                reader = new ConsoleReader();
                // Make sure ConsoleReader doesn't do funny things with backslashes:
                reader.setExpandEvents(false);
                in = new ConsoleReaderInputStream(reader);
            } catch (IOException e) {
                err.println("Unexceptected I/O error:");
                err.println(e.toString());
                err.println();
                exit(1);
            }
        }
        if (outFileName != null) {
            try {
                OutputStream log = new FileOutputStream(outFileName, true);
                out = new TeePrintStream(out, log);
                err = new TeePrintStream(err, log);
                in = new LogInputStream(in, log);
            } catch (FileNotFoundException e) {
                err.println("Error opening output file '" + outFileName + "'");
                err.println();
                exit(1);
            }
        }
        Properties props = new Properties();
        InputStream propsIn = null;
        if (propsFileName == null) {
            propsIn = Client.class.getResourceAsStream("ra.properties");
            if (propsIn == null) {
                err.println("Error loading properties from /ra/ra.properties in the jar file");
                exit(1);
            }
            try {
                props.load(propsIn);
            } catch (IOException e) {
                err.println("Error loading properties from /ra/ra.properties in the jar file");
                err.println(e.toString());
                err.println();
                exit(1);
            }
        } else {
            try {
                props.load(new FileInputStream(propsFileName));
            } catch (IOException e) {
                err.println("Error loading properties from " + propsFileName);
                err.println(e.toString());
                err.println();
                exit(1);
            }
        }
        if (url != null)
            props.setProperty("url", url);
        if (user != null)
            props.setProperty("user", user);
        if (password != null)
            props.setProperty("password", password);
        if (promptPassword) {
            try {
                props.setProperty("password",
                                  getPassword((reader == null)?
                                              new ConsoleReader() :
                                              reader));
            } catch (IOException e) {
                err.println("Unexceptected I/O error:");
                err.println(e.toString());
                err.println();
                exit(1);
            }
        }
        try {
            db = new DB(props.getProperty("url"), props);
        } catch (Exception e) {
            err.println("Error connecting to the database");
            err.println(e.toString());
            err.println();
            exit(1);
        }
        if (schema != null)
            props.setProperty("schema", schema);

        if (reader != null) {
            reader.addCompleter(new StringsCompleter(new String [] {
                "\\help;", "\\quit;", "\\list;", "\\sqlexec_{",
                "\\select_{", "\\project_{", "\\join", "\\join_{", "\\rename_{",
                "\\cross", "\\union", "\\diff", "\\intersect"
            }));
        }
        return null;
    }
    */
    public void start(){
        welcome();
        RAConfig config = null;
        RA ra = null;
        try {
            config = new RAConfig.Builder().url("jdbc:sqlite:/sample.db").build();
            ra = new RA(config);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RAConfigException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        in = System.in;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (true) {
            int lineNumber = 1;
            String line = "";
            while (!line.contains(";")) {
                out.print(getPrompt(lineNumber));
                try {
                    line += reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                lineNumber++;
            }
            line += "\n";
            IQueryResult result = ra.query(line);
            String resultString = result.toJsonString();
            //TODO: Handle errors
            System.out.println(resultString);
            JSONObject dataSection = new JSONObject(resultString).getJSONObject("data");
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
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

}
