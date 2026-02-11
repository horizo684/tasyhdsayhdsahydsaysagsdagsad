package com.amethyst.client.modules;

import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;

public class FPSCounter extends Module {
    
    public FPSCounter() {
        super("FPS Counter", "Shows your current FPS");
        this.setEnabled(true);
    }
    
    public String getText() {
        return "FPS: " + Minecraft.getDebugFPS();
    }
}
