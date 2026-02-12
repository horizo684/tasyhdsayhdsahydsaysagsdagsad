package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.Module.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Saturation extends Module {
    private Minecraft mc = Minecraft.getMinecraft();
    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    public Saturation() {
        super("Saturation", "Centers health/armor bars and hides hunger", 0, Category.RENDER);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderHealth(RenderGameOverlayEvent.Pre event) {
        if (!isEnabled() || mc.thePlayer == null) {
            return;
        }

        // Отменяем стандартный рендер голода
        if (event.type == RenderGameOverlayEvent.ElementType.FOOD) {
            event.setCanceled(true);
        }

        // Отменяем стандартный рендер здоровья и рисуем по центру
        if (event.type == RenderGameOverlayEvent.ElementType.HEALTH) {
            event.setCanceled(true);
            renderCenteredHealth(event.resolution);
        }
        
        // Отменяем стандартный рендер брони и рисуем над здоровьем
        if (event.type == RenderGameOverlayEvent.ElementType.ARMOR) {
            event.setCanceled(true);
            renderCenteredArmor(event.resolution);
        }
    }

    private void renderCenteredHealth(ScaledResolution resolution) {
        EntityPlayer player = mc.thePlayer;
        int health = (int) Math.ceil(player.getHealth());
        int maxHealth = (int) Math.ceil(player.getMaxHealth());
        
        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();
        
        int hearts = (int) Math.ceil(maxHealth / 2.0F);
        int heartWidth = 9;
        int totalWidth = hearts * heartWidth;
        
        int startX = (screenWidth - totalWidth) / 2;
        int startY = screenHeight - 39;
        
        mc.getTextureManager().bindTexture(ICONS);
        
        for (int i = 0; i < hearts; i++) {
            int x = startX + i * heartWidth;
            int y = startY;
            
            // Рисуем пустое сердце (фон)
            drawTexturedModalRect(x, y, 16, 0, 9, 9);
            
            int currentHeartHealth = health - i * 2;
            
            // Рисуем заполненное сердце
            if (currentHeartHealth >= 2) {
                drawTexturedModalRect(x, y, 52, 0, 9, 9);
            } else if (currentHeartHealth == 1) {
                drawTexturedModalRect(x, y, 61, 0, 9, 9);
            }
        }
    }
    
    private void renderCenteredArmor(ScaledResolution resolution) {
        EntityPlayer player = mc.thePlayer;
        int armor = player.getTotalArmorValue();
        
        if (armor <= 0) {
            return; // Не рисуем если нет брони
        }
        
        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();
        
        int armorIcons = (int) Math.ceil(armor / 2.0F);
        int armorWidth = 9;
        int totalWidth = armorIcons * armorWidth;
        
        int startX = (screenWidth - totalWidth) / 2;
        int startY = screenHeight - 49; // На 10 пикселей выше сердец
        
        mc.getTextureManager().bindTexture(ICONS);
        
        for (int i = 0; i < armorIcons; i++) {
            int x = startX + i * armorWidth;
            int y = startY;
            
            // Рисуем пустую броню (фон)
            drawTexturedModalRect(x, y, 16, 9, 9, 9);
            
            int currentArmorValue = armor - i * 2;
            
            // Рисуем заполненную броню
            if (currentArmorValue >= 2) {
                drawTexturedModalRect(x, y, 34, 9, 9, 9);
            } else if (currentArmorValue == 1) {
                drawTexturedModalRect(x, y, 25, 9, 9, 9);
            }
        }
    }

    private void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
        Gui.drawModalRectWithCustomSizedTexture(x, y, textureX, textureY, width, height, 256, 256);
    }
}