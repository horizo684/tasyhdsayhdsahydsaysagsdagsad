package com.amethyst.client;

import com.amethyst.client.modules.Scoreboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Score;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScoreboardRenderer {

    private final Minecraft mc = Minecraft.getMinecraft();

    // ── Відключаємо ванільний scoreboard через Forge ──────────────────────────
    public static void updateScoreboardVisibility() {
        Scoreboard mod = (Scoreboard) AmethystClient.moduleManager.getModuleByName("Scoreboard");
        if (mod != null && mod.isEnabled()) {
            GuiIngameForge.renderObjective = false;
        } else {
            GuiIngameForge.renderObjective = true;
        }
    }

    // ── Draw custom scoreboard ────────────────────────────────────────────────
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderPost(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        // Оновлюємо видимість ванільного scoreboard
        updateScoreboardVisibility();

        Scoreboard mod = (Scoreboard) AmethystClient.moduleManager.getModuleByName("Scoreboard");
        if (mod == null || !mod.isEnabled()) return;

        net.minecraft.scoreboard.Scoreboard sb = mc.theWorld == null ? null : mc.theWorld.getScoreboard();
        if (sb == null) return;

        ScoreObjective objective = sb.getObjectiveInDisplaySlot(1);
        if (objective == null) return;

        ScaledResolution sr = new ScaledResolution(mc);

        int x = HUDConfig.getScoreboardX();
        if (x == -1) x = sr.getScaledWidth() - 115;
        int y = HUDConfig.getScoreboardY();

        float scale = mod.getScale();

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1f);

        int sx = (int)(x / scale);
        int sy = (int)(y / scale);

        Collection<Score> scores = objective.getScoreboard().getSortedScores(objective);
        List<Score> filteredScores = new ArrayList<>();
        for (Score score : scores) {
            if (score != null && score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) {
                filteredScores.add(score);
            }
        }

        if (filteredScores.size() > 15) {
            filteredScores = filteredScores.subList(0, 15);
        }

        String title = objective.getDisplayName();
        int titleWidth = mc.fontRendererObj.getStringWidth(title);
        int maxLineWidth = titleWidth;

        for (Score score : filteredScores) {
            ScorePlayerTeam team = objective.getScoreboard().getPlayersTeam(score.getPlayerName());
            String displayText = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
            int lineWidth = mc.fontRendererObj.getStringWidth(displayText);
            if (mod.isShowNumbers()) {
                lineWidth += mc.fontRendererObj.getStringWidth(" " + score.getScorePoints());
            }
            if (lineWidth > maxLineWidth) maxLineWidth = lineWidth;
        }

        int boxWidth = maxLineWidth + 6;
        int totalHeight = 10 + filteredScores.size() * 10 + 3;

        if (mod.isShowBackground()) {
            int bgAlpha = (int)(mod.getBgAlpha() * 255) << 24;
            drawRect(sx - 3, sy - 1, sx + boxWidth, sy + totalHeight, bgAlpha);
        }

        mc.fontRendererObj.drawStringWithShadow(title, sx + boxWidth / 2 - titleWidth / 2, sy, mod.getTitleColor());

        int currentY = sy + 11;
        // Реверсуємо список щоб текст відображався правильно (зверху вниз)
        List<Score> reversedScores = new ArrayList<>(filteredScores);
        java.util.Collections.reverse(reversedScores);
        
        for (Score score : reversedScores) {
            ScorePlayerTeam team = objective.getScoreboard().getPlayersTeam(score.getPlayerName());
            String displayText = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());

            mc.fontRendererObj.drawStringWithShadow(displayText, sx, currentY, mod.getTextColor());

            if (mod.isShowNumbers()) {
                String scoreStr = String.valueOf(score.getScorePoints());
                int scoreWidth = mc.fontRendererObj.getStringWidth(scoreStr);
                mc.fontRendererObj.drawStringWithShadow(scoreStr, sx + boxWidth - scoreWidth - 3, currentY, mod.getNumberColor());
            }

            currentY += 10;
        }

        GlStateManager.popMatrix();
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
}