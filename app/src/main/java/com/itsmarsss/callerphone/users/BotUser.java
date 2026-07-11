package com.itsmarsss.callerphone.users;

import com.itsmarsss.callerphone.Constants;
import com.itsmarsss.callerphone.Response;
import com.itsmarsss.callerphone.minigames.IMiniGame;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class BotUser implements Comparable<BotUser> {

    private String id = "";
    private UserStatus status = UserStatus.USER;
    private String reason = "";
    private String prefix = "";
    private long credits = 0;
    private long executed = 0;
    private long transmitted = 0;

    private final ConcurrentHashMap<String, IMiniGame> miniGames = new ConcurrentHashMap<>();

    public BotUser() {
    }

    public BotUser(String id) {
        this.id = id;
    }

    public BotUser(String id, UserStatus status, String reason, String prefix,
                   long credits, long executed, long transmitted) {
        this.id = id;
        this.status = status;
        this.reason = reason;
        this.prefix = prefix;
        this.credits = credits;
        this.executed = executed;
        this.transmitted = transmitted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status != null ? status : UserStatus.USER;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason != null ? reason : "";
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix != null ? prefix : "";
    }

    public long getCredits() {
        return credits;
    }

    public void setCredits(long credits) {
        this.credits = credits;
    }

    public long getExecuted() {
        return executed;
    }

    public void setExecuted(long executed) {
        this.executed = executed;
    }

    public long getTransmitted() {
        return transmitted;
    }

    public void setTransmitted(long transmitted) {
        this.transmitted = transmitted;
    }

    public String toJSON() {
        String statusName = status != null ? status.name().toLowerCase(Locale.ROOT) : "user";
        return String.format(Response.USER_TEMPLATE.toString(),
                id, statusName, reason, prefix, credits, executed, transmitted);
    }

    public void addCredits(long amount) {
        this.credits += amount;
    }

    public void addExecuted(long amount) {
        this.executed += amount;
    }

    public void addTransmitted(long amount) {
        this.transmitted += amount;
    }

    public boolean addGame(IMiniGame game) {
        if (game == null || miniGames.size() >= Constants.USER_MAX_GAMES) {
            return false;
        }
        miniGames.put(game.getID(), game);
        return true;
    }

    public boolean removeGame(String gameId) {
        return miniGames.remove(gameId) != null;
    }

    public IMiniGame getGame(String gameId) {
        return miniGames.get(gameId);
    }

    public boolean setGame(IMiniGame game) {
        if (game == null || !miniGames.containsKey(game.getID())) {
            return false;
        }
        miniGames.put(game.getID(), game);
        return true;
    }

    @Override
    public int compareTo(@NotNull BotUser user) {
        return Long.compare(user.credits, this.credits);
    }
}
