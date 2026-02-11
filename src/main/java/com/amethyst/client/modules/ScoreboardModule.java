package com.amethyst.client.modules;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import com.amethyst.client.ScoreboardPickerGUI;

public class ScoreboardModule extends Module {

    public int x = -1, y = 5;

    public ScoreboardModule() {
        super("Scoreboard", "Custom scoreboard renderer", Keyboard.KEY_NONE, Category.RENDER);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_P)) {
            mc.displayGuiScreen(new ScoreboardPickerGUI(mc.currentScreen, this));
        }
    }

    @SubscribeEvent
    public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (this.isEnabled() && event.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderOverlayText(RenderGameOverlayEvent.Text event) {
        if (!this.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        net.minecraft.scoreboard.Scoreboard scoreboard = mc.theWorld.getScoreboard();
        net.minecraft.scoreboard.ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return;

        int x = this.x;
        int y = this.y;

        if (x == -1) {
            net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(mc);
            x = sr.getScaledWidth() - 115;
        }

        com.amethyst.client.ScoreboardRenderer.renderScoreboard(mc, scoreboard, objective, x, y, this);
    }
}