package com.marsss.callerphone.channelpool;

public class PoolConfig {
    private String pwd;
    private int cap;
    private boolean pub;

    public PoolConfig(String pwd, int cap, boolean pub) {
        this.pwd = pwd;
        this.cap = cap;
        this.pub = pub;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public int getCap() {
        return cap;
    }

    public void setCap(int cap) {
        this.cap = Math.min(cap, 10);
    }

    public boolean isPub() {
        return pub;
    }

    public void setPub(boolean pub) {
        this.pub = pub;
    }
}
