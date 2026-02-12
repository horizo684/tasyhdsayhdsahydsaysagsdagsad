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
            // Обфусцированные имена полей ItemRenderer
            equippedProgressField = ItemRenderer.class.getDeclaredField("field_78454_c");
            equippedProgressField.setAccessible(true);
            
            prevEquippedProgressField = ItemRenderer.class.getDeclaredField("field_78451_d");
            prevEquippedProgressField.setAccessible(true);
            
            itemToRenderField = ItemRenderer.class.getDeclaredField("field_78453_b");
            itemToRenderField.setAccessible(true);
            
            equippedItemSlotField = ItemRenderer.class.getDeclaredField("field_78455_a");
            equippedItemSlotField.setAccessible(true);
        } catch (Exception e) {
            System.err.println("[CustomItemRenderer] Failed to get reflection fields: " + e.getMessage());
        }
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
        
        // Применяем кастомные трансформации только если нужно
        boolean needsCustom = false;
        
        if (stack != null) {
            Item item = stack.getItem();
            needsCustom = (anim.isOldBlockhit() && player.isBlocking()) ||
                         (anim.isOldSword() && item instanceof ItemSword) ||
                         (anim.isOldBow() && item instanceof ItemBow) ||
                         (anim.isOldRod() && item instanceof ItemFishingRod) ||
                         (anim.isOldEating() && item instanceof ItemFood) ||
                         anim.getItemPosX() != 0.56f ||
                         anim.getItemPosY() != -0.52f ||
                         anim.getItemPosZ() != -0.72f ||
                         anim.getItemScale() != 0.4f;
        } else {
            needsCustom = anim.isPunching();
        }
        
        if (!needsCustom) {
            super.renderItemInFirstPerson(partialTicks);
            return;
        }
        
        // Применяем кастомные трансформации
        float equippedProgress = 0;
        float prevEquippedProgress = 0;
        
        try {
            equippedProgress = equippedProgressField.getFloat(this);
            prevEquippedProgress = prevEquippedProgressField.getFloat(this);
        } catch (Exception e) {
            // Ignore
        }
        
        float interpEquipped = prevEquippedProgress + (equippedProgress - prevEquippedProgress) * partialTicks;
        
        GlStateManager.pushMatrix();
        
        // === БАЗОВЫЕ ТРАНСФОРМАЦИИ ===
        applyBaseTransforms(interpEquipped, anim);
        
        // === СПЕЦИФИЧНЫЕ АНИМАЦИИ ===
        applySpecificAnimations(stack, player, partialTicks, anim);
        
        // Рендерим сам предмет
        if (stack != null) {
            renderItemStack(player, stack, ItemCameraTransforms.TransformType.FIRST_PERSON);
        }
        
        GlStateManager.popMatrix();
    }
    
    private void applyBaseTransforms(float equippedProgress, Animations anim) {
        // Базовая трансформация как в vanilla
        GlStateManager.translate(0.56F, -0.52F, -0.72F);
        GlStateManager.translate(0.0F, equippedProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        
        // Применяем кастомные смещения
        float dx = anim.getItemPosX() - 0.56f;
        float dy = anim.getItemPosY() - (-0.52f);
        float dz = anim.getItemPosZ() - (-0.72f);
        
        GlStateManager.translate(dx, dy, dz);
        
        // Применяем масштаб
        float scale = anim.getItemScale() / 0.4f;
        GlStateManager.scale(scale, scale, scale);
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
    
    private void renderItemStack(EntityLivingBase entity, ItemStack stack, ItemCameraTransforms.TransformType transform) {
        if (stack != null) {
            GlStateManager.pushMatrix();
            
            if (entity.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            
            mc.getItemRenderer().renderItem(entity, stack, transform);
            
            GlStateManager.popMatrix();
        }
    }
}
