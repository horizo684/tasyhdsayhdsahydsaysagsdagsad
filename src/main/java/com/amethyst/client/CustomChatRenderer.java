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

            int lineY = sy;
            int linesDrawn = 0;

            for (int i = 0; i < drawnLines.size() && linesDrawn < maxMessages; i++) {
                ChatLine line = drawnLines.get(i);
                if (line == null) continue;

                int lineAge = updateCounter - line.getUpdatedCounter();

                // ФИКС: Когда чат открыт — показываем ВСЕ сообщения с полной непрозрачностью
                double opacity;
                if (chatOpen) {
                    opacity = 1.0;
                } else {
                    // Когда чат закрыт — применяем fade-out после 200 тиков
                    if (lineAge >= 200) continue;
                    opacity = 1.0 - (double) lineAge / 200.0;
                }

                opacity = opacity * opacity; // smooth curve
                opacity *= chatOpacity; // user setting

                // Fade-in анимация (если включено)
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
                
                // Обрезаем текст если он шире чем chatWidth
                if (mc.fontRendererObj.getStringWidth(text) > chatWidthPx - 4) {
                    text = mc.fontRendererObj.trimStringToWidth(text, chatWidthPx - 4) + "...";
                }

                mc.fontRendererObj.drawStringWithShadow(text, sx + 2, lineY + 1, 
                    (alpha << 24) | (mod.getTextColor() & 0xFFFFFF));

                lineY += 9;
                linesDrawn++;
            }

            GlStateManager.popMatrix();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
}