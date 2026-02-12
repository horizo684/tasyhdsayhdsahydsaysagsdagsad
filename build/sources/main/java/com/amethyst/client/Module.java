package com.amethyst.client;

public class Module {
    // Перечисление категорий модулей
    public enum Category {
        COMBAT,
        MOVEMENT,
        RENDER,
        PLAYER,
        MISC
    }

    private String name;
    private String description;
    private int keyBind;
    private Category category;
    private boolean enabled;

    // Конструктор модуля
    public Module(String name, String description, int keyBind, Category category) {
        this.name = name;
        this.description = description;
        this.keyBind = keyBind;
        this.category = category;
        this.enabled = false; // по умолчанию выключен
    }

    // Методы для включения и отключения модуля, можно переопределять в наследниках
    public void onEnable() {}
    public void onDisable() {}

    // Переключение состояния модуля
    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    // Проверка, включен ли модуль
    public boolean isEnabled() {
        return enabled;
    }

    // Установка состояния модуля
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    // Геттеры
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getKeyBind() {
        return keyBind;
    }

    public int getKey() {
        return keyBind;
    }

    public void setKey(int key) {
        this.keyBind = key;
    }

    public void setKeyCode(int key) {
        this.keyBind = key;
    }

    public Category getCategory() {
        return category;
    }
    
    // Методы для сохранения и загрузки специфических настроек модуля
    // Переопределяются в наследниках при необходимости
    public void saveSettings() {}
    public void loadSettings() {}
}