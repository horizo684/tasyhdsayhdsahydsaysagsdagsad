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
     * Основано на Orange's 1.7 Animations и коде из Optifine issue #2098
     */
    private void apply17Transforms(ItemStack stack, EntityPlayerSP player, float equipProgress, float swingProgress, Animations anim) {
        boolean isBlocking = stack != null && player.isBlocking();
        boolean isEating = player.getItemInUse() != null && stack != null && stack.getItem() instanceof ItemFood;
        boolean isSwinging = player.isSwingInProgress && !isEating && !isBlocking;
        
        // === ДОПОЛНИТЕЛЬНЫЕ 1.7 ТРАНСФОРМАЦИИ ДЛЯ ОПРЕДЕЛЕННЫХ ПРЕДМЕТОВ ===
        // Bow (лук) - Item ID 261
        if (anim.isOldBow() && stack != null && Item.getIdFromItem(stack.getItem()) == 261) {
            GlStateManager.translate(-0.01f, 0.05f, -0.06f);
        }
        
        // Fishing Rod (удочка) - Item ID 346
        if (anim.isOldRod() && stack != null && Item.getIdFromItem(stack.getItem()) == 346) {
            GlStateManager.translate(0.08f, -0.027f, -0.33f);
            GlStateManager.scale(0.93f, 1.0f, 1.0f);
        }
        
        // Sword swing с уменьшением размера
        if (anim.isOldSword() && isSwinging && stack != null && !isEating && !isBlocking) {
            GlStateManager.scale(0.85f, 0.85f, 0.85f);
            GlStateManager.translate(-0.078f, 0.003f, 0.05f);
        }
        
        // === БАЗОВЫЕ ТРАНСФОРМАЦИИ (как в vanilla) ===
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        
        float swing = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float swingSqrt = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        
        // === VANILLA SWING ТРАНСФОРМАЦИИ ===
        GlStateManager.rotate(swing * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(swingSqrt * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(swingSqrt * -80.0F, 1.0F, 0.0F, 0.0F);
        
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
        
        // === 1.7 BLOCKHIT ===
        // Это ключевая часть! Blockhit работает потому что свинг происходит
        // даже когда игрок блокируется (благодаря BlockhitHandler)
        if (anim.isOldBlockhit() && isBlocking) {
            System.out.println("[CustomItemRenderer] >>> BLOCKHIT 1.7 ACTIVE <<<");
            // В 1.7 blockhit просто позволял свинг анимации работать при блокировке
            // Основная магия в том, что BlockhitHandler запускает свинг
            // А мы здесь не отменяем его трансформации
        }
    }
    
    /**
     * Получает модуль анимаций
     */
    private Animations getAnimations() {
        if (AmethystClient.moduleManager == null) return null;
        return (Animations) AmethystClient.moduleManager.getModuleByName("Animations");
    }
}