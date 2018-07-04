package com.example.navjit.konnect.model;

public class ChatThread {
    private String messengerOne;
    private String messengerTwo;

    public ChatThread(){
    }

    public ChatThread(String messengerOne, String messengerTwo) {
        this.messengerOne = messengerOne;
        this.messengerTwo = messengerTwo;
    }

    public String getMessengerOne() {
        return messengerOne;
    }

    public void setMessengerOne(String messengerOne) {
        this.messengerOne = messengerOne;
    }

    public String getMessengerTwo() {
        return messengerTwo;
    }

    public void setMessengerTwo(String messengerTwo) {
        this.messengerTwo = messengerTwo;
    }
}
