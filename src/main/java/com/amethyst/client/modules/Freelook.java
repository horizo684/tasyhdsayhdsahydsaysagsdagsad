package com.amethyst.client.modules;

import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class Freelook extends Module {
    
    private boolean perspectiveToggled = false;
    private float cameraYaw = 0F;
    private float cameraPitch = 0F;
    private int previousPerspective = 0;
    
    public Freelook() {
        super("Freelook", "Look around freely", Keyboard.KEY_LMENU, Category.MISC);
    }
    
    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        perspectiveToggled = false;
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        
        // Проверяем нажатие клавиши бинда
        if (Keyboard.isKeyDown(this.getKeyBind())) {
            if (!perspectiveToggled) {
                perspectiveToggled = true;
                cameraYaw = mc.thePlayer.rotationYaw;
                cameraPitch = mc.thePlayer.rotationPitch;
                
                // Сохраняем текущую перспективу
                previousPerspective = mc.gameSettings.thirdPersonView;
                
                // Переключаем в третье лицо если в первом
                if (previousPerspective == 0) {
                    mc.gameSettings.thirdPersonView = 1;
                }
            }
        } else {
            if (perspectiveToggled) {
                perspectiveToggled = false;
                
                // Восстанавливаем оригинальную перспективу
                mc.gameSettings.thirdPersonView = previousPerspective;
            }
        }
    }
    
    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (perspectiveToggled) {
            Minecraft mc = Minecraft.getMinecraft();
            Entity entity = mc.getRenderViewEntity();
            
            if (entity != null) {
                // Применяем сохранённые углы камеры
                event.yaw = cameraYaw;
                event.pitch = cameraPitch;
                
                // Обновляем углы камеры от движения мыши
                float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
                float f1 = f * f * f * 8.0F;
                
                cameraYaw += mc.mouseHelper.deltaX * f1 * 0.15F;
                cameraPitch -= mc.mouseHelper.deltaY * f1 * 0.15F;
                
                // Ограничиваем pitch
                if (cameraPitch > 90.0F) {
                    cameraPitch = 90.0F;
                }
                if (cameraPitch < -90.0F) {
                    cameraPitch = -90.0F;
                }
            }
        }
    }
    
    public boolean isActive() {
        return perspectiveToggled;
    }
}
