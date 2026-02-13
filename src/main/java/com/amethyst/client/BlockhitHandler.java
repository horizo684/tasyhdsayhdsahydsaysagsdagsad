package com.amethyst.client;

import com.amethyst.client.modules.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Обработчик 1.7 blockhit свинга
 * Основано на Orange's 1.7 Animations Mod
 */
public class BlockhitHandler {
    
    private final Minecraft mc;
    
    public BlockhitHandler() {
        this.mc = Minecraft.getMinecraft();
    }
    
    /**
     * Вызывается каждый тик клиента для обработки blockhit свинга
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        attemptSwing();
    }
    
    /**
     * Также можно вызывать при рендере overlay (как в оригинальном Orange mod)
     */
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        
        attemptSwing();
    }
    
    /**
     * Пытается выполнить свинг если игрок блокхитит
     * Код из Orange's 1.7 Animations Mod
     */
    private void attemptSwing() {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) {
            return;
        }
        
        // Проверяем включен ли модуль анимаций
        Animations anim = getAnimations();
        if (anim == null || !anim.isEnabled() || !anim.isOldBlockhit()) {
            return;
        }
        
        // Проверяем что игрок держит предмет
        if (player.getHeldItem() == null) {
            return;
        }
        
        // Проверяем что игрок использует предмет (блокируется)
        if (player.getItemInUseCount() <= 0) {
            return;
        }
        
        // Проверяем что обе кнопки мыши нажаты
        boolean leftClick = mc.gameSettings.keyBindAttack.isKeyDown();
        boolean rightClick = mc.gameSettings.keyBindUseItem.isKeyDown();
        
        if (!leftClick || !rightClick) {
            return;
        }
        
        // Debug output
        if (mc.objectMouseOver != null) {
            System.out.println("[BlockhitHandler] Both buttons pressed! Mouse over: " + mc.objectMouseOver.typeOfHit);
        }
        
        // Проверяем что игрок смотрит на блок (не атакует в воздух)
        if (mc.objectMouseOver == null) {
            System.out.println("[BlockhitHandler] objectMouseOver is null - skipping swing");
            return;
        }
        
        if (mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            System.out.println("[BlockhitHandler] Not looking at block - skipping swing");
            return;
        }
        
        // Выполняем свинг
        System.out.println("[BlockhitHandler] >>> EXECUTING SWING FOR BLOCKHIT! <<<");
        swingItem(player);
    }
    
    /**
     * Выполняет свинг предмета
     * Код из Orange's 1.7 Animations Mod
     */
    private void swingItem(EntityPlayerSP player) {
        // Вычисляем длительность свинга с учетом эффектов
        int swingAnimationEnd;
        
        if (player.isPotionActive(Potion.digSpeed)) {
            int amplifier = player.getActivePotionEffect(Potion.digSpeed).getAmplifier();
            swingAnimationEnd = 6 - (1 + amplifier) * 1;
        } else if (player.isPotionActive(Potion.digSlowdown)) {
            int amplifier = player.getActivePotionEffect(Potion.digSlowdown).getAmplifier();
            swingAnimationEnd = 6 + (1 + amplifier) * 2;
        } else {
            swingAnimationEnd = 6;
        }
        
        System.out.println("[BlockhitHandler] swingAnimationEnd = " + swingAnimationEnd + 
                           ", isSwingInProgress = " + player.isSwingInProgress + 
                           ", swingProgressInt = " + player.swingProgressInt);
        
        // Запускаем новый свинг если текущий закончен или еще не начался
        if (!player.isSwingInProgress || 
            player.swingProgressInt >= swingAnimationEnd / 2 || 
            player.swingProgressInt < 0) {
            
            System.out.println("[BlockhitHandler] STARTING NEW SWING!");
            player.swingProgressInt = -1;
            player.isSwingInProgress = true;
        } else {
            System.out.println("[BlockhitHandler] Swing already in progress, skipping");
        }
    }
    
    /**
     * Получает модуль анимаций
     */
    private Animations getAnimations() {
        if (AmethystClient.moduleManager == null) {
            return null;
        }
        return (Animations) AmethystClient.moduleManager.getModuleByName("Animations");
    }
}
