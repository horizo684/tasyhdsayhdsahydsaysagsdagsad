package com.amethyst.client;

import com.amethyst.client.Module;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends GuiScreen {

    private List<CategoryPanel> categoryPanels = new ArrayList<>();
    private ModuleSettingsPanel settingsPanel = null;

    @Override
    public void initGui() {
        categoryPanels.clear();
        
        int startX = 10;
        int startY = 10;
        int offsetX = 0;

        for (Module.Category category : Module.Category.values()) {
            CategoryPanel panel = new CategoryPanel(category, startX + offsetX, startY);
            categoryPanels.add(panel);
            offsetX += 130;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        for (CategoryPanel panel : categoryPanels) {
            panel.draw(mouseX, mouseY);
        }

        if (settingsPanel != null) {
            settingsPanel.draw(mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (settingsPanel != null) {
            settingsPanel.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        for (CategoryPanel panel : categoryPanels) {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (settingsPanel != null) {
                settingsPanel = null;
                return;
            }
        }

        if (settingsPanel != null) {
            settingsPanel.keyTyped(typedChar, keyCode);
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

    private class CategoryPanel {
        private Module.Category category;
        private int x, y;
        private int width = 120;
        private int headerHeight = 20;
        private boolean expanded = true;
        private List<ModuleButton> moduleButtons = new ArrayList<>();

        public CategoryPanel(Module.Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;

            for (Module module : AmethystClient.moduleManager.getModulesInCategory(category)) {
                moduleButtons.add(new ModuleButton(module));
            }
        }

        public void draw(int mouseX, int mouseY) {
            // Draw header
            int headerColor = isMouseOver(mouseX, mouseY, x, y, width, headerHeight) ? 0xFF2A2A2A : 0xFF1A1A1A;
            drawRect(x, y, x + width, y + headerHeight, headerColor);
            
            String categoryName = category.name();
            fontRendererObj.drawString(categoryName, x + 5, y + 6, 0xFFFFFF);

            if (!expanded) return;

            // Draw modules
            int currentY = y + headerHeight;
            for (ModuleButton button : moduleButtons) {
                button.draw(x, currentY, width, mouseX, mouseY);
                currentY += 15;
            }
        }

        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            if (isMouseOver(mouseX, mouseY, x, y, width, headerHeight)) {
                if (mouseButton == 1) {
                    expanded = !expanded;
                }
                return;
            }

            if (!expanded) return;

            int currentY = y + headerHeight;
            for (ModuleButton button : moduleButtons) {
                if (isMouseOver(mouseX, mouseY, x, currentY, width, 15)) {
                    if (mouseButton == 0) {
                        button.module.toggle();
                    } else if (mouseButton == 1) {
                        openSettings(button.module, x + width + 5, currentY);
                    }
                    return;
                }
                currentY += 15;
            }
        }

        private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private class ModuleButton {
        private Module module;

        public ModuleButton(Module module) {
            this.module = module;
        }

        public void draw(int x, int y, int width, int mouseX, int mouseY) {
            int bgColor;
            if (module.isEnabled()) {
                bgColor = 0xFF00AA00;
            } else if (isMouseOver(mouseX, mouseY, x, y, width, 15)) {
                bgColor = 0xFF3A3A3A;
            } else {
                bgColor = 0xFF2A2A2A;
            }

            drawRect(x, y, x + width, y + 15, bgColor);
            
            fontRendererObj.drawString(module.getName(), x + 5, y + 4, 0xFFFFFF);

            if (module.getKey() != Keyboard.KEY_NONE) {
                String keyName = Keyboard.getKeyName(module.getKey());
                int keyWidth = fontRendererObj.getStringWidth(keyName);
                fontRendererObj.drawString("[" + keyName + "]", x + width - keyWidth - 5, y + 4, 0xAAAAAA);
            }
        }

        private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private class ModuleSettingsPanel {
        private Module module;
        private int x, y;
        private int width = 200;
        private int height = 150;
        private boolean listeningForKey = false;

        public ModuleSettingsPanel(Module module, int x, int y) {
            this.module = module;
            this.x = x;
            this.y = y;
        }

        public void draw(int mouseX, int mouseY) {
            drawRect(x, y, x + width, y + height, 0xEE000000);
            drawRect(x, y, x + width, y + 20, 0xFF1A1A1A);

            fontRendererObj.drawString(module.getName() + " Settings", x + 5, y + 6, 0xFFFFFF);

            int currentY = y + 30;

            // Enabled toggle
            String enabledText = "Enabled: " + (module.isEnabled() ? "ON" : "OFF");
            fontRendererObj.drawString(enabledText, x + 10, currentY, module.isEnabled() ? 0x00FF00 : 0xFF0000);
            currentY += 20;

            // Key bind
            String keyText = "Key: " + (listeningForKey ? "Press a key..." : 
                           (module.getKey() == Keyboard.KEY_NONE ? "None" : Keyboard.getKeyName(module.getKey())));
            fontRendererObj.drawString(keyText, x + 10, currentY, listeningForKey ? 0xFFFF00 : 0xFFFFFF);
            currentY += 20;

            fontRendererObj.drawString("Left click to toggle", x + 10, y + height - 40, 0xAAAAAA);
            fontRendererObj.drawString("Right click key to change", x + 10, y + height - 25, 0xAAAAAA);
            fontRendererObj.drawString("ESC to close", x + 10, y + height - 10, 0xAAAAAA);
        }

        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            if (!isMouseOver(mouseX, mouseY)) return;

            int currentY = y + 30;

            // Enabled toggle area
            if (mouseY >= currentY && mouseY <= currentY + 15) {
                if (mouseButton == 0) {
                    module.toggle();
                }
            }
            currentY += 20;

            // Key bind area
            if (mouseY >= currentY && mouseY <= currentY + 15) {
                if (mouseButton == 1) {
                    listeningForKey = true;
                }
            }
        }

        public void keyTyped(char typedChar, int keyCode) {
            if (listeningForKey) {
                if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                    module.setKey(Keyboard.KEY_NONE);
                } else {
                    module.setKey(keyCode);
                }
                listeningForKey = false;
            }
        }

        private boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }
}