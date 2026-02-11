package com.amethyst.client.modules;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import com.amethyst.client.CustomChatPickerGUI;
import com.amethyst.client.Module; // убедитесь, что импорт правильный

public class CustomChat extends Module {

    private boolean fadeMessages = true;
    private boolean showTimestamps = false;
    private boolean showBackground = true;
    private float scale = 1.0f;
    private int maxMessages = 10;
    private int textColor = 0xFFFFFF;
    private float bgAlpha = 0.5f;

    public CustomChat() {
        // Передаем параметры в конструктор базового класса
        super("CustomChat", "Movable chat with smooth animations", Keyboard.KEY_NONE, Category.RENDER);
    }

    @Override
    public void onEnable() {
        // Ваш код при включении модуля
    }

    @Override
    public void onDisable() {
        // Ваш код при отключении модуля
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!this.isEnabled()) return;
        
        if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_C)) {
            mc.displayGuiScreen(new CustomChatPickerGUI(mc.currentScreen, this));
        }
    }

    public boolean isFadeMessages() { 
        return fadeMessages; 
    }
    
    public void setFadeMessages(boolean fade) { 
        this.fadeMessages = fade; 
    }

    public boolean isShowTimestamps() { 
        return showTimestamps; 
    }
    
    public void setShowTimestamps(boolean show) { 
        this.showTimestamps = show; 
    }

    public boolean isShowBackground() {
        return showBackground;
    }

    public void setShowBackground(boolean show) {
        this.showBackground = show;
    }

    public float getScale() { 
        return scale; 
    }
    
    public void setScale(float scale) { 
        this.scale = Math.max(0.5f, Math.min(2.0f, scale)); 
    }

    public int getMaxMessages() { 
        return maxMessages; 
    }
    
    public void setMaxMessages(int max) { 
        this.maxMessages = Math.max(3, Math.min(20, max)); 
    }

    public int getTextColor() { 
        return textColor; 
    }
    
    public void setTextColor(int color) { 
        this.textColor = color; 
    }

    public float getBgAlpha() {
        return bgAlpha;
    }

    public void setBgAlpha(float alpha) {
        this.bgAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }
}