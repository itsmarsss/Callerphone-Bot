package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;

import java.util.ArrayList;
import java.util.HashMap;

public class ChannelPool {

    public HashMap<String, String> passw = new HashMap<>();
    public HashMap<String, String> parent = new HashMap<>();
    public HashMap<String, ArrayList<String>> childr = new HashMap<>();

    public void setPassword(String ID, String PASS) {
        passw.put(ID, PASS);
    }

    public void removePassword(String ID) {
        passw.remove(ID);
    }

    public void clearChildren(String ID) {
        ArrayList<String> pool = childr.get(ID);
        for (String id : pool) {
            Callerphone.jda.getTextChannelById(id).sendMessage("This pool has been ended by the host channel.").queue();
            parent.remove(id);
        }
        childr.remove(ID);
    }

    public void addChildren(String IDh, String IDc) {
        if (childr.get(IDh) != null) {
            childr.get(IDh).add(IDc);
            parent.put(IDc, IDh);
            // TODO:
            // Joined/Success messages
        } else {
            // TODO:
            // ID not hosted
        }
    }

    public void removeChildren(String IDh, String IDc) {
        if(parent.get(IDc) != null) {
            childr.get(IDh).remove(IDc);
            parent.remove(IDc);
            // TODO:
            // Leave/Success messages
        } else {
            // TODO:
            // Not in pool
        }
    }
}