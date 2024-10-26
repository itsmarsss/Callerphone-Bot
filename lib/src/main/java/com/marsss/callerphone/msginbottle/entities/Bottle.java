package com.marsss.callerphone.msginbottle.entities;

import java.util.ArrayList;
import java.util.List;

public class Bottle {
    private String uuid;
    private List<Page> pages;

    public Bottle(String uuid, ArrayList<Page> pages) {
        this.uuid = uuid;
        this.pages = pages;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }
}
