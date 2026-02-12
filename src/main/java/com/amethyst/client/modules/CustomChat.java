package com.amethyst.client.modules;

import com.amethyst.client.Module;

public class CustomChat extends Module {

    private boolean showBackground = true;
    private float   bgAlpha       = 0.35f;
    private float   scale         = 1.0f;
    private boolean fadeMessages  = true;
    private int     maxMessages   = 10;
    private int     textColor     = 0xFFFFFFFF;
    private boolean showTimestamps = false;
    private float   chatWidth     = 1.0f;  // 0.5 - 2.0 как в настройках MC
    private float   chatOpacity   = 1.0f;  // 0.0 - 1.0 прозрачность текста
    private float   chatScale     = 1.0f;  // 0.5 - 2.0 масштаб чата

    public CustomChat() {
        super("CustomChat", "Movable chat with smooth fade-in animation", 0, Category.RENDER);
    }

    public boolean isShowBackground() { return showBackground; }
    public float   getBgAlpha()       { return bgAlpha; }
    public float   getScale()         { return scale; }
    public boolean isFadeMessages()   { return fadeMessages; }
    public int     getMaxMessages()   { return maxMessages; }
    public int     getTextColor()     { return textColor; }
    public boolean isShowTimestamps() { return showTimestamps; }
    public float   getChatWidth()     { return chatWidth; }
    public float   getChatOpacity()   { return chatOpacity; }
    public float   getChatScale()     { return chatScale; }

    public void setShowBackground(boolean v) { showBackground = v; }
    public void setBgAlpha(float v)          { bgAlpha = Math.max(0f, Math.min(1f, v)); }
    public void setScale(float v)            { scale = Math.max(0.5f, Math.min(2.0f, v)); }
    public void setFadeMessages(boolean v)   { fadeMessages = v; }
    public void setMaxMessages(int v)        { maxMessages = Math.max(3, Math.min(20, v)); }
    public void setTextColor(int v)          { textColor = v | 0xFF000000; }
    public void setShowTimestamps(boolean v) { showTimestamps = v; }
    public void setChatWidth(float v)        { chatWidth = Math.max(0.5f, Math.min(2.0f, v)); }
    public void setChatOpacity(float v)      { chatOpacity = Math.max(0f, Math.min(1f, v)); }
    public void setChatScale(float v)        { chatScale = Math.max(0.5f, Math.min(2.0f, v)); }
}