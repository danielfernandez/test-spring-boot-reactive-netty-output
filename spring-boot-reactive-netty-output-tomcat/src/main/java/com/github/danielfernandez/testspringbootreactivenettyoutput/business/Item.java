package com.github.danielfernandez.testspringbootreactivenettyoutput.business;

public class Item {

    private final int id;
    private final String name;

    public Item(final int id, final String name) {
        super();
        this.id = id;
        this.name = name;
    }


    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

}
