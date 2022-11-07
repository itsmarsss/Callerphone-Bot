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

    public static void hostPool(String ID) {
        childr.put(ID, new ArrayList<>());
        childr.get(ID).add(ID);
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
        if (childr.containsKey(IDh)) {
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
        if (parent.containsKey(IDc)) {
            childr.get(IDh).remove(IDc);
            parent.remove(IDc);
            // TODO:
            // Leave/Success messages
        } else {
            // TODO:
            // Not in pool
        }
    }

    public static void messageOut(String IDs, String IDo, String msg) {
        if (!parent.containsKey(IDs)) {
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
        } else {
            messageOut(parent.get(IDs), IDo, msg);
        }
    }
}