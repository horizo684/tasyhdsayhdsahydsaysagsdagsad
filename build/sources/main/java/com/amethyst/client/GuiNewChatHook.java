package com.amethyst.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.List;

public class GuiNewChatHook {
    
    private Field drawnChatLinesField = null;
    
    public GuiNewChatHook() {
        try {
            drawnChatLinesField = GuiNewChat.class.getDeclaredField("drawnChatLines");
            drawnChatLinesField.setAccessible(true);
        } catch (Exception e) {
            try {
                drawnChatLinesField = GuiNewChat.class.getDeclaredField("field_146252_h");
                drawnChatLinesField.setAccessible(true);
            } catch (Exception ex) {
                System.err.println("Could not access drawnChatLines field!");
                ex.printStackTrace();
            }
        }
    }
    
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CHAT) {
            return;
        }
        
        if (drawnChatLinesField == null) {
            return;
        }
        
        Minecraft mc = Minecraft.getMinecraft();
        GuiNewChat chat = mc.ingameGUI.getChatGUI();
        FontRenderer fontRenderer = mc.fontRendererObj;
        
        try {
            int updateCounter = mc.ingameGUI.getUpdateCounter();
            
            @SuppressWarnings("unchecked")
            List<ChatLine> drawnChatLines = (List<ChatLine>) drawnChatLinesField.get(chat);
            
            if (drawnChatLines == null) {
                return;
            }
            
            for (int i = 0; i < drawnChatLines.size() && i < 100; ++i) {
                ChatLine chatline = drawnChatLines.get(i);
                
                if (chatline != null) {
                    int lineAge = updateCounter - chatline.getUpdatedCounter();
                    
                    if (lineAge < 200 || chat.getChatOpen()) {
                        double opacity = chat.getChatOpen() ? 1.0D : (double)(1.0F - (float)lineAge / 200.0F);
                        opacity = opacity * opacity;
                        int alpha = (int)(255.0D * opacity);
                        
                        if (alpha > 3 && chat.getChatOpen()) {
                            int yPos = 9 - i * 9;
                            int xPos = 2;
                            
                            String icon = "§a[§f+§a]";
                            
                            Gui.drawRect(xPos, yPos - 1, xPos + fontRenderer.getStringWidth(icon) + 2, yPos + 8, 
                                (alpha / 2 << 24) | 0x000000);
                            
                            fontRenderer.drawStringWithShadow(icon, xPos + 1, yPos, 0xFFFFFF | (alpha << 24));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @SubscribeEvent
    public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        
        if (org.lwjgl.input.Mouse.getEventButtonState() && org.lwjgl.input.Mouse.getEventButton() == 0) {
            try {
                int mouseX = org.lwjgl.input.Mouse.getEventX() * event.gui.width / mc.displayWidth;
                int mouseY = event.gui.height - org.lwjgl.input.Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;
                
                // Спочатку перевіряємо чи CustomChat увімкнений
                com.amethyst.client.modules.CustomChat customChat = 
                    (com.amethyst.client.modules.CustomChat) AmethystClient.moduleManager.getModuleByName("CustomChat");
                
                if (customChat != null && customChat.isEnabled()) {
                    // Використовуємо обробник кліків з CustomChatRenderer
                    if (AmethystClient.customChatRenderer != null) {
                        AmethystClient.customChatRenderer.handleChatClick(mouseX, mouseY);
                    }
                } else {
                    // Ванільний чат - використовуємо старий код
                    FontRenderer fontRenderer = mc.fontRendererObj;
                    int iconWidth = fontRenderer.getStringWidth("§a[§f+§a]") + 3;
                    
                    if (mouseX >= 2 && mouseX <= 2 + iconWidth) {
                        IChatComponent chatComponent = mc.ingameGUI.getChatGUI().getChatComponent(mouseX, mouseY);
                        
                        if (chatComponent != null) {
                            String fullMessage = chatComponent.getFormattedText();
                            fullMessage = fullMessage.replaceAll("§[0-9a-fk-or]", "");
                            ChatCopyButton.copyToClipboard(fullMessage);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}