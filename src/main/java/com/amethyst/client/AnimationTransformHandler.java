package com.amethyst.client;

import com.amethyst.client.modules.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;

/**
 * Обработчик трансформаций предметов для 1.7 анимаций
 * Вызывается через ASM инжект в ItemRenderer.transformFirstPersonItem
 */
public class AnimationTransformHandler {
    
    private static Minecraft mc = Minecraft.getMinecraft();
    
    /**
     * ГЛАВНЫЙ МЕТОД - вызывается из ASM инжекта
     * Применяет 1.7 трансформации ПЕРЕД vanilla кодом
     */
    public static void applyTransforms(float equipProgress, float swingProgress) {
        // Получаем модуль анимаций
        Animations anim = getAnimations();
        if (anim == null) {
            System.out.println("[AnimationTransformHandler] WARNING: Animations module is NULL!");
            return;
        }
        
        if (!anim.isEnabled()) {
            return; // Модуль выключен - не применяем ничего
        }
        
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;
        
        ItemStack stack = player.getHeldItem();
        
        // Проверяем нужны ли 1.7 анимации
        if (!needs17Animation(stack, player, anim)) {
            return; // Не нужны - пусть vanilla делает свое
        }
        
        // Применяем 1.7 трансформации
        apply17Transforms(stack, player, swingProgress, anim);
    }
    
    /**
     * Проверяет нужны ли 1.7 анимации
     */
    private static boolean needs17Animation(ItemStack stack, EntityPlayerSP player, Animations anim) {
        if (stack != null) {
            Item item = stack.getItem();
            
            // Блокхит
            if (anim.isOldBlockhit() && player.isBlocking()) {
                return true;
            }
            
            // Sword swing
            if (anim.isOldSword() && item instanceof ItemSword && player.isSwingInProgress) {
                return true;
            }
            
            // Bow
            if (anim.isOldBow() && item instanceof ItemBow && player.getItemInUse() != null) {
                return true;
            }
            
            // Rod
            if (anim.isOldRod() && item instanceof ItemFishingRod) {
                return true;
            }
            
            // Eating
            if (anim.isOldEating() && item instanceof ItemFood && player.getItemInUse() != null) {
                return true;
            }
        } else {
            // Punching
            if (anim.isPunching() && player.isSwingInProgress) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Применяет 1.7 трансформации
     * КРИТИЧНО: Эти трансформации применяются ПЕРЕД vanilla scale!
     */
    private static void apply17Transforms(ItemStack stack, EntityPlayerSP player, float swingProgress, Animations anim) {
        // Расчет swing
        float swing = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        
        boolean isBlocking = stack != null && player.isBlocking();
        
        // === 1.7 BLOCKHIT (ПРИОРИТЕТ!) ===
        if (anim.isOldBlockhit() && isBlocking) {
            System.out.println("[AnimationTransformHandler] Applying 1.7 blockhit animation!");
            // 1.7 blockhit трансформация
            // Эти значения взяты из декомпиляции оригинального 1.7 ItemRenderer
            GlStateManager.translate(-0.5F, 0.2F, 0.0F);
            GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(60.0F, 0.0F, 0.0F, 1.0F);
            return; // Блокхит отменяет все остальное
        }
        
        // === PUNCHING (без предмета) ===
        if (stack == null && anim.isPunching() && player.isSwingInProgress) {
            GlStateManager.rotate(-swing * 40.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(swing * 10.0F, 0.0F, 1.0F, 0.0F);
            return;
        }
        
        if (stack == null) return;
        
        Item item = stack.getItem();
        
        // === SWORD SWING ===
        if (anim.isOldSword() && item instanceof ItemSword && player.isSwingInProgress) {
            if (anim.isSmoothSwing()) {
                // Плавный свинг (квадратичная интерполяция)
                GlStateManager.rotate(-swing * 40.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(swing * 10.0F, 0.0F, 1.0F, 0.0F);
            } else {
                // Линейный свинг
                GlStateManager.rotate(-swingProgress * 40.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(swingProgress * 10.0F, 0.0F, 1.0F, 0.0F);
            }
            return;
        }
        
        // === BOW ===
        if (anim.isOldBow() && item instanceof ItemBow && player.getItemInUse() != null) {
            int useCount = stack.getMaxItemUseDuration() - player.getItemInUseCount();
            float pull = (float)useCount / 20.0F;
            
            // 1.7 bow pull calculation
            pull = (pull * pull + pull * 2.0F) / 3.0F;
            if (pull > 1.0F) pull = 1.0F;
            
            GlStateManager.translate(0.0F, pull * -0.2F, 0.0F);
            GlStateManager.rotate(pull * -20.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(pull * 10.0F, 1.0F, 0.0F, 0.0F);
            return;
        }
        
        // === FISHING ROD ===
        if (anim.isOldRod() && item instanceof ItemFishingRod) {
            GlStateManager.translate(0.16F, 0.08F, -0.1F);
            GlStateManager.rotate(-10.0F, 1.0F, 0.0F, 0.0F);
            return;
        }
        
        // === EATING ===
        if (anim.isOldEating() && item instanceof ItemFood && player.getItemInUse() != null) {
            int useCount = stack.getMaxItemUseDuration() - player.getItemInUseCount();
            float eatProgress = (float)useCount / (float)stack.getMaxItemUseDuration();
            
            GlStateManager.translate(0.0F, eatProgress * -0.6F, 0.0F);
            GlStateManager.rotate(eatProgress * 90.0F, 0.0F, 1.0F, 0.0F);
            return;
        }
    }
    
    /**
     * Получает модуль анимаций
     */
    private static Animations getAnimations() {
        if (AmethystClient.moduleManager == null) return null;
        return (Animations) AmethystClient.moduleManager.getModuleByName("Animations");
    }
}