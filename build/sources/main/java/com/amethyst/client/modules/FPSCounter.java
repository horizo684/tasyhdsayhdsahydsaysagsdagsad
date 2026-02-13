package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.Module.Category;
import net.minecraft.client.Minecraft;

public class FPSCounter extends Module {
    
    public FPSCounter() {
        super("FPS Counter", "Shows your current FPS", 0, Category.RENDER);
        this.setEnabled(true);
    }
    
    public String getText() {
        return "FPS: " + Minecraft.getDebugFPS();
    }
}
