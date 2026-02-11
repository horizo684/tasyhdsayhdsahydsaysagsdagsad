package com.amethyst.client;

import com.amethyst.client.modules.Scoreboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Score;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScoreboardRenderer {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        Scoreboard mod = (Scoreboard) AmethystClient.moduleManager.getModuleByName("Scoreboard");
        if (mod == null || !mod.isEnabled()) return;

        net.minecraft.scoreboard.Scoreboard sb = mc.theWorld == null ? null : mc.theWorld.getScoreboard();
        if (sb == null) return;

        // Берём sidebar objective — то что обычно показывает сервер справа
        ScoreObjective objective = sb.getObjectiveInDisplaySlot(1);
        if (objective == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth();
        int H = sr.getScaledHeight();

        int x = HUDConfig.getScoreboardX();
        int y = HUDConfig.getScoreboardY();
        float scale = mod.getScale();

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1f);

        int sx = (int)(x / scale);
        int sy = (int)(y / scale);

        renderScoreboard(sb, objective, sx, sy, mod);

        GlStateManager.popMatrix();
    }

    private void renderScoreboard(net.minecraft.scoreboard.Scoreboard sb,
                                   ScoreObjective objective,
                                   int x, int y,
                                   Scoreboard mod) {
        // Собираем строки (max 15 как у ваниллы)
        Collection<Score> scores = sb.getSortedScores(objective);
        List<Score> list = new ArrayList<>();
        for (Score s : scores) {
            if (s.getPlayerName() != null && !s.getPlayerName().startsWith("#")) {
                list.add(s);
            }
        }
        if (list.size() > 15) list = list.subList(list.size() - 15, list.size());

        String title = objective.getDisplayName();

        // Вычисляем ширину панели
        int maxW = mc.fontRendererObj.getStringWidth(title);
        for (Score s : list) {
            String name = formatName(sb, s);
            String num  = String.valueOf(s.getScorePoints());
            int lineW = mc.fontRendererObj.getStringWidth(name)
                      + (mod.isShowNumbers() ? mc.fontRendererObj.getStringWidth(" " + num) : 0)
                      + 6;
            if (lineW > maxW) maxW = lineW;
        }
        maxW += 8;

        int lineH  = 10;
        int rows   = list.size();
        int totalH = (rows + 1) * lineH + 4; // +1 заголовок, +4 padding

        // ── Фон ───────────────────────────────────────────────────────────────
        if (mod.isShowBackground()) {
            int alpha = (int)(mod.getBgAlpha() * 255);

            // Тёмный фон строк
            int bg = (alpha << 24) | 0x000000;
            drawRect(x, y + lineH, x + maxW, y + totalH, bg);

            // Заголовок чуть темнее
            int titleBg = (Math.min(255, alpha + 40) << 24) | 0x000000;
            drawRect(x, y, x + maxW, y + lineH + 2, titleBg);
        }

        // ── Заголовок ──────────────────────────────────────────────────────────
        int titleX = x + maxW / 2 - mc.fontRendererObj.getStringWidth(title) / 2;
        mc.fontRendererObj.drawStringWithShadow(title, titleX, y + 2, mod.getTitleColor());

        // ── Строки (снизу вверх — как у ваниллы) ──────────────────────────────
        for (int i = 0; i < rows; i++) {
            Score s = list.get(rows - 1 - i);
            String name = formatName(sb, s);
            int rowY = y + lineH * (i + 1) + 4;

            mc.fontRendererObj.drawStringWithShadow(name, x + 3, rowY, mod.getTextColor());

            if (mod.isShowNumbers()) {
                String num = String.valueOf(s.getScorePoints());
                int numX = x + maxW - mc.fontRendererObj.getStringWidth(num) - 3;
                mc.fontRendererObj.drawStringWithShadow(num, numX, rowY, mod.getNumberColor());
            }
        }
    }

    /** Форматирует имя игрока с учётом командных цветов */
    private String formatName(net.minecraft.scoreboard.Scoreboard sb, Score s) {
        ScorePlayerTeam team = sb.getPlayersTeam(s.getPlayerName());
        if (team != null) {
            return ScorePlayerTeam.formatPlayerName(team, s.getPlayerName());
        }
        return s.getPlayerName();
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