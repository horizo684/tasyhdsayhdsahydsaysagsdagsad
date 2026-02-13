package com.amethyst.client.modules;

import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class AutoText extends Module {
    
    private Map<Integer, String> binds = new HashMap<>();
    
    public AutoText() {
        super("AutoText", "Send commands with keybinds", 0, Category.MISC);
    }
    
    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null) return;
        
        int key = Keyboard.getEventKey();
        if (!Keyboard.getEventKeyState()) return;
        
        if (binds.containsKey(key)) {
            String command = binds.get(key);
            if (command != null && !command.isEmpty()) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
            }
        }
    }
    
    public Map<Integer, String> getBinds() {
        return binds;
    }
    
    public void addBind(int key, String command) {
        binds.put(key, command);
    }
    
    public void removeBind(int key) {
        binds.remove(key);
    }
    
    public void clearBinds() {
        binds.clear();
    }
    
    public String getCommand(int key) {
        return binds.get(key);
    }
    
    public boolean hasBind(int key) {
        return binds.containsKey(key);
    }
    
    @Override
    public void saveSettings() {}
    
    @Override
    public void loadSettings() {}
}
