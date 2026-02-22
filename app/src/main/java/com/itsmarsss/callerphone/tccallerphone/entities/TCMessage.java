package com.itsmarsss.callerphone.tccallerphone.entities;

public class TCMessage {
    private boolean caller;
    private String author;
    private String authorName;
    private String channel;
    private String content;
    private String[] flags;
    private long sent;

    public TCMessage() {}

    public TCMessage(boolean caller, String author, String authorName, String channel, String content, String[] flags, long sent) {
        this.caller = caller;
        this.author = author;
        this.authorName = authorName;
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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
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
        return (caller ? "Caller " : "Receiver ")
                + (authorName != null ? authorName : "Unknown")
                + "(" + author + ")"
                + ": " + content;
    }
}
