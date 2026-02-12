package com.amethyst.client;

import com.amethyst.client.modules.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;

public class CustomItemRenderer extends ItemRenderer {
    
    private final Minecraft mc;
    
    // Reflection fields
    private Field equippedProgressField;
    private Field prevEquippedProgressField;
    private Field itemToRenderField;
    private Field equippedItemSlotField;
    
    public CustomItemRenderer(Minecraft mcIn) {
        super(mcIn);
        this.mc = mcIn;
        
        try {
            // Пытаемся получить поля ItemRenderer (пробуем разные варианты имен)
            equippedProgressField = getFieldByNames(ItemRenderer.class, 
                "field_78454_c", "equippedProgress");
            if (equippedProgressField != null) {
                equippedProgressField.setAccessible(true);
            }
            
            prevEquippedProgressField = getFieldByNames(ItemRenderer.class,
                "field_78451_d", "prevEquippedProgress");
            if (prevEquippedProgressField != null) {
                prevEquippedProgressField.setAccessible(true);
            }
            
            itemToRenderField = getFieldByNames(ItemRenderer.class,
                "field_78453_b", "itemToRender");
            if (itemToRenderField != null) {
                itemToRenderField.setAccessible(true);
            }
            
            equippedItemSlotField = getFieldByNames(ItemRenderer.class,
                "field_78455_a", "equippedItemSlot");
            if (equippedItemSlotField != null) {
                equippedItemSlotField.setAccessible(true);
            }
            
            System.out.println("[CustomItemRenderer] Successfully initialized reflection fields");
        } catch (Exception e) {
            System.err.println("[CustomItemRenderer] Failed to get reflection fields: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Field getFieldByNames(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // Try next name
            }
        }
        return null;
    }
    
    private Animations getAnimations() {
        if (AmethystClient.moduleManager == null) return null;
        return (Animations) AmethystClient.moduleManager.getModuleByName("Animations");
    }
    
    @Override
    public void renderItemInFirstPerson(float partialTicks) {
        Animations anim = getAnimations();
        
        // Если анимации выключены или модуль не найден - используем vanilla
        if (anim == null || !anim.isEnabled()) {
            super.renderItemInFirstPerson(partialTicks);
            return;
        }
        
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) {
            super.renderItemInFirstPerson(partialTicks);
            return;
        }
        
        ItemStack stack = player.getHeldItem();
        
        // Проверяем нужны ли кастомные анимации
        boolean needsCustom = false;
        
        if (stack != null) {
            Item item = stack.getItem();
            
            // Проверяем активные 1.7 анимации
            if (anim.isOldBlockhit() && player.isBlocking()) needsCustom = true;
            if (anim.isOldSword() && item instanceof ItemSword && player.isSwingInProgress) needsCustom = true;
            if (anim.isOldBow() && item instanceof ItemBow && player.getItemInUse() != null) needsCustom = true;
            if (anim.isOldRod() && item instanceof ItemFishingRod) needsCustom = true;
            if (anim.isOldEating() && item instanceof ItemFood && player.getItemInUse() != null) needsCustom = true;
        } else {
            needsCustom = anim.isPunching() && player.isSwingInProgress;
        }
        
        // Если кастомные анимации не нужны - используем vanilla
        if (!needsCustom) {
            super.renderItemInFirstPerson(partialTicks);
            return;
        }
        
        // === КАСТОМНЫЙ РЕНДЕРИНГ С 1.7 АНИМАЦИЯМИ ===
        
        float equippedProgress = 0;
        float prevEquippedProgress = 0;
        
        try {
            equippedProgress = equippedProgressField.getFloat(this);
            prevEquippedProgress = prevEquippedProgressField.getFloat(this);
        } catch (Exception e) {
            // Fallback to vanilla if reflection fails
            super.renderItemInFirstPerson(partialTicks);
            return;
        }
        
        float interpEquipped = prevEquippedProgress + (equippedProgress - prevEquippedProgress) * partialTicks;
        
        GlStateManager.pushMatrix();
        
        // === БАЗОВЫЕ VANILLA ТРАНСФОРМАЦИИ (как в оригинале) ===
        // Позиция из настроек (1.7 = 0.56, -0.52, -0.72 / 1.8 = vanilla)
        float posX = anim.getItemPosX();
        float posY = anim.getItemPosY();
        float posZ = anim.getItemPosZ();
        
        GlStateManager.translate(posX, posY, posZ);
        GlStateManager.translate(0.0F, interpEquipped * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        
        // ВСЕГДА vanilla масштаб 0.4
        float scale = 0.4F;
        GlStateManager.scale(scale, scale, scale);
        
        // === СПЕЦИФИЧНЫЕ 1.7 АНИМАЦИИ ===
        applySpecificAnimations(stack, player, partialTicks, anim);
        
        // Рендерим предмет БЕЗ дополнительных трансформаций
        if (stack != null) {
            if (player.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            
            // Используем renderItem напрямую, который НЕ добавляет свои трансформации
            mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIRST_PERSON);
        }
        
        GlStateManager.popMatrix();
    }
    
    private void applySpecificAnimations(ItemStack stack, EntityPlayerSP player, float partialTicks, Animations anim) {
        if (stack == null) {
            // Анимация кулака
            if (anim.isPunching() && player.isSwingInProgress) {
                float swingProgress = player.getSwingProgress(partialTicks);
                float swing = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
                
                GlStateManager.rotate(-swing * 35.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(swing * 10.0F, 0.0F, 1.0F, 0.0F);
            }
            return;
        }
        
        Item item = stack.getItem();
        
        // === БЛОКИРОВАНИЕ (1.7 стиль) ===
        if (anim.isOldBlockhit() && player.isBlocking()) {
            GlStateManager.translate(anim.getBlockPosX(), anim.getBlockPosY(), anim.getBlockPosZ());
            
            // 1.7 позиция блокирования - более мягкая
            GlStateManager.translate(-0.3F, 0.1F, 0.0F);
            GlStateManager.rotate(20.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-60.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(40.0F, 0.0F, 1.0F, 0.0F);
        }
        
        // === МЕЧ (1.7 свинг) ===
        if (anim.isOldSword() && item instanceof ItemSword && player.isSwingInProgress) {
            float swingProgress = player.getSwingProgress(partialTicks);
            
            if (anim.isSmoothSwing()) {
                float smoothSwing = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
                GlStateManager.rotate(-smoothSwing * 40.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(smoothSwing * 10.0F, 0.0F, 1.0F, 0.0F);
            } else {
                GlStateManager.rotate(-swingProgress * 40.0F, 1.0F, 0.0F, 0.0F);
            }
        }
        
        // === ЛУК (1.7 натяжение) ===
        if (anim.isOldBow() && item instanceof ItemBow && player.getItemInUse() != null) {
            int useCount = stack.getMaxItemUseDuration() - player.getItemInUseCount();
            float pull = (float)useCount / 20.0F;
            pull = (pull * pull + pull * 2.0F) / 3.0F;
            
            if (pull > 1.0F) pull = 1.0F;
            
            GlStateManager.translate(0.0F, pull * -0.2F, 0.0F);
            GlStateManager.rotate(pull * -20.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(pull * 10.0F, 1.0F, 0.0F, 0.0F);
        }
        
        // === УДОЧКА (1.7 позиция) ===
        if (anim.isOldRod() && item instanceof ItemFishingRod) {
            GlStateManager.translate(0.16F, 0.08F, -0.1F);
            GlStateManager.rotate(-10.0F, 1.0F, 0.0F, 0.0F);
        }
        
        // === ЕДА (1.7 поедание) ===
        if (anim.isOldEating() && item instanceof ItemFood && player.getItemInUse() != null) {
            int useCount = stack.getMaxItemUseDuration() - player.getItemInUseCount();
            float eatProgress = (float)useCount / (float)stack.getMaxItemUseDuration();
            
            GlStateManager.translate(0.0F, eatProgress * -0.6F, 0.0F);
            GlStateManager.rotate(eatProgress * 90.0F, 0.0F, 1.0F, 0.0F);
        }
    }
}
