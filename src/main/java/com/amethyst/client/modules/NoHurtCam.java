package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.Module.Category;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoHurtCam extends Module {
    private Minecraft mc = Minecraft.getMinecraft();

    public NoHurtCam() {
        super("NoHurtCam", "Removes camera shake when taking damage", 0, Category.MISC);
    }

    /**
     * PRIMARY FIX: Cancels camera roll every single frame
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (!isEnabled() || mc.thePlayer == null) {
            return;
        }
        
        // Полностью обнуляем все углы камеры при уроне
        event.roll = 0.0F;
        event.pitch = event.pitch; // Оставляем как есть
        event.yaw = event.yaw;     // Оставляем как есть
    }

    /**
     * SECONDARY FIX: Zeroes out ALL hurt-related fields every tick
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isEnabled() || mc.thePlayer == null) {
            return;
        }
        
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        // КРИТИЧЕСКИ ВАЖНО: Обнуляем hurtTime - именно он вызывает тряску!
        mc.thePlayer.hurtTime = 0;
        mc.thePlayer.maxHurtTime = 0;
        
        // Обнуляем все shake-поля
        mc.thePlayer.prevCameraYaw = 0.0F;
        mc.thePlayer.cameraYaw = 0.0F;
        
        // Обнуляем attackedAtYaw (угол атаки)
        mc.thePlayer.attackedAtYaw = 0.0F;
    }
    
    /**
     * THIRD FIX: Cancel on every render tick too
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!isEnabled() || mc.thePlayer == null) {
            return;
        }
        
        // Обнуляем даже между кадрами
        mc.thePlayer.hurtTime = 0;
        mc.thePlayer.prevCameraYaw = 0.0F;
        mc.thePlayer.cameraYaw = 0.0F;
    }
}