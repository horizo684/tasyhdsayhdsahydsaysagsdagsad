package com.amethyst.client;

import com.amethyst.client.modules.ScoreboardModule;
import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreboardRenderer {

    public static void renderScoreboard(Minecraft mc,
                                       net.minecraft.scoreboard.Scoreboard scoreboard,
                                       net.minecraft.scoreboard.ScoreObjective objective,
                                       int x, int y) {
        Module moduleObj = AmethystClient.moduleManager.getModuleByName("Scoreboard");
        if (moduleObj == null || !(moduleObj instanceof ScoreboardModule)) return;
        ScoreboardModule mod = (ScoreboardModule) moduleObj;
        renderScoreboard(mc, scoreboard, objective, x, y, mod);
    }

    public static void renderScoreboard(Minecraft mc,
                                       net.minecraft.scoreboard.Scoreboard scoreboard,
                                       net.minecraft.scoreboard.ScoreObjective objective,
                                       int x, int y,
                                       ScoreboardModule mod) {
        if (objective == null) return;

        FontRenderer fontRenderer = mc.fontRendererObj;

        Collection<Score> scores = scoreboard.getSortedScores(objective);
        List<Score> filteredScores = scores.stream()
                .filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#"))
                .collect(Collectors.toList());

        if (filteredScores.size() > 15) {
            filteredScores = new ArrayList<>(filteredScores).subList(0, 15);
        }

        int maxWidth = fontRenderer.getStringWidth(objective.getDisplayName());
        for (Score score : filteredScores) {
            Team team = scoreboard.getPlayersTeam(score.getPlayerName());
            String displayText = formatPlayerName(team, score.getPlayerName());
            int width = fontRenderer.getStringWidth(displayText);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        int scoreboardHeight = filteredScores.size() * fontRenderer.FONT_HEIGHT;
        int totalHeight = scoreboardHeight + fontRenderer.FONT_HEIGHT + 3;

        int posY = y + totalHeight;

        Gui.drawRect(x - 2, y - 1, x + maxWidth + 2, y + totalHeight, 0x50000000);
        Gui.drawRect(x - 2, y - 1, x + maxWidth + 2, y + fontRenderer.FONT_HEIGHT, 0x60000000);

        String title = objective.getDisplayName();
        int titleWidth = fontRenderer.getStringWidth(title);
        fontRenderer.drawString(title, x + (maxWidth - titleWidth) / 2, y, 0xFFFFFF);

        int lineY = y + fontRenderer.FONT_HEIGHT + 2;

        int index = 0;
        for (Score score : filteredScores) {
            index++;
            Team team = scoreboard.getPlayersTeam(score.getPlayerName());
            String displayText = formatPlayerName(team, score.getPlayerName());
            String scoreText = EnumChatFormatting.RED + "" + score.getScorePoints();

            fontRenderer.drawString(displayText, x, lineY, 0xFFFFFF);
            fontRenderer.drawString(scoreText, x + maxWidth - fontRenderer.getStringWidth(scoreText), lineY, 0xFFFFFF);

            lineY += fontRenderer.FONT_HEIGHT;
        }
    }

    private static String formatPlayerName(Team team, String playerName) {
        if (team == null) {
            return playerName;
        }
        return team.getColorPrefix() + playerName + team.getColorSuffix();
    }
}