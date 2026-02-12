package com.amethyst.client;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class AmethystConfig {
    
    private Configuration config;
    
    public AmethystConfig(File file) {
        config = new Configuration(file);
        config.load();
    }
    
    public void set(String key, Object value) {
        if (value instanceof Boolean) {
            config.get(Configuration.CATEGORY_GENERAL, key, (Boolean) value).set((Boolean) value);
        } else if (value instanceof Integer) {
            config.get(Configuration.CATEGORY_GENERAL, key, (Integer) value).set((Integer) value);
        } else if (value instanceof String) {
            config.get(Configuration.CATEGORY_GENERAL, key, (String) value).set((String) value);
        } else if (value instanceof Float) {
            config.get(Configuration.CATEGORY_GENERAL, key, (double) (Float) value).set((double) (Float) value);
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        return config.get(Configuration.CATEGORY_GENERAL, key, defaultValue).getBoolean();
    }
    
    public int getInt(String key, int defaultValue) {
        return config.get(Configuration.CATEGORY_GENERAL, key, defaultValue).getInt();
    }
    
    public String getString(String key, String defaultValue) {
        return config.get(Configuration.CATEGORY_GENERAL, key, defaultValue).getString();
    }
    
    public float getFloat(String key, float defaultValue) {
        return (float) config.get(Configuration.CATEGORY_GENERAL, key, (double) defaultValue).getDouble();
    }
    
    public void save() {
        if (config.hasChanged()) {
            config.save();
        }
    }
}