package com.amethyst.client;

import com.amethyst.client.modules.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class EventHandler {
    private Minecraft mc = Minecraft.getMinecraft();
    private int discordUpdateTicks = 0;
    private static final int DISCORD_UPDATE_INTERVAL = 100; // Обновлять каждые 5 секунд (100 тиков)
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        
        if (KeyBindings.openGUI.isPressed()) {
            mc.displayGuiScreen(new ModernClickGUI());
        }
        
        if (KeyBindings.openHUDEditor.isPressed()) {
            mc.displayGuiScreen(new HUDEditorGUI());
        }
        
        for (Module module : AmethystClient.moduleManager.getModules()) {
            if (module instanceof Refill) {
                if (module.getKeyCode() != Keyboard.KEY_NONE && Keyboard.isKeyDown(module.getKeyCode())) {
                    ((Refill) module).triggerRefill();
                }
                continue;
            }
            
            if (module.getKeyCode() != Keyboard.KEY_NONE && Keyboard.isKeyDown(module.getKeyCode())) {
                module.toggle();
            }
        }
    }
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        AutoSoup autoSoup = (AutoSoup) AmethystClient.moduleManager.getModuleByName("AutoSoup");
        if (autoSoup != null) {
            autoSoup.onTick();
        }
        
        Refill refill = (Refill) AmethystClient.moduleManager.getModuleByName("Refill");
        if (refill != null) {
            refill.onTick();
        }
        
        FullBright fullBright = (FullBright) AmethystClient.moduleManager.getModuleByName("FullBright");
        if (fullBright != null) {
            if (fullBright.isEnabled()) {
                mc.gameSettings.gammaSetting = 100.0F;
            } else {
                if (mc.gameSettings.gammaSetting > 1.0F) {
                    mc.gameSettings.gammaSetting = 1.0F;
                }
            }
        }
        
        // Обновление Discord RPC каждые 5 секунд
        DiscordRPC discordRPC = (DiscordRPC) AmethystClient.moduleManager.getModuleByName("Discord RPC");
        if (discordRPC != null && discordRPC.isEnabled() && discordRPC.isConnected()) {
            discordUpdateTicks++;
            if (discordUpdateTicks >= DISCORD_UPDATE_INTERVAL) {
                discordRPC.updatePresence();
                discordUpdateTicks = 0;
            }
        }
    }
}