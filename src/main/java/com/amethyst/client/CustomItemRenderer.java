package com.amethyst.client;

import com.amethyst.client.modules.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.storage.MapData;

import java.lang.reflect.Method;

/**
 * Кастомный ItemRenderer с поддержкой 1.7 анимаций
 * Наследуется от vanilla ItemRenderer и переопределяет transformFirstPersonItem
 */
public class CustomItemRenderer extends ItemRenderer {
    
    private final Minecraft mc;
    
    public CustomItemRenderer(Minecraft mcIn) {
        super(mcIn);
        this.mc = mcIn;
    }
    
    /**
     * Публичная обертка для transformFirstPersonItem
     * Вызывается через рефлексию из патча
     */
    public void doTransformFirstPersonItem(float equipProgress, float swingProgress) {
        Animations anim = getAnimations();
        
        // Если модуль выключен или не существует - используем vanilla
        if (anim == null || !anim.isEnabled()) {
            callVanillaTransform(equipProgress, swingProgress);
            return;
        }
        
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) {
            callVanillaTransform(equipProgress, swingProgress);
            return;
        }
        
        ItemStack stack = player.getHeldItem();
        
        // Проверяем нужны ли 1.7 анимации
        if (!needs17Animation(stack, player, anim)) {
            // Используем vanilla трансформации
            callVanillaTransform(equipProgress, swingProgress);
            return;
        }
        
        System.out.println("[CustomItemRenderer] Applying 1.7 transforms!");
        
        // Применяем наши 1.7 трансформации вместо vanilla
        apply17Transforms(stack, player, equipProgress, swingProgress, anim);
    }
    
    /**
     * Вызывает vanilla метод transformFirstPersonItem
     * Метод стал публичным благодаря ASM патчу
     */
    private void callVanillaTransform(float equipProgress, float swingProgress) {
        try {
            // Пытаемся вызвать напрямую (если ASM сработал)
            Method method = ItemRenderer.class.getMethod("transformFirstPersonItem", float.class, float.class);
            method.invoke(this, equipProgress, swingProgress);
        } catch (NoSuchMethodException e) {
            // Метод еще приватный - пытаемся через рефлексию
            try {
                Method method = ItemRenderer.class.getDeclaredMethod("transformFirstPersonItem", float.class, float.class);
                method.setAccessible(true);
                method.invoke(this, equipProgress, swingProgress);
            } catch (Exception ex) {
                System.err.println("[CustomItemRenderer] Failed to call vanilla transform: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[CustomItemRenderer] Failed to call vanilla transform: " + e.getMessage());
        }
    }
    
    /**
     * Проверяет нужны ли 1.7 анимации
     */
    private boolean needs17Animation(ItemStack stack, EntityPlayerSP player, Animations anim) {
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
     * Применяет 1.7 трансформации (полная замена vanilla метода)
     */
    private void apply17Transforms(ItemStack stack, EntityPlayerSP player, float equipProgress, float swingProgress, Animations anim) {
        // Базовые трансформации (как в vanilla)
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        
        float swing = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float swingSqrt = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        
        boolean isBlocking = stack != null && player.isBlocking();
        
        // === 1.7 BLOCKHIT ===
        if (anim.isOldBlockhit() && isBlocking) {
            System.out.println("[CustomItemRenderer] >>> BLOCKHIT 1.7 <<<");
            
            // Сбрасываем предыдущие vanilla трансформации
            GlStateManager.rotate(-swingSqrt * 20.0F, -1.0F, -0.0F, -0.0F);
            GlStateManager.rotate(-swing * 20.0F, -1.0F, -0.0F, -0.0F);
            GlStateManager.rotate(-swing * 80.0F, 1.0F, -0.0F, -0.0F);
            
            // Применяем 1.7 blockhit трансформацию
            GlStateManager.translate(-0.5F, 0.2F, 0.0F);
            GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(60.0F, 0.0F, 0.0F, 1.0F);
            return;
        }
        
        // === PUNCHING ===
        if (stack == null && anim.isPunching() && player.isSwingInProgress) {
            GlStateManager.rotate(-swingSqrt * 40.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(swing * 10.0F, 0.0F, 1.0F, 0.0F);
            return;
        }
        
        if (stack == null) {
            // Vanilla punching
            GlStateManager.rotate(-swingSqrt * 40.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-swing * 20.0F, 1.0F, 0.0F, 0.0F);
            return;
        }
        
        Item item = stack.getItem();
        
        // === SWORD SWING ===
        if (anim.isOldSword() && item instanceof ItemSword && player.isSwingInProgress) {
            if (anim.isSmoothSwing()) {
                GlStateManager.rotate(-swingSqrt * 40.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(swing * 10.0F, 0.0F, 1.0F, 0.0F);
            } else {
                GlStateManager.rotate(-swingProgress * 40.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(swingProgress * 10.0F, 0.0F, 1.0F, 0.0F);
            }
            return;
        }
        
        // === BOW ===
        if (anim.isOldBow() && item instanceof ItemBow && player.getItemInUse() != null) {
            int useCount = stack.getMaxItemUseDuration() - player.getItemInUseCount();
            float pull = (float)useCount / 20.0F;
            
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
        
        // Если ничего не подошло - используем vanilla swing
        GlStateManager.rotate(-swingSqrt * 40.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-swing * 20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-swing * 80.0F, 1.0F, 0.0F, 0.0F);
    }
    
    /**
     * Получает модуль анимаций
     */
    private Animations getAnimations() {
        if (AmethystClient.moduleManager == null) return null;
        return (Animations) AmethystClient.moduleManager.getModuleByName("Animations");
    }
}