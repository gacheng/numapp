package com.relic.numapp.utils;

public enum Constants {

    ACK ("Acknowledge"),
    PNG ("Ping"),
    PEN ("Pending"),

    terminate ("Terminate Signal"), //stop client and server
    stop ("Stop Signal"),  //stop client only
    all("Complete Directory"),
    processing ("Processing Directory"),
    automatic ("Automatic Number"),
    manual ("Manual Number");

    private final String name;

    private Constants(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }
}
