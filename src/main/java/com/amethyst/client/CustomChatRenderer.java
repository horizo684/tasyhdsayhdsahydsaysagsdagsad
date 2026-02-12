package com.amethyst.client;

import com.amethyst.client.modules.CustomChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomChatRenderer {

    private final Minecraft mc = Minecraft.getMinecraft();
    private Field drawnChatLinesField;
    private Map<String, Long> messageAppearTimes = new LinkedHashMap<>();
    private static final long FADE_IN_DURATION = 200;
    private int scrollOffset = 0; // Кількість рядків для скролу
    
    // Для обробки кліків на іконку копіювання
    private static class MessageBounds {
        int x, y, width, height;
        String message;
        MessageBounds(int x, int y, int width, int height, String message) {
            this.x = x; this.y = y; this.width = width; this.height = height; this.message = message;
        }
    }
    private List<MessageBounds> lastMessageBounds = new java.util.ArrayList<>();

    public CustomChatRenderer() {
        try {
            drawnChatLinesField = GuiNewChat.class.getDeclaredField("drawnChatLines");
            drawnChatLinesField.setAccessible(true);
        } catch (Exception e) {
            try {
                drawnChatLinesField = GuiNewChat.class.getDeclaredField("field_146252_h");
                drawnChatLinesField.setAccessible(true);
            } catch (Exception ex) {
                System.err.println("[CustomChat] Failed to access drawnChatLines field!");
                ex.printStackTrace();
            }
        }
    }

    // ── CANCEL vanilla chat ───────────────────────────────────────────────────
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderChatPre(RenderGameOverlayEvent.Pre event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CHAT) return;

        CustomChat mod = (CustomChat) AmethystClient.moduleManager.getModuleByName("CustomChat");
        if (mod != null && mod.isEnabled()) {
            event.setCanceled(true);
            
            // Обробка скролінгу чату
            handleChatScroll(mod);
        }
    }
    
    private void handleChatScroll(CustomChat mod) {
        GuiNewChat chat = mc.ingameGUI.getChatGUI();
        if (chat == null || !chat.getChatOpen()) {
            scrollOffset = 0; // Скидаємо скрол коли чат закритий
            return;
        }
        
        int scroll = org.lwjgl.input.Mouse.getEventDWheel();
        if (scroll != 0) {
            if (scroll > 0) {
                scrollOffset++; // Скрол вгору - показати старіші повідомлення
            } else {
                scrollOffset--; // Скрол вниз - показати новіші повідомлення
            }
            
            // Обмеження скролу
            if (scrollOffset < 0) scrollOffset = 0;
            
            try {
                if (drawnChatLinesField != null) {
                    @SuppressWarnings("unchecked")
                    List<ChatLine> drawnLines = (List<ChatLine>) drawnChatLinesField.get(chat);
                    int maxScroll = Math.max(0, drawnLines.size() - mod.getMaxMessages());
                    if (scrollOffset > maxScroll) scrollOffset = maxScroll;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ── Draw custom chat ──────────────────────────────────────────────────────
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderChatPost(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        CustomChat mod = (CustomChat) AmethystClient.moduleManager.getModuleByName("CustomChat");
        if (mod == null || !mod.isEnabled() || drawnChatLinesField == null) return;

        GuiNewChat chat = mc.ingameGUI.getChatGUI();
        if (chat == null) return;

        try {
            @SuppressWarnings("unchecked")
            List<ChatLine> drawnLines = (List<ChatLine>) drawnChatLinesField.get(chat);
            if (drawnLines == null || drawnLines.isEmpty()) return;

            ScaledResolution sr = new ScaledResolution(mc);
            int x = HUDConfig.getChatX();
            int y = HUDConfig.getChatY();
            if (y == -1) y = sr.getScaledHeight() - 60;

            // ВАЖНО: chat.getChatOpen() показывает открыт ли чат для ввода
            boolean chatOpen = chat.getChatOpen();
            int updateCounter = mc.ingameGUI.getUpdateCounter();
            int maxMessages = mod.getMaxMessages();
            float chatWidth = mod.getChatWidth();
            float chatScale = mod.getChatScale();
            float chatOpacity = mod.getChatOpacity();

            // Ширина чата в пикселях (как в ванилле: 320 * width_multiplier)
            int chatWidthPx = (int)(320 * chatWidth);

            GlStateManager.pushMatrix();
            GlStateManager.scale(chatScale, chatScale, 1f);

            int sx = (int)(x / chatScale);
            int sy = (int)(y / chatScale);

            // Рахуємо скільки видимих рядків у нас є з урахуванням скролу
            List<ChatLine> visibleLines = new java.util.ArrayList<>();
            int startIndex = chatOpen ? scrollOffset : 0; // Скрол працює тільки коли чат відкритий
            
            for (int i = startIndex; i < drawnLines.size() && visibleLines.size() < maxMessages; i++) {
                ChatLine line = drawnLines.get(i);
                if (line == null) continue;
                
                int lineAge = updateCounter - line.getUpdatedCounter();
                if (!chatOpen && lineAge >= 400) continue;
                
                visibleLines.add(line);
            }

            // Малюємо знизу вгору, починаючи з найстаршого повідомлення
            // (реверсуємо список щоб найстаріші були вгорі, найновіші внизу)
            int lineY = sy;
            lastMessageBounds.clear(); // Очищаємо попередні границі
            
            // Ітеруємо у зворотньому порядку (від кінця до початку)
            for (int i = visibleLines.size() - 1; i >= 0; i--) {
                ChatLine line = visibleLines.get(i);
                if (line == null) continue;

                int lineAge = updateCounter - line.getUpdatedCounter();

                // Коли чат відкритий — показуємо всі повідомлення з повною непрозорістю
                double opacity;
                if (chatOpen) {
                    opacity = 1.0;
                } else {
                    // Коли чат закритий — застосовуємо fade-out після 400 тиків (20 секунд)
                    if (lineAge >= 400) continue;
                    opacity = 1.0 - (double) lineAge / 400.0;
                }

                opacity = opacity * opacity; // smooth curve
                opacity *= chatOpacity; // user setting

                // Fade-in анимація (якщо увімкнено)
                if (mod.isFadeMessages() && !chatOpen) {
                    String key = line.getChatComponent().getFormattedText();
                    if (!messageAppearTimes.containsKey(key)) {
                        messageAppearTimes.put(key, System.currentTimeMillis());
                    }
                    long elapsed = System.currentTimeMillis() - messageAppearTimes.get(key);
                    if (elapsed < FADE_IN_DURATION) {
                        float t = (float) elapsed / FADE_IN_DURATION;
                        float fadeIn = t * t * (3f - 2f * t); // smoothstep
                        opacity *= fadeIn;
                    }
                }

                int alpha = (int) (opacity * 255);
                if (alpha < 3) continue;

                // Background
                if (mod.isShowBackground()) {
                    int bgAlpha = (int) (mod.getBgAlpha() * alpha);
                    drawRect(sx, lineY, sx + chatWidthPx, lineY + 9, (bgAlpha << 24));
                }

                // Text
                String text = line.getChatComponent().getFormattedText();
                
                // Перенос довгих повідомлень на нові рядки
                int maxWidth = chatWidthPx - 4;
                List<String> lines = wrapText(mc.fontRendererObj, text, maxWidth);
                
                int lineStartY = lineY; // Зберігаємо початкову Y для іконки копіювання
                
                for (String wrappedLine : lines) {
                    mc.fontRendererObj.drawStringWithShadow(wrappedLine, sx + 2, lineY + 1, 
                        (alpha << 24) | (mod.getTextColor() & 0xFFFFFF));
                    lineY += 9;
                }
                
                // Малюємо іконку копіювання якщо чат відкритий і модуль CopyChat увімкнений
                if (chatOpen) {
                    Module copyChat = AmethystClient.moduleManager.getModuleByName("CopyChat");
                    if (copyChat != null && copyChat.isEnabled()) {
                        String copyIcon = "§a[§f+§a]";
                        int iconWidth = mc.fontRendererObj.getStringWidth(copyIcon);
                        int iconX = sx + chatWidthPx - iconWidth - 2;
                        mc.fontRendererObj.drawStringWithShadow(copyIcon, iconX, lineStartY + 1, 
                            0xFFFFFFFF | (alpha << 24));
                        
                        // Зберігаємо позицію для обробки кліків (з урахуванням масштабу)
                        int realX = (int)(iconX * chatScale);
                        int realY = (int)(lineStartY * chatScale);
                        int realWidth = (int)(iconWidth * chatScale);
                        int realHeight = 9;
                        lastMessageBounds.add(new MessageBounds(realX, realY, realWidth, realHeight, text));
                    }
                }
            }

            GlStateManager.popMatrix();
            
            // Показуємо індикатор скролу якщо чат проскролений
            if (chatOpen && scrollOffset > 0) {
                String scrollIndicator = "▲ +" + scrollOffset + " Старых сообщений";
                int indicatorWidth = mc.fontRendererObj.getStringWidth(scrollIndicator);
                mc.fontRendererObj.drawStringWithShadow(scrollIndicator, 
                    x + (chatWidthPx - indicatorWidth) / 2, 
                    y - 12, 
                    0xFF88AAFF);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }

    // Метод для розбиття довгого тексту на кілька рядків
    private List<String> wrapText(net.minecraft.client.gui.FontRenderer fr, String text, int maxWidth) {
        List<String> result = new java.util.ArrayList<>();
        
        if (fr.getStringWidth(text) <= maxWidth) {
            result.add(text);
            return result;
        }
        
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            
            if (fr.getStringWidth(testLine) <= maxWidth) {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    result.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Слово само по собі задовге - обрізаємо його
                    result.add(fr.trimStringToWidth(word, maxWidth));
                }
            }
        }
        
        if (currentLine.length() > 0) {
            result.add(currentLine.toString());
        }
        
        return result;
    }
    
    // Метод для обробки кліків по іконці копіювання
    public void handleChatClick(int mouseX, int mouseY) {
        CustomChat mod = (CustomChat) AmethystClient.moduleManager.getModuleByName("CustomChat");
        if (mod == null || !mod.isEnabled()) return;
        
        Module copyChat = AmethystClient.moduleManager.getModuleByName("CopyChat");
        if (copyChat == null || !copyChat.isEnabled()) return;
        
        GuiNewChat chat = mc.ingameGUI.getChatGUI();
        if (chat == null || !chat.getChatOpen()) return;
        
        // Перевіряємо чи клік був на іконці копіювання
        for (MessageBounds bounds : lastMessageBounds) {
            if (mouseX >= bounds.x && mouseX <= bounds.x + bounds.width &&
                mouseY >= bounds.y && mouseY <= bounds.y + bounds.height) {
                // Копіюємо повідомлення
                String cleanMessage = bounds.message.replaceAll("§[0-9a-fk-or]", "");
                ChatCopyButton.copyToClipboard(cleanMessage);
                return;
            }
        }
    }
}