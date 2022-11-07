package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ChannelPool {

    public static HashMap<String, String> passw = new HashMap<>();
    public static HashMap<String, String> parent = new HashMap<>();
    public static HashMap<String, ArrayList<String>> childr = new HashMap<>();

    public static int hostPool(String ID) {
        if (childr.containsKey(ID)) {
            return 413;
        } else if (parent.containsKey(ID)) {
            return 409;
        } else {
            childr.put(ID, new ArrayList<>());
            childr.get(ID).add(ID);
            return 201;
        }
    }

    public static int joinPool(String IDh, String IDc) {
        if (childr.containsKey(IDc)) {
            return 413;
        } else if (parent.containsKey(IDc)) {
            return 409;
        } else {
            return addChildren(IDh, IDc);
        }
    }

    public static int leavePool(String ID) {
        if (parent.containsKey(ID)) {
            return removeChildren(parent.get(ID), ID);
        } else if (childr.containsKey(ID)) {
            return 409;
        } else {
            return 404;
        }
    }

    public static int endPool(String ID) {
        if (childr.containsKey(ID)) {
            return clearChildren(ID);
        } else {
            return 404;
        }
    }

    public static void setPassword(String ID, String PASS) {
        passw.put(ID, PASS);
    }

    public static void removePassword(String ID) {
        passw.remove(ID);
    }

    public static int clearChildren(String ID) {
        if (childr.containsKey(ID)) {
            ArrayList<String> pool = childr.get(ID);
            for (String id : pool) {
                if(id == ID)
                    continue;
                Callerphone.jda.getTextChannelById(id).sendMessage("This pool has been ended by the host channel (" + Callerphone.jda.getTextChannelById(ID).getName() + ").").queue();
                parent.remove(id);
            }
            childr.remove(ID);
            return 200;
        } else {
            return 404;
        }
    }

    public static int addChildren(String IDh, String IDc) {
        if (childr.containsKey(IDh)) {
            if (childr.get(IDh).size() >= 11) {
                return 414;
            } else {
                childr.get(IDh).add(IDc);
                parent.put(IDc, IDh);
                return 200;
            }
        } else {
            return 404;
        }
    }

    public static int removeChildren(String IDh, String IDc) {
        if (parent.containsKey(IDc)) {
            childr.get(IDh).remove(IDc);
            parent.remove(IDc);
            return 200;
        } else {
            return 404;
        }
    }

    public static void broadCast(String IDs, String IDo, String msg) {
        if (!parent.containsKey(IDs)) {
            if (!childr.containsKey(IDs))
                return;
            for (String id : childr.get(IDs)) {
                if (id.equals(IDo))
                    continue;
                MessageAction ma = Callerphone.jda.getTextChannelById(id).sendMessage(msg);

                Collection<ActionRow> actionrow = new ArrayList<>();
                Collection<Button> collection = new ArrayList<>();

                String link = String.format("https://discord.com/channels/%s/%s", Callerphone.jda.getTextChannelById(IDo).getGuild().getId(), Callerphone.jda.getTextChannelById(IDo).getId());

                collection.add(Button.link(link, "From: #" + Callerphone.jda.getTextChannelById(IDo).getName()));

                ActionRow row = ActionRow.of(collection);
                actionrow.add(row);
                ma.setActionRows(actionrow);
                ma.queue();

            }
        } else if (parent.containsKey(IDs)) {
            broadCast(parent.get(IDs), IDo, msg);
        }
    }
}