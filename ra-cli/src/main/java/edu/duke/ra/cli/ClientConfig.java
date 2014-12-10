package edu.duke.ra.cli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import jline.console.ConsoleReader;
import jline.internal.InputStreamReader;
import edu.duke.ra.core.RAConfig;
import edu.duke.ra.core.RAConfigException;

public class ClientConfig {
    private Arguments arguments;
    private CmdLineParser parser;
    private boolean isValid;

    private PrintStream out;
    private PrintStream err;
    private ConsoleReader reader;
    private RAConfig raConfig;
    private OutputStream log;

    public ClientConfig(String[] args) {
        isValid = true;

        arguments = new Arguments();
        parser = new CmdLineParser(arguments);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e1) {
            isValid = false;
        }
        try {
            configureIO();
            configureDBAndVerbosity();
        }
        catch (IOException | RAConfigException e) {
            out = System.out;
            err = System.err;
            err.println("Error: " + e.getMessage());
            err.println();
            isValid = false;
        }
    }

    void configureIO() throws IOException {
        InputStream in = makeInputStream();
        makeOutputStreams(in);
        reader = new ConsoleReader(in, out);
        reader.setExpandEvents(false);
    }
    InputStream makeInputStream() throws IOException{
        InputStream in;
        if (arguments.inputFile().length() == 0) {
            in = System.in;
        }
        else {
            in = new FileInputStream(arguments.inputFile());
        }
        return in;
    }
    void makeOutputStreams(InputStream in) throws FileNotFoundException {
        if (arguments.outputFile().length() == 0) {
            log = null;
            out = System.out;
            err = System.err;
        }
        else {
            log = new FileOutputStream(arguments.outputFile(), true);
            out = new TeePrintStream(System.out, log);
            err = new TeePrintStream(System.err, log);
            in = new LogInputStream(in, log);
        }
    }

    void configureDBAndVerbosity() throws IOException, RAConfigException {
        RAConfig.Builder builder = new RAConfig.Builder();
        String url = arguments.url();
        if (url.length() > 0) {
            builder.url(url);
        }
        String user = arguments.user();
        if (user.length() > 0) {
            builder.user(user);
        }
        String password = arguments.password();
        if (arguments.promptPassword()){
            password = getPassword(reader);
        }
        if (password.length() > 0) {
            builder.password(password);
        }
        String propertiesFile = arguments.propertiesFile();
        if (propertiesFile.length() > 0) {
            builder.propsFileName(propertiesFile);
        }
        builder.verbose(arguments.verbose());
        raConfig = builder.build();
    }
    String getPassword(ConsoleReader reader) throws IOException {
        String password = reader.readLine("Password: ", new Character((char)0));
        if (password == null) {
            throw new IOException("Error reading password input");
        }
        return password;
    }

    public boolean help() {
        return arguments.help();
    }
    public boolean isValid() {
        return isValid;
    }
    public void usage() {
        out.println("Usage: ra [Options] [PROPS_FILE]");
        out.println("Options:");
        parser.printUsage(out);
    }

    public boolean verbose(){
        return arguments.verbose();
    }
    public PrintStream out() {
        return out;
    }
    public PrintStream err() {
        return err;
    }
    public ConsoleReader reader() {
        return reader;
    }
    public RAConfig raConfig() {
        return raConfig;
    }
    public OutputStream log(){
        return log;
    }
}
