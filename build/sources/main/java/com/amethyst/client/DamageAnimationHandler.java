package com.amethyst.client;

import com.amethyst.client.modules.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles 1.7-style damage animations where entire entity (including armor) flashes red when hit
 */
public class DamageAnimationHandler {
    
    private static final Map<Integer, Long> lastHitTime = new HashMap<>();
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    /**
     * Call this when an entity takes damage to track the hit
     */
    public static void onEntityHurt(EntityLivingBase entity) {
        if (entity != null) {
            lastHitTime.put(entity.getEntityId(), System.currentTimeMillis());
        }
    }
    
    /**
     * Apply red overlay BEFORE rendering (highest priority to render first)
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderLivingPre(RenderLivingEvent.Pre event) {
        Animations anim = getAnimations();
        if (anim == null || !anim.isEnabled() || !anim.isOldDamage()) {
            return;
        }
        
        EntityLivingBase entity = event.entity;
        if (entity == null) return;
        
        Long hitTime = lastHitTime.get(entity.getEntityId());
        if (hitTime == null) return;
        
        long timeSinceHit = System.currentTimeMillis() - hitTime;
        int flashDuration = anim.getArmorFlashDuration() * 50; // Convert ticks to ms (1 tick = 50ms)
        
        if (timeSinceHit < flashDuration) {
            // Calculate fade progress (1.0 at start, 0.0 at end)
            float fadeProgress = 1.0f - ((float)timeSinceHit / flashDuration);
            float intensity = anim.getArmorRedIntensity() * fadeProgress;
            
            // Apply red color using GL directly
            // This affects ALL subsequent rendering including armor layers
            GL11.glPushAttrib(GL11.GL_CURRENT_BIT);
            
            // Set the color for rendering - reduces green and blue channels
            float r = 1.0f;
            float g = 1.0f - intensity;
            float b = 1.0f - intensity;
            
            GL11.glColor4f(r, g, b, 1.0f);
        } else {
            // Remove old entries
            lastHitTime.remove(entity.getEntityId());
        }
    }
    
    /**
     * Restore color state AFTER rendering
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderLivingPost(RenderLivingEvent.Post event) {
        Animations anim = getAnimations();
        if (anim == null || !anim.isEnabled() || !anim.isOldDamage()) {
            return;
        }
        
        EntityLivingBase entity = event.entity;
        if (entity == null) return;
        
        Long hitTime = lastHitTime.get(entity.getEntityId());
        if (hitTime != null) {
            long timeSinceHit = System.currentTimeMillis() - hitTime;
            int flashDuration = anim.getArmorFlashDuration() * 50;
            
            if (timeSinceHit < flashDuration) {
                // Restore GL state
                GL11.glPopAttrib();
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }
    
    private Animations getAnimations() {
        if (AmethystClient.moduleManager == null) return null;
        return (Animations) AmethystClient.moduleManager.getModuleByName("Animations");
    }
    
    /**
     * Clean up old entries periodically
     */
    public static void cleanup() {
        long currentTime = System.currentTimeMillis();
        lastHitTime.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > 5000);
    }
}
