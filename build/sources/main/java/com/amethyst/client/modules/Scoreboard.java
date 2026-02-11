package com.amethyst.client.modules;

import com.amethyst.client.Module;

public class Scoreboard extends Module {

    // ── Settings ──────────────────────────────────────────────────────────────
    private boolean showNumbers   = true;   // цифры справа
    private boolean showBackground = true;  // полупрозрачный фон
    private float   bgAlpha       = 0.4f;   // прозрачность фона (0-1)
    private float   scale         = 1.0f;   // масштаб (0.5-2.0)
    private int     textColor     = 0xFFFFFFFF;
    private int     numberColor   = 0xFFFF5555; // красный как у ваниллы
    private int     titleColor    = 0xFFFFFF55; // жёлтый как у ваниллы

    public Scoreboard() {
        super("Scoreboard", "Custom scoreboard replacing server default");
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public boolean isShowNumbers()    { return showNumbers; }
    public boolean isShowBackground() { return showBackground; }
    public float   getBgAlpha()       { return bgAlpha; }
    public float   getScale()         { return scale; }
    public int     getTextColor()     { return textColor; }
    public int     getNumberColor()   { return numberColor; }
    public int     getTitleColor()    { return titleColor; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setShowNumbers(boolean v)    { showNumbers = v; }
    public void setShowBackground(boolean v) { showBackground = v; }
    public void setBgAlpha(float v)          { bgAlpha = Math.max(0f, Math.min(1f, v)); }
    public void setScale(float v)            { scale = Math.max(0.5f, Math.min(2.0f, v)); }
    public void setTextColor(int v)          { textColor = v | 0xFF000000; }
    public void setNumberColor(int v)        { numberColor = v | 0xFF000000; }
    public void setTitleColor(int v)         { titleColor = v | 0xFF000000; }
}