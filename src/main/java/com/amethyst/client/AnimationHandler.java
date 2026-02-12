package com.amethyst.client;

import com.amethyst.client.modules.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AnimationHandler {
    
    private Minecraft mc = Minecraft.getMinecraft();
    
    public Animations getAnimations() {
        if (AmethystClient.moduleManager == null) return null;
        return (Animations) AmethystClient.moduleManager.getModuleByName("Animations");
    }
    
    // Используем RenderLivingEvent для более широкого охвата
    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre event) {
        Animations anim = getAnimations();
        if (anim == null || !anim.isEnabled()) return;
        if (!anim.isOldDamage()) return;
        
        EntityLivingBase entity = event.entity;
        
        // Применяем красный эффект только к игроку от первого лица
        if (entity == mc.thePlayer && entity.hurtTime > 0) {
            int maxHurt = anim.getArmorFlashDuration();
            float hurtPercent = (float)entity.hurtTime / (float)maxHurt;
            
            // Ограничиваем процент от 0 до 1
            hurtPercent = Math.max(0.0F, Math.min(1.0F, hurtPercent));
            
            if (hurtPercent > 0) {
                float intensity = anim.getArmorRedIntensity();
                
                // Красный канал всегда 1.0
                float red = 1.0F;
                // Зеленый и синий уменьшаются в зависимости от урона
                float green = 1.0F - (hurtPercent * intensity);
                float blue = green;
                
                // Применяем цвет через GlStateManager
                GlStateManager.color(red, green, blue, 1.0F);
                
                // Дополнительно устанавливаем через GL11 для надежности
                org.lwjgl.opengl.GL11.glColor4f(red, green, blue, 1.0F);
            }
        }
    }
    
    @SubscribeEvent
    public void onRenderLivingPost(RenderLivingEvent.Post event) {
        // Сброс цвета после рендера любой сущности
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        org.lwjgl.opengl.GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Animations anim = getAnimations();
        if (anim == null || !anim.isEnabled()) return;
        if (!anim.isOldDamage()) return;
        
        EntityPlayer player = event.entityPlayer;
        
        // === СТАРЫЙ УРОН (красная броня) для всех игроков ===
        if (player.hurtTime > 0) {
            int maxHurt = anim.getArmorFlashDuration();
            float hurtPercent = (float)player.hurtTime / (float)maxHurt;
            hurtPercent = Math.max(0.0F, Math.min(1.0F, hurtPercent));
            
            if (hurtPercent > 0) {
                float intensity = anim.getArmorRedIntensity();
                float red = 1.0F;
                float green = 1.0F - (hurtPercent * intensity);
                float blue = green;
                
                GlStateManager.color(red, green, blue, 1.0F);
                org.lwjgl.opengl.GL11.glColor4f(red, green, blue, 1.0F);
            }
        }
    }
    
    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        // Сброс цвета после рендера игрока
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        org.lwjgl.opengl.GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
