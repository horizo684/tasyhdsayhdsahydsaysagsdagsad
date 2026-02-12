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
        // Иконки копирования теперь рисуются в CustomChatRenderer
        // Этот код больше не используется
    }
    
    @SubscribeEvent
    public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        
        if (org.lwjgl.input.Mouse.getEventButtonState() && org.lwjgl.input.Mouse.getEventButton() == 0) {
            try {
                int mouseX = org.lwjgl.input.Mouse.getEventX() * event.gui.width / mc.displayWidth;
                int mouseY = event.gui.height - org.lwjgl.input.Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;
                
                // Сначала проверяем включен ли CustomChat
                com.amethyst.client.modules.CustomChat customChat = 
                    (com.amethyst.client.modules.CustomChat) AmethystClient.moduleManager.getModuleByName("CustomChat");
                
                if (customChat != null && customChat.isEnabled()) {
                    // Используем обработчик кликов из CustomChatRenderer
                    if (AmethystClient.customChatRenderer != null) {
                        AmethystClient.customChatRenderer.handleChatClick(mouseX, mouseY);
                    }
                } else {
                    // Ванильный чат - используем старый код
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