package com.amethyst.client;

import com.amethyst.client.modules.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Обработчик 1.7 blockhit на основе Orange's 1.7 Animations Mod
 * Использует RenderHandEvent вместо прямого патча ItemRenderer
 * 
 * КЛЮЧЕВОЕ ОТЛИЧИЕ от предыдущих версий:
 * Вместо патча transformFirstPersonItem, мы работаем на уровне RenderHandEvent
 * который вызывается ПЕРЕД рендером руки, позволяя нам управлять свингом
 */
public class OrangeBlockhitHandler {
    
    private final Minecraft mc;
    
    public OrangeBlockhitHandler() {
        this.mc = Minecraft.getMinecraft();
    }
    
    /**
     * Обработчик события рендера руки
     * Вызывается каждый кадр когда рендерится рука игрока
     * 
     * Это ТОЧНАЯ точка где Orange mod делает свою магию!
     */
    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        attemptSwing();
    }
    
    /**
     * Пытается выполнить свинг если условия для blockhit выполнены
     * Код основан на декомпиляции Orange's 1.7 Animations
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
        
        // КЛЮЧЕВОЕ УСЛОВИЕ ИЗ ORANGE MOD:
        // Игрок должен ИСПОЛЬЗОВАТЬ предмет (блокироваться)
        if (player.getItemInUseCount() <= 0) {
            return;
        }
        
        // Проверяем что обе кнопки мыши нажаты
        boolean leftClick = mc.gameSettings.keyBindAttack.isKeyDown();
        boolean rightClick = mc.gameSettings.keyBindUseItem.isKeyDown();
        
        if (!leftClick || !rightClick) {
            return;
        }
        
        // Проверяем что игрок смотрит на блок
        if (mc.objectMouseOver == null) {
            return;
        }
        
        if (mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }
        
        // ВСЕ УСЛОВИЯ ВЫПОЛНЕНЫ - выполняем свинг!
        System.out.println("[OrangeBlockhit] >>> EXECUTING BLOCKHIT SWING! <<<");
        swingItem(player);
    }
    
    /**
     * Выполняет свинг предмета
     * Точная копия логики из Orange's 1.7 Animations
     */
    private void swingItem(EntityPlayerSP player) {
        // Вычисляем длительность свинга с учетом эффектов
        int swingAnimationEnd;
        
        if (player.isPotionActive(Potion.digSpeed)) {
            int amplifier = player.getActivePotionEffect(Potion.digSpeed).getAmplifier();
            swingAnimationEnd = 6 - (1 + amplifier);
        } else if (player.isPotionActive(Potion.digSlowdown)) {
            int amplifier = player.getActivePotionEffect(Potion.digSlowdown).getAmplifier();
            swingAnimationEnd = 6 + (1 + amplifier) * 2;
        } else {
            swingAnimationEnd = 6;
        }
        
        // КЛЮЧЕВАЯ ЛОГИКА из Orange mod:
        // Запускаем новый свинг только если:
        // 1. Свинг не идет (!isSwingInProgress)
        // 2. ИЛИ свинг уже прошел половину пути (>= swingAnimationEnd / 2)
        // 3. ИЛИ свинг в неправильном состоянии (< 0)
        if (!player.isSwingInProgress || 
            player.swingProgressInt >= swingAnimationEnd / 2 || 
            player.swingProgressInt < 0) {
            
            System.out.println("[OrangeBlockhit] Starting new swing animation!");
            player.swingProgressInt = -1;
            player.isSwingInProgress = true;
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
