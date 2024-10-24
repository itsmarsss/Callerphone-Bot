package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Response;

import java.util.LinkedList;

public class PoolConfig {
    private String hostId;
    private String pwd;
    private int cap;
    private boolean pub;

    public final LinkedList<String> children = new LinkedList<>();

    public PoolConfig(String hostId, String pwd, int cap, boolean pub) {
        this.hostId = hostId;
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
        StringBuilder childrenList = new StringBuilder();
        for (int i = 0; i < children.size(); i++) {
            childrenList.append("\"").append(children.get(i)).append("\"");
            if (i != children.size() - 1) {
                childrenList.append(",");
            }
        }

        return String.format(Response.POOL_TEMPLATE.toString(), hostId, pwd, cap, pub, childrenList.toString());
    }
}
