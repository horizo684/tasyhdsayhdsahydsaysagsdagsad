package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.orangemarshall.animations.ArmorAnimation;
import com.orangemarshall.animations.BlockhitAnimation;
import com.orangemarshall.animations.config.Config;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.lang.reflect.Constructor;

/**
 * Animations module — wraps Orange's 1.7 Animations Mod.
 * Toggle in ClickGUI enables/disables Orange's event listeners.
 * Orange's Config fields are synced from our settings.
 */
public class Animations extends Module {

    private BlockhitAnimation blockhitAnimation;
    private ArmorAnimation    armorAnimation;
    private boolean           registered = false;

    // Settings that map directly to Orange's Config
    private boolean blockhit = true;
    private boolean punching = true;
    private boolean oldRod   = true;
    private boolean redArmor = true;
    private boolean deepRed  = false;
    private boolean oldEnchantGlint    = false;
    private boolean thirdPersonBlocking = false;

    // Extra GUI-only toggles (not in Orange's Config but shown in picker)
    private boolean oldSword   = true;
    private boolean smoothSwing = true;
    private boolean oldBow     = true;
    private boolean oldEating  = true;
    private float   swingSpeed = 6.0f;
    private int     armorFlashDuration = 10;
    private float   armorRedIntensity  = 0.8f;

    public Animations() {
        super("Animations", "1.7 animations", 0, Category.RENDER);
        this.setEnabled(true);
    }

    // ── Module lifecycle ──────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        initOrangeConfig();
        syncConfig();
        registerOrange();
    }

    @Override
    public void onDisable() {
        unregisterOrange();
    }

    // ── Orange integration ────────────────────────────────────────────────────

    private void initOrangeConfig() {
        try {
            if (Config.getInstance() != null) return;
            Constructor<Config> ctor = Config.class.getDeclaredConstructor(File.class, String.class);
            ctor.setAccessible(true);
            ctor.newInstance(new File("config/orange_animations.cfg"), "1");
        } catch (Exception e) {
            System.err.println("[Animations] Config init failed: " + e);
        }
    }

    private void syncConfig() {
        try {
            Config cfg = Config.getInstance();
            if (cfg == null) return;
            cfg.blockhit             = this.blockhit;
            cfg.punching             = this.punching;
            cfg.oldRod               = this.oldRod;
            cfg.redArmor             = this.redArmor;
            cfg.deepRed              = this.deepRed;
            cfg.oldEnchantGlint      = this.oldEnchantGlint;
            cfg.thirdPersonBlocking  = this.thirdPersonBlocking;
        } catch (Exception e) {
            System.err.println("[Animations] syncConfig failed: " + e);
        }
    }

    private void registerOrange() {
        if (registered) return;
        try {
            if (blockhitAnimation == null) blockhitAnimation = new BlockhitAnimation();
            if (armorAnimation    == null) armorAnimation    = new ArmorAnimation();
            MinecraftForge.EVENT_BUS.register(blockhitAnimation);
            MinecraftForge.EVENT_BUS.register(armorAnimation);
            registered = true;
        } catch (Exception e) {
            System.err.println("[Animations] Register failed: " + e);
            e.printStackTrace();
        }
    }

    private void unregisterOrange() {
        if (!registered) return;
        try {
            if (blockhitAnimation != null) MinecraftForge.EVENT_BUS.unregister(blockhitAnimation);
            if (armorAnimation    != null) MinecraftForge.EVENT_BUS.unregister(armorAnimation);
        } catch (Exception ignored) {}
        registered = false;
    }

    // ── Getters / Setters (all sync to Orange Config) ────────────────────────

    public boolean isOldBlockhit()             { return blockhit; }
    public void    setOldBlockhit(boolean v)   { blockhit = v; syncConfig(); }

    public boolean isPunching()                { return punching; }
    public void    setPunching(boolean v)      { punching = v; syncConfig(); }

    public boolean isOldRod()                  { return oldRod; }
    public void    setOldRod(boolean v)        { oldRod = v; syncConfig(); }

    public boolean isOldDamage()               { return redArmor; }
    public void    setOldDamage(boolean v)     { redArmor = v; syncConfig(); }

    public boolean isOldSword()                { return oldSword; }
    public void    setOldSword(boolean v)      { oldSword = v; }

    public boolean isSmoothSwing()             { return smoothSwing; }
    public void    setSmoothSwing(boolean v)   { smoothSwing = v; }

    public boolean isOldBow()                  { return oldBow; }
    public void    setOldBow(boolean v)        { oldBow = v; }

    public boolean isOldEating()               { return oldEating; }
    public void    setOldEating(boolean v)     { oldEating = v; }

    public float getSwingSpeed()               { return swingSpeed; }
    public void  setSwingSpeed(float v)        { swingSpeed = Math.max(1f, Math.min(20f, v)); }

    public int  getArmorFlashDuration()        { return armorFlashDuration; }
    public void setArmorFlashDuration(int v)   { armorFlashDuration = Math.max(1, Math.min(40, v)); }

    public float getArmorRedIntensity()        { return armorRedIntensity; }
    public void  setArmorRedIntensity(float v) { armorRedIntensity = Math.max(0f, Math.min(1f, v)); }

    public void setPreset17() {
        blockhit = true; punching = true; oldRod = true; redArmor = true;
        oldSword = true; smoothSwing = true; oldBow = true; oldEating = true;
        swingSpeed = 6f; armorFlashDuration = 10; armorRedIntensity = 0.8f;
        syncConfig();
    }
    public void setPreset18() {
        blockhit = false; punching = false; oldRod = false; redArmor = false;
        oldSword = false; smoothSwing = false; oldBow = false; oldEating = false;
        swingSpeed = 8f; armorFlashDuration = 0; armorRedIntensity = 0f;
        syncConfig();
    }
    public void resetToVanilla() { setPreset18(); }

    // ── Config persistence ────────────────────────────────────────────────────

    @Override
    public void saveSettings() {
        com.amethyst.client.AmethystClient.config.set(getName() + ".blockhit",   blockhit);
        com.amethyst.client.AmethystClient.config.set(getName() + ".punching",   punching);
        com.amethyst.client.AmethystClient.config.set(getName() + ".oldRod",     oldRod);
        com.amethyst.client.AmethystClient.config.set(getName() + ".redArmor",   redArmor);
        com.amethyst.client.AmethystClient.config.set(getName() + ".oldSword",   oldSword);
        com.amethyst.client.AmethystClient.config.set(getName() + ".smoothSwing",smoothSwing);
        com.amethyst.client.AmethystClient.config.set(getName() + ".oldBow",     oldBow);
        com.amethyst.client.AmethystClient.config.set(getName() + ".oldEating",  oldEating);
    }

    @Override
    public void loadSettings() {
        blockhit    = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".blockhit",    true);
        punching    = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".punching",    true);
        oldRod      = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".oldRod",      true);
        redArmor    = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".redArmor",    true);
        oldSword    = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".oldSword",    true);
        smoothSwing = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".smoothSwing", true);
        oldBow      = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".oldBow",      true);
        oldEating   = com.amethyst.client.AmethystClient.config.getBoolean(getName() + ".oldEating",   true);
        syncConfig();
    }
}
