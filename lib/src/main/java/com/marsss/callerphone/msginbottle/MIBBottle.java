package com.marsss.callerphone.msginbottle;

import java.util.LinkedList;
import java.util.UUID;

public class MIBBottle {
    private String uuid;
    private LinkedList<String> participantID;
    private LinkedList<String> page;
    private long timeSent;

    public MIBBottle(String participantID, String page) {
        uuid = UUID.randomUUID().toString();
        addParticipant(participantID);
        addPage(page);
        timeSent = System.currentTimeMillis();
    }

    public void addParticipant(String participantID) {
        this.participantID.add(participantID);
    }

    public void addPage(String page) {
        this.page.add(page);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public LinkedList<String> getParticipantID() {
        return participantID;
    }

    public void getParticipantID(LinkedList<String> participantID) {
        this.participantID = participantID;
    }

    public LinkedList<String> getPage() {
        return page;
    }

    public void getPage(LinkedList<String> page) {
        this.page = page;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }
}
