package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;

import java.util.ArrayList;
import java.util.HashMap;

public class ChannelPool {

    public static HashMap<String, String> passw = new HashMap<>();
    public static HashMap<String, String> parent = new HashMap<>();
    public static HashMap<String, ArrayList<String>> childr = new HashMap<>();

    public static void hostPool(String ID) {
        childr.put(ID, new ArrayList<>());
    }

    public static void joinPool(String IDh, String IDc) {
        addChildren(IDh, IDc);
    }

    public static void setPassword(String ID, String PASS) {
        passw.put(ID, PASS);
    }

    public static void removePassword(String ID) {
        passw.remove(ID);
    }

    public static void clearChildren(String ID) {
        ArrayList<String> pool = childr.get(ID);
        for (String id : pool) {
            Callerphone.jda.getTextChannelById(id).sendMessage("This pool has been ended by the host channel.").queue();
            parent.remove(id);
        }
        childr.remove(ID);
    }

    public static void addChildren(String IDh, String IDc) {
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

    public static void removeChildren(String IDh, String IDc) {
        if (parent.get(IDc) != null) {
            childr.get(IDh).remove(IDc);
            parent.remove(IDc);
            // TODO:
            // Leave/Success messages
        } else {
            // TODO:
            // Not in pool
        }
    }

    public static void messageOut(String ID, String msg) {
        if (parent.get(ID) == null) {
            for (String id : childr.get(ID)) {
                Callerphone.jda.getTextChannelById(id).sendMessage(msg).queue();
            }
        } else {
            messageOut(parent.get(ID), msg);
        }
    }
}