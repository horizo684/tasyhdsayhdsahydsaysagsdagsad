package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.Module.Category;

public class Animations extends Module {

    // Animation settings
    private boolean oldBlockhit = true;      // 1.7 blockhit animation
    private boolean oldDamage = true;        // 1.7 damage/hurt animation (red armor)
    private boolean oldRod = true;           // 1.7 fishing rod animation
    private boolean oldBow = true;           // 1.7 bow animation
    private boolean oldSword = true;         // 1.7 sword swing
    private boolean oldEating = true;        // 1.7 eating animation
    private boolean punching = true;         // Show punching animation
    
    // Swing settings
    private float swingSpeed = 6.0f;         // Swing animation speed (1.7 = 6, 1.8 = 8)
    private boolean smoothSwing = true;      // Smooth sword swing
    
    // Item position settings (1.7 style positioning)
    private float itemPosX = 0.56f;          // Item X position
    private float itemPosY = -0.52f;         // Item Y position
    private float itemPosZ = -0.72f;         // Item Z position
    private float itemScale = 0.4f;          // Item scale (1.7 items are smaller)
    
    // Blockhit settings
    private float blockPosX = 0.0f;          // Block X offset
    private float blockPosY = 0.0f;          // Block Y offset
    private float blockPosZ = 0.0f;          // Block Z offset
    
    // Red armor damage
    private int armorFlashDuration = 10;     // Ticks for red armor flash (1.7 = 10)
    private float armorRedIntensity = 0.8f;  // Intensity of red color (0.0-1.0)

    public Animations() {
        super("Animations", "1.7 style animations", 0, Category.RENDER);
        this.setEnabled(true); // Включен по умолчанию
    }

    // ===== Getters and Setters =====
    
    public boolean isOldBlockhit() {
        return oldBlockhit;
    }

    public void setOldBlockhit(boolean oldBlockhit) {
        this.oldBlockhit = oldBlockhit;
    }

    public boolean isOldDamage() {
        return oldDamage;
    }

    public void setOldDamage(boolean oldDamage) {
        this.oldDamage = oldDamage;
    }

    public boolean isOldRod() {
        return oldRod;
    }

    public void setOldRod(boolean oldRod) {
        this.oldRod = oldRod;
    }

    public boolean isOldBow() {
        return oldBow;
    }

    public void setOldBow(boolean oldBow) {
        this.oldBow = oldBow;
    }

    public boolean isOldSword() {
        return oldSword;
    }

    public void setOldSword(boolean oldSword) {
        this.oldSword = oldSword;
    }

    public boolean isOldEating() {
        return oldEating;
    }

    public void setOldEating(boolean oldEating) {
        this.oldEating = oldEating;
    }

    public boolean isPunching() {
        return punching;
    }

    public void setPunching(boolean punching) {
        this.punching = punching;
    }

    public float getSwingSpeed() {
        return swingSpeed;
    }

    public void setSwingSpeed(float swingSpeed) {
        this.swingSpeed = Math.max(1.0f, Math.min(20.0f, swingSpeed));
    }

    public boolean isSmoothSwing() {
        return smoothSwing;
    }

    public void setSmoothSwing(boolean smoothSwing) {
        this.smoothSwing = smoothSwing;
    }

    public float getItemPosX() {
        return itemPosX;
    }

    public void setItemPosX(float itemPosX) {
        this.itemPosX = Math.max(-2.0f, Math.min(2.0f, itemPosX));
    }

    public float getItemPosY() {
        return itemPosY;
    }

    public void setItemPosY(float itemPosY) {
        this.itemPosY = Math.max(-2.0f, Math.min(2.0f, itemPosY));
    }

    public float getItemPosZ() {
        return itemPosZ;
    }

    public void setItemPosZ(float itemPosZ) {
        this.itemPosZ = Math.max(-2.0f, Math.min(2.0f, itemPosZ));
    }

    public float getItemScale() {
        return itemScale;
    }

    public void setItemScale(float itemScale) {
        this.itemScale = Math.max(0.1f, Math.min(1.0f, itemScale));
    }

    public float getBlockPosX() {
        return blockPosX;
    }

    public void setBlockPosX(float blockPosX) {
        this.blockPosX = Math.max(-1.0f, Math.min(1.0f, blockPosX));
    }

    public float getBlockPosY() {
        return blockPosY;
    }

    public void setBlockPosY(float blockPosY) {
        this.blockPosY = Math.max(-1.0f, Math.min(1.0f, blockPosY));
    }

    public float getBlockPosZ() {
        return blockPosZ;
    }

    public void setBlockPosZ(float blockPosZ) {
        this.blockPosZ = Math.max(-1.0f, Math.min(1.0f, blockPosZ));
    }

    public int getArmorFlashDuration() {
        return armorFlashDuration;
    }

    public void setArmorFlashDuration(int armorFlashDuration) {
        this.armorFlashDuration = Math.max(1, Math.min(40, armorFlashDuration));
    }

    public float getArmorRedIntensity() {
        return armorRedIntensity;
    }

    public void setArmorRedIntensity(float armorRedIntensity) {
        this.armorRedIntensity = Math.max(0.0f, Math.min(1.0f, armorRedIntensity));
    }
    
    // === Preset methods ===
    
    /**
     * Reset to default 1.7 values
     */
    public void setPreset17() {
        oldBlockhit = true;
        oldDamage = true;
        oldRod = true;
        oldBow = true;
        oldSword = true;
        oldEating = true;
        punching = true;
        swingSpeed = 6.0f;
        smoothSwing = true;
        itemPosX = 0.56f;
        itemPosY = -0.52f;
        itemPosZ = -0.72f;
        itemScale = 0.4f;
        armorFlashDuration = 10;
        armorRedIntensity = 0.8f;
    }
    
    /**
     * Reset to default 1.8 values
     */
    public void setPreset18() {
        oldBlockhit = false;
        oldDamage = false;
        oldRod = false;
        oldBow = false;
        oldSword = false;
        oldEating = false;
        punching = false;
        swingSpeed = 8.0f;
        smoothSwing = false;
        itemPosX = 0.0f;
        itemPosY = 0.0f;
        itemPosZ = 0.0f;
        itemScale = 0.4f; // Нормальный размер предметов в Minecraft
        armorFlashDuration = 0;
        armorRedIntensity = 0.0f;
    }
    
    /**
     * Reset to vanilla Minecraft values (pure default)
     */
    public void resetToVanilla() {
        // Disable all custom animations
        oldBlockhit = false;
        oldDamage = false;
        oldRod = false;
        oldBow = false;
        oldSword = false;
        oldEating = false;
        punching = false;
        smoothSwing = false;
        
        // Reset swing speed to vanilla
        swingSpeed = 8.0f;
        
        // Reset item position to vanilla (no offset)
        itemPosX = 0.0f;
        itemPosY = 0.0f;
        itemPosZ = 0.0f;
        itemScale = 0.4f; // Нормальный размер предметов в Minecraft
        
        // Reset block position to vanilla (no offset)
        blockPosX = 0.0f;
        blockPosY = 0.0f;
        blockPosZ = 0.0f;
        
        // Disable damage effects
        armorFlashDuration = 0;
        armorRedIntensity = 0.0f;
    }
    
    @Override
    public void saveSettings() {
        com.amethyst.client.AmethystClient.config.set(getName() + ".oldBlockhit", oldBlockhit);
        com.amethyst.client.AmethystClient.config.set(getName() + ".oldDamage", oldDamage);
        com.amethyst.client.AmethystClient.config.set(getName() + ".oldRod", oldRod);
        com.amethyst.client.AmethystClient.config.set(getName() + ".oldBow", oldBow);
        com.amethyst.client.AmethystClient.config.set(getName() + ".oldSword", oldSword);
        com.amethyst.client.AmethystClient.config.set(getName() + ".oldEating", oldEating);
        com.amethyst.client.AmethystClient.config.set(getName() + ".punching", punching);
        com.amethyst.client.AmethystClient.config.set(getName() + ".swingSpeed", swingSpeed);
        com.amethyst.client.AmethystClient.config.set(getName() + ".smoothSwing", smoothSwing);
        com.amethyst.client.AmethystClient.config.set(getName() + ".itemPosX", itemPosX);
        com.amethyst.client.AmethystClient.config.set(getName() + ".itemPosY", itemPosY);
        com.amethyst.client.AmethystClient.config.set(getName() + ".itemPosZ", itemPosZ);
        com.amethyst.client.AmethystClient.config.set(getName() + ".itemScale", itemScale);
        com.amethyst.client.AmethystClient.config.set(getName() + ".blockPosX", blockPosX);
        com.amethyst.client.AmethystClient.config.set(getName() + ".blockPosY", blockPosY);
        com.amethyst.client.AmethystClient.config.set(getName() + ".blockPosZ", blockPosZ);
        com.amethyst.client.AmethystClient.config.set(getName() + ".armorFlashDuration", armorFlashDuration);
        com.amethyst.client.AmethystClient.config.set(getName() + ".armorRedIntensity", armorRedIntensity);
    }
    
    @Override
    public void loadSettings() {
        oldBlockhit = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".oldBlockhit", true);
        oldDamage = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".oldDamage", true);
        oldRod = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".oldRod", true);
        oldBow = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".oldBow", true);
        oldSword = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".oldSword", true);
        oldEating = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".oldEating", true);
        punching = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".punching", true);
        swingSpeed = com.amethyst.client.AmethystClient.config.getFloat(getName() + ".swingSpeed", 6.0f);
        smoothSwing = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".smoothSwing", true);
        itemPosX = com.amethyst.client.AmethystClient.config.getFloat(getName() + ".itemPosX", 0.56f);
        itemPosY = com.amethyst.client.AmethystClient.config.getFloat(getName() + ".itemPosY", -0.52f);
        itemPosZ = com.amethyst.client.AmethystClient.config.getFloat(getName() + ".itemPosZ", -0.72f);
        itemScale = com.amethyst.client.AmethystClient.config.getFloat(getName() + ".itemScale", 0.4f);
        blockPosX = com.amethyst.client.AmethystClient.config.getFloat(getName() + ".blockPosX", 0.0f);
        blockPosY = com.amethyst.client.AmethystClient.config.getFloat(getName() + ".blockPosY", 0.0f);
        blockPosZ = com.amethyst.client.AmethystClient.config.getFloat(getName() + ".blockPosZ", 0.0f);
        armorFlashDuration = com.amethyst.client.AmethystClient.config.getInt(getName() + ".armorFlashDuration", 10);
        armorRedIntensity = com.amethyst.client.AmethystClient.config.getFloat(getName() + ".armorRedIntensity", 0.8f);
    }
}