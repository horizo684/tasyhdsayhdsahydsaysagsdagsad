package com.amethyst.client.irc;

import com.amethyst.client.AmethystClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * GUI для настройки IRC (Custom Label и Color)
 */
public class IRCSettingsGUI extends GuiScreen {
    
    private final GuiScreen parent;
    private final IRCManager ircManager;
    
    private GuiTextField labelField;
    private GuiButton doneButton;
    private GuiButton cancelButton;
    
    // Color picker
    private int selectedColor;
    private final int[] presetColors = {
        0xFF9966FF, // Purple (default)
        0xFFFF6666, // Red
        0xFF66FF66, // Green
        0xFF6666FF, // Blue
        0xFFFFFF66, // Yellow
        0xFFFF66FF, // Magenta
        0xFF66FFFF, // Cyan
        0xFFFFFFFF, // White
        0xFFFF9933, // Orange
        0xFFFF3399, // Pink
    };
    
    private float openProgress = 0f;
    
    public IRCSettingsGUI(GuiScreen parent, IRCManager ircManager) {
        this.parent = parent;
        this.ircManager = ircManager;
        this.selectedColor = ircManager.getMyColor();
    }
    
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        
        ScaledResolution sr = new ScaledResolution(mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;
        
        // Text field для custom label
        labelField = new GuiTextField(0, fontRendererObj, centerX - 100, centerY - 60, 200, 20);
        labelField.setMaxStringLength(32);
        labelField.setText(ircManager.getMyCustomLabel());
        labelField.setFocused(true);
        
        // Buttons
        buttonList.add(doneButton = new GuiButton(1, centerX - 100, centerY + 80, 95, 20, "Done"));
        buttonList.add(cancelButton = new GuiButton(2, centerX + 5, centerY + 80, 95, 20, "Cancel"));
        
        openProgress = 0f;
    }
    
    @Override
    public void updateScreen() {
        labelField.updateCursorCounter();
        
        if (openProgress < 1f) {
            openProgress = Math.min(1f, openProgress + 0.1f);
        }
    }
    
    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth();
        int H = sr.getScaledHeight();
        int centerX = W / 2;
        int centerY = H / 2;
        
        // Background
        float alpha = openProgress;
        int bgAlpha = (int)(alpha * 0xCC);
        drawGradientRect(0, 0, W, H, bgAlpha << 24, (bgAlpha << 24) | 0x050510);
        
        // Scale animation
        float scale = 0.8f + openProgress * 0.2f;
        GlStateManager.pushMatrix();
        GlStateManager.translate(centerX, centerY, 0);
        GlStateManager.scale(scale, scale, 1f);
        GlStateManager.translate(-centerX, -centerY, 0);
        GlStateManager.color(1f, 1f, 1f, alpha);
        
        // Panel
        int panelW = 400;
        int panelH = 240;
        int panelX = centerX - panelW / 2;
        int panelY = centerY - panelH / 2;
        
        drawRect(panelX, panelY, panelX + panelW, panelY + panelH, 0xEE181825);
        drawGradientRect(panelX, panelY, panelX + panelW, panelY + 1, 0xFF9966FF, 0xFF6644FF);
        
        // Title
        String title = "IRC SETTINGS";
        fontRendererObj.drawStringWithShadow(title, panelX + 16, panelY + 12, 0xFF9966FF);
        fontRendererObj.drawString("Customize your mod badge", panelX + 16, panelY + 25, 0x66AABBCC);
        
        // Custom Label section
        fontRendererObj.drawString("Custom Label:", panelX + 20, panelY + 50, 0xFFCCCCCC);
        labelField.drawTextBox();
        
        // Color picker section
        fontRendererObj.drawString("Badge Color:", panelX + 20, panelY + 90, 0xFFCCCCCC);
        
        // Color swatches
        int swatchSize = 24;
        int swatchSpacing = 4;
        int swatchX = panelX + 20;
        int swatchY = panelY + 105;
        
        for (int i = 0; i < presetColors.length; i++) {
            int col = i % 5;
            int row = i / 5;
            
            int x = swatchX + col * (swatchSize + swatchSpacing);
            int y = swatchY + row * (swatchSize + swatchSpacing);
            
            // Background
            drawRect(x, y, x + swatchSize, y + swatchSize, 0xFF222222);
            
            // Color
            drawRect(x + 2, y + 2, x + swatchSize - 2, y + swatchSize - 2, presetColors[i]);
            
            // Selection border
            if (presetColors[i] == selectedColor) {
                drawRect(x - 1, y - 1, x + swatchSize + 1, y, 0xFFFFFFFF);
                drawRect(x - 1, y + swatchSize, x + swatchSize + 1, y + swatchSize + 1, 0xFFFFFFFF);
                drawRect(x - 1, y, x, y + swatchSize, 0xFFFFFFFF);
                drawRect(x + swatchSize, y, x + swatchSize + 1, y + swatchSize, 0xFFFFFFFF);
            }
            
            // Hover effect
            if (mx >= x && mx <= x + swatchSize && my >= y && my <= y + swatchSize) {
                drawRect(x, y, x + swatchSize, y + swatchSize, 0x55FFFFFF);
            }
        }
        
        // Preview
        fontRendererObj.drawString("Preview:", panelX + 220, panelY + 105, 0xFFCCCCCC);
        
        String preview = labelField.getText().isEmpty() ? "AMETHYST USER" : labelField.getText();
        int previewWidth = fontRendererObj.getStringWidth(preview);
        int previewX = panelX + 220 + (160 - previewWidth) / 2;
        int previewY = panelY + 125;
        
        // Preview background
        drawRect(previewX - 4, previewY - 2, previewX + previewWidth + 4, previewY + 11, 0x88000000);
        
        // Preview text
        fontRendererObj.drawString(preview, previewX, previewY, selectedColor);
        
        GlStateManager.popMatrix();
        
        super.drawScreen(mx, my, pt);
    }
    
    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        labelField.mouseClicked(mx, my, btn);
        
        ScaledResolution sr = new ScaledResolution(mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;
        
        // Check color swatches
        int swatchSize = 24;
        int swatchSpacing = 4;
        int swatchX = centerX - 180;
        int swatchY = centerY - 15;
        
        for (int i = 0; i < presetColors.length; i++) {
            int col = i % 5;
            int row = i / 5;
            
            int x = swatchX + col * (swatchSize + swatchSpacing);
            int y = swatchY + row * (swatchSize + swatchSpacing);
            
            if (mx >= x && mx <= x + swatchSize && my >= y && my <= y + swatchSize) {
                selectedColor = presetColors[i];
                mc.getSoundHandler().playSound(
                    net.minecraft.client.audio.PositionedSoundRecord.create(
                        new net.minecraft.util.ResourceLocation("gui.button.press"), 1.0F
                    )
                );
                return;
            }
        }
        
        super.mouseClicked(mx, my, btn);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) { // Done
            String label = labelField.getText();
            if (label.isEmpty()) {
                label = "AMETHYST USER";
            }
            
            ircManager.setMyCustomLabel(label);
            ircManager.setMyColor(selectedColor);
            
            mc.displayGuiScreen(parent);
        } else if (button.id == 2) { // Cancel
            mc.displayGuiScreen(parent);
        }
    }
    
    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (labelField.isFocused()) {
            labelField.textboxKeyTyped(c, key);
        }
        
        if (key == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
        } else if (key == Keyboard.KEY_RETURN) {
            actionPerformed(doneButton);
        }
    }
    
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}