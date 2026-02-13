package com.amethyst.client;

import com.amethyst.client.modules.Animations;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class AnimationPickerGUI extends GuiScreen {

    private final GuiScreen parent;
    private final Animations animations;
    
    private int scrollOffset = 0;
    private int maxScroll = 0;
    
    private float openProgress = 0f;
    private boolean closing = false;
    
    private boolean isDragging = false;
    private String draggingSlider = null;

    public AnimationPickerGUI(GuiScreen parent, Animations animations) {
        this.parent = parent;
        this.animations = animations;
    }

    @Override
    public void initGui() {
        scrollOffset = 0;
        openProgress = 0f;
        closing = false;
    }

    @Override
    public void updateScreen() {
        if (!closing) {
            openProgress = Math.min(1f, openProgress + 0.1f);
        } else {
            openProgress = Math.max(0f, openProgress - 0.15f);
            if (openProgress <= 0f) {
                mc.displayGuiScreen(parent);
            }
        }
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth();
        int H = sr.getScaledHeight();

        // Background
        float alpha = openProgress;
        int bgAlpha = (int)(alpha * 0xCC);
        drawGradientRect(0, 0, W, H, bgAlpha << 24, (bgAlpha << 24) | 0x050510);

        // Scale animation
        float scale = 0.8f + openProgress * 0.2f;
        GlStateManager.pushMatrix();
        GlStateManager.translate(W / 2f, H / 2f, 0);
        GlStateManager.scale(scale, scale, 1f);
        GlStateManager.translate(-W / 2f, -H / 2f, 0);
        GlStateManager.color(1f, 1f, 1f, alpha);

        int panelW = 420;
        int panelH = 340;
        int panelX = W / 2 - panelW / 2;
        int panelY = H / 2 - panelH / 2;

        // Panel background
        drawRoundedPanel(panelX, panelY, panelW, panelH);

        // Header
        String title = "⚡ ANIMATIONS SETTINGS";
        mc.fontRendererObj.drawStringWithShadow(title, panelX + 16, panelY + 12, 0xFF9966FF);
        mc.fontRendererObj.drawString("Configure 1.7 style animations", 
                panelX + 16, panelY + 25, 0x66AABBCC);

        // Accent line
        drawGradientRect(panelX + 12, panelY + 36, panelX + panelW - 12, panelY + 37,
                0xFF9966FF, 0xFF6644FF);

        // Content area with scrolling
        int contentY = panelY + 45;
        int contentH = panelH - 60;
        
        ScaledResolution sr2 = new ScaledResolution(mc);
        int sf = sr2.getScaleFactor();
        
        // Enable scissor
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
        org.lwjgl.opengl.GL11.glScissor(
            panelX * sf,
            mc.displayHeight - (contentY + contentH) * sf,
            panelW * sf,
            contentH * sf
        );

        // Draw content with scroll
        drawContentScrollable(panelX, contentY - scrollOffset, panelW, mx, my);
        
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);

        // Close button
        int btnX = panelX + panelW - 28;
        int btnY = panelY + 8;
        boolean closeHovered = mx >= btnX && mx <= btnX + 20 && my >= btnY && my <= btnY + 20;
        
        int closeColor = closeHovered ? 0xFFFF6666 : 0xFFAA4444;
        drawRect(btnX, btnY, btnX + 20, btnY + 20, closeColor);
        mc.fontRendererObj.drawStringWithShadow("×", btnX + 6, btnY + 5, 0xFFFFFFFF);

        GlStateManager.popMatrix();
        
        super.drawScreen(mx, my, pt);
    }

    private void drawContentScrollable(int x, int y, int w, int mx, int my) {
        int yPos = y + 10;
        
        // === TOGGLES ===
        yPos = drawSection(x, yPos, w, "ANIMATION TOGGLES", 0xFF66CCFF);
        
        yPos = drawToggle(x + 20, yPos, w - 40, "Old Blockhit", 
                animations.isOldBlockhit(), mx, my);
        
        yPos = drawToggle(x + 20, yPos, w - 40, "Old Sword Swing", 
                animations.isOldSword(), mx, my);
        
        yPos = drawToggle(x + 20, yPos, w - 40, "Smooth Swing", 
                animations.isSmoothSwing(), mx, my);
        
        yPos = drawToggle(x + 20, yPos, w - 40, "Old Bow", 
                animations.isOldBow(), mx, my);
        
        yPos = drawToggle(x + 20, yPos, w - 40, "Old Rod", 
                animations.isOldRod(), mx, my);
        
        yPos = drawToggle(x + 20, yPos, w - 40, "Old Eating", 
                animations.isOldEating(), mx, my);
        
        yPos = drawToggle(x + 20, yPos, w - 40, "Punching", 
                animations.isPunching(), mx, my);
        
        yPos = drawToggle(x + 20, yPos, w - 40, "Old Damage (Red Armor)", 
                animations.isOldDamage(), mx, my);

        yPos += 10;

        // === SWING ===
        yPos = drawSection(x, yPos, w, "SWING SETTINGS", 0xFFFFAA33);
        
        yPos = drawSlider(x + 20, yPos, w - 40,
                "Swing Speed", animations.getSwingSpeed(), 1.0f, 20.0f, mx, my);

        yPos += 10;

        // === DAMAGE ===
        if (animations.isOldDamage()) {
            yPos = drawSection(x, yPos, w, "DAMAGE SETTINGS", 0xFFFF4455);
            
            yPos = drawSlider(x + 20, yPos, w - 40,
                    "Flash Duration", animations.getArmorFlashDuration(), 1, 40, mx, my);
            
            yPos = drawSlider(x + 20, yPos, w - 40,
                    "Red Intensity", animations.getArmorRedIntensity(), 0.0f, 1.0f, mx, my);
            
            yPos += 10;
        }

        // === PRESETS ===
        yPos = drawSection(x, yPos, w, "PRESETS", 0xFFAA66FF);
        
        yPos = drawButton(x + 20, yPos, (w - 60) / 3, "1.7 Style", mx, my);
        yPos -= 32;
        yPos = drawButton(x + 30 + (w - 60) / 3, yPos, (w - 60) / 3, "1.8 Style", mx, my);
        yPos -= 32;
        yPos = drawButton(x + 40 + 2 * (w - 60) / 3, yPos, (w - 60) / 3, "Vanilla", mx, my);
        
        yPos += 42;
        
        maxScroll = Math.max(0, yPos - y - 240);
    }

    private int drawSection(int x, int y, int w, String title, int color) {
        mc.fontRendererObj.drawString(title, x + 10, y + 4, color);
        drawGradientRect(x + 10, y + 14, x + w - 10, y + 15, color & 0x77FFFFFF, 0x00FFFFFF);
        return y + 20;
    }

    private int drawToggle(int x, int y, int w, String label, boolean value, int mx, int my) {
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + 28;
        
        int bgColor = hovered ? 0x55334455 : 0x33222233;
        drawRect(x, y, x + w, y + 28, bgColor);
        
        mc.fontRendererObj.drawString(label, x + 8, y + 9, 0xFFCCCCCC);
        
        int toggleX = x + w - 40;
        int toggleY = y + 6;
        int toggleW = 32;
        int toggleH = 16;
        
        int trackColor = value ? 0xFF66FF66 : 0xFF666666;
        drawRect(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, trackColor);
        
        int thumbX = value ? toggleX + toggleW - 14 : toggleX + 2;
        drawRect(thumbX, toggleY + 2, thumbX + 12, toggleY + toggleH - 2, 0xFFFFFFFF);
        
        return y + 32;
    }

    private int drawSlider(int x, int y, int w, String label, float value, float min, float max, int mx, int my) {
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + 30;
        
        int bgColor = hovered ? 0x55334455 : 0x33222233;
        drawRect(x, y, x + w, y + 30, bgColor);
        
        String displayValue = String.format("%.2f", value);
        mc.fontRendererObj.drawString(label + ": " + displayValue, x + 8, y + 4, 0xFFCCCCCC);
        
        int barX = x + 8;
        int barY = y + 18;
        int barW = w - 16;
        float normalized = (value - min) / (max - min);
        int filledW = (int)(normalized * barW);
        
        drawRect(barX, barY, barX + barW, barY + 8, 0xFF444444);
        drawRect(barX, barY, barX + filledW, barY + 8, 0xFF66CCFF);
        
        return y + 34;
    }

    private int drawButton(int x, int y, int w, String label, int mx, int my) {
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + 28;
        
        int bgColor = hovered ? 0xFF6644FF : 0xFF4433AA;
        drawRect(x, y, x + w, y + 28, bgColor);
        
        int textX = x + (w - mc.fontRendererObj.getStringWidth(label)) / 2;
        mc.fontRendererObj.drawString(label, textX, y + 9, 0xFFFFFFFF);
        
        return y + 32;
    }

    private void drawRoundedPanel(int x, int y, int w, int h) {
        drawRect(x + 2, y, x + w - 2, y + h, 0xEE181825);
        drawRect(x, y + 2, x + 2, y + h - 2, 0xEE181825);
        drawRect(x + w - 2, y + 2, x + w, y + h - 2, 0xEE181825);
        
        drawGradientRect(x, y, x + w, y + 1, 0xFF9966FF, 0xFF6644FF);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth();
        int H = sr.getScaledHeight();
        int panelW = 420;
        int panelH = 340;
        int panelX = W / 2 - panelW / 2;
        int panelY = H / 2 - panelH / 2;

        // Close button
        int btnX = panelX + panelW - 28;
        int btnY = panelY + 8;
        if (mx >= btnX && mx <= btnX + 20 && my >= btnY && my <= btnY + 20) {
            closing = true;
            return;
        }

        // ИСПРАВЛЕНО: Правильный расчет координат с учетом scroll
        int contentY = panelY + 45;
        int contentH = panelH - 60;
        
        // Проверяем что клик внутри content area
        if (my < contentY || my > contentY + contentH) {
            super.mouseClicked(mx, my, btn);
            return;
        }
        
        // Преобразуем координаты клика в координаты контента
        int scrolledMy = my - contentY + scrollOffset;
        
        // Начальная позиция контента
        int yPos = 10;
        
        // TOGGLES section
        yPos += 20; // Section header
        
        // Проверяем каждый toggle
        if (checkToggleClick(mx, scrolledMy, panelX + 20, yPos, panelW - 40)) {
            animations.setOldBlockhit(!animations.isOldBlockhit());
            AmethystClient.moduleManager.saveConfig();
            return;
        }
        yPos += 32;
        
        if (checkToggleClick(mx, scrolledMy, panelX + 20, yPos, panelW - 40)) {
            animations.setOldSword(!animations.isOldSword());
            AmethystClient.moduleManager.saveConfig();
            return;
        }
        yPos += 32;
        
        if (checkToggleClick(mx, scrolledMy, panelX + 20, yPos, panelW - 40)) {
            animations.setSmoothSwing(!animations.isSmoothSwing());
            AmethystClient.moduleManager.saveConfig();
            return;
        }
        yPos += 32;
        
        if (checkToggleClick(mx, scrolledMy, panelX + 20, yPos, panelW - 40)) {
            animations.setOldBow(!animations.isOldBow());
            AmethystClient.moduleManager.saveConfig();
            return;
        }
        yPos += 32;
        
        if (checkToggleClick(mx, scrolledMy, panelX + 20, yPos, panelW - 40)) {
            animations.setOldRod(!animations.isOldRod());
            AmethystClient.moduleManager.saveConfig();
            return;
        }
        yPos += 32;
        
        if (checkToggleClick(mx, scrolledMy, panelX + 20, yPos, panelW - 40)) {
            animations.setOldEating(!animations.isOldEating());
            AmethystClient.moduleManager.saveConfig();
            return;
        }
        yPos += 32;
        
        if (checkToggleClick(mx, scrolledMy, panelX + 20, yPos, panelW - 40)) {
            animations.setPunching(!animations.isPunching());
            AmethystClient.moduleManager.saveConfig();
            return;
        }
        yPos += 32;
        
        if (checkToggleClick(mx, scrolledMy, panelX + 20, yPos, panelW - 40)) {
            animations.setOldDamage(!animations.isOldDamage());
            AmethystClient.moduleManager.saveConfig();
            return;
        }
        yPos += 32;
        
        yPos += 10; // Gap
        yPos += 20; // Swing section header
        
        // Swing Speed slider
        if (checkSliderClick(mx, scrolledMy, panelX + 20, yPos, panelW - 40)) {
            isDragging = true;
            draggingSlider = "swingSpeed";
            updateSliderValue(panelX + 20, panelW - 40, mx);
            return;
        }
        yPos += 34;
        
        yPos += 10; // Gap
        
        // Damage settings (if enabled)
        if (animations.isOldDamage()) {
            yPos += 20; // Damage section header
            
            if (checkSliderClick(mx, scrolledMy, panelX + 20, yPos, panelW - 40)) {
                isDragging = true;
                draggingSlider = "armorFlashDuration";
                updateSliderValue(panelX + 20, panelW - 40, mx);
                return;
            }
            yPos += 34;
            
            if (checkSliderClick(mx, scrolledMy, panelX + 20, yPos, panelW - 40)) {
                isDragging = true;
                draggingSlider = "armorRedIntensity";
                updateSliderValue(panelX + 20, panelW - 40, mx);
                return;
            }
            yPos += 34;
            
            yPos += 10; // Gap
        }
        
        // Presets
        yPos += 20; // Presets section header
        
        int btnW = (panelW - 60) / 3;
        if (checkButtonClick(mx, scrolledMy, panelX + 20, yPos, btnW)) {
            animations.setPreset17();
            AmethystClient.moduleManager.saveConfig();
            return;
        }
        if (checkButtonClick(mx, scrolledMy, panelX + 30 + btnW, yPos, btnW)) {
            animations.setPreset18();
            AmethystClient.moduleManager.saveConfig();
            return;
        }
        if (checkButtonClick(mx, scrolledMy, panelX + 40 + 2 * btnW, yPos, btnW)) {
            animations.resetToVanilla();
            AmethystClient.moduleManager.saveConfig();
            return;
        }

        super.mouseClicked(mx, my, btn);
    }
    
    private boolean checkToggleClick(int mx, int my, int x, int y, int w) {
        return mx >= x && mx <= x + w && my >= y && my <= y + 28;
    }
    
    private boolean checkSliderClick(int mx, int my, int x, int y, int w) {
        return mx >= x && mx <= x + w && my >= y && my <= y + 30;
    }
    
    private boolean checkButtonClick(int mx, int my, int x, int y, int w) {
        return mx >= x && mx <= x + w && my >= y && my <= y + 28;
    }

    private void updateSliderValue(int x, int w, int mx) {
        if (draggingSlider == null) return;
        
        int barW = w - 16;
        float normalized = Math.max(0, Math.min(1, (float)(mx - (x + 8)) / barW));
        
        switch (draggingSlider) {
            case "swingSpeed":
                animations.setSwingSpeed(1.0f + normalized * 19.0f);
                break;
            case "armorFlashDuration":
                animations.setArmorFlashDuration((int)(1 + normalized * 39));
                break;
            case "armorRedIntensity":
                animations.setArmorRedIntensity(normalized);
                break;
        }
        
        AmethystClient.moduleManager.saveConfig();
    }
    
    @Override
    protected void mouseClickMove(int mx, int my, int btn, long timeSinceClick) {
        if (isDragging && draggingSlider != null) {
            ScaledResolution sr = new ScaledResolution(mc);
            int W = sr.getScaledWidth();
            int panelW = 420;
            int panelX = W / 2 - panelW / 2;
            updateSliderValue(panelX + 20, panelW - 40, mx);
        }
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        isDragging = false;
        draggingSlider = null;
        super.mouseReleased(mx, my, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset -= Integer.signum(wheel) * 20;
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
        }
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (key == 1) { // ESC
            closing = true;
        } else {
            super.keyTyped(c, key);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}