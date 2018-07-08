package com.example.navjit.konnect.model;

import android.os.Parcelable;

import java.io.Serializable;

public class ChatUser implements Serializable {
    //private int id;
    private String userName;
    private String firstName;
    private String lastName;
    private String userType;
    private String password;
    private String email;

    public ChatUser(){
    }

    public ChatUser(String userName, String firstName, String lastName, String userType, String password) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.password = password;
    }

   // public int getId(){ return id;}

   // public void setId(int Id){ this.id = Id; }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}

