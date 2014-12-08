package edu.duke.ra.core;

import java.io.PrintWriter;
import java.io.StringWriter;

public class RAException extends Exception{
    private String name;
    private String description;
    private String message;
    private String stackTrace;
    private String details;
    public RAException(String name, String description, String details, Exception exception) {
        this.name = name;
        this.description = description;
        this.details = details;
        this.message = exception.getMessage();
        this.stackTrace = stringifyStackTrace(exception);
    }
    private String stringifyStackTrace(Exception exception){
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }
    public String name(){
        return name;
    }
    public String description(){
        return description;
    }
    public String details(){
        return details;
    }
    public String message(){
        return message;
    }
    public String stackTrace(){
        return stackTrace;
    }
}
