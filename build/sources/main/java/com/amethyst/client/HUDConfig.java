package com.amethyst.client;

import java.io.*;
import java.util.Properties;

public class HUDConfig {

    private static final File CONFIG_FILE = new File("amethyst/hud_config.properties");
    private static Properties props = new Properties();

    // Positions
    private static int soupCounterX = 10, soupCounterY = 10;
    private static int fpsCounterX = 10, fpsCounterY = 30;
    private static int pingCounterX = 10, pingCounterY = 50;
    private static int clockX = 10, clockY = 70;
    private static int cpsCounterX = 10, cpsCounterY = 90;
    private static int scoreboardX = -1, scoreboardY = 10;
    private static int chatX = 2, chatY = -1;

    // Scales (новое!)
    private static float soupCounterScale = 1.0f;
    private static float fpsCounterScale = 1.0f;
    private static float pingCounterScale = 1.0f;
    private static float clockScale = 1.0f;
    private static float cpsCounterScale = 1.0f;
    private static float scoreboardScale = 1.0f;
    private static float chatScale = 1.0f;

    static {
        load();
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
            save();
            return;
        }

        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            
            // Load positions
            soupCounterX = getInt("soupCounterX", 10);
            soupCounterY = getInt("soupCounterY", 10);
            fpsCounterX = getInt("fpsCounterX", 10);
            fpsCounterY = getInt("fpsCounterY", 30);
            pingCounterX = getInt("pingCounterX", 10);
            pingCounterY = getInt("pingCounterY", 50);
            clockX = getInt("clockX", 10);
            clockY = getInt("clockY", 70);
            cpsCounterX = getInt("cpsCounterX", 10);
            cpsCounterY = getInt("cpsCounterY", 90);
            scoreboardX = getInt("scoreboardX", -1);
            scoreboardY = getInt("scoreboardY", 10);
            chatX = getInt("chatX", 2);
            chatY = getInt("chatY", -1);
            
            // Load scales
            soupCounterScale = getFloat("soupCounterScale", 1.0f);
            fpsCounterScale = getFloat("fpsCounterScale", 1.0f);
            pingCounterScale = getFloat("pingCounterScale", 1.0f);
            clockScale = getFloat("clockScale", 1.0f);
            cpsCounterScale = getFloat("cpsCounterScale", 1.0f);
            scoreboardScale = getFloat("scoreboardScale", 1.0f);
            chatScale = getFloat("chatScale", 1.0f);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            // Save positions
            props.setProperty("soupCounterX", String.valueOf(soupCounterX));
            props.setProperty("soupCounterY", String.valueOf(soupCounterY));
            props.setProperty("fpsCounterX", String.valueOf(fpsCounterX));
            props.setProperty("fpsCounterY", String.valueOf(fpsCounterY));
            props.setProperty("pingCounterX", String.valueOf(pingCounterX));
            props.setProperty("pingCounterY", String.valueOf(pingCounterY));
            props.setProperty("clockX", String.valueOf(clockX));
            props.setProperty("clockY", String.valueOf(clockY));
            props.setProperty("cpsCounterX", String.valueOf(cpsCounterX));
            props.setProperty("cpsCounterY", String.valueOf(cpsCounterY));
            props.setProperty("scoreboardX", String.valueOf(scoreboardX));
            props.setProperty("scoreboardY", String.valueOf(scoreboardY));
            props.setProperty("chatX", String.valueOf(chatX));
            props.setProperty("chatY", String.valueOf(chatY));
            
            // Save scales
            props.setProperty("soupCounterScale", String.valueOf(soupCounterScale));
            props.setProperty("fpsCounterScale", String.valueOf(fpsCounterScale));
            props.setProperty("pingCounterScale", String.valueOf(pingCounterScale));
            props.setProperty("clockScale", String.valueOf(clockScale));
            props.setProperty("cpsCounterScale", String.valueOf(cpsCounterScale));
            props.setProperty("scoreboardScale", String.valueOf(scoreboardScale));
            props.setProperty("chatScale", String.valueOf(chatScale));
            
            props.store(fos, "Amethyst HUD Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getInt(String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static float getFloat(String key, float defaultValue) {
        String value = props.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ═══ GETTERS & SETTERS - POSITIONS ═══

    public static int getSoupCounterX() { return soupCounterX; }
    public static void setSoupCounterX(int x) { soupCounterX = x; save(); }
    
    public static int getSoupCounterY() { return soupCounterY; }
    public static void setSoupCounterY(int y) { soupCounterY = y; save(); }

    public static int getFPSCounterX() { return fpsCounterX; }
    public static void setFPSCounterX(int x) { fpsCounterX = x; save(); }
    
    public static int getFPSCounterY() { return fpsCounterY; }
    public static void setFPSCounterY(int y) { fpsCounterY = y; save(); }

    public static int getPingCounterX() { return pingCounterX; }
    public static void setPingCounterX(int x) { pingCounterX = x; save(); }
    
    public static int getPingCounterY() { return pingCounterY; }
    public static void setPingCounterY(int y) { pingCounterY = y; save(); }

    public static int getClockX() { return clockX; }
    public static void setClockX(int x) { clockX = x; save(); }
    
    public static int getClockY() { return clockY; }
    public static void setClockY(int y) { clockY = y; save(); }

    public static int getCPSCounterX() { return cpsCounterX; }
    public static void setCPSCounterX(int x) { cpsCounterX = x; save(); }
    
    public static int getCPSCounterY() { return cpsCounterY; }
    public static void setCPSCounterY(int y) { cpsCounterY = y; save(); }

    public static int getScoreboardX() { return scoreboardX; }
    public static void setScoreboardX(int x) { scoreboardX = x; save(); }
    
    public static int getScoreboardY() { return scoreboardY; }
    public static void setScoreboardY(int y) { scoreboardY = y; save(); }

    public static int getChatX() { return chatX; }
    public static void setChatX(int x) { chatX = x; save(); }
    
    public static int getChatY() { return chatY; }
    public static void setChatY(int y) { chatY = y; save(); }

    // ═══ GETTERS & SETTERS - SCALES ═══

    public static float getSoupCounterScale() { return soupCounterScale; }
    public static void setSoupCounterScale(float scale) { soupCounterScale = scale; save(); }

    public static float getFPSCounterScale() { return fpsCounterScale; }
    public static void setFPSCounterScale(float scale) { fpsCounterScale = scale; save(); }

    public static float getPingCounterScale() { return pingCounterScale; }
    public static void setPingCounterScale(float scale) { pingCounterScale = scale; save(); }

    public static float getClockScale() { return clockScale; }
    public static void setClockScale(float scale) { clockScale = scale; save(); }

    public static float getCPSCounterScale() { return cpsCounterScale; }
    public static void setCPSCounterScale(float scale) { cpsCounterScale = scale; save(); }

    public static float getScoreboardScale() { return scoreboardScale; }
    public static void setScoreboardScale(float scale) { scoreboardScale = scale; save(); }

    public static float getChatScale() { return chatScale; }
    public static void setChatScale(float scale) { chatScale = scale; save(); }
}