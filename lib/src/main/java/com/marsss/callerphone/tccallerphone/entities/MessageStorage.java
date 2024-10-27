package com.marsss.callerphone.tccallerphone.entities;

import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.concurrent.CompletableFuture;

public class MessageStorage {
    private boolean caller;
    private String author;
    private String channel;
    private String content;
    private String[] flags;
    private long sent;

    public MessageStorage(boolean caller, String author, String channel, String content, String[] flags, long sent) {
        this.caller = caller;
        this.author = author;
        this.channel = channel;
        this.content = content;
        this.flags = flags;
        this.sent = sent;
    }

    public boolean isCaller() {
        return caller;
    }

    public void setCaller(boolean caller) {
        this.caller = caller;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String[] getFlags() {
        return flags;
    }

    public void setFlags(String[] flags) {
        this.flags = flags;
    }

    public long getSent() {
        return sent;
    }

    public void setSent(long sent) {
        this.sent = sent;
    }

    public String toString() {
        CompletableFuture<String> future = new CompletableFuture<>();

        ToolSet.getUser(author).queue(user -> {
            future.complete(user.getName());
        });

        try {
            return (caller ? "Caller " : "Receiver ")
                    + future.get()
                    + "(" + author + ")"
                    + ": " + content;
        } catch (Exception e) {
        }

        return (caller ? "Caller " : "Receiver ")
                + "null"
                + "(" + author + ")"
                + ": " + content;
    }
}
