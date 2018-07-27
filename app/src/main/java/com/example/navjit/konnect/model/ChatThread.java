/**************************************************************
 *     Author : ChandraShekhar
 *
 *     This java bean is mapping the thread in firebase.
 ***************************************************************/

package com.example.navjit.konnect.model;

public class ChatThread {

    private String threadId;
    private String messengerOne;
    private String messengerTwo;

    public ChatThread(){
    }

    public ChatThread(String threadId,String messengerOne, String messengerTwo) {
        this.threadId=threadId;
        this.messengerOne = messengerOne;
        this.messengerTwo = messengerTwo;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
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
