package edu.duke.ra.core;

public class Column {
    private String name;
    private String type;
    public Column(String name, String type){
        this.name = name;
        this.type = type;
    }
    public String name(){
        return name;
    }
    public String type(){
        return type;
    }
}
