package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ChannelPool {
    public static final int IS_HOST = 1;
    public static final int IS_CHILD = 2;
    public static final int NOT_FOUND = 3;
    public static final int INCORRECT_PASS = 4;
    public static final int SUCCESS = 5;
    public static final int ERROR = 8;
    public static final int FULL = 9;

    public static HashMap<String, PoolConfig> config = new HashMap<>();
    public static HashMap<String, String> parent = new HashMap<>();
    public static HashMap<String, ArrayList<String>> childr = new HashMap<>();

    public static int hostPool(String ID) {
        if (isHost(ID)) {
            return ChannelPool.IS_HOST;//413
        } else if (parent.containsKey(ID)) {
            return ChannelPool.IS_CHILD;//409
        } else {
            config.put(ID, new PoolConfig("", 10, true));
            childr.put(ID, new ArrayList<>());
            childr.get(ID).add(ID);
            return ChannelPool.SUCCESS;//201
        }
    }

    public static int joinPool(String IDh, String IDc, String pwd) {
        if (isHost(IDc)) {
            return ChannelPool.IS_HOST;//413
        } else if (isChild(IDc)) {
            return ChannelPool.IS_CHILD;//409
        } else {
            if (!config.get(IDh).isPub()) {
                return ChannelPool.NOT_FOUND;//404
            } else if (!config.get(IDh).getPwd().equals(pwd)) {
                return ChannelPool.INCORRECT_PASS;//401
            } else {
                return addChildren(IDh, IDc);
            }
        }
    }

    public static int leavePool(String ID) {
        if (isHost(ID)) {
            return ChannelPool.IS_HOST;
        } else if (isChild(ID)) {
            return removeChildren(parent.get(ID), ID);
        }
        return ChannelPool.ERROR;
    }

    public static int endPool(String ID) {
        if (isHost(ID)) {
            return clearChildren(ID);
        } else {
            return ChannelPool.NOT_FOUND;
        }
    }

    public static boolean hasPassword(String id) {
        if (config.containsKey(id)) {
            return config.get(id).getPwd().equals("");
        }
        return false;
    }

    public static int setPassword(String id, String pwd) {
        if (config.containsKey(id)) {
            config.get(id).setPwd(pwd);
            return ChannelPool.SUCCESS;
        }
        return ChannelPool.ERROR;
    }

    public static String getPassword(String id) {
        if (config.containsKey(id)) {
            return config.get(id).getPwd();
        }
        return "";
    }

    public static int setCap(String id, int cap) {
        if (isHost(id)) {
            config.get(id).setCap(cap);
            return ChannelPool.SUCCESS;
        }
        return ChannelPool.ERROR;
    }

    public static int setPublicity(String id, boolean pub) {
        if (isHost(id)) {
            config.get(id).setPub(pub);
            return ChannelPool.SUCCESS;
        }
        return ChannelPool.ERROR;
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
        if (isHost(ID)) {
            ArrayList<String> pool = childr.get(ID);
            for (String id : pool) {
                if (id.equals(ID))
                    continue;
                Callerphone.jda.getTextChannelById(id).sendMessage(Callerphone.Callerphone + "This pool has been ended by the host channel `ID: " + id + "` (#" + Callerphone.jda.getTextChannelById(id).getName() + ").").queue();
                parent.remove(id);
            }
            childr.remove(ID);
            config.remove(ID);
            return ChannelPool.SUCCESS;
        } else if (!isHost(ID)) {
            return ChannelPool.IS_CHILD;
        }
        return ChannelPool.ERROR;
    }

    public static int addChildren(String IDh, String IDc) {
        if (isHost(IDh)) {
            if (childr.get(IDh).size() >= config.get(IDh).getCap()) {
                return ChannelPool.FULL;
            } else {
                systemBroadCast(IDh, Callerphone.Callerphone + "Channel `ID: " + IDc + "` (#" + Callerphone.jda.getTextChannelById(IDc).getName() + ") has joined this pool. " + (childr.get(IDh).size() + 1) + "/" + config.get(IDh).getCap());
                childr.get(IDh).add(IDc);
                parent.put(IDc, IDh);
                return ChannelPool.SUCCESS;
            }
        }
        return ChannelPool.ERROR;
    }

    public static int removeChildren(String IDh, String IDc) {
        if (isChild(IDc)) {
            childr.get(IDh).remove(IDc);
            parent.remove(IDc);
            systemBroadCast(IDh, Callerphone.Callerphone + "Channel `ID: " + IDc + "` (#" + Callerphone.jda.getTextChannelById(IDc).getName() + ") has left this pool. " + childr.get(IDh).size() + "/" + config.get(IDh).getCap());
            return ChannelPool.SUCCESS;
        }
        return ChannelPool.ERROR;
    }

    public static boolean isHost(String ID) {
        return !parent.containsKey(ID) && childr.containsKey(ID);
    }

    public static boolean isChild(String ID) {
        return parent.containsKey(ID) && !childr.containsKey(ID);
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
                        systemBroadCast(sender, Callerphone.Callerphone + "Channel ID: `" + id + "` has left this pool.");
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
                systemBroadCast(IDhost, Callerphone.Callerphone + "Channel ID: `" + id + "` has left this pool.");
            } else {
                MessageAction ma = Callerphone.jda.getTextChannelById(id).sendMessage(msg);
                ma.queue();
            }

        }
    }

}