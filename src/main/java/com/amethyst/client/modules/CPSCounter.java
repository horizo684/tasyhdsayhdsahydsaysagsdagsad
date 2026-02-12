package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.Module.Category;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class CPSCounter extends Module {
    private List<Long> clicks = new ArrayList<>();
    private static final long CLICK_TIMEOUT = 1000;
    
    public enum CPSStyle {
        WHITE(0xFFFFFFFF, "White"),
        RAINBOW(0, "Rainbow"),
        GRADIENT(0, "Gradient"),
        FIRE(0, "Fire"),
        OCEAN(0, "Ocean"),
        PURPLE(0, "Purple");
        
        private int color;
        private String name;
        
        CPSStyle(int color, String name) {
            this.color = color;
            this.name = name;
        }
        
        public int getColor() {
            return color;
        }
        
        public String getName() {
            return name;
        }
    }
    
    private CPSStyle currentStyle;
    private int styleIndex;
    
    public CPSCounter() {
        super("CPS Counter", "Shows your clicks per second", 0, Category.RENDER);
        this.setEnabled(true);
        this.currentStyle = CPSStyle.RAINBOW;
        this.styleIndex = 1;
    }
    
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (!isEnabled()) {
            return;
        }
        
        if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0) {
            clicks.add(System.currentTimeMillis());
        }
    }
    
    public int getCPS() {
        long currentTime = System.currentTimeMillis();
        clicks.removeIf(clickTime -> currentTime - clickTime > CLICK_TIMEOUT);
        return clicks.size();
    }
    
    public String getText() {
        return "CPS: " + getCPS();
    }
    
    public void nextStyle() {
        styleIndex = (styleIndex + 1) % CPSStyle.values().length;
        currentStyle = CPSStyle.values()[styleIndex];
    }
    
    public void previousStyle() {
        styleIndex--;
        if (styleIndex < 0) {
            styleIndex = CPSStyle.values().length - 1;
        }
        currentStyle = CPSStyle.values()[styleIndex];
    }
    
    public CPSStyle getCurrentStyle() {
        return currentStyle;
    }
    
    public int getDisplayColor() {
        switch (currentStyle) {
            case RAINBOW:
                float hue = (System.currentTimeMillis() % 3600) / 3600.0f;
                return java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f) | 0xFF000000;
            case GRADIENT:
                return 0xFF00D9FF;
            case FIRE:
                return 0xFFFF5555;
            case OCEAN:
                return 0xFF55FFFF;
            case PURPLE:
                return 0xFFAA00AA;
            case WHITE:
            default:
                return 0xFFFFFFFF;
        }
    }
}