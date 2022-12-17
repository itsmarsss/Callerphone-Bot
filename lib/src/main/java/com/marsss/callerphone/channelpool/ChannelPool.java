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

    public static PoolStatus hostPool(String ID) {
        if (isHost(ID)) {
            return PoolStatus.IS_HOST;//413
        } else if (parent.containsKey(ID)) {
            return PoolStatus.IS_CHILD;//409
        }
        config.put(ID, new PoolConfig("", 10, true));
        childr.put(ID, new ArrayList<>());
        childr.get(ID).add(ID);
        return PoolStatus.SUCCESS;//201
    }

    public static PoolStatus joinPool(String IDh, String IDc, String pwd) {
        if (isHost(IDc)) {
            return PoolStatus.IS_HOST;//413
        } else if (isChild(IDc)) {
            return PoolStatus.IS_CHILD;//409
        }

        if (!config.get(IDh).isPub()) {
            return PoolStatus.NOT_FOUND;//404
        } else if (!config.get(IDh).getPwd().equals(pwd)) {
            return PoolStatus.INCORRECT_PASS;//401
        }
        return addChildren(IDh, IDc);
    }

    public static PoolStatus leavePool(String ID) {
        if (isHost(ID)) {
            return PoolStatus.IS_HOST;
        } else if (isChild(ID)) {
            return removeChildren(parent.get(ID), ID);
        }
        return PoolStatus.ERROR;
    }

    public static PoolStatus endPool(String ID) {
        if (isHost(ID)) {
            return clearChildren(ID);
        }
        return PoolStatus.NOT_FOUND;
    }

    public static boolean hasPassword(String id) {
        if (config.containsKey(id)) {
            return !config.get(id).getPwd().equals("");
        }
        return false;
    }

    public static PoolStatus setPassword(String id, String pwd) {
        if (config.containsKey(id)) {
            config.get(id).setPwd(pwd);
            return PoolStatus.SUCCESS;
        }
        return PoolStatus.ERROR;
    }

    public static String getPassword(String id) {
        if (config.containsKey(id)) {
            return config.get(id).getPwd();
        }
        return "";
    }

    public static PoolStatus setCap(String id, int cap) {
        if (isHost(id)) {
            config.get(id).setCap(cap);
            return PoolStatus.SUCCESS;
        }
        return PoolStatus.ERROR;
    }

    public static PoolStatus setPublicity(String id, boolean pub) {
        if (isHost(id)) {
            config.get(id).setPub(pub);
            return PoolStatus.SUCCESS;
        }
        return PoolStatus.ERROR;
    }

    public static ArrayList<String> getClients(String ID) {
        if (!parent.containsKey(ID) && !childr.containsKey(ID)) {
            return new ArrayList<>();
        }

        if (parent.containsKey(ID)) {
            return childr.get(parent.get(ID));
        }
        return childr.get(ID);
    }

    public static PoolStatus clearChildren(String ID) {
        if (isHost(ID)) {
            ArrayList<String> pool = childr.get(ID);
            pool.stream()
                    .filter(cur -> !cur.equals(ID))
                    .forEach(id -> {
                        Callerphone.jda.getTextChannelById(id).sendMessage(Callerphone.Callerphone + "This pool has been ended by the host channel `ID: " + id + "` (#" + Callerphone.jda.getTextChannelById(id).getName() + ").").queue();
                        parent.remove(id);
                    });

            childr.remove(ID);
            config.remove(ID);
            return PoolStatus.SUCCESS;
        } else if (!isHost(ID)) {
            return PoolStatus.IS_CHILD;
        }
        return PoolStatus.ERROR;
    }

    public static PoolStatus addChildren(String IDh, String IDc) {
        if (isHost(IDh)) {
            if (childr.get(IDh).size() >= config.get(IDh).getCap()) {
                return PoolStatus.FULL;
            }

            systemBroadCast(IDh, Callerphone.Callerphone + "Channel `ID: " + IDc + "` (#" + Callerphone.jda.getTextChannelById(IDc).getName() + ") has joined this pool. " + (childr.get(IDh).size() + 1) + "/" + config.get(IDh).getCap());
            childr.get(IDh).add(IDc);
            parent.put(IDc, IDh);
            return PoolStatus.SUCCESS;

        }
        return PoolStatus.ERROR;
    }

    public static PoolStatus removeChildren(String IDh, String IDc) {
        if (isChild(IDc)) {
            childr.get(IDh).remove(IDc);
            parent.remove(IDc);
            systemBroadCast(IDh, Callerphone.Callerphone + "Channel `ID: " + IDc + "` (#" + Callerphone.jda.getTextChannelById(IDc).getName() + ") has left this pool. " + childr.get(IDh).size() + "/" + config.get(IDh).getCap());
            return PoolStatus.SUCCESS;
        }
        return PoolStatus.ERROR;
    }

    public static boolean isHost(String ID) {
        return !parent.containsKey(ID) && childr.containsKey(ID);
    }

    public static boolean isChild(String ID) {
        return parent.containsKey(ID) && !childr.containsKey(ID);
    }

    public static void broadCast(String sender, String original, String msg) {
        if (isHost(sender)) {
            handleIsHost(sender, original, msg);
        } else if (parent.containsKey(sender)) {
            broadCast(parent.get(sender), original, msg);
        }
    }

    private static void handleIsHost(String sender, String original, String msg) {
        ArrayList<String> pool = childr.get(sender);
        pool.stream().filter(id -> !id.equals(original)).forEach(id -> {
            if (Callerphone.jda.getTextChannelById(id) == null) {
                handleChannelLeft(sender, id);
                return;
            }
            buildMessageAction(original, msg, id).queue();
        });
    }

    private static MessageAction buildMessageAction(String original, String msg, String id) {
        MessageAction ma = Callerphone.jda.getTextChannelById(id).sendMessage(msg);
        Collection<ActionRow> actionrow = new ArrayList<>();
        Collection<Button> collection = new ArrayList<>();

        String link = String.format("https://discord.com/channels/%s/%s", Callerphone.jda.getTextChannelById(original).getGuild().getId(), Callerphone.jda.getTextChannelById(original).getId());

        collection.add(Button.link(link, "From: #" + Callerphone.jda.getTextChannelById(original).getName() + " (" + Callerphone.jda.getTextChannelById(original).getGuild().getName() + ")"));

        ActionRow row = ActionRow.of(collection);
        actionrow.add(row);
        ma.setActionRows(actionrow);
        return ma;
    }

    private static void handleChannelLeft(String sender, String id) {
        if (sender.equals(id)) {
            clearChildren(sender);
            return;
        }
        childr.get(sender).remove(id);
        systemBroadCast(sender, Callerphone.Callerphone + "Channel `ID: " + id + "` has left this pool.");
    }


    public static void systemBroadCast(String IDhost, String msg) {
        ArrayList<String> pool = childr.get(IDhost);
        for (String id : pool) {
            if (Callerphone.jda.getTextChannelById(id) == null) {
                systemBroadCast(IDhost, Callerphone.Callerphone + "Channel `ID: " + id + "` has left this pool.");
                continue;
            }
            MessageAction ma = Callerphone.jda.getTextChannelById(id).sendMessage(msg);
            ma.queue();
        }
    }

}