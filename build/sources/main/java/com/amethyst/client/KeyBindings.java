package com.amethyst.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindings {
    
    public static KeyBinding openGUI;
    public static KeyBinding openHUDEditor;
    public static KeyBinding perspective360;
    
    public static void register() {
        openGUI = new KeyBinding("Open GUI", Keyboard.KEY_RSHIFT, "AmethystClient");
        openHUDEditor = new KeyBinding("Edit HUD", Keyboard.KEY_RCONTROL, "AmethystClient");
        perspective360 = new KeyBinding("360 Perspective", Keyboard.KEY_LMENU, "AmethystClient");
        
        ClientRegistry.registerKeyBinding(openGUI);
        ClientRegistry.registerKeyBinding(openHUDEditor);
        ClientRegistry.registerKeyBinding(perspective360);
    }
}
