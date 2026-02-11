package com.amethyst.client.modules;

import com.amethyst.client.Module;

public class CustomChat extends Module {

    // ── Settings ──────────────────────────────────────────────────────────────
    private boolean showBackground = true;
    private float   bgAlpha       = 0.35f;
    private float   scale         = 1.0f;
    private boolean fadeMessages  = true;   // плавное появление
    private int     maxMessages   = 10;     // сколько строк видно
    private int     textColor     = 0xFFFFFFFF;
    private boolean showTimestamps = false;

    public CustomChat() {
        super("CustomChat", "Movable chat with smooth fade-in animation");
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public boolean isShowBackground() { return showBackground; }
    public float   getBgAlpha()       { return bgAlpha; }
    public float   getScale()         { return scale; }
    public boolean isFadeMessages()   { return fadeMessages; }
    public int     getMaxMessages()   { return maxMessages; }
    public int     getTextColor()     { return textColor; }
    public boolean isShowTimestamps() { return showTimestamps; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setShowBackground(boolean v) { showBackground = v; }
    public void setBgAlpha(float v)          { bgAlpha = Math.max(0f, Math.min(1f, v)); }
    public void setScale(float v)            { scale = Math.max(0.5f, Math.min(2.0f, v)); }
    public void setFadeMessages(boolean v)   { fadeMessages = v; }
    public void setMaxMessages(int v)        { maxMessages = Math.max(3, Math.min(20, v)); }
    public void setTextColor(int v)          { textColor = v | 0xFF000000; }
    public void setShowTimestamps(boolean v) { showTimestamps = v; }
}