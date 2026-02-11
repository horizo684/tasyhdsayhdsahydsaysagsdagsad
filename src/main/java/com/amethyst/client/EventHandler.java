package com.amethyst.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class EventHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        
        if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            if (Keyboard.isKeyDown(Keyboard.KEY_H)) {
                mc.displayGuiScreen(new HUDEditorGUI());
            }
            
            if (Keyboard.isKeyDown(Keyboard.KEY_M)) {
                mc.displayGuiScreen(new ClickGUI());
            }
        }
    }
}