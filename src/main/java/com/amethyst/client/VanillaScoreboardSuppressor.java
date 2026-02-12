package com.amethyst.client;

import com.amethyst.client.modules.Scoreboard;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VanillaScoreboardSuppressor {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderScoreboard(RenderGameOverlayEvent.Pre event) {
        // Cancel vanilla SCOREBOARD render element when our module is active
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        // We can't cancel "SCOREBOARD" type directly in 1.8.9 Forge because
        // it's rendered inside GuiIngame.renderGameOverlay as part of TEXT.
        // The ScoreboardRenderer uses EventPriority.HIGH on TEXT/Post, so
        // it overwrites/replaces naturally. Nothing extra needed here.
    }

    /**
     * Called from ScoreboardRenderer — we cancel the vanilla scoreboard
     * drawing by temporarily setting sidebar objective to null via a flag.
     * In 1.8.9 the scoreboard sidebar is drawn inside renderScoreboard()
     * which is called from GuiIngame, so we hook the TEXT Pre event here
     * and store a cancel flag that ScoreboardRenderer checks.
     */

    // In practice for 1.8.9 the cleanest approach is to rely on
    // ScoreboardRenderer drawing over the vanilla one (same position).
    // If needed, a Mixin or ASM would be required for true suppression.
    // This class is a placeholder that can be extended.
}