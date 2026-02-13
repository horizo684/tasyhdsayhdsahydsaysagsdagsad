package com.amethyst.client;

import com.amethyst.client.modules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class EventHandler {
    private Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (KeyBindings.openGUI.isPressed()) {
            mc.displayGuiScreen(new ModernClickGUI());
        }

        if (KeyBindings.openHUDEditor.isPressed()) {
            mc.displayGuiScreen(new HUDEditorGUI());
        }

        for (Module module : AmethystClient.moduleManager.getModules()) {
            if (module instanceof Refill) {
                if (module.getKey() != Keyboard.KEY_NONE && Keyboard.isKeyDown(module.getKey())) {
                    ((Refill) module).triggerRefill();
                }
                continue;
            }

            if (module.getKey() != Keyboard.KEY_NONE && Keyboard.isKeyDown(module.getKey())) {
                module.toggle();
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event.phase != TickEvent.Phase.START) return;

        AutoSoup autoSoup = (AutoSoup) AmethystClient.moduleManager.getModuleByName("AutoSoup");
        if (autoSoup != null) autoSoup.onTick();

        Refill refill = (Refill) AmethystClient.moduleManager.getModuleByName("Refill");
        if (refill != null) refill.onTick();

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
        
        // Clean up old damage animation entries every second
        if (mc.theWorld.getTotalWorldTime() % 20 == 0) {
            DamageAnimationHandler.cleanup();
        }
    }
    
    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        if (event.entity instanceof EntityLivingBase) {
            DamageAnimationHandler.onEntityHurt((EntityLivingBase) event.entity);
        }
    }
}