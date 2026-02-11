package com.amethyst.client.gui;

import com.amethyst.client.AmethystClient;
import com.amethyst.client.modules.Module;
import com.amethyst.client.modules.Module.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends GuiScreen {

    private List<CategoryPanel> panels = new ArrayList<>();
    private ModuleSettingsPanel settingsPanel = null;
    
    public ClickGUI() {
        int startX = 20;
        int startY = 20;
        int panelSpacing = 140;
        
        int i = 0;
        for (Category category : Category.values()) {
            panels.add(new CategoryPanel(category, startX + (i * panelSpacing), startY));
            i++;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        
        // Draw all category panels
        for (CategoryPanel panel : panels) {
            panel.draw(mouseX, mouseY, partialTicks);
        }
        
        // Draw settings panel on top
        if (settingsPanel != null) {
            settingsPanel.draw(mouseX, mouseY, partialTicks);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        // Check settings panel first (it's on top)
        if (settingsPanel != null) {
            if (settingsPanel.mouseClicked(mouseX, mouseY, button)) {
                return; // Consumed by settings panel
            }
        }
        
        // Check category panels
        for (CategoryPanel panel : panels) {
            panel.mouseClicked(mouseX, mouseY, button);
        }
        
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (CategoryPanel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, state);
        }
        
        if (settingsPanel != null) {
            settingsPanel.mouseReleased(mouseX, mouseY, state);
        }
        
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        
        if (settingsPanel != null && settingsPanel.isWaitingForKey()) {
            settingsPanel.setKey(keyCode);
            return;
        }
        
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void openSettings(Module module, int x, int y) {
        settingsPanel = new ModuleSettingsPanel(module, x, y);
    }

    public void closeSettings() {
        settingsPanel = null;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CATEGORY PANEL
    // ═══════════════════════════════════════════════════════════════════════════

    class CategoryPanel {
        private Category category;
        private int x, y;
        private int width = 120;
        private int headerHeight = 20;
        private boolean dragging = false;
        private int dragOffsetX, dragOffsetY;
        private boolean expanded = true;
        private float expandProgress = 1.0f;
        
        private List<ModuleButton> moduleButtons = new ArrayList<>();

        public CategoryPanel(Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
            
            for (Module module : AmethystClient.moduleManager.getModulesInCategory(category)) {
                moduleButtons.add(new ModuleButton(module));
            }
        }

        public void draw(int mouseX, int mouseY, float partialTicks) {
            Minecraft mc = Minecraft.getMinecraft();
            
            // Animate expand/collapse
            float targetProgress = expanded ? 1.0f : 0.0f;
            if (expandProgress != targetProgress) {
                float speed = 0.15f;
                if (expandProgress < targetProgress) {
                    expandProgress = Math.min(expandProgress + speed, targetProgress);
                } else {
                    expandProgress = Math.max(expandProgress - speed, targetProgress);
                }
            }
            
            // Header
            boolean headerHovered = mouseX >= x && mouseX <= x + width && 
                                   mouseY >= y && mouseY <= y + headerHeight;
            int headerColor = headerHovered ? 0xFF3A3A3A : 0xFF2A2A2A;
            drawRect(x, y, x + width, y + headerHeight, headerColor);
            
            // Category name
            String name = category.name();
            int nameWidth = mc.fontRendererObj.getStringWidth(name);
            mc.fontRendererObj.drawStringWithShadow(name, 
                x + width / 2 - nameWidth / 2, 
                y + 6, 0xFFFFFFFF);
            
            // Draw expand indicator
            String indicator = expanded ? "▼" : "▶";
            mc.fontRendererObj.drawStringWithShadow(indicator, x + width - 12, y + 6, 0xFFAAAAAA);
            
            // Modules (with animation)
            if (expandProgress > 0.01f) {
                int currentY = y + headerHeight;
                int visibleModules = (int)(moduleButtons.size() * expandProgress);
                
                for (int i = 0; i < visibleModules; i++) {
                    ModuleButton btn = moduleButtons.get(i);
                    float alpha = (i == visibleModules - 1) ? 
                        (expandProgress * moduleButtons.size() - visibleModules) : 1.0f;
                    
                    btn.draw(x, currentY, width, mouseX, mouseY, alpha);
                    currentY += 16;
                }
            }
            
            // Border
            drawHollowRect(x, y, x + width, y + getHeight(), 0xFF000000);
        }

        public void mouseClicked(int mouseX, int mouseY, int button) {
            // Header click
            if (mouseX >= x && mouseX <= x + width && 
                mouseY >= y && mouseY <= y + headerHeight) {
                
                if (button == 0) { // Left click - drag
                    dragging = true;
                    dragOffsetX = mouseX - x;
                    dragOffsetY = mouseY - y;
                } else if (button == 1) { // Right click - expand/collapse
                    expanded = !expanded;
                }
                return;
            }
            
            // Module clicks
            if (expanded && expandProgress > 0.5f) {
                int currentY = y + headerHeight;
                for (ModuleButton btn : moduleButtons) {
                    if (mouseX >= x && mouseX <= x + width && 
                        mouseY >= currentY && mouseY <= currentY + 16) {
                        
                        if (button == 0) { // Left click - toggle
                            btn.module.toggle();
                        } else if (button == 1) { // Right click - settings
                            openSettings(btn.module, mouseX, mouseY);
                        }
                        return;
                    }
                    currentY += 16;
                }
            }
        }

        public void mouseReleased(int mouseX, int mouseY, int state) {
            if (dragging) {
                x = mouseX - dragOffsetX;
                y = mouseY - dragOffsetY;
            }
            dragging = false;
        }

        private int getHeight() {
            return headerHeight + (int)(moduleButtons.size() * 16 * expandProgress);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MODULE BUTTON
    // ═══════════════════════════════════════════════════════════════════════════

    class ModuleButton {
        private Module module;
        private float toggleAnimation = 0.0f;

        public ModuleButton(Module module) {
            this.module = module;
            toggleAnimation = module.isEnabled() ? 1.0f : 0.0f;
        }

        public void draw(int x, int y, int width, int mouseX, int mouseY, float alpha) {
            Minecraft mc = Minecraft.getMinecraft();
            
            // Animate toggle
            float targetAnim = module.isEnabled() ? 1.0f : 0.0f;
            if (toggleAnimation != targetAnim) {
                float speed = 0.1f;
                if (toggleAnimation < targetAnim) {
                    toggleAnimation = Math.min(toggleAnimation + speed, targetAnim);
                } else {
                    toggleAnimation = Math.max(toggleAnimation - speed, targetAnim);
                }
            }
            
            // Background
            boolean hovered = mouseX >= x && mouseX <= x + width && 
                             mouseY >= y && mouseY <= y + 16;
            
            int bgColor;
            if (module.isEnabled()) {
                int enabledColor = interpolateColor(0xFF1E4D2B, 0xFF2EAD4B, toggleAnimation);
                bgColor = hovered ? lighten(enabledColor, 0.1f) : enabledColor;
            } else {
                bgColor = hovered ? 0xFF353535 : 0xFF202020;
            }
            
            // Apply alpha
            int finalAlpha = (int)(alpha * 255) << 24;
            bgColor = (bgColor & 0x00FFFFFF) | finalAlpha;
            
            drawRect(x, y, x + width, y + 16, bgColor);
            
            // Module name - с обрезкой если не помещается
            String moduleName = module.getName();
            int textColor = module.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA;
            textColor = (textColor & 0x00FFFFFF) | finalAlpha;
            
            // Обрезаем текст если он не помещается
            int maxWidth = width - 8; // Отступ справа для кейбинда
            if (module.getKey() != Keyboard.KEY_NONE) {
                String keyName = Keyboard.getKeyName(module.getKey());
                int keyWidth = mc.fontRendererObj.getStringWidth(keyName);
                maxWidth = width - keyWidth - 12; // Больше отступ если есть кейбинд
            }
            
            String displayName = moduleName;
            int nameWidth = mc.fontRendererObj.getStringWidth(displayName);
            
            // Обрезаем текст если он слишком длинный
            if (nameWidth > maxWidth) {
                while (nameWidth > maxWidth - 10 && displayName.length() > 0) {
                    displayName = displayName.substring(0, displayName.length() - 1);
                    nameWidth = mc.fontRendererObj.getStringWidth(displayName + "...");
                }
                displayName = displayName + "...";
            }
            
            mc.fontRendererObj.drawString(displayName, x + 4, y + 4, textColor);
            
            // Keybind indicator
            if (module.getKey() != Keyboard.KEY_NONE) {
                String keyName = Keyboard.getKeyName(module.getKey());
                int keyWidth = mc.fontRendererObj.getStringWidth(keyName);
                mc.fontRendererObj.drawString(keyName, x + width - keyWidth - 4, y + 4, 
                    (0xFF666666 & 0x00FFFFFF) | finalAlpha);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MODULE SETTINGS PANEL
    // ═══════════════════════════════════════════════════════════════════════════

    class ModuleSettingsPanel {
        private Module module;
        private int x, y;
        private int width = 200;
        private int height = 200;
        private boolean waitingForKey = false;
        
        private Slider animationSpeedSlider;
        private int selectedAnimation = 0;
        private String[] animations = {"Slide", "Fade", "Scale", "Smooth", "Bounce"};
        
        public ModuleSettingsPanel(Module module, int x, int y) {
            this.module = module;
            
            // Center panel on click position
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            this.x = Math.min(Math.max(x - width / 2, 10), sr.getScaledWidth() - width - 10);
            this.y = Math.min(Math.max(y - 20, 10), sr.getScaledHeight() - height - 10);
            
            // Initialize slider
            animationSpeedSlider = new Slider("Animation Speed", 0.05f, 0.3f, 0.15f);
        }

        public void draw(int mouseX, int mouseY, float partialTicks) {
            Minecraft mc = Minecraft.getMinecraft();
            
            // Background
            drawRect(x, y, x + width, y + height, 0xEE1A1A1A);
            drawHollowRect(x, y, x + width, y + height, 0xFF000000);
            
            // Header
            drawRect(x, y, x + width, y + 20, 0xFF2A2A2A);
            String title = module.getName() + " Settings";
            int titleWidth = mc.fontRendererObj.getStringWidth(title);
            
            // Обрезаем заголовок если нужно
            String displayTitle = title;
            if (titleWidth > width - 30) {
                while (titleWidth > width - 40 && displayTitle.length() > 0) {
                    displayTitle = displayTitle.substring(0, displayTitle.length() - 1);
                    titleWidth = mc.fontRendererObj.getStringWidth(displayTitle + "...");
                }
                displayTitle = displayTitle + "...";
                titleWidth = mc.fontRendererObj.getStringWidth(displayTitle);
            }
            
            mc.fontRendererObj.drawStringWithShadow(displayTitle, 
                x + width / 2 - titleWidth / 2, 
                y + 6, 0xFFFFFFFF);
            
            // Close button
            boolean closeHovered = mouseX >= x + width - 18 && mouseX <= x + width - 3 &&
                                  mouseY >= y + 3 && mouseY <= y + 17;
            drawRect(x + width - 18, y + 3, x + width - 3, y + 17, 
                closeHovered ? 0xFFFF5555 : 0xFF883333);
            mc.fontRendererObj.drawString("×", x + width - 13, y + 6, 0xFFFFFFFF);
            
            int currentY = y + 30;
            
            // Keybind
            currentY = drawKeybindSection(mouseX, mouseY, currentY);
            
            // Animation type
            currentY = drawAnimationSection(mouseX, mouseY, currentY);
            
            // Animation speed slider
            currentY += 5;
            animationSpeedSlider.draw(x + 10, currentY, width - 20, mouseX, mouseY);
            currentY += 30;
        }

        private int drawKeybindSection(int mouseX, int mouseY, int startY) {
            Minecraft mc = Minecraft.getMinecraft();
            
            mc.fontRendererObj.drawString("Keybind:", x + 10, startY, 0xFFCCCCCC);
            
            int buttonY = startY + 12;
            boolean keyButtonHovered = mouseX >= x + 10 && mouseX <= x + width - 10 &&
                                      mouseY >= buttonY && mouseY <= buttonY + 20;
            
            int buttonColor = waitingForKey ? 0xFF4A4AFF : (keyButtonHovered ? 0xFF404040 : 0xFF303030);
            drawRect(x + 10, buttonY, x + width - 10, buttonY + 20, buttonColor);
            
            String keyText = waitingForKey ? "Press any key..." : 
                           (module.getKey() == Keyboard.KEY_NONE ? "None" : Keyboard.getKeyName(module.getKey()));
            int keyTextWidth = mc.fontRendererObj.getStringWidth(keyText);
            mc.fontRendererObj.drawStringWithShadow(keyText, 
                x + width / 2 - keyTextWidth / 2, 
                buttonY + 6, 0xFFFFFFFF);
            
            return buttonY + 25;
        }

        private int drawAnimationSection(int mouseX, int mouseY, int startY) {
            Minecraft mc = Minecraft.getMinecraft();
            
            mc.fontRendererObj.drawString("Animation Type:", x + 10, startY, 0xFFCCCCCC);
            
            int currentY = startY + 12;
            for (int i = 0; i < animations.length; i++) {
                boolean hovered = mouseX >= x + 10 && mouseX <= x + width - 10 &&
                                 mouseY >= currentY && mouseY <= currentY + 18;
                boolean selected = i == selectedAnimation;
                
                int bgColor = selected ? 0xFF2EAD4B : (hovered ? 0xFF353535 : 0xFF252525);
                drawRect(x + 10, currentY, x + width - 10, currentY + 18, bgColor);
                
                // Radio button
                drawCircle(x + 18, currentY + 9, 4, selected ? 0xFF2EAD4B : 0xFF666666);
                if (selected) {
                    drawCircle(x + 18, currentY + 9, 2, 0xFFFFFFFF);
                }
                
                mc.fontRendererObj.drawString(animations[i], x + 28, currentY + 5, 
                    selected ? 0xFFFFFFFF : 0xFFAAAAAA);
                
                currentY += 18;
            }
            
            return currentY + 5;
        }

        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            if (button == 0) {
                // Close button
                if (mouseX >= x + width - 18 && mouseX <= x + width - 3 &&
                    mouseY >= y + 3 && mouseY <= y + 17) {
                    closeSettings();
                    return true;
                }
                
                // Keybind button
                int keyButtonY = y + 42;
                if (mouseX >= x + 10 && mouseX <= x + width - 10 &&
                    mouseY >= keyButtonY && mouseY <= keyButtonY + 20) {
                    waitingForKey = !waitingForKey;
                    return true;
                }
                
                // Animation selection
                int animStartY = y + 79;
                for (int i = 0; i < animations.length; i++) {
                    int btnY = animStartY + (i * 18);
                    if (mouseX >= x + 10 && mouseX <= x + width - 10 &&
                        mouseY >= btnY && mouseY <= btnY + 18) {
                        selectedAnimation = i;
                        return true;
                    }
                }
                
                // Slider
                if (animationSpeedSlider.mouseClicked(mouseX, mouseY)) {
                    return true;
                }
                
                // Consume clicks inside panel
                if (mouseX >= x && mouseX <= x + width && 
                    mouseY >= y && mouseY <= y + height) {
                    return true;
                }
            }
            
            return false;
        }

        public void mouseReleased(int mouseX, int mouseY, int state) {
            animationSpeedSlider.mouseReleased();
        }

        public boolean isWaitingForKey() {
            return waitingForKey;
        }

        public void setKey(int keyCode) {
            if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                module.setKey(Keyboard.KEY_NONE);
            } else {
                module.setKey(keyCode);
            }
            waitingForKey = false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SLIDER COMPONENT
    // ═══════════════════════════════════════════════════════════════════════════

    class Slider {
        private String label;
        private float min, max, value;
        private boolean dragging = false;

        public Slider(String label, float min, float max, float defaultValue) {
            this.label = label;
            this.min = min;
            this.max = max;
            this.value = defaultValue;
        }

        public void draw(int x, int y, int width, int mouseX, int mouseY) {
            Minecraft mc = Minecraft.getMinecraft();
            
            // Label
            mc.fontRendererObj.drawString(label, x, y, 0xFFCCCCCC);
            
            // Value
            String valueText = String.format("%.2f", value);
            mc.fontRendererObj.drawString(valueText, x + width - mc.fontRendererObj.getStringWidth(valueText), y, 0xFF00FF00);
            
            // Slider track
            int trackY = y + 12;
            drawRect(x, trackY, x + width, trackY + 4, 0xFF1A1A1A);
            
            // Slider fill
            float percentage = (value - min) / (max - min);
            int fillWidth = (int)(width * percentage);
            drawRect(x, trackY, x + fillWidth, trackY + 4, 0xFF2EAD4B);
            
            // Slider handle
            int handleX = x + fillWidth - 3;
            boolean hovered = mouseX >= handleX && mouseX <= handleX + 6 &&
                            mouseY >= trackY - 2 && mouseY <= trackY + 6;
            drawRect(handleX, trackY - 2, handleX + 6, trackY + 6, 
                hovered || dragging ? 0xFFFFFFFF : 0xFFCCCCCC);
            
            // Update value while dragging
            if (dragging) {
                float newPercentage = Math.max(0, Math.min(1, (mouseX - x) / (float)width));
                value = min + (max - min) * newPercentage;
            }
        }

        public boolean mouseClicked(int mouseX, int mouseY) {
            dragging = true;
            return true;
        }

        public void mouseReleased() {
            dragging = false;
        }

        public float getValue() {
            return value;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private void drawHollowRect(int left, int top, int right, int bottom, int color) {
        drawRect(left, top, right, top + 1, color);
        drawRect(left, bottom - 1, right, bottom, color);
        drawRect(left, top, left + 1, bottom, color);
        drawRect(right - 1, top, right, bottom, color);
    }

    private void drawCircle(int x, int y, int radius, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_BLEND);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
        org.lwjgl.opengl.GL11.glBlendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);
        org.lwjgl.opengl.GL11.glColor4f(red, green, blue, alpha);
        org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN);
        
        for (int i = 0; i <= 360; i += 10) {
            double angle = Math.toRadians(i);
            org.lwjgl.opengl.GL11.glVertex2d(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius);
        }
        
        org.lwjgl.opengl.GL11.glEnd();
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_BLEND);
    }

    private int interpolateColor(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int r = (int)(r1 + (r2 - r1) * ratio);
        int g = (int)(g1 + (g2 - g1) * ratio);
        int b = (int)(b1 + (b2 - b1) * ratio);
        
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int lighten(int color, float amount) {
        int r = Math.min(255, (int)(((color >> 16) & 0xFF) * (1 + amount)));
        int g = Math.min(255, (int)(((color >> 8) & 0xFF) * (1 + amount)));
        int b = Math.min(255, (int)((color & 0xFF) * (1 + amount)));
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }
}