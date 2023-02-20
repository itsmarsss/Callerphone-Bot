package com.marsss.callerphone;

import com.marsss.callerphone.channelpool.ChannelPool;
import com.marsss.callerphone.channelpool.PoolConfig;
import com.marsss.callerphone.users.BotUser;
import com.marsss.callerphone.users.UserStatus;
import net.dv8tion.jda.api.entities.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

public class Storage {
    public static final Logger logger = LoggerFactory.getLogger(Storage.class);

    public static final LinkedList<String> filter = new LinkedList<>();
    public static final HashMap<String, BotUser> users = new HashMap<>();

    static void readData() {
        try {
            getFilter(new File(Callerphone.parent + "/filter.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("------------------------------");
            logger.error("Error with filter.txt");
        }

        System.out.println("------------------------------");

        try {
            importUsers(new File(Callerphone.parent + "/users.json"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("------------------------------");
            logger.error("Error with pools.txt");
        }

        try {
            importPools(new File(Callerphone.parent + "/pools.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("------------------------------");
            logger.error("Error with pools.txt");
        }

        try {
            importPoolsConfig(new File(Callerphone.parent + "/poolconfig.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("------------------------------");
            logger.error("Error with poolconfig.txt");
        }

        System.out.println("------------------------------");
    }

    private static void getFilter(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();

            while (line != null) {
                filter.add(line);
                line = br.readLine();
            }
        }
    }

    private static boolean importUsers(File file) throws IOException, ParseException {
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(file)) {
            JSONObject object = (JSONObject) parser.parse(reader);

            JSONArray usersArr = (JSONArray) object.get("users");

            for(Object user : usersArr) {
                try {
                    JSONObject userObj = (JSONObject) user;
                    String id = (String) userObj.get("id");
                    String statusStr = (String) userObj.get("status");
                    String reason = (String) userObj.get("reason");
                    String prefix = (String) userObj.get("prefix");
                    long credits = Long.parseLong((String) userObj.get("credits"));
                    long executed = Long.parseLong((String) userObj.get("executed"));
                    long transmitted = Long.parseLong((String) userObj.get("transmitted"));

                    UserStatus status = UserStatus.USER;

                    switch (statusStr) {
                        case "user":
                            status = UserStatus.USER;
                            break;
                        case "moderator":
                            status = UserStatus.MODERATOR;
                            break;
                        case "warned":
                            status = UserStatus.WARNED;
                            break;
                        case "blacklisted":
                            status = UserStatus.BLACKLISTED;
                            break;
                    }

                    BotUser botUser = new BotUser(id, status, reason, prefix, credits, executed, transmitted);

                    users.put(id, botUser);
                } catch(Exception e) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void importPoolsConfig(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                String[] split = line.split(":");
                String host = split[0];
                String[] config = split[1].split(",");
                ChannelPool.config.put(host, new PoolConfig(config[0], Integer.parseInt(config[1]), Boolean.parseBoolean(config[2])));

                line = br.readLine();
            }
        }
    }

    private static void importPools(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                String[] split = line.split(":");
                String host = split[0];
                String[] children = split[1].split(",");
                ChannelPool.hostPool(host);
                for (String id : children) {
                    ChannelPool.joinPool(host, id, "");
                }
                line = br.readLine();
            }
        }
    }





    public static void reward(User user, int amount) {
        userCredits.put(user.getId(), userCredits.getOrDefault(user.getId(), 0L) + amount);
        logger.info("User: " + user.getId() + " earned: " + amount + " credits.");
    }

    public static long getCredits(User user) {
        return userCredits.getOrDefault(user.getId(), 0L);
    }

    public static void addExecute(User user, int amount) {
        userExecuted.put(user.getId(), userExecuted.getOrDefault(user.getId(), 0L) + amount);

        userTransmitted.put(user.getId(), userTransmitted.getOrDefault(user.getId(), 0L));
    }

    public static long getExecuted(User user) {
        return userExecuted.getOrDefault(user.getId(), 0L);
    }

    public static void addTransmit(User user, int amount) {
        userTransmitted.put(user.getId(), userTransmitted.getOrDefault(user.getId(), 0L) + amount);

        userExecuted.put(user.getId(), userExecuted.getOrDefault(user.getId(), 0L));
    }

    public static long getTransmitted(User user) {
        return userTransmitted.getOrDefault(user.getId(), 0L);
    }

    public static long getUserCooldown(User user) {
        poolChatCoolDown.put(user.getId(), poolChatCoolDown.getOrDefault(user.getId(), 0L));

        return poolChatCoolDown.get(user.getId());
    }

    public static void updateUserCooldown(User user) {
        poolChatCoolDown.put(user.getId(), System.currentTimeMillis());
    }


}
