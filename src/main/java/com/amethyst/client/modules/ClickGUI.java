package com.amethyst.client.modules;

import com.amethyst.client.Module;

public class ClickGUI extends Module {

    public enum AnimType {
        ZOOM("Zoom"),
        SLIDE_DOWN("Slide Down"),
        SLIDE_UP("Slide Up"),
        FADE("Fade"),
        BOUNCE("Bounce");

        public final String label;
        AnimType(String label) { this.label = label; }
    }

    public enum GUIStyle {
        MODERN("Modern"),
        MINIMAL("Minimal"),
        COMPACT("Compact");

        public final String label;
        GUIStyle(String label) { this.label = label; }
    }

    private AnimType animType  = AnimType.ZOOM;
    private GUIStyle guiStyle  = GUIStyle.MODERN;
    private float    animSpeed = 0.17f;   // 0.05 – 0.40
    private boolean  blur      = false;
    private boolean  particles = false;

    public ClickGUI() {
        super("ClickGUI", "Customize GUI animations and appearance", 0, Category.MISC);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public AnimType getAnimType()  { return animType; }
    public GUIStyle getGUIStyle()  { return guiStyle; }
    public float    getAnimSpeed() { return animSpeed; }
    public boolean  isBlur()       { return blur; }
    public boolean  isParticles()  { return particles; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setAnimType(AnimType v)  { animType  = v; }
    public void setGUIStyle(GUIStyle v)  { guiStyle  = v; }
    public void setAnimSpeed(float v)    { animSpeed = Math.max(0.05f, Math.min(0.40f, v)); }
    public void setBlur(boolean v)       { blur      = v; }
    public void setParticles(boolean v)  { particles = v; }

    // Convenience: cycle to next animation type
    public void cycleAnimType() {
        AnimType[] vals = AnimType.values();
        animType = vals[(animType.ordinal() + 1) % vals.length];
    }

    public void cycleGUIStyle() {
        GUIStyle[] vals = GUIStyle.values();
        guiStyle = vals[(guiStyle.ordinal() + 1) % vals.length];
    }
}
