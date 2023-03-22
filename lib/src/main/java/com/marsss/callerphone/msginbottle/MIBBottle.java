package com.marsss.callerphone.msginbottle;

import java.util.LinkedList;

public class MIBBottle {
    private String UUID;
    private LinkedList<String> participantID;
    private LinkedList<String> page;

    public MIBBottle(String participantID, String page) {
        addParticipant(participantID);
        addPage(page);
    }

    public void addParticipant(String participantID) {
        this.participantID.add(participantID);
    }

    public void addPage(String page) {
        this.page.add(page);
    }
}
