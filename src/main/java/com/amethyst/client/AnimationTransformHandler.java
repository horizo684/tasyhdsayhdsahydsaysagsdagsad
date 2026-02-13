package com.amethyst.client;

import com.amethyst.client.modules.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Timer;

import java.lang.reflect.Field;

/**
 * Обработчик трансформаций предметов для 1.7 анимаций
 * Теперь работает через перехват swingProgress в renderItemInFirstPerson
 */
public class AnimationTransformHandler {
    
    private static Minecraft mc = Minecraft.getMinecraft();
    private static Field timerField = null;
    private static Field renderPartialTicksField = null;
    
    static {
        // Инициализируем рефлексию для доступа к timer
        try {
            // Ищем поле timer в Minecraft
            for (Field field : Minecraft.class.getDeclaredFields()) {
                if (field.getType() == Timer.class) {
                    field.setAccessible(true);
                    timerField = field;
                    System.out.println("[AnimationTransformHandler] Found timer field: " + field.getName());
                    break;
                }
            }
            
            // Ищем поле renderPartialTicks в Timer
            if (timerField != null) {
                for (Field field : Timer.class.getDeclaredFields()) {
                    if (field.getType() == float.class) {
                        field.setAccessible(true);
                        renderPartialTicksField = field;
                        System.out.println("[AnimationTransformHandler] Found renderPartialTicks field: " + field.getName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AnimationTransformHandler] Failed to setup reflection: " + e.getMessage());
        }
    }
    
    /**
     * Получает renderPartialTicks через рефлексию
     */
    private static float getRenderPartialTicks() {
        try {
            if (timerField != null && renderPartialTicksField != null) {
                Timer timer = (Timer) timerField.get(mc);
                if (timer != null) {
                    return renderPartialTicksField.getFloat(timer);
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки, вернем fallback
        }
        
        // Fallback: используем 1.0F (полный тик)
        return 1.0F;
    }
    
    /**
     * КЛЮЧЕВОЙ МЕТОД для 1.7 BLOCKHIT!
     * 
     * Vanilla renderItemInFirstPerson передает swingProgress = 0 когда игрок блокируется.
     * Мы заменяем его на player.getSwingProgress() чтобы анимация НЕ ПРЕРЫВАЛАСЬ!
     * 
     * @param equipProgress - прогресс экипировки предмета
     * @param vanillaSwingProgress - что vanilla хотела передать (0 при блокировке)
     * @return правильный swingProgress для 1.7 blockhit
     */
    public static float getCorrectSwingProgress(float equipProgress, float vanillaSwingProgress) {
        // Получаем модуль анимаций
        Animations anim = getAnimations();
        if (anim == null || !anim.isEnabled() || !anim.isOldBlockhit()) {
            // Модуль выключен или blockhit выключен - используем vanilla значение
            return vanillaSwingProgress;
        }
        
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) {
            return vanillaSwingProgress;
        }
        
        // Проверяем блокируется ли игрок
        if (!player.isBlocking()) {
            // Не блокируется - используем vanilla значение
            return vanillaSwingProgress;
        }
        
        // КЛЮЧЕВОЙ МОМЕНТ!
        // Игрок блокируется, но у него может быть активная анимация свинга
        // Получаем РЕАЛЬНЫЙ swingProgress напрямую от игрока
        float partialTicks = getRenderPartialTicks();
        float realSwingProgress = player.getSwingProgress(partialTicks);
        
        if (realSwingProgress > 0) {
            System.out.println("[BlockhitFix] BLOCKHIT DETECTED! vanilla=" + vanillaSwingProgress + 
                             " real=" + realSwingProgress + " (partialTicks=" + partialTicks + ")");
        }
        
        // Возвращаем РЕАЛЬНЫЙ progress, игнорируя то что vanilla передала 0
        return realSwingProgress;
    }
    
    /**
     * Старый метод - больше не используется, но оставлен для совместимости
     */
    @Deprecated
    public static void applyTransforms(float equipProgress, float swingProgress) {
        // Этот метод больше не нужен - вся логика теперь в getCorrectSwingProgress
        // Но оставляем его чтобы не ломать существующие вызовы
    }
    
    /**
     * Получает модуль анимаций
     */
    private static Animations getAnimations() {
        if (AmethystClient.moduleManager == null) return null;
        return (Animations) AmethystClient.moduleManager.getModuleByName("Animations");
    }
}