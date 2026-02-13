package com.amethyst.client;

import net.minecraft.entity.EntityLivingBase;

/**
 * Stub — armor damage animation is now handled by Orange's ArmorAnimation class.
 * This file exists only because EventHandler.java still calls these static methods.
 */
public class DamageAnimationHandler {
    public static void onEntityHurt(EntityLivingBase entity) {
        // Handled by Orange's ArmorAnimation via EVENT_BUS
    }
    public static void cleanup() {
        // No-op
    }
}
