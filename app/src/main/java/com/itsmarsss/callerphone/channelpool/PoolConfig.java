package com.itsmarsss.callerphone.channelpool;

import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.Response;

import java.util.concurrent.CopyOnWriteArrayList;

public class PoolConfig {
    private final String hostId;
    private String pwd;
    private int cap;
    private boolean pub;

    public final CopyOnWriteArrayList<String> children = new CopyOnWriteArrayList<>();

    public PoolConfig(String hostId, String pwd, int cap, boolean pub) {
        this.hostId = hostId;
        this.pwd = pwd;
        this.cap = Math.min(Math.max(cap, Constants.POOL_MIN_CAPACITY), Constants.POOL_MAX_CAPACITY);
        this.pub = pub;
    }

    public String getHostId() {
        return hostId;
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
        this.cap = Math.min(Math.max(cap, Constants.POOL_MIN_CAPACITY), Constants.POOL_MAX_CAPACITY);
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
