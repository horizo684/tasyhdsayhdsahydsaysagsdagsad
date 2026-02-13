package com.amethyst.client.modules;

import com.amethyst.client.Module;

public class CustomChat extends Module {

    private boolean showBackground = true;
    private boolean fadeMessages   = true;
    private boolean showTimestamps = false;

    private float bgAlpha     = 0.5f;
    private float chatWidth   = 1.0f;
    private float chatOpacity = 1.0f;
    private float chatScale   = 1.0f;
    private int   maxMessages = 10;
    private int   textColor   = 0xFFFFFFFF;

    public CustomChat() {
        super("CustomChat", "Vanilla-style chat beautifier with custom styling", 0, Category.RENDER);
    }

    public boolean isShowBackground() { return showBackground; }
    public boolean isFadeMessages()   { return fadeMessages; }
    public boolean isShowTimestamps() { return showTimestamps; }

    public float getBgAlpha()     { return bgAlpha; }
    public float getChatWidth()   { return chatWidth; }
    public float getChatOpacity() { return chatOpacity; }
    public float getChatScale()   { return chatScale; }
    public int   getMaxMessages() { return maxMessages; }
    public int   getTextColor()   { return textColor; }

    public void setShowBackground(boolean v) { showBackground = v; }
    public void setFadeMessages(boolean v)   { fadeMessages = v; }
    public void setShowTimestamps(boolean v) { showTimestamps = v; }

    public void setBgAlpha(float v)     { bgAlpha     = Math.max(0f, Math.min(1f, v)); }
    public void setChatWidth(float v)   { chatWidth   = Math.max(0.5f, Math.min(2.0f, v)); }
    public void setChatOpacity(float v) { chatOpacity = Math.max(0f, Math.min(1f, v)); }
    public void setChatScale(float v)   { chatScale   = Math.max(0.5f, Math.min(2.0f, v)); }
    public void setMaxMessages(int v)   { maxMessages = Math.max(3, Math.min(20, v)); }
    public void setTextColor(int v)     { textColor   = v; }
}
