package com.marsss.callerphone.msginbottle.entities;

import java.util.ArrayList;
import java.util.List;

public class Bottle {
    private String id;
    private List<Page> pages;

    public Bottle(String id, ArrayList<Page> pages) {
        this.id = id;
        this.pages = pages;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }
}
