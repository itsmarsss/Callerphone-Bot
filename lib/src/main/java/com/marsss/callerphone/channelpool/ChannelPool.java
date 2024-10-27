package com.marsss.callerphone.channelpool;

import com.marsss.callerphone.Response;
import com.marsss.callerphone.ToolSet;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class ChannelPool {

    public static final HashMap<String, PoolConfig> config = new HashMap<>();
    public static final HashMap<String, String> parent = new HashMap<>();
    //public static final HashMap<String, ArrayList<String>> childr = new HashMap<>();

    public static boolean permissionCheck(Member member, SlashCommandInteractionEvent e) {
        if (member == null) {
            return true;
        }

        final boolean PERMS = !member.hasPermission(Permission.MANAGE_CHANNEL);
        if (PERMS) {
            e.reply(Response.NO_PERMISSION.toString()).queue();

        }
        return PERMS;
    }

    public static PoolStatus endPool(String id) {
        if (isHost(id)) {
            return clearChildren(id); // Success/Error
        } else if (isChild(id)) {
            return PoolStatus.IS_CHILD;
        }
        return PoolStatus.NOT_FOUND;
    }

    public static PoolStatus hostPool(String id) {
        if (isHost(id)) {
            return PoolStatus.IS_HOST;
        } else if (isChild(id)) {
            return PoolStatus.IS_CHILD;
        }
        config.put(id, new PoolConfig(id, "", 10, true));
        config.get(id).children.add(id);
        return PoolStatus.SUCCESS;
    }

    public static PoolStatus joinPool(String hostId, String clientId, String pwd) {
        if (!config.containsKey(hostId)) {
            return PoolStatus.NOT_FOUND;
        } else if (isHost(clientId)) {
            return PoolStatus.IS_HOST;
        } else if (isChild(clientId)) {
            return PoolStatus.IS_CHILD;
        } else if (!config.get(hostId).getPwd().equals(pwd)) {
            if (!config.get(hostId).isPub()) {
                return PoolStatus.NOT_FOUND;
            }
            return PoolStatus.INCORRECT_PASS;
        }
        return addChildren(hostId, clientId); // Success/Full/Error
    }

    public static PoolStatus leavePool(String id) {
        if (isHost(id)) {
            return PoolStatus.IS_HOST;
        } else if (isChild(id)) {
            return removeChild(parent.get(id), id); // Success/Error
        }
        return PoolStatus.NOT_FOUND;
    }

    public static PoolStatus setCap(String id, int cap) {
        if (isHost(id)) {
            config.get(id).setCap(cap);
            return PoolStatus.SUCCESS;
        }
        return PoolStatus.NOT_FOUND;
    }


    public static LinkedList<String> getClients(String id) {
        if (!parent.containsKey(id) && !config.containsKey(id)) {
            return new LinkedList<>();
        }

        if (parent.containsKey(id)) {
            return config.get(parent.get(id)).children;
        }
        return config.get(id).children;
    }

    public static PoolStatus setPublicity(String id, boolean pub) {
        if (isHost(id)) {
            config.get(id).setPub(pub);
            return PoolStatus.SUCCESS;
        } else {
            return PoolStatus.NOT_FOUND;
        }
    }

    public static boolean hasPassword(String id) {
        if (config.containsKey(id)) {
            return !config.get(id).getPwd().isEmpty();
        }
        return false;
    }

    public static PoolStatus setPassword(String id, String pwd) {
        if (isHost(id)) {
            config.get(id).setPwd(pwd);
            return PoolStatus.SUCCESS;
        } else {
            return PoolStatus.NOT_FOUND;
        }
    }

    public static String getPassword(String id) {
        if (config.containsKey(id)) {
            return config.get(id).getPwd();
        }
        return "";
    }

    public static String getPublicity(String id) {
        if (config.containsKey(id)) {
            if(config.get(id).isPub()) {
                return "true";
            }
            return "false";
        }
        return "false";
    }

    public static int getCapacity(String id) {
        if (config.containsKey(id)) {
            return config.get(id).getCap();
        }
        return 0;
    }

    public static PoolStatus clearChildren(String id) {
        if (isHost(id)) {
            LinkedList<String> pool = config.get(id).children;
            pool.stream()
                    .filter(cur -> !cur.equals(id))
                    .forEach(iId -> {
                        final TextChannel HOST_CHANNEL = ToolSet.getTextChannel(iId);
                        if (HOST_CHANNEL != null) {
                            HOST_CHANNEL
                                    .sendMessage(
                                            ToolSet.CP_EMJ + "This pool has been ended by the host channel `ID: " + iId
                                                    + "` (#" + HOST_CHANNEL.getName() + ")."
                                    ).queue();
                        }
                        parent.remove(iId);
                    });

            config.remove(id);
            return PoolStatus.SUCCESS;
        }
        return PoolStatus.ERROR;
    }

    public static PoolStatus addChildren(String hostId, String childId) {
        if (isHost(hostId)) {
            if (config.get(hostId).children.size() >= config.get(hostId).getCap()) {
                return PoolStatus.FULL;
            }

            final TextChannel HOST_CHANNEL = ToolSet.getTextChannel(hostId);
            final TextChannel CHILD_CHANNEL = ToolSet.getTextChannel(childId);
            if (HOST_CHANNEL != null) {
                if (CHILD_CHANNEL != null) {
                    systemBroadCast(hostId,
                            ToolSet.CP_EMJ + "Channel `ID: " + childId
                                    + "` (#" + CHILD_CHANNEL.getName() + ") has joined this pool. "
                                    + (config.get(hostId).children.size() + 1) + "/" + config.get(hostId).getCap()
                    );
                } else {
                    systemBroadCast(hostId,
                            ToolSet.CP_EMJ + "Channel `ID: " + childId
                                    + "` (#[N/A NOT FOUND]) has joined this pool. "
                                    + (config.get(hostId).children.size() + 1) + "/" + config.get(hostId).getCap()
                    );
                }
            }

            config.get(hostId).children.add(childId);
            parent.put(childId, hostId);
            return PoolStatus.SUCCESS;
        }
        return PoolStatus.ERROR;
    }

    public static PoolStatus removeChild(String hostID, String clientID) {
        if (isChild(hostID)) {
            return PoolStatus.IS_CHILD;
        }

        if (isChild(clientID)) {
            config.get(hostID).children.remove(clientID);
            parent.remove(clientID);

            final TextChannel HOST_CHANNEL = ToolSet.getTextChannel(hostID);
            final TextChannel CHILD_CHANNEL = ToolSet.getTextChannel(clientID);
            if (HOST_CHANNEL != null) {
                if (CHILD_CHANNEL != null) {
                    systemBroadCast(hostID,
                            ToolSet.CP_EMJ + "Channel `ID: " + clientID + "` (#" + CHILD_CHANNEL.getName() + ") has left this pool. "
                                    + config.get(hostID).children.size() + "/" + config.get(hostID).getCap()
                    );
                } else {
                    systemBroadCast(hostID,
                            ToolSet.CP_EMJ + "Channel `ID: " + clientID + "` (#[N/A NOT FOUND]) has left this pool. "
                                    + config.get(hostID).children.size() + "/" + config.get(hostID).getCap()
                    );
                }
            }

            return PoolStatus.SUCCESS;
        } else {
            return PoolStatus.NOT_FOUND;
        }
    }

    public static boolean isHost(String id) {
        return !parent.containsKey(id) && config.containsKey(id);
    }

    public static boolean isChild(String id) {
        return parent.containsKey(id) && !config.containsKey(id);
    }

    public static void broadCast(String sender, String original, String msg) {
        if (isHost(sender)) {
            handleIsHost(sender, original, msg);
        } else if (parent.containsKey(sender)) {
            broadCast(parent.get(sender), original, msg);
        }
    }

    private static void handleIsHost(String sender, String original, String msg) {
        LinkedList<String> pool = config.get(sender).children;
        pool.stream().filter(id -> !id.equals(original)).forEach(id -> {
            final TextChannel TEXT_CHANNEL = ToolSet.getTextChannel(id);
            if (TEXT_CHANNEL == null) {
                handleChannelLeft(sender, id);
                return;
            }
            final MessageCreateAction MESSAGE_ACTION = buildMessageAction(original, msg, id);
            if (MESSAGE_ACTION != null) {
                MESSAGE_ACTION.complete();
            }
        });
    }

    private static MessageCreateAction buildMessageAction(String original, String msg, String id) {
        MessageCreateAction ma;

        final TextChannel TEXT_CHANNEL = ToolSet.getTextChannel(id);

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

        final TextChannel ORIGINAL_CHANNEL = ToolSet.getTextChannel(original);
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
        ma = ma.setComponents(actionrow);
        return ma;
    }

    private static void handleChannelLeft(String sender, String id) {
        if (sender.equals(id)) {
            clearChildren(sender);
            return;
        }
        config.get(sender).children.remove(id);
        systemBroadCast(sender, String.format(PoolResponse.LEFT_POOL.toString(), id));
    }


    public static void systemBroadCast(String hostId, String msg) {
        LinkedList<String> pool = config.get(hostId).children;
        for (String id : pool) {
            final TextChannel TEXT_CHANNEL = ToolSet.getTextChannel(id);
            if (TEXT_CHANNEL == null) {
                systemBroadCast(hostId, String.format(PoolResponse.LEFT_POOL.toString(), id));
                continue;
            }
            MessageCreateAction ma = TEXT_CHANNEL.sendMessage(msg);
            ma.complete();
        }
    }

}