package com.marsss.callerphone.msginbottle.entities;

public class Page {
    private int pageNum;
    private String author;
    private String message;
    private boolean signed;
    private long released;

    public Page(int pageNum, String author, String message, boolean signed, long released) {
        this.pageNum = pageNum;
        this.author = author;
        this.message = message;
        this.signed = signed;
        this.released = released;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public long getReleased() {
        return released;
    }

    public void setReleased(long released) {
        this.released = released;
    }
}
