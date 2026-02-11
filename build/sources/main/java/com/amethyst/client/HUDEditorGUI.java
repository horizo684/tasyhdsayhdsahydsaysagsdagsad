package com.amethyst.client;

import com.amethyst.client.modules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class HUDEditorGUI extends GuiScreen {
    
    private Minecraft mc = Minecraft.getMinecraft();
    private HUDElement draggedElement = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
    private enum HUDElement {
        SOUP_COUNTER,
        FPS_COUNTER,
        PING_COUNTER,
        CLOCK,
        CPS_COUNTER
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        
        ScaledResolution sr = new ScaledResolution(mc);
        
        // Draw title
        String title = "HUD Editor - Drag elements to reposition";
        int titleWidth = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawStringWithShadow(title, (sr.getScaledWidth() - titleWidth) / 2, 10, 0xFFFFFFFF);
        
        // Draw instructions
        String instructions = "Press ESC to save and exit";
        int instructionsWidth = mc.fontRendererObj.getStringWidth(instructions);
        mc.fontRendererObj.drawStringWithShadow(instructions, (sr.getScaledWidth() - instructionsWidth) / 2, 25, 0xFFAAAAAA);
        
        // Draw all HUD elements with boxes
        drawSoupCounter(mouseX, mouseY);
        drawFPSCounter(mouseX, mouseY);
        drawPingCounter(mouseX, mouseY);
        drawClockCounter(mouseX, mouseY);
        drawCPSCounter(mouseX, mouseY);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawSoupCounter(int mouseX, int mouseY) {
        SoupCounter soupCounter = (SoupCounter) AmethystClient.moduleManager.getModuleByName("SoupCounter");
        if (soupCounter == null || !soupCounter.isEnabled()) {
            return;
        }
        
        int x = HUDConfig.getSoupCounterX();
        int y = HUDConfig.getSoupCounterY();
        int soupCount = soupCounter.getSoupCount();
        
        // Draw soup icon
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        
        ItemStack soupStack = new ItemStack(Items.mushroom_stew);
        mc.getRenderItem().renderItemAndEffectIntoGUI(soupStack, x, y);
        
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        
        // Draw count
        String text = String.valueOf(soupCount);
        mc.fontRendererObj.drawStringWithShadow(text, x + 20, y + 4, 0xFFFFFFFF);
        
        // Draw bounding box
        int width = 20 + mc.fontRendererObj.getStringWidth(text);
        boolean hovered = isHovered(mouseX, mouseY, x, y, width, 16);
        drawRect(x - 2, y - 2, x + width + 2, y + 18, hovered ? 0x8000FF00 : 0x40FFFFFF);
    }
    
    private void drawFPSCounter(int mouseX, int mouseY) {
        FPSCounter fpsCounter = (FPSCounter) AmethystClient.moduleManager.getModuleByName("FPS Counter");
        if (fpsCounter == null || !fpsCounter.isEnabled()) {
            return;
        }
        
        int x = HUDConfig.getFPSCounterX();
        int y = HUDConfig.getFPSCounterY();
        String text = fpsCounter.getText();
        
        mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFF00FF00);
        
        // Draw bounding box
        int width = mc.fontRendererObj.getStringWidth(text);
        boolean hovered = isHovered(mouseX, mouseY, x, y, width, 10);
        drawRect(x - 2, y - 2, x + width + 2, y + 12, hovered ? 0x8000FF00 : 0x40FFFFFF);
    }
    
    private void drawPingCounter(int mouseX, int mouseY) {
        PingCounter pingCounter = (PingCounter) AmethystClient.moduleManager.getModuleByName("Ping Counter");
        if (pingCounter == null || !pingCounter.isEnabled()) {
            return;
        }
        
        int x = HUDConfig.getPingCounterX();
        int y = HUDConfig.getPingCounterY();
        String text = pingCounter.getText();
        
        mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFFFFF00);
        
        // Draw bounding box
        int width = mc.fontRendererObj.getStringWidth(text);
        boolean hovered = isHovered(mouseX, mouseY, x, y, width, 10);
        drawRect(x - 2, y - 2, x + width + 2, y + 12, hovered ? 0x8000FF00 : 0x40FFFFFF);
    }
    
    private void drawClockCounter(int mouseX, int mouseY) {
        Clock clock = (Clock) AmethystClient.moduleManager.getModuleByName("Clock");
        if (clock == null || !clock.isEnabled()) {
            return;
        }
        
        int x = HUDConfig.getClockX();
        int y = HUDConfig.getClockY();
        String text = clock.getText();
        
        mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFFFFFFF);
        
        // Draw bounding box
        int width = mc.fontRendererObj.getStringWidth(text);
        boolean hovered = isHovered(mouseX, mouseY, x, y, width, 10);
        drawRect(x - 2, y - 2, x + width + 2, y + 12, hovered ? 0x8000FF00 : 0x40FFFFFF);
    }
    
    private void drawCPSCounter(int mouseX, int mouseY) {
        CPSCounter cpsCounter = (CPSCounter) AmethystClient.moduleManager.getModuleByName("CPS Counter");
        if (cpsCounter == null || !cpsCounter.isEnabled()) {
            return;
        }
        
        int x = HUDConfig.getCPSCounterX();
        int y = HUDConfig.getCPSCounterY();
        String text = cpsCounter.getText();
        
        mc.fontRendererObj.drawStringWithShadow(text, x, y, cpsCounter.getDisplayColor());
        
        // Draw bounding box
        int width = mc.fontRendererObj.getStringWidth(text);
        boolean hovered = isHovered(mouseX, mouseY, x, y, width, 10);
        drawRect(x - 2, y - 2, x + width + 2, y + 12, hovered ? 0x8000FF00 : 0x40FFFFFF);
    }
    
    private boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x - 2 && mouseX <= x + width + 2 && mouseY >= y - 2 && mouseY <= y + height + 2;
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            // Check Soup Counter
            SoupCounter soupCounter = (SoupCounter) AmethystClient.moduleManager.getModuleByName("SoupCounter");
            if (soupCounter != null && soupCounter.isEnabled()) {
                int x = HUDConfig.getSoupCounterX();
                int y = HUDConfig.getSoupCounterY();
                int width = 36;
                if (isHovered(mouseX, mouseY, x, y, width, 16)) {
                    draggedElement = HUDElement.SOUP_COUNTER;
                    dragOffsetX = mouseX - x;
                    dragOffsetY = mouseY - y;
                    return;
                }
            }
            
            // Check FPS Counter
            FPSCounter fpsCounter = (FPSCounter) AmethystClient.moduleManager.getModuleByName("FPS Counter");
            if (fpsCounter != null && fpsCounter.isEnabled()) {
                int x = HUDConfig.getFPSCounterX();
                int y = HUDConfig.getFPSCounterY();
                int width = mc.fontRendererObj.getStringWidth(fpsCounter.getText());
                if (isHovered(mouseX, mouseY, x, y, width, 10)) {
                    draggedElement = HUDElement.FPS_COUNTER;
                    dragOffsetX = mouseX - x;
                    dragOffsetY = mouseY - y;
                    return;
                }
            }
            
            // Check Ping Counter
            PingCounter pingCounter = (PingCounter) AmethystClient.moduleManager.getModuleByName("Ping Counter");
            if (pingCounter != null && pingCounter.isEnabled()) {
                int x = HUDConfig.getPingCounterX();
                int y = HUDConfig.getPingCounterY();
                int width = mc.fontRendererObj.getStringWidth(pingCounter.getText());
                if (isHovered(mouseX, mouseY, x, y, width, 10)) {
                    draggedElement = HUDElement.PING_COUNTER;
                    dragOffsetX = mouseX - x;
                    dragOffsetY = mouseY - y;
                    return;
                }
            }
            
            // Check Clock
            Clock clock = (Clock) AmethystClient.moduleManager.getModuleByName("Clock");
            if (clock != null && clock.isEnabled()) {
                int x = HUDConfig.getClockX();
                int y = HUDConfig.getClockY();
                int width = mc.fontRendererObj.getStringWidth(clock.getText());
                if (isHovered(mouseX, mouseY, x, y, width, 10)) {
                    draggedElement = HUDElement.CLOCK;
                    dragOffsetX = mouseX - x;
                    dragOffsetY = mouseY - y;
                    return;
                }
            }
            
            // Check CPS Counter
            CPSCounter cpsCounter = (CPSCounter) AmethystClient.moduleManager.getModuleByName("CPS Counter");
            if (cpsCounter != null && cpsCounter.isEnabled()) {
                int x = HUDConfig.getCPSCounterX();
                int y = HUDConfig.getCPSCounterY();
                int width = mc.fontRendererObj.getStringWidth(cpsCounter.getText());
                if (isHovered(mouseX, mouseY, x, y, width, 10)) {
                    draggedElement = HUDElement.CPS_COUNTER;
                    dragOffsetX = mouseX - x;
                    dragOffsetY = mouseY - y;
                    return;
                }
            }
        }
        
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        draggedElement = null;
        super.mouseReleased(mouseX, mouseY, state);
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (draggedElement != null) {
            int newX = mouseX - dragOffsetX;
            int newY = mouseY - dragOffsetY;
            
            switch (draggedElement) {
                case SOUP_COUNTER:
                    HUDConfig.setSoupCounterX(newX);
                    HUDConfig.setSoupCounterY(newY);
                    break;
                case FPS_COUNTER:
                    HUDConfig.setFPSCounterX(newX);
                    HUDConfig.setFPSCounterY(newY);
                    break;
                case PING_COUNTER:
                    HUDConfig.setPingCounterX(newX);
                    HUDConfig.setPingCounterY(newY);
                    break;
                case CLOCK:
                    HUDConfig.setClockX(newX);
                    HUDConfig.setClockY(newY);
                    break;
                case CPS_COUNTER:
                    HUDConfig.setCPSCounterX(newX);
                    HUDConfig.setCPSCounterY(newY);
                    break;
            }
        }
        
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}