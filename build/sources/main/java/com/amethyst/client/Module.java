package com.amethyst.client;

import org.lwjgl.input.Keyboard;

public abstract class Module {
    private String name;
    private String description;
    private boolean enabled;
    private int color;
    private int keyCode;
    
    public Module(String name, String description) {
        this.name = name;
        this.description = description;
        this.enabled = false;
        this.color = 0xFFFFFF;
        this.keyCode = Keyboard.KEY_NONE;
    }
    
    public int getKeyCode() {
        return keyCode;
    }
    
    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }
    
    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (this.enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }
    
    public void onEnable() {}
    public void onDisable() {}
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
}