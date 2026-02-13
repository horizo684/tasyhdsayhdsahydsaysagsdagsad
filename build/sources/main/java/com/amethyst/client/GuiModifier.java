package com.amethyst.client;

import com.amethyst.client.modules.Saturation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiModifier {
    
    private Minecraft mc = Minecraft.getMinecraft();
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderFood(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.FOOD) {
            // Cancel default food rendering (hunger bar)
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onRenderHealth(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HEALTH) {
            return;
        }
        
        EntityPlayer player = mc.thePlayer;
        if (player == null) {
            return;
        }
        
        ScaledResolution sr = new ScaledResolution(mc);
        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();
        
        // Render Saturation
        Saturation saturation = (Saturation) AmethystClient.moduleManager.getModuleByName("Saturation");
        if (saturation != null && saturation.isEnabled()) {
            FoodStats foodStats = player.getFoodStats();
            float saturationLevel = foodStats.getSaturationLevel();
            
            // Draw saturation value above hotbar (centered)
            String satText = String.format("§6Saturation: §f%.1f", saturationLevel);
            int textWidth = mc.fontRendererObj.getStringWidth(satText);
            int x = (width / 2) - (textWidth / 2);
            int y = height - 55; // Above hearts
            
            mc.fontRendererObj.drawStringWithShadow(satText, x, y, 0xFFFFFFFF);
        }
    }
}