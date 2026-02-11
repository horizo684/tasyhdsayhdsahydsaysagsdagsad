package com.amethyst.client.modules;

import com.amethyst.client.HUDConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class ScoreboardModule extends Module {

    private boolean showNumbers = true;

    public ScoreboardModule() {
        super("Scoreboard", "Custom scoreboard renderer", Keyboard.KEY_NONE, Category.RENDER);
    }

    @Override
    public void onEnable() {
        // Регистрируем обработчик событий
    }

    @Override
    public void onDisable() {
        // Отменяем регистрацию
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        // Отключаем ванильный scoreboard когда наш модуль включен
        if (this.isEnabled() && event.getType() == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderScoreboard(RenderGameOverlayEvent.Text event) {
        if (!this.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        ScoreObjective objective = null;

        if (scoreboard != null) {
            objective = scoreboard.getObjectiveInDisplaySlot(1);
        }

        if (objective != null) {
            // Кастомный рендеринг scoreboard
            int x = HUDConfig.getScoreboardX();
            int y = HUDConfig.getScoreboardY();
            
            if (x == -1) x = event.getResolution().getScaledWidth() - 115;
            if (y == -1) y = 10;

            // Здесь ваша кастомная логика отрисовки
            renderCustomScoreboard(objective, x, y);
        }
    }

    private void renderCustomScoreboard(ScoreObjective objective, int x, int y) {
        // Ваша кастомная отрисовка scoreboard
        // Можете использовать свой собственный код
    }

    public boolean isShowNumbers() {
        return showNumbers;
    }

    public void setShowNumbers(boolean showNumbers) {
        this.showNumbers = showNumbers;
    }
}