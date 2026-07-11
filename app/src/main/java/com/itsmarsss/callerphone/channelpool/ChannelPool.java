package com.itsmarsss.callerphone.channelpool;

import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.ToolSet;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChannelPool {

    public static final ConcurrentHashMap<String, PoolConfig> config = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, String> parent = new ConcurrentHashMap<>();

    public static boolean permissionCheck(Member member, SlashCommandInteractionEvent e) {
        if (member == null) {
            return true;
        }

        if (!member.hasPermission(Permission.MANAGE_CHANNEL)) {
            e.reply(Response.NO_PERMISSION.toString()).queue();
            return true;
        }
        return false;
    }

    public static PoolStatus endPool(String id) {
        if (isHost(id)) {
            return clearChildren(id);
        }
        if (isChild(id)) {
            return PoolStatus.IS_CHILD;
        }
        return PoolStatus.NOT_FOUND;
    }

    public static PoolStatus hostPool(String id) {
        if (isHost(id)) {
            return PoolStatus.IS_HOST;
        }
        if (isChild(id)) {
            return PoolStatus.IS_CHILD;
        }
        PoolConfig pool = new PoolConfig(id, "", Constants.POOL_DEFAULT_CAPACITY, true);
        pool.children.add(id);
        config.put(id, pool);
        return PoolStatus.SUCCESS;
    }

    public static PoolStatus joinPool(String hostId, String clientId, String pwd) {
        PoolConfig host = config.get(hostId);
        if (host == null) {
            return PoolStatus.NOT_FOUND;
        }
        if (isHost(clientId)) {
            return PoolStatus.IS_HOST;
        }
        if (isChild(clientId)) {
            return PoolStatus.IS_CHILD;
        }
        if (!host.getPwd().equals(pwd)) {
            if (!host.isPub()) {
                return PoolStatus.NOT_FOUND;
            }
            return PoolStatus.INCORRECT_PASS;
        }
        return addChildren(hostId, clientId);
    }

    public static PoolStatus leavePool(String id) {
        if (isHost(id)) {
            return PoolStatus.IS_HOST;
        }
        if (isChild(id)) {
            return removeChild(parent.get(id), id);
        }
        return PoolStatus.NOT_FOUND;
    }

    public static PoolStatus setCap(String id, int cap) {
        PoolConfig pool = config.get(id);
        if (pool == null || !isHost(id)) {
            return PoolStatus.NOT_FOUND;
        }
        pool.setCap(cap);
        return PoolStatus.SUCCESS;
    }

    public static LinkedList<String> getClients(String id) {
        String hostId = parent.getOrDefault(id, id);
        PoolConfig pool = config.get(hostId);
        if (pool == null) {
            return new LinkedList<>();
        }
        return new LinkedList<>(pool.children);
    }

    public static PoolStatus setPublicity(String id, boolean pub) {
        PoolConfig pool = config.get(id);
        if (pool == null || !isHost(id)) {
            return PoolStatus.NOT_FOUND;
        }
        pool.setPub(pub);
        return PoolStatus.SUCCESS;
    }

    public static boolean hasPassword(String id) {
        PoolConfig pool = config.get(id);
        return pool != null && !pool.getPwd().isEmpty();
    }

    public static PoolStatus setPassword(String id, String pwd) {
        PoolConfig pool = config.get(id);
        if (pool == null || !isHost(id)) {
            return PoolStatus.NOT_FOUND;
        }
        pool.setPwd(pwd);
        return PoolStatus.SUCCESS;
    }

    public static String getPassword(String id) {
        PoolConfig pool = config.get(id);
        return pool != null ? pool.getPwd() : "";
    }

    public static String getPublicity(String id) {
        PoolConfig pool = config.get(id);
        return pool != null && pool.isPub() ? "true" : "false";
    }

    public static int getCapacity(String id) {
        PoolConfig pool = config.get(id);
        return pool != null ? pool.getCap() : 0;
    }

    public static PoolStatus clearChildren(String hostId) {
        PoolConfig pool = config.get(hostId);
        if (pool == null || !isHost(hostId)) {
            return PoolStatus.ERROR;
        }

        TextChannel hostChannel = ToolSet.getTextChannel(hostId);
        String hostName = hostChannel != null ? hostChannel.getName() : "unknown";

        for (String childId : pool.children) {
            if (childId.equals(hostId)) {
                continue;
            }
            TextChannel childChannel = ToolSet.getTextChannel(childId);
            if (childChannel != null) {
                childChannel.sendMessage(
                        ToolSet.CP_EMJ + "This pool has been ended by the host channel `ID: " + hostId
                                + "` (#" + hostName + ")."
                ).queue();
            }
            parent.remove(childId);
        }

        config.remove(hostId);
        return PoolStatus.SUCCESS;
    }

    public static PoolStatus addChildren(String hostId, String childId) {
        PoolConfig pool = config.get(hostId);
        if (pool == null || !isHost(hostId)) {
            return PoolStatus.ERROR;
        }
        if (pool.children.size() >= pool.getCap()) {
            return PoolStatus.FULL;
        }

        TextChannel childChannel = ToolSet.getTextChannel(childId);
        String childName = childChannel != null ? childChannel.getName() : "[N/A NOT FOUND]";
        int nextSize = pool.children.size() + 1;

        systemBroadCast(hostId,
                ToolSet.CP_EMJ + "Channel `ID: " + childId
                        + "` (#" + childName + ") has joined this pool. "
                        + nextSize + "/" + pool.getCap()
        );

        pool.children.add(childId);
        parent.put(childId, hostId);
        return PoolStatus.SUCCESS;
    }

    public static PoolStatus removeChild(String hostId, String clientId) {
        if (isChild(hostId)) {
            return PoolStatus.IS_CHILD;
        }
        if (!isChild(clientId)) {
            return PoolStatus.NOT_FOUND;
        }

        PoolConfig pool = config.get(hostId);
        if (pool == null) {
            return PoolStatus.NOT_FOUND;
        }

        pool.children.remove(clientId);
        parent.remove(clientId);

        TextChannel childChannel = ToolSet.getTextChannel(clientId);
        String childName = childChannel != null ? childChannel.getName() : "[N/A NOT FOUND]";

        systemBroadCast(hostId,
                ToolSet.CP_EMJ + "Channel `ID: " + clientId + "` (#" + childName + ") has left this pool. "
                        + pool.children.size() + "/" + pool.getCap()
        );

        return PoolStatus.SUCCESS;
    }

    public static boolean isHost(String id) {
        return !parent.containsKey(id) && config.containsKey(id);
    }

    public static boolean isChild(String id) {
        return parent.containsKey(id) && !config.containsKey(id);
    }

    public static boolean isInPool(String id) {
        return isHost(id) || isChild(id);
    }

    public static void broadCast(String sender, String original, String msg) {
        String hostId = isHost(sender) ? sender : parent.get(sender);
        if (hostId == null) {
            return;
        }
        handleIsHost(hostId, original, msg);
    }

    private static void handleIsHost(String hostId, String original, String msg) {
        PoolConfig pool = config.get(hostId);
        if (pool == null) {
            return;
        }

        List<String> leftChannels = new CopyOnWriteArrayList<>();
        for (String id : pool.children) {
            if (id.equals(original)) {
                continue;
            }
            MessageCreateAction action = buildMessageAction(original, msg, id);
            if (action == null) {
                leftChannels.add(id);
                continue;
            }
            action.queue();
        }

        for (String leftId : leftChannels) {
            handleChannelLeft(hostId, leftId);
        }
    }

    private static MessageCreateAction buildMessageAction(String original, String msg, String destinationId) {
        TextChannel destination = ToolSet.getTextChannel(destinationId);
        if (destination == null) {
            return null;
        }

        TextChannel origin = ToolSet.getTextChannel(original);
        MessageCreateAction action = destination.sendMessage(msg);
        if (origin == null) {
            return action;
        }

        String link = String.format("https://discord.com/channels/%s/%s",
                origin.getGuild().getId(), origin.getId());
        String name = truncate(origin.getName(), 10);
        String guild = truncate(origin.getGuild().getName(), 10);

        return action.setComponents(
                ActionRow.of(Button.link(link, "From: #" + name + " (" + guild + ")"))
        );
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        return value.length() > maxLen ? value.substring(0, maxLen + 1) + "..." : value;
    }

    private static void handleChannelLeft(String hostId, String channelId) {
        if (hostId.equals(channelId)) {
            clearChildren(hostId);
            return;
        }
        PoolConfig pool = config.get(hostId);
        if (pool == null) {
            return;
        }
        pool.children.remove(channelId);
        parent.remove(channelId);
        systemBroadCast(hostId, String.format(PoolResponse.LEFT_POOL.toString(), channelId));
    }

    public static void systemBroadCast(String hostId, String msg) {
        PoolConfig pool = config.get(hostId);
        if (pool == null) {
            return;
        }

        List<String> leftChannels = new CopyOnWriteArrayList<>();
        for (String id : pool.children) {
            TextChannel channel = ToolSet.getTextChannel(id);
            if (channel == null) {
                leftChannels.add(id);
                continue;
            }
            channel.sendMessage(msg).queue();
        }

        for (String leftId : leftChannels) {
            if (!leftId.equals(hostId)) {
                pool.children.remove(leftId);
                parent.remove(leftId);
            }
        }
    }
}
