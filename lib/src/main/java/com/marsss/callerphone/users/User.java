package com.marsss.callerphone.users;

public class User {

/*
"id": "1234567890",
"status": "user/moderator/blacklist",
"reason": "",
"prefix": "",
"credits": 0,
"executed": 0,
"transmitted": 0
*/
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
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    private String id;
    private UserStatus status;
    private String reason;
    private long credits;
    private long executed;
    private long transmitted;

    public User() {
    }

    public User(String id, UserStatus status, String reason, long credits, long executed, long transmitted) {
        this.id = id;
        this.status = status;
        this.reason = reason;
        this.credits = credits;
        this.executed = executed;
        this.transmitted = transmitted;
    }
}
