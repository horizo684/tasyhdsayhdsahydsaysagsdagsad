package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.Module.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoSprint extends Module {
    private Minecraft mc = Minecraft.getMinecraft();
    
    public AutoSprint() {
        super("AutoSprint", "Auto sprint when moving forward", 0, Category.MISC);
    }
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        // Проверяем: игрок нажимает W (движется вперёд)?
        boolean movingForward = mc.gameSettings.keyBindForward.isKeyDown();
        
        if (movingForward) {
            // СИМУЛИРУЕМ ЗАЖАТИЕ КЛАВИШИ СПРИНТА (по умолчанию Left Ctrl)
            // Это эквивалентно тому, как если бы игрок сам зажал кнопку бега
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        } else {
            // Если W не нажат - отпускаем кнопку спринта
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        }
    }
    
    @Override
    public void onDisable() {
        // При выключении модуля - отпускаем кнопку спринта
        if (mc != null && mc.gameSettings != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        }
    }
}