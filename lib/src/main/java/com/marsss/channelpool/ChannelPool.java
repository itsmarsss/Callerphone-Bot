package com.marsss;

public class ChannelPool {

    public HashMap<String, String>passw = new HashMap<String, String>();
    public HashMap<String, String>parent = new HashMap<String, String>();
    public HashMap<String, ArrayList<String>>childr = new HashMap<String, ArrayList<String>>();

    public void setPassword(String ID, String PASS) {
        passw.put(ID, PASS);
    }

    public void removePassword(String ID) {
        passw.remove(ID);
    }

    public void clearChildren(String ID) {
        ArrayList<String>pool = childr.get(id);
        for(String id : pool) {
            Bot.jda.getTextChannelById(id).sendMessage("This pool has been ended by the host channel.").queue();
            parent.remove(id);
        }
        childr.remove(id);
    }

    public void addChildren(String IDh, String IDc) {
        if(childr.get(IDh) != null) {
            childr.get(IDh).add(IDc);
            parent.put(IDc, IDh);
            // TODO:
                // Joined/Success messages
        }
    }

    public void removeChildren(String IDh, String IDc) {
        if(childr.get(IDh)) == null) {
            childr.put(IDh, new ArrayList<String>());
        }
        childr.get(IDh).remove(IDc);
        parent.remove(IDc);
    }
}