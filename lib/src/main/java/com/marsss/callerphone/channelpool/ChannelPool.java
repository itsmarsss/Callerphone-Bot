package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Callerphone;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
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

    private static final JDA jda = Callerphone.jda;
    private static final String CP_EMJ = Callerphone.Callerphone;
    private static final String NO_PERMISSION = CP_EMJ + "You need `Manage Channel` permission to run this command.";

    public static boolean permissionCheck(Member member, SlashCommandEvent e) {
        final boolean PERMS = !member.hasPermission(Permission.MANAGE_CHANNEL);
        if (PERMS) {
            e.reply(NO_PERMISSION).queue();

        }
        return PERMS;
    }

    public static boolean permissionCheck(Member member, Message message) {
        final boolean PERMS = !member.hasPermission(Permission.MANAGE_CHANNEL);
        if (PERMS) {
            message.reply(NO_PERMISSION).queue();

        }
        return PERMS;
    }

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
                        final TextChannel HOST_CHANNEL = Callerphone.getTextChannel(id);
                        if (HOST_CHANNEL != null) {
                            HOST_CHANNEL
                                    .sendMessage(
                                            CP_EMJ + "This pool has been ended by the host channel `ID: " + id
                                                    + "` (#" + HOST_CHANNEL.getName() + ")."
                                    ).queue();
                        }
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

            final TextChannel HOST_CHANNEL = Callerphone.getTextChannel(IDh);
            final TextChannel CHILD_CHANNEL = Callerphone.getTextChannel(IDc);
            if (HOST_CHANNEL != null) {
                if (CHILD_CHANNEL != null) {
                    systemBroadCast(IDh,
                            CP_EMJ + "Channel `ID: " + IDc
                                    + "` (#" + CHILD_CHANNEL.getName() + ") has joined this pool. "
                                    + (childr.get(IDh).size() + 1) + "/" + config.get(IDh).getCap()
                    );
                } else {
                    systemBroadCast(IDh,
                            CP_EMJ + "Channel `ID: " + IDc
                                    + "` (#[N/A NOT FOUND]) has joined this pool. "
                                    + (childr.get(IDh).size() + 1) + "/" + config.get(IDh).getCap()
                    );
                }
            }
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

            final TextChannel HOST_CHANNEL = Callerphone.getTextChannel(IDh);
            final TextChannel CHILD_CHANNEL = Callerphone.getTextChannel(IDc);
            if (HOST_CHANNEL != null) {
                if (CHILD_CHANNEL != null) {
                    systemBroadCast(IDh,
                            CP_EMJ + "Channel `ID: " + IDc + "` (#" + CHILD_CHANNEL.getName() + ") has left this pool. "
                                    + childr.get(IDh).size() + "/" + config.get(IDh).getCap()
                    );
                } else {
                    systemBroadCast(IDh,
                            CP_EMJ + "Channel `ID: " + IDc + "` (#[N/A NOT FOUND]) has left this pool. "
                                    + childr.get(IDh).size() + "/" + config.get(IDh).getCap()
                    );
                }
            }

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
            final TextChannel TEXT_CHANNEL = Callerphone.getTextChannel(id);
            if (TEXT_CHANNEL == null) {
                handleChannelLeft(sender, id);
                return;
            }
            buildMessageAction(original, msg, id).queue();
        });
    }

    private static MessageAction buildMessageAction(String original, String msg, String id) {
        MessageAction ma;

        final TextChannel TEXT_CHANNEL = Callerphone.getTextChannel(id);

        if (TEXT_CHANNEL != null) {
            ma = TEXT_CHANNEL.sendMessage(msg);
        } else {
            return null;
        }
        Collection<ActionRow> actionrow = new ArrayList<>();
        Collection<Button> collection = new ArrayList<>();

        String link;
        String name;
        String guild;

        final TextChannel ORIGINAL_CHANNEL = Callerphone.getTextChannel(original);
        if (ORIGINAL_CHANNEL != null) {
            link = String.format(
                    "https://discord.com/channels/%s/%s",
                    ORIGINAL_CHANNEL.getGuild().getId(),
                    ORIGINAL_CHANNEL.getId()
            );
            name = (ORIGINAL_CHANNEL.getName().length() > 10 ? ORIGINAL_CHANNEL.getName().substring(0, 11) + "..." : ORIGINAL_CHANNEL.getName());
            guild = (ORIGINAL_CHANNEL.getGuild().getName().length() > 10
                    ?
                    ORIGINAL_CHANNEL.getGuild().getName().substring(0, 11) + "..."
                    :
                    ORIGINAL_CHANNEL.getGuild().getName());
        } else {
            link = "[N/A NOT FOUND]";
            name = "[N/A NOT FOUND]";
            guild = "[N/A NOT FOUND]";
        }

        collection.add(
                Button.link(
                        link,
                        "From: #" + name
                                + " (" + guild + ")"
                )
        );

        ActionRow row = ActionRow.of(collection);
        actionrow.add(row);
        ma.setActionRows(actionrow);
        return ma;
    }

    private static final String LEFT_POOL = CP_EMJ + "Channel `ID: %s` has left this pool.";

    private static void handleChannelLeft(String sender, String id) {
        if (sender.equals(id)) {
            clearChildren(sender);
            return;
        }
        childr.get(sender).remove(id);
        systemBroadCast(sender, String.format(LEFT_POOL, id));
    }


    public static void systemBroadCast(String IDhost, String msg) {
        ArrayList<String> pool = childr.get(IDhost);
        for (String id : pool) {
            final TextChannel TEXT_CHANNEL = Callerphone.getTextChannel(id);
            if (TEXT_CHANNEL == null) {
                systemBroadCast(IDhost, String.format(LEFT_POOL, id));
                continue;
            }
            MessageAction ma = TEXT_CHANNEL.sendMessage(msg);
            ma.queue();
        }
    }

}