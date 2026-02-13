package com.amethyst.client.irc;

import java.util.UUID;

/**
 * Представляет пользователя мода в IRC системе
 */
public class IRCUser {
    
    private final UUID uuid;
    private String customLabel;
    private int color;
    private long lastSeen;
    
    public IRCUser(UUID uuid, String customLabel, int color) {
        this.uuid = uuid;
        this.customLabel = customLabel;
        this.color = color;
        this.lastSeen = System.currentTimeMillis();
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getCustomLabel() {
        return customLabel;
    }
    
    public void setCustomLabel(String customLabel) {
        this.customLabel = customLabel;
        this.lastSeen = System.currentTimeMillis();
    }
    
    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
        this.lastSeen = System.currentTimeMillis();
    }
    
    public long getLastSeen() {
        return lastSeen;
    }
    
    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return "IRCUser{" +
                "uuid=" + uuid +
                ", label='" + customLabel + '\'' +
                ", color=" + Integer.toHexString(color) +
                '}';
    }
}