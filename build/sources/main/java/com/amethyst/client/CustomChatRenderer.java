package com.amethyst.client;

import com.amethyst.client.modules.CustomChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomChatRenderer {

    private final Minecraft mc = Minecraft.getMinecraft();
    private Field drawnChatLines = null;

    // Для fade-in анимации: храним время появления каждой строки
    // key = updatedCounter строки, value = System.currentTimeMillis() когда она была добавлена
    private final java.util.LinkedHashMap<Integer, Long> lineAppearTimes = new java.util.LinkedHashMap<>();

    public CustomChatRenderer() {
        // Рефлексия для доступа к drawnChatLines
        for (String fn : new String[]{"drawnChatLines", "field_146252_h"}) {
            try {
                drawnChatLines = GuiNewChat.class.getDeclaredField(fn);
                drawnChatLines.setAccessible(true);
                break;
            } catch (Exception ignored) {}
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CHAT) return;

        CustomChat mod = (CustomChat) AmethystClient.moduleManager.getModuleByName("CustomChat");
        if (mod == null || !mod.isEnabled()) return;

        // Отменяем стандартный чат
        event.setCanceled(true);

        // Рисуем наш чат только когда чат-GUI закрыт
        // (когда открыт — Minecraft рисует его сам через GuiChat)
        if (mc.currentScreen == null || !(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat)) {
            renderCustomChat(mod);
        }
    }

    @SuppressWarnings("unchecked")
    private void renderCustomChat(CustomChat mod) {
        if (drawnChatLines == null || mc.ingameGUI == null) return;

        GuiNewChat chatGui = mc.ingameGUI.getChatGUI();
        if (chatGui == null) return;

        List<ChatLine> lines;
        try {
            lines = (List<ChatLine>) drawnChatLines.get(chatGui);
        } catch (Exception e) {
            return;
        }
        if (lines == null || lines.isEmpty()) return;

        int x     = HUDConfig.getChatX();
        int y     = HUDConfig.getChatY();
        float scale = mod.getScale();
        int maxLines = mod.getMaxMessages();
        int lineH = (int)(10 * scale);
        int updateCounter = mc.ingameGUI.getUpdateCounter();

        // Берём последние maxLines строк
        int count = Math.min(lines.size(), maxLines);
        List<ChatLine> visible = new ArrayList<>(lines.subList(0, count));

        // Регистрируем время появления новых строк
        for (ChatLine cl : visible) {
            int key = cl.getUpdatedCounter();
            if (!lineAppearTimes.containsKey(key)) {
                lineAppearTimes.put(key, System.currentTimeMillis());
            }
        }
        // Чистим старые
        if (lineAppearTimes.size() > 200) {
            java.util.Iterator<Integer> it = lineAppearTimes.keySet().iterator();
            while (lineAppearTimes.size() > 100 && it.hasNext()) { it.next(); it.remove(); }
        }

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1f);

        int sx = (int)(x / scale);
        int sy = (int)(y / scale);

        // Ширина чата (примерно)
        int chatW = (int)(320 / scale);

        // Рисуем строки снизу вверх
        for (int i = 0; i < count; i++) {
            ChatLine cl = visible.get(i);
            int lineAge = updateCounter - cl.getUpdatedCounter();

            // Ванильный фейд: 200 тиков (~10 сек)
            float vanillaAlpha;
            if (lineAge > 200) continue; // слишком старое
            vanillaAlpha = lineAge < 100 ? 1.0f : 1.0f - (lineAge - 100) / 100.0f;
            vanillaAlpha = Math.max(0f, Math.min(1f, vanillaAlpha));

            // Плавное появление (fade-in за 200мс)
            float fadeIn = 1.0f;
            if (mod.isFadeMessages()) {
                Long appeared = lineAppearTimes.get(cl.getUpdatedCounter());
                if (appeared != null) {
                    long elapsed = System.currentTimeMillis() - appeared;
                    fadeIn = Math.min(1.0f, elapsed / 200.0f);
                    // Easing: smooth step
                    fadeIn = fadeIn * fadeIn * (3f - 2f * fadeIn);
                }
            }

            float alpha = vanillaAlpha * fadeIn;
            if (alpha <= 0.01f) continue;

            int rowY = sy - (i + 1) * 10;

            // Фон
            if (mod.isShowBackground()) {
                int bgA = (int)(mod.getBgAlpha() * alpha * 255);
                if (bgA > 0) drawRect(sx - 1, rowY - 1, sx + chatW, rowY + 9,
                        (bgA << 24) | 0x000000);
            }

            // Текст
            String text = cl.getChatComponent().getFormattedText();

            // Timestamp
            if (mod.isShowTimestamps()) {
                String ts = new SimpleDateFormat("HH:mm").format(new Date()) + " ";
                text = "§8" + ts + "§r" + text;
            }

            int textAlpha = (int)(alpha * 255);
            // drawStringWithShadow не поддерживает прозрачность напрямую —
            // используем GlStateManager.color для управления альфой
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1f, 1f, 1f, alpha);

            mc.fontRendererObj.drawStringWithShadow(text, sx, rowY,
                    (textAlpha << 24) | (mod.getTextColor() & 0x00FFFFFF));

            GlStateManager.color(1f, 1f, 1f, 1f);
        }

        GlStateManager.popMatrix();
    }

    private void drawRect(int x1, int y1, int x2, int y2, int color) {
        float a = (float)((color >> 24) & 0xFF) / 255f;
        float r = (float)((color >> 16) & 0xFF) / 255f;
        float g = (float)((color >>  8) & 0xFF) / 255f;
        float b = (float)( color        & 0xFF) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);

        net.minecraft.client.renderer.Tessellator ts = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer wr = ts.getWorldRenderer();
        wr.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
        wr.pos(x1, y2, 0).endVertex();
        wr.pos(x2, y2, 0).endVertex();
        wr.pos(x2, y1, 0).endVertex();
        wr.pos(x1, y1, 0).endVertex();
        ts.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}