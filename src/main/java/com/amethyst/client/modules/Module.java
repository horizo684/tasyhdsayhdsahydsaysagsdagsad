package com.amethyst.client.modules;

public class Module {
    private String name;
    private String description;
    private int keyBind;
    private Category category;
    private boolean enabled;

    public enum Category {
        COMBAT, MOVEMENT, RENDER, PLAYER, MISC
    }

    public Module(String name, String description, int keyBind, Category category) {
        this.name = name;
        this.description = description;
        this.keyBind = keyBind;
        this.category = category;
        this.enabled = false;
    }

    public void onEnable() {}
    public void onDisable() {}

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { 
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    
    public int getKeyBind() { return keyBind; }
    public int getKey() { return keyBind; }
    public void setKey(int key) { this.keyBind = key; }
    
    public Category getCategory() { return category; }
}