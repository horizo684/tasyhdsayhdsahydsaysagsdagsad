package com.amethyst.client;

import com.amethyst.client.modules.ScoreboardModule;
import com.amethyst.client.Module;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VanillaScoreboardSuppressor {

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        Module moduleObj = AmethystClient.moduleManager.getModuleByName("Scoreboard");
        if (moduleObj == null || !(moduleObj instanceof ScoreboardModule)) return;
        ScoreboardModule mod = (ScoreboardModule) moduleObj;
        
        if (mod != null && mod.isEnabled()) {
            if (event.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
                event.setCanceled(true);
            }
        }
    }
}