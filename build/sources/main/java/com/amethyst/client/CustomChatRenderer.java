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
    private float smoothScrollOffset = 0.0f; // Плавний скрол
    private int targetScrollOffset = 0; // Целевая позиция скрола
    private static final float SCROLL_SPEED = 0.15f; // Скорость плавного скрола (уменьшена для более плавного движения)
    
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
        
        // Если чат закрыт, сбрасываем только целевые значения, но не прерываем анимацию
        if (chat == null || !chat.getChatOpen()) {
            targetScrollOffset = 0;
            // НЕ сбрасываем smoothScrollOffset здесь - пусть плавно анимируется к 0
            return;
        }
        
        int scroll = org.lwjgl.input.Mouse.getEventDWheel();
        if (scroll != 0) {
            // Очень плавный скроллинг - всего 1 строка за раз
            float scrollAmount = (scroll > 0 ? 1.0f : -1.0f);
            targetScrollOffset += scrollAmount;
            
            // Ограничения скрола
            if (targetScrollOffset < 0) targetScrollOffset = 0;
            
            try {
                if (drawnChatLinesField != null) {
                    @SuppressWarnings("unchecked")
                    List<ChatLine> drawnLines = (List<ChatLine>) drawnChatLinesField.get(chat);
                    int maxScroll = Math.max(0, drawnLines.size() - mod.getMaxMessages());
                    if (targetScrollOffset > maxScroll) targetScrollOffset = maxScroll;
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
            
            // Периодическая очистка кеша старых сообщений (каждые 100 тиков)
            if (!chatOpen && updateCounter % 100 == 0) {
                messageAppearTimes.entrySet().removeIf(entry -> {
                    long age = System.currentTimeMillis() - entry.getValue();
                    return age > 15000; // Удаляем записи старше 15 секунд
                });
            }
            
            // Постоянно обновляем плавную анимацию скроллинга
            if (Math.abs(smoothScrollOffset - targetScrollOffset) > 0.01f) {
                float diff = targetScrollOffset - smoothScrollOffset;
                // Очень медленное и плавное сглаживание
                smoothScrollOffset += diff * 0.15f;
            } else {
                smoothScrollOffset = targetScrollOffset;
            }
            scrollOffset = Math.round(smoothScrollOffset);

            // Ширина чата в пикселях (как в ванилле: 320 * width_multiplier)
            int chatWidthPx = (int)(320 * chatWidth);

            GlStateManager.pushMatrix();
            GlStateManager.scale(chatScale, chatScale, 1f);

            int sx = (int)(x / chatScale);
            int sy = (int)(y / chatScale);

            // Рахуємо скільки видимих рядків у нас є з урахуванням скролу
            List<ChatLine> visibleLines = new java.util.ArrayList<>();
            // Используем smoothScrollOffset напрямую для максимальной плавности
            int startIndex = chatOpen ? (int)smoothScrollOffset : 0;
            
            // Собираем видимые сообщения
            for (int i = startIndex; i < drawnLines.size() && visibleLines.size() < maxMessages; i++) {
                ChatLine line = drawnLines.get(i);
                if (line == null) continue;
                
                // Показываем все сообщения без ограничения по времени (как в ванильном Minecraft)
                // Затухание будет применяться ниже в логике рендеринга
                visibleLines.add(line);
            }

            // Малюємо знизу вгору
            // У drawnLines індекс 0 = НАЙНОВІШЕ повідомлення
            // Малюємо у ЗВОРОТНОМУ порядку щоб найновіше було ЗНИЗУ
            int totalHeight = 0;
            for (ChatLine line : visibleLines) {
                if (line == null) continue;
                String text = line.getChatComponent().getFormattedText();
                int maxWidth = chatWidthPx - 4;
                List<String> lines = wrapText(mc.fontRendererObj, text, maxWidth);
                totalHeight += lines.size() * 9;
            }
            
            // КРИТИЧНО: Завжди починаємо знизу чату (не від totalHeight!)
            // Це забезпечує що повідомлення ЗАВЖДИ ростуть знизу вверх
            int maxChatHeight = maxMessages * 9;
            
            // Добавляем плавное смещение при скроллинге
            float scrollFraction = smoothScrollOffset - (int)smoothScrollOffset;
            int pixelOffset = (int)(scrollFraction * 9); // 9 пикселей на строку
            
            int lineY = sy + maxChatHeight - 9 + pixelOffset; // Начинаем с низа чата + плавное смещение
            lastMessageBounds.clear();
            
            // Ітеруємо у ЗВОРОТНОМУ порядку (від останнього до першого)
            // щоб найновіше повідомлення (індекс 0) було знизу
            for (int i = visibleLines.size() - 1; i >= 0; i--) {
                ChatLine line = visibleLines.get(i);
                if (line == null) continue;

                int lineAge = updateCounter - line.getUpdatedCounter();

                // Коли чат відкритий — показуємо всі повідомлення з повною непрозорістю
                double opacity;
                if (chatOpen) {
                    opacity = 1.0;
                } else {
                    // Як у ванільному Minecraft: 8 секунд (160 тіків) повна видимість, потім затухання
                    if (lineAge < 160) {
                        // Перші 8 секунд - повна видимість
                        opacity = 1.0;
                    } else if (lineAge < 200) {
                        // Наступні 2 секунди (40 тіків) - плавне затухання
                        float fadeProgress = (float)(lineAge - 160) / 40.0f;
                        opacity = 1.0 - fadeProgress;
                    } else {
                        // Після 10 секунд - невидимо
                        String key = line.getChatComponent().getFormattedText();
                        messageAppearTimes.remove(key);
                        continue;
                    }
                }

                // НЕ застосовуємо подвійний smooth curve - це робило затухання занадто м'яким
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
                if (alpha < 3) {
                    // Якщо повідомлення майже невидиме, все одно рахуємо його висоту
                    String text = line.getChatComponent().getFormattedText();
                    int maxWidth = chatWidthPx - 4;
                    List<String> lines = wrapText(mc.fontRendererObj, text, maxWidth);
                    lineY -= lines.size() * 9;
                    continue;
                }

                // Text
                String text = line.getChatComponent().getFormattedText();
                
                // Перенос довгих повідомлень на нові рядки
                int maxWidth = chatWidthPx - 4;
                List<String> lines = wrapText(mc.fontRendererObj, text, maxWidth);
                
                // Рисуем каждую строку перенесенного сообщения
                for (int j = lines.size() - 1; j >= 0; j--) {
                    String wrappedLine = lines.get(j);
                    
                    // Background
                    if (mod.isShowBackground()) {
                        int bgAlpha = (int) (mod.getBgAlpha() * alpha);
                        drawRect(sx, lineY, sx + chatWidthPx, lineY + 9, (bgAlpha << 24));
                    }
                    
                    // Получаем цвет текста из настроек и применяем наш alpha для затухания
                    int textColor = mod.getTextColor() & 0x00FFFFFF;
                    int finalColor = (alpha << 24) | textColor;
                    mc.fontRendererObj.drawStringWithShadow(wrappedLine, sx + 2, lineY + 1, finalColor);
                    
                    // Малюємо іконку копіювання
                    if (chatOpen) {
                        Module copyChat = AmethystClient.moduleManager.getModuleByName("CopyChat");
                        if (copyChat != null && copyChat.isEnabled()) {
                            String copyIcon = "§a[§f+§a]";
                            int iconWidth = mc.fontRendererObj.getStringWidth(copyIcon);
                            int iconX = sx + chatWidthPx - iconWidth - 2;
                            int iconColor = (alpha << 24) | 0x00FFFFFF;
                            mc.fontRendererObj.drawStringWithShadow(copyIcon, iconX, lineY + 1, iconColor);
                            
                            // Зберігаємо позицію для обробки кліків (з урахуванням масштабу)
                            int realX = (int)(iconX * chatScale);
                            int realY = (int)(lineY * chatScale);
                            int realWidth = (int)(iconWidth * chatScale);
                            int realHeight = (int)(9 * chatScale);
                            lastMessageBounds.add(new MessageBounds(realX, realY, realWidth, realHeight, text));
                        }
                    }
                    
                    lineY -= 9; // Двигаемся вверх для следующей строки
                }
            }

            GlStateManager.popMatrix();
            
            // Показуємо індикатор скролу якщо чат проскролений
            if (chatOpen && scrollOffset > 0) {
                String scrollIndicator = "▲ +" + scrollOffset + " старих повідомлень";
                int indicatorWidth = mc.fontRendererObj.getStringWidth(scrollIndicator);
                // Позиционируем индикатор над чатом
                int indicatorY = y - 12;
                mc.fontRendererObj.drawStringWithShadow(scrollIndicator, 
                    x + (int)((chatWidthPx * chatScale - indicatorWidth) / 2), 
                    indicatorY, 
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