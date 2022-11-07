package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ChannelPool {

    public static HashMap<String, PoolConfig> config = new HashMap<>();
    public static HashMap<String, String> parent = new HashMap<>();
    public static HashMap<String, ArrayList<String>> childr = new HashMap<>();

    public static int hostPool(String ID) {
        if (childr.containsKey(ID)) {
            return 413;
        } else if (parent.containsKey(ID)) {
            return 409;
        } else {
            config.put(ID, new PoolConfig("", 10, true));
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

    public static int setPassword(String id, String pwd) {
        if (isHost(id)) {
            config.get(id).setPwd(pwd);
            Callerphone.jda.getTextChannelById(id).sendMessage("This pool now has password: ||" + config.get(id).getPwd() + "||.").queue();
            return 202;
        } else {
            return 404;
        }
    }

    public static int removePassword(String id) {
        if (isHost(id)) {
            config.get(id).setPwd("");
            Callerphone.jda.getTextChannelById(id).sendMessage("This pool now has no password.").queue();
            return 202;
        } else {
            return 404;
        }
    }

    public static int setCap(String id, int cap) {
        if (isHost(id)) {
            config.get(id).setCap(cap);
            return 202;
        } else {
            return 404;
        }
    }

    public static int setPublicity(String id, boolean pub) {
        if (isHost(id)) {
            config.get(id).setPub(pub);
            if (config.get(id).isPub()) {
                Callerphone.jda.getTextChannelById(id).sendMessage("This pool is now public.").queue();
            } else {
                Callerphone.jda.getTextChannelById(id).sendMessage("This pool is now private.").queue();
            }
            return 202;
        } else {
            return 404;
        }
    }

    public static ArrayList<String> getClients(String ID) {
        if (!parent.containsKey(ID) && !childr.containsKey(ID)) {
            return new ArrayList<>();
        } else {
            if (parent.containsKey(ID)) {
                return childr.get(parent.get(ID));
            } else {
                return childr.get(ID);
            }
        }
    }

    public static int clearChildren(String ID) {
        if (childr.containsKey(ID)) {
            ArrayList<String> pool = childr.get(ID);
            for (String id : pool) {
                if (id.equals(ID))
                    continue;
                Callerphone.jda.getTextChannelById(id).sendMessage("This pool has been ended by the host channel (`#" + id + "`).").queue();
                parent.remove(id);
            }
            childr.remove(ID);
            config.remove(ID);
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

    public static boolean isHost(String ID) {
        return !parent.containsKey(ID) && childr.containsKey(ID);
    }

    public static void broadCast(String sender, String original, String msg) {
        if (isHost(sender)) {
            ArrayList<String> pool = childr.get(sender);
            for (String id : pool) {
                if (id.equals(original))
                    continue;

                if (Callerphone.jda.getTextChannelById(id) == null) {
                    if (sender.equals(id)) {
                        clearChildren(sender);
                    } else {
                        childr.get(sender).remove(id);
                        systemBroadCast(sender, "Channel ID: " + id + " has left this pool.");
                    }
                } else {
                    MessageAction ma = Callerphone.jda.getTextChannelById(id).sendMessage(msg);
                    Collection<ActionRow> actionrow = new ArrayList<>();
                    Collection<Button> collection = new ArrayList<>();

                    String link = String.format("https://discord.com/channels/%s/%s", Callerphone.jda.getTextChannelById(original).getGuild().getId(), Callerphone.jda.getTextChannelById(original).getId());

                    collection.add(Button.link(link, "From: #" + Callerphone.jda.getTextChannelById(original).getName() + " (" + Callerphone.jda.getTextChannelById(original).getGuild().getName() + ")"));

                    ActionRow row = ActionRow.of(collection);
                    actionrow.add(row);
                    ma.setActionRows(actionrow)
                            .queue();
                }

            }
        } else if (parent.containsKey(sender)) {
            broadCast(parent.get(sender), original, msg);
        }
    }

    public static void systemBroadCast(String IDhost, String msg) {
        ArrayList<String> pool = childr.get(IDhost);
        for (String id : pool) {
            if (Callerphone.jda.getTextChannelById(id) == null) {
                systemBroadCast(IDhost, "Channel ID: " + id + " has left this pool.");
            } else {
                MessageAction ma = Callerphone.jda.getTextChannelById(id).sendMessage(msg);
                ma.queue();
            }

        }
    }

}