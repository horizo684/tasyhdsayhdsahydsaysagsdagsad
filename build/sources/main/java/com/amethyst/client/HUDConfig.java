package com.amethyst.client;

public class HUDConfig {

    // ── Existing HUD elements ─────────────────────────────────────────────────
    private static int soupCounterX = 5,  soupCounterY = 20;
    private static int fpsCounterX  = 5,  fpsCounterY  = 35;
    private static int pingCounterX = 5,  pingCounterY = 50;
    private static int clockX       = 5,  clockY       = 65;
    private static int cpsCounterX  = 5,  cpsCounterY  = 80;

    // ── New: Scoreboard ───────────────────────────────────────────────────────
    private static int scoreboardX  = -1; // -1 = auto (right side)
    private static int scoreboardY  = 5;

    // ── New: Custom Chat ──────────────────────────────────────────────────────
    private static int chatX = 2;
    private static int chatY = -1; // -1 = auto (bottom)

    // ── Scale values ──────────────────────────────────────────────────────────
    private static float soupCounterScale  = 1.0f;
    private static float cpsCounterScale   = 1.0f;
    private static float fpsCounterScale   = 1.0f;
    private static float pingCounterScale  = 1.0f;
    private static float clockScale        = 1.0f;

    // ─────────────────────────────────────────────────────────────────────────
    // Existing getters/setters
    // ─────────────────────────────────────────────────────────────────────────

    public static int getSoupCounterX()  { return soupCounterX; }
    public static int getSoupCounterY()  { return soupCounterY; }
    public static void setSoupCounterX(int x) { soupCounterX = x; save(); }
    public static void setSoupCounterY(int y) { soupCounterY = y; save(); }

    public static int getFPSCounterX()   { return fpsCounterX; }
    public static int getFPSCounterY()   { return fpsCounterY; }
    public static void setFPSCounterX(int x) { fpsCounterX = x; save(); }
    public static void setFPSCounterY(int y) { fpsCounterY = y; save(); }

    public static int getPingCounterX()  { return pingCounterX; }
    public static int getPingCounterY()  { return pingCounterY; }
    public static void setPingCounterX(int x) { pingCounterX = x; save(); }
    public static void setPingCounterY(int y) { pingCounterY = y; save(); }

    public static int getClockX()        { return clockX; }
    public static int getClockY()        { return clockY; }
    public static void setClockX(int x)  { clockX = x; save(); }
    public static void setClockY(int y)  { clockY = y; save(); }

    public static int getCPSCounterX()   { return cpsCounterX; }
    public static int getCPSCounterY()   { return cpsCounterY; }
    public static void setCPSCounterX(int x) { cpsCounterX = x; save(); }
    public static void setCPSCounterY(int y) { cpsCounterY = y; save(); }

    // ── Scoreboard ────────────────────────────────────────────────────────────

    /** Returns X; if -1 caller should compute auto (screen width - panelWidth - 5) */
    public static int getScoreboardX()   { return scoreboardX; }
    public static int getScoreboardY()   { return scoreboardY; }
    public static void setScoreboardX(int x) { scoreboardX = x; save(); }
    public static void setScoreboardY(int y) { scoreboardY = y; save(); }

    // ── Chat ──────────────────────────────────────────────────────────────────

    /** Returns X for chat panel */
    public static int getChatX()         { return chatX; }
    /** Returns Y; if -1 caller should compute auto (screen height - 50) */
    public static int getChatY()         { return chatY; }
    public static void setChatX(int x)   { chatX = x; save(); }
    public static void setChatY(int y)   { chatY = y; save(); }

    // ── Scale ─────────────────────────────────────────────────────────────────

    public static float getSoupCounterScale()  { return soupCounterScale; }
    public static void  setSoupCounterScale(float s) { soupCounterScale = clamp(s); save(); }
    public static float getCPSCounterScale()   { return cpsCounterScale; }
    public static void  setCPSCounterScale(float s)  { cpsCounterScale = clamp(s); save(); }
    public static float getFPSCounterScale()   { return fpsCounterScale; }
    public static void  setFPSCounterScale(float s)  { fpsCounterScale = clamp(s); save(); }
    public static float getPingCounterScale()  { return pingCounterScale; }
    public static void  setPingCounterScale(float s) { pingCounterScale = clamp(s); save(); }
    public static float getClockScale()        { return clockScale; }
    public static void  setClockScale(float s) { clockScale = clamp(s); save(); }

    private static float clamp(float s) { return Math.max(0.5f, Math.min(3.0f, s)); }

    // ── Persistence ───────────────────────────────────────────────────────────

    public static void load() {
        soupCounterX = AmethystClient.config.getInt("hud.soupCounterX", 5);
        soupCounterY = AmethystClient.config.getInt("hud.soupCounterY", 20);
        fpsCounterX  = AmethystClient.config.getInt("hud.fpsCounterX",  5);
        fpsCounterY  = AmethystClient.config.getInt("hud.fpsCounterY",  35);
        pingCounterX = AmethystClient.config.getInt("hud.pingCounterX", 5);
        pingCounterY = AmethystClient.config.getInt("hud.pingCounterY", 50);
        clockX       = AmethystClient.config.getInt("hud.clockX",       5);
        clockY       = AmethystClient.config.getInt("hud.clockY",       65);
        cpsCounterX  = AmethystClient.config.getInt("hud.cpsCounterX",  5);
        cpsCounterY  = AmethystClient.config.getInt("hud.cpsCounterY",  80);

        scoreboardX  = AmethystClient.config.getInt("hud.scoreboardX",  -1);
        scoreboardY  = AmethystClient.config.getInt("hud.scoreboardY",   5);
        chatX        = AmethystClient.config.getInt("hud.chatX",          2);
        chatY        = AmethystClient.config.getInt("hud.chatY",         -1);

        soupCounterScale = AmethystClient.config.getFloat("hud.soupCounterScale", 1.0f);
        cpsCounterScale  = AmethystClient.config.getFloat("hud.cpsCounterScale",  1.0f);
        fpsCounterScale  = AmethystClient.config.getFloat("hud.fpsCounterScale",  1.0f);
        pingCounterScale = AmethystClient.config.getFloat("hud.pingCounterScale", 1.0f);
        clockScale       = AmethystClient.config.getFloat("hud.clockScale",        1.0f);
    }

    private static void save() {
        AmethystClient.config.set("hud.soupCounterX",  soupCounterX);
        AmethystClient.config.set("hud.soupCounterY",  soupCounterY);
        AmethystClient.config.set("hud.fpsCounterX",   fpsCounterX);
        AmethystClient.config.set("hud.fpsCounterY",   fpsCounterY);
        AmethystClient.config.set("hud.pingCounterX",  pingCounterX);
        AmethystClient.config.set("hud.pingCounterY",  pingCounterY);
        AmethystClient.config.set("hud.clockX",        clockX);
        AmethystClient.config.set("hud.clockY",        clockY);
        AmethystClient.config.set("hud.cpsCounterX",   cpsCounterX);
        AmethystClient.config.set("hud.cpsCounterY",   cpsCounterY);
        AmethystClient.config.set("hud.scoreboardX",   scoreboardX);
        AmethystClient.config.set("hud.scoreboardY",   scoreboardY);
        AmethystClient.config.set("hud.chatX",         chatX);
        AmethystClient.config.set("hud.chatY",         chatY);
        AmethystClient.config.set("hud.soupCounterScale",  soupCounterScale);
        AmethystClient.config.set("hud.cpsCounterScale",   cpsCounterScale);
        AmethystClient.config.set("hud.fpsCounterScale",   fpsCounterScale);
        AmethystClient.config.set("hud.pingCounterScale",  pingCounterScale);
        AmethystClient.config.set("hud.clockScale",        clockScale);
        AmethystClient.config.save();
    }
}