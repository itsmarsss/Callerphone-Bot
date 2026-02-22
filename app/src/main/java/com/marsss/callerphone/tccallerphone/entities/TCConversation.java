package com.marsss.callerphone.tccallerphone.entities;

import com.marsss.callerphone.ToolSet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TCConversation {
    private String id;

    private List<String> participants;
    private List<TCMessage> messages;

    private String callerTCId;
    private String receiverTCId;

    private long callerLastMessage;
    private long receiverLastMessage;

    private boolean callerAnonymous;
    private boolean receiverAnonymous;

    private long started;
    private long ended;

    private boolean report;

    public TCConversation() {
        this.id = ToolSet.generateUID();

        this.started = Instant.now().getEpochSecond();
        this.ended = -1;

        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();

        this.callerTCId = "";
        this.receiverTCId = "";

        this.callerLastMessage = this.started;
        this.receiverLastMessage = this.started;

        this.callerAnonymous = false;
        this.receiverAnonymous = false;

        this.report = false;
    }

    public TCConversation(String callerTCId, String receiverTCId, boolean callerAnonymous, boolean receiverAnonymous, boolean report) {
        this.id = ToolSet.generateUID();

        this.started = Instant.now().getEpochSecond();
        this.ended = -1;

        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();

        this.callerTCId = callerTCId;
        this.receiverTCId = receiverTCId;

        this.callerLastMessage = this.started;
        this.receiverLastMessage = this.started;

        this.callerAnonymous = callerAnonymous;
        this.receiverAnonymous = receiverAnonymous;

        this.report = report;
    }

    // Get

    public long getStarted() {
        return started;
    }

    public long getEnded() {
        return ended;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public List<TCMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<TCMessage> messages) {
        this.messages = messages;
    }
    public String getCallerTCId() {
        return callerTCId;
    }

    public String getReceiverTCId() {
        return receiverTCId;
    }

    public long getCallerLastMessage() {
        return callerLastMessage;
    }

    public long getReceiverLastMessage() {
        return receiverLastMessage;
    }

    public boolean getCallerAnonymous() {
        return callerAnonymous;
    }

    public boolean getReceiverAnonymous() {
        return receiverAnonymous;
    }

    public boolean getReport() {
        return report;
    }

    // Set

    public void setStarted(long started) {
        this.started = started;
    }

    public void setEnded(long ended) {
        this.ended = ended;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public void addMessage(TCMessage message) {
        messages.add(message);
    }

    public void setCallerTCId(String id) {
        callerTCId = id;
    }

    public void setReceiverTCId(String id) {
        receiverTCId = id;
    }

    public void setCallerLastMessage(long time) {
        callerLastMessage = time;
    }

    public void setReceiverLastMessage(long time) {
        receiverLastMessage = time;
    }

    public void setCallerAnonymous(boolean canon) {
        callerAnonymous = canon;
    }

    public void setReceiverAnonymous(boolean ranon) {
        receiverAnonymous = ranon;
    }

    public void setReport(boolean rep) {
        report = rep;
    }

    public void resetMessage() {
        messages.clear();
        callerTCId = "empty";
        receiverTCId = "";
        callerLastMessage = 0;
        receiverLastMessage = 0;
        callerAnonymous = false;
        receiverAnonymous = false;
        report = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
