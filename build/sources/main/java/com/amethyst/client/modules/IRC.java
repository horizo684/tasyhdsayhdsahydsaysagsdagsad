package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.AmethystClient;

/**
 * Модуль IRC (In-game Rich Communication)
 * Позволяет видеть кто использует мод и отображать кастомные метки
 */
public class IRC extends Module {
    
    public IRC() {
        super("IRC", "See who uses Amethyst Client", 0, Category.MISC);
        this.setEnabled(true); // Включён по умолчанию
    }
    
    @Override
    public void onEnable() {
        // IRC включён
        System.out.println("[IRC Module] Enabled");
    }
    
    @Override
    public void onDisable() {
        // IRC выключен
        System.out.println("[IRC Module] Disabled");
    }
}