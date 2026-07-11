package com.itsmarsss.callerphone.safety;

import java.time.Instant;
import java.util.UUID;

public final class Sanction {
    private String id = UUID.randomUUID().toString();
    private String userId;
    private String product;
    private String type;
    private boolean active = true;
    private String reason = "";
    private String moderatorId = "";
    private Instant createdAt = Instant.now();
    private Instant expiresAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? "" : reason;
    }

    public String getModeratorId() {
        return moderatorId;
    }

    public void setModeratorId(String moderatorId) {
        this.moderatorId = moderatorId == null ? "" : moderatorId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isEffective(Instant now) {
        if (!active) {
            return false;
        }
        return expiresAt == null || expiresAt.isAfter(now);
    }
}
