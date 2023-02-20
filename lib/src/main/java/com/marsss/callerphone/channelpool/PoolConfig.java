package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Response;

import java.util.LinkedList;

public class PoolConfig {
    private String hostID;
    private String pwd;
    private int cap;
    private boolean pub;

    public LinkedList<String> children = new LinkedList<>();

    public PoolConfig(String hostID, String pwd, int cap, boolean pub) {
        this.hostID = hostID;
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

    public String toJSON() {
        String childrenList = "";
        for (int i = 0; i < children.size(); i++) {
            childrenList += "\"" + children.get(i) + "\"";
            if (i != children.size() - 1) {
                childrenList += ",";
            }
        }

        return String.format(Response.POOL_TEMPLATE.toString(), hostID, pwd, cap, pub, childrenList);
    }
}
