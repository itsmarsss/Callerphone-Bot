package com.marsss.callerphone;

public class Config {
    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    private String botToken;
    public Config() {
    }

    public boolean isValid() {
        return !botToken.isEmpty();
    }
}
