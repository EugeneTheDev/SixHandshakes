package com.eugene.sixhandshakes.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {

    private String firstName, lastName;
    private int id;

    public User() {
    }

    public User(String firstName, String lastName, int id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonIgnore
    public boolean isEmpty(){
        return firstName.isEmpty() || lastName.isEmpty() || id<=0;
    }
}
