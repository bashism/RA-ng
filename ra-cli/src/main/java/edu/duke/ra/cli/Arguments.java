package edu.duke.ra.cli;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class Arguments {
    @Option(name = "-h", aliases = "--help",
            usage = "Print this message, and exit")
    private boolean help = false;
    public boolean help(){
        return help;
    }

    @Option(name = "-v", aliases = "--verbose",
            usage = "Turn on verbose output")
    private boolean verbose = false;
    public boolean verbose(){
        return verbose;
    }

    @Option(name = "-i", aliases = "--input",
            metaVar = "FILE",
            usage = "Read commands from FILE instead of standard input")
    private String inputFile = "";
    public String inputFile(){
        return inputFile;
    }
    @Option(name = "-o", aliases = "--output",
            metaVar = "FILE",
            usage = "Save a transcript of the session in FILE")
    private String outputFile = "";
    public String outputFile(){
        return outputFile;
    }

    @Option(name = "-l", aliases = "--url",
            metaVar = "URL",
            usage = "Use URL for JDBC database connection,"
            + " overriding the URL in PROPS_FILE")
    private String url = "";
    public String url(){
        return url;
    }
    @Option(name = "-u", aliases = "--user",
            metaVar = "USER",
            usage = "Connect to the database as USER,"
                    + " overriding any user in PROPS_FILE")
    private String user = "";
    public String user(){
        return user;
    }
    @Option(name = "-p", aliases = "--password",
            metaVar = "PASSWD",
            usage = "Use PASSWD to connect to the database,"
                    + " overriding any password in PROPS_FILE")
    private String password = "";
    public String password(){
        return password;
    }
    @Option(name = "-P", aliases = "--prompt-password",
            usage = "Prompt for database password,"
                    + " overriding any password in PROPS_FILE")
    private boolean promptPassword = false;
    public boolean promptPassword(){
        return promptPassword;
    }

    @Argument(metaVar = "PROPS_FILE",
            usage = "specifies the JDBC connection URL and properties"
                    + " (defaults to /ra.properties packaged in ra-core.jar)")
    List<String> propertiesFile = new ArrayList<>();
    public String propertiesFile(){
        if (propertiesFile.size() > 0) {
            return propertiesFile.get(0);
        }
        else return "";
    }
}
