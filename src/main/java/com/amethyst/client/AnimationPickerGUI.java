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
    
    // Animation state
    private float openProgress = 0f;
    private boolean closing = false;
    
    // Slider dragging state
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

        // Background dim
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
        
        // Enable scissor for scrolling
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
        org.lwjgl.opengl.GL11.glScissor(
            panelX * sf,
            mc.displayHeight - (contentY + contentH) * sf,
            panelW * sf,
            contentH * sf
        );

        int yPos = contentY - scrollOffset;

        // === PRESET BUTTONS ===
        yPos = drawSection(panelX, yPos, panelW, "PRESETS", 0xFF44AAFF);
        
        int preset17Y = yPos;
        int preset18Y = yPos + 32;
        
        // 1.7 Preset Button
        boolean hover17 = mx >= panelX + 16 && mx <= panelX + panelW/2 - 8 &&
                         my >= preset17Y && my <= preset17Y + 26;
        drawButton(panelX + 16, preset17Y, panelW/2 - 24, 26,
                "1.7 Animations", 0xFF44AA44, hover17);
        
        // 1.8 Preset Button
        boolean hover18 = mx >= panelX + panelW/2 + 8 && mx <= panelX + panelW - 16 &&
                         my >= preset18Y && my <= preset18Y + 26;
        drawButton(panelX + panelW/2 + 8, preset18Y, panelW/2 - 24, 26,
                "1.8 Animations", 0xFFAA4444, hover18);
        
        yPos += 70;

        // === ANIMATION TOGGLES ===
        yPos = drawSection(panelX, yPos, panelW, "ANIMATION TYPES", 0xFFFF4455);
        
        yPos = drawToggle(panelX + 20, yPos, panelW - 40, 
                "Old Blockhit", "1.7 blocking animation", 
                animations.isOldBlockhit(), mx, my);
        
        yPos = drawToggle(panelX + 20, yPos, panelW - 40,
                "Old Damage", "Red armor when hit (1.7)", 
                animations.isOldDamage(), mx, my);
        
        yPos = drawToggle(panelX + 20, yPos, panelW - 40,
                "Old Rod", "1.7 fishing rod animation", 
                animations.isOldRod(), mx, my);
        
        yPos = drawToggle(panelX + 20, yPos, panelW - 40,
                "Old Bow", "1.7 bow draw animation", 
                animations.isOldBow(), mx, my);
        
        yPos = drawToggle(panelX + 20, yPos, panelW - 40,
                "Old Sword", "1.7 sword swing", 
                animations.isOldSword(), mx, my);
        
        yPos = drawToggle(panelX + 20, yPos, panelW - 40,
                "Old Eating", "1.7 eating animation", 
                animations.isOldEating(), mx, my);
        
        yPos = drawToggle(panelX + 20, yPos, panelW - 40,
                "Punching", "Show punch animation", 
                animations.isPunching(), mx, my);
        
        yPos = drawToggle(panelX + 20, yPos, panelW - 40,
                "Smooth Swing", "Smooth sword swinging", 
                animations.isSmoothSwing(), mx, my);

        yPos += 10;

        // === SWING SETTINGS ===
        yPos = drawSection(panelX, yPos, panelW, "SWING SETTINGS", 0xFFFFAA33);
        
        yPos = drawSlider(panelX + 20, yPos, panelW - 40,
                "Swing Speed", animations.getSwingSpeed(), 1.0f, 20.0f, mx, my);

        yPos += 10;

        // === ITEM POSITION ===
        yPos = drawSection(panelX, yPos, panelW, "ITEM POSITION", 0xFF44FF99);
        
        yPos = drawSlider(panelX + 20, yPos, panelW - 40,
                "Position X", animations.getItemPosX(), -2.0f, 2.0f, mx, my);
        
        yPos = drawSlider(panelX + 20, yPos, panelW - 40,
                "Position Y", animations.getItemPosY(), -2.0f, 2.0f, mx, my);
        
        yPos = drawSlider(panelX + 20, yPos, panelW - 40,
                "Position Z", animations.getItemPosZ(), -2.0f, 2.0f, mx, my);
        
        yPos = drawSlider(panelX + 20, yPos, panelW - 40,
                "Item Scale", animations.getItemScale(), 0.1f, 1.0f, mx, my);

        yPos += 10;

        // === BLOCK POSITION ===
        yPos = drawSection(panelX, yPos, panelW, "BLOCK POSITION", 0xFF7B68EE);
        
        yPos = drawSlider(panelX + 20, yPos, panelW - 40,
                "Block X", animations.getBlockPosX(), -1.0f, 1.0f, mx, my);
        
        yPos = drawSlider(panelX + 20, yPos, panelW - 40,
                "Block Y", animations.getBlockPosY(), -1.0f, 1.0f, mx, my);
        
        yPos = drawSlider(panelX + 20, yPos, panelW - 40,
                "Block Z", animations.getBlockPosZ(), -1.0f, 1.0f, mx, my);

        yPos += 10;

        // === DAMAGE SETTINGS ===
        if (animations.isOldDamage()) {
            yPos = drawSection(panelX, yPos, panelW, "DAMAGE SETTINGS", 0xFFFF4455);
            
            yPos = drawSlider(panelX + 20, yPos, panelW - 40,
                    "Flash Duration", animations.getArmorFlashDuration(), 1, 40, mx, my);
            
            yPos = drawSlider(panelX + 20, yPos, panelW - 40,
                    "Red Intensity", animations.getArmorRedIntensity(), 0.0f, 1.0f, mx, my);
            
            yPos += 10;
        }

        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);

        // Calculate max scroll
        int totalContentHeight = yPos - (contentY - scrollOffset);
        maxScroll = Math.max(0, totalContentHeight - contentH + 10);

        // Scrollbar
        if (maxScroll > 0) {
            int sbX = panelX + panelW - 8;
            drawRect(sbX, contentY, sbX + 4, contentY + contentH, 0x22FFFFFF);
            float frac = (float) scrollOffset / maxScroll;
            int thumbH = Math.max(20, contentH - (int)(frac * (contentH - 30)));
            int thumbY = contentY + (int)(frac * (contentH - thumbH));
            drawRect(sbX, thumbY, sbX + 4, thumbY + thumbH, 0xAA9966FF);
        }

        // Close button
        String closeText = "✕";
        int closeX = panelX + panelW - 30;
        int closeY = panelY + 8;
        boolean closeHover = mx >= closeX && mx <= closeX + 20 && my >= closeY && my <= closeY + 20;
        mc.fontRendererObj.drawStringWithShadow(closeText, closeX + 6, closeY + 6,
                closeHover ? 0xFFFF4444 : 0x88FFFFFF);

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
        super.drawScreen(mx, my, pt);
    }

    private int drawSection(int x, int y, int w, String title, int color) {
        mc.fontRendererObj.drawString(title, x + 16, y, color);
        drawRect(x + 16, y + 11, x + w - 16, y + 12, 0x22FFFFFF);
        return y + 20;
    }

    private int drawToggle(int x, int y, int w, String label, String desc, boolean state, int mx, int my) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + 30;
        
        if (hover) {
            drawRect(x, y, x + w, y + 30, 0x18FFFFFF);
        }
        
        mc.fontRendererObj.drawString(label, x + 8, y + 6, 0xFFFFFFFF);
        mc.fontRendererObj.drawString(desc, x + 8, y + 18, 0x66AABBCC);
        
        int toggleX = x + w - 50;
        int toggleY = y + 8;
        int toggleColor = state ? 0xFF44AA44 : 0xFFAA4444;
        
        drawRect(toggleX, toggleY, toggleX + 40, toggleY + 14, (toggleColor & 0x00FFFFFF) | 0x44000000);
        drawHollowRect(toggleX, toggleY, toggleX + 40, toggleY + 14, toggleColor);
        
        String toggleText = state ? "ON" : "OFF";
        mc.fontRendererObj.drawString(toggleText, toggleX + (state ? 15 : 11), toggleY + 3, toggleColor);
        
        return y + 34;
    }

    private int drawSlider(int x, int y, int w, String label, float value, float min, float max, int mx, int my) {
        float normalized = (value - min) / (max - min);
        
        mc.fontRendererObj.drawString(label, x + 8, y + 2, 0xFFFFFFFF);
        
        String valueText = String.format("%.2f", value);
        mc.fontRendererObj.drawString(valueText, x + w - mc.fontRendererObj.getStringWidth(valueText) - 8, y + 2, 0xFF44AAFF);
        
        int barY = y + 14;
        int barW = w - 16;
        
        drawRect(x + 8, barY, x + 8 + barW, barY + 6, 0x33FFFFFF);
        drawRect(x + 8, barY, x + 8 + (int)(barW * normalized), barY + 6, 0xFF9966FF);
        
        int thumbX = x + 8 + (int)(barW * normalized) - 4;
        boolean hover = mx >= thumbX && mx <= thumbX + 8 && my >= barY - 2 && my <= barY + 8;
        drawRect(thumbX, barY - 2, thumbX + 8, barY + 8, hover ? 0xFFFFFFFF : 0xFFCCCCCC);
        
        return y + 26;
    }

    private void drawButton(int x, int y, int w, int h, String text, int color, boolean hover) {
        drawRect(x, y, x + w, y + h, hover ? ((color & 0x00FFFFFF) | 0x44000000) : ((color & 0x00FFFFFF) | 0x22000000));
        drawHollowRect(x, y, x + w, y + h, color);
        
        int textW = mc.fontRendererObj.getStringWidth(text);
        mc.fontRendererObj.drawStringWithShadow(text, x + w/2 - textW/2, y + h/2 - 4, hover ? 0xFFFFFFFF : color);
    }

    private void drawRoundedPanel(int x, int y, int w, int h) {
        drawRect(x + 3, y, x + w - 3, y + h, 0xEE0A0C14);
        drawRect(x, y + 3, x + w, y + h - 3, 0xEE0A0C14);
        drawHollowRect(x + 1, y + 1, x + w - 1, y + h - 1, 0x18FFFFFF);
        drawHollowRect(x + 2, y + 2, x + w - 2, y + h - 2, 0x10FFFFFF);
    }

    private void drawHollowRect(int l, int t, int r, int b, int color) {
        drawRect(l, t, r, t + 1, color);
        drawRect(l, b - 1, r, b, color);
        drawRect(l, t, l + 1, b, color);
        drawRect(r - 1, t, r, b, color);
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
        int closeX = panelX + panelW - 30;
        int closeY = panelY + 8;
        if (mx >= closeX && mx <= closeX + 20 && my >= closeY && my <= closeY + 20) {
            closing = true;
            return;
        }

        int contentY = panelY + 45;
        int yPos = contentY - scrollOffset;

        // Skip preset section header
        yPos += 20;
        
        // Preset buttons
        if (my >= yPos && my <= yPos + 26) {
            if (mx >= panelX + 16 && mx <= panelX + panelW/2 - 8) {
                animations.setPreset17();
                AmethystClient.moduleManager.saveConfig();
                return;
            }
        }
        yPos += 32;
        if (my >= yPos && my <= yPos + 26) {
            if (mx >= panelX + panelW/2 + 8 && mx <= panelX + panelW - 16) {
                animations.setPreset18();
                AmethystClient.moduleManager.saveConfig();
                return;
            }
        }
        yPos += 70 - 32;

        // Animation toggles section
        yPos += 20;
        yPos = handleToggleClick(panelX + 20, yPos, panelW - 40, mx, my, 
                () -> animations.setOldBlockhit(!animations.isOldBlockhit()));
        yPos = handleToggleClick(panelX + 20, yPos, panelW - 40, mx, my,
                () -> animations.setOldDamage(!animations.isOldDamage()));
        yPos = handleToggleClick(panelX + 20, yPos, panelW - 40, mx, my,
                () -> animations.setOldRod(!animations.isOldRod()));
        yPos = handleToggleClick(panelX + 20, yPos, panelW - 40, mx, my,
                () -> animations.setOldBow(!animations.isOldBow()));
        yPos = handleToggleClick(panelX + 20, yPos, panelW - 40, mx, my,
                () -> animations.setOldSword(!animations.isOldSword()));
        yPos = handleToggleClick(panelX + 20, yPos, panelW - 40, mx, my,
                () -> animations.setOldEating(!animations.isOldEating()));
        yPos = handleToggleClick(panelX + 20, yPos, panelW - 40, mx, my,
                () -> animations.setPunching(!animations.isPunching()));
        yPos = handleToggleClick(panelX + 20, yPos, panelW - 40, mx, my,
                () -> animations.setSmoothSwing(!animations.isSmoothSwing()));

        yPos += 10;
        
        // Swing settings section
        yPos += 20; // header
        yPos = handleSliderClick(panelX + 20, yPos, panelW - 40, mx, my, "swingSpeed");
        
        yPos += 10;
        
        // Item position section
        yPos += 20; // header
        yPos = handleSliderClick(panelX + 20, yPos, panelW - 40, mx, my, "itemPosX");
        yPos = handleSliderClick(panelX + 20, yPos, panelW - 40, mx, my, "itemPosY");
        yPos = handleSliderClick(panelX + 20, yPos, panelW - 40, mx, my, "itemPosZ");
        yPos = handleSliderClick(panelX + 20, yPos, panelW - 40, mx, my, "itemScale");
        
        yPos += 10;
        
        // Block position section
        yPos += 20; // header
        yPos = handleSliderClick(panelX + 20, yPos, panelW - 40, mx, my, "blockPosX");
        yPos = handleSliderClick(panelX + 20, yPos, panelW - 40, mx, my, "blockPosY");
        yPos = handleSliderClick(panelX + 20, yPos, panelW - 40, mx, my, "blockPosZ");
        
        yPos += 10;
        
        // Damage settings (if enabled)
        if (animations.isOldDamage()) {
            yPos += 20; // header
            yPos = handleSliderClick(panelX + 20, yPos, panelW - 40, mx, my, "armorFlashDuration");
            yPos = handleSliderClick(panelX + 20, yPos, panelW - 40, mx, my, "armorRedIntensity");
        }

        super.mouseClicked(mx, my, btn);
    }

    private int handleSliderClick(int x, int y, int w, int mx, int my, String sliderName) {
        int barY = y + 14;
        int barW = w - 16;
        
        if (mx >= x + 8 && mx <= x + 8 + barW && my >= barY - 2 && my <= barY + 8) {
            isDragging = true;
            draggingSlider = sliderName;
            updateSliderValue(x, w, mx);
        }
        
        return y + 26;
    }

    private int handleToggleClick(int x, int y, int w, int mx, int my, Runnable action) {
        if (mx >= x && mx <= x + w && my >= y && my <= y + 30) {
            action.run();
            AmethystClient.moduleManager.saveConfig();
        }
        return y + 34;
    }
    
    private void updateSliderValue(int x, int w, int mx) {
        if (draggingSlider == null) return;
        
        int barW = w - 16;
        float normalized = Math.max(0, Math.min(1, (float)(mx - (x + 8)) / barW));
        
        switch (draggingSlider) {
            case "swingSpeed":
                animations.setSwingSpeed(1.0f + normalized * 19.0f);
                break;
            case "itemPosX":
                animations.setItemPosX(-2.0f + normalized * 4.0f);
                break;
            case "itemPosY":
                animations.setItemPosY(-2.0f + normalized * 4.0f);
                break;
            case "itemPosZ":
                animations.setItemPosZ(-2.0f + normalized * 4.0f);
                break;
            case "itemScale":
                animations.setItemScale(0.1f + normalized * 0.9f);
                break;
            case "blockPosX":
                animations.setBlockPosX(-1.0f + normalized * 2.0f);
                break;
            case "blockPosY":
                animations.setBlockPosY(-1.0f + normalized * 2.0f);
                break;
            case "blockPosZ":
                animations.setBlockPosZ(-1.0f + normalized * 2.0f);
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
        super.mouseClickMove(mx, my, btn, timeSinceClick);
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
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            scrollOffset = Math.max(0, Math.min(scrollOffset + (scroll > 0 ? -20 : 20), maxScroll));
        }
    }

    @Override
    protected void keyTyped(char ch, int key) throws IOException {
        if (key == 1) {
            closing = true;
            return;
        }
        super.keyTyped(ch, key);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}