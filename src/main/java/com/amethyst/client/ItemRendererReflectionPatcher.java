package com.amethyst.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Патчер который через рефлексию заменяет final itemRenderer
 * и патчит вызовы transformFirstPersonItem
 */
public class ItemRendererReflectionPatcher {
    
    private static CustomItemRenderer customRenderer;
    private static boolean isPatched = false;
    
    /**
     * Патчит ItemRenderer в EntityRenderer
     */
    public static void patch(Minecraft mc) {
        try {
            System.out.println("[ItemRendererPatcher] Starting patching process...");
            
            // Создаем наш кастомный рендерер
            customRenderer = new CustomItemRenderer(mc);
            
            // Получаем поле itemRenderer в EntityRenderer
            Field itemRendererField = mc.entityRenderer.getClass().getDeclaredField("itemRenderer");
            itemRendererField.setAccessible(true);
            
            // Убираем final модификатор
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(itemRendererField, itemRendererField.getModifiers() & ~Modifier.FINAL);
            
            // Заменяем значение поля
            itemRendererField.set(mc.entityRenderer, customRenderer);
            
            isPatched = true;
            System.out.println("[ItemRendererPatcher] Successfully patched ItemRenderer!");
            
        } catch (NoSuchFieldException e) {
            System.err.println("[ItemRendererPatcher] Field not found: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("[ItemRendererPatcher] Access denied: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ItemRendererPatcher] Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Проверяет успешность патча
     */
    public static boolean isPatched() {
        return isPatched;
    }
    
    /**
     * Получает кастомный рендерер (для отладки)
     */
    public static CustomItemRenderer getCustomRenderer() {
        return customRenderer;
    }
}