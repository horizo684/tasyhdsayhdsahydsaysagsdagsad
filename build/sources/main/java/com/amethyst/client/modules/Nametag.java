package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.Module.Category;
import java.awt.Color;

public class Nametag extends Module {

    // Названия стилей (те же что в ColorChanger для консистентности)
    public static final String[] STYLE_NAMES = {
        "Rainbow", "Sunset", "Ocean", "Fire", "Purple", "Green",
        "Cherry", "Matrix", "Neon", "Gold", "Ice", "Vaporwave",
        "Candy", "Toxic", "Aqua"
    };

    private int    styleIndex   = 0;
    private String customLabel  = "AMETHYST USER";
    private int    customColor  = 0xFFFFFFFF;

    public Nametag() {
        super("Nametag", "Shows animated tag above your name in F5", 0, Category.MISC);
    }

    /**
     * Возвращает анимированный цвет для символа на позиции index
     * Вызывается каждый кадр при рендеринге → автоматическая анимация!
     */
    public int getCharColor(int index, int total) {
        return getStyleColor(styleIndex, index, total, customColor);
    }

    /**
     * АНИМИРОВАННЫЕ СТИЛИ - используют System.currentTimeMillis()
     * Полностью аналогично ColorChanger.getPresetColor()
     */
    public static int getStyleColor(int style, int index, int total, int customCol) {
        // ВРЕМЯ - основа анимации! Меняется каждый кадр
        float time = (System.currentTimeMillis() % 10000L) / 10000.0f * 3.0f; // speed=3 для nametag
        
        // Смещение для каждого символа
        float offset = total > 1 ? (float) index / (float) total : 0f;
        
        // Итоговая фаза (0.0 - 1.0)
        float phase = (time + offset) % 1.0f;
        
        switch (style) {
            case 0: // Rainbow - классическая радуга
                return Color.HSBtoRGB(phase, 1.0f, 1.0f) | 0xFF000000;
                
            case 1: // Sunset - закат
                return animatedGradient(phase, new float[]{0.08f, 0.95f, 0.83f, 0.08f}, 1.0f, 1.0f);
                
            case 2: // Ocean - океан
                return animatedGradient(phase, new float[]{0.50f, 0.58f, 0.65f, 0.50f}, 1.0f, 1.0f);
                
            case 3: // Fire - огонь
                return animatedGradient(phase, new float[]{0.00f, 0.08f, 0.15f, 0.00f}, 1.0f, 1.0f);
                
            case 4: // Purple - фиолетовая аура
                float pulse = (float) Math.sin(phase * Math.PI * 2) * 0.5f + 0.5f;
                float h4 = 0.73f + pulse * 0.07f;
                return Color.HSBtoRGB(h4, 0.9f, 1.0f) | 0xFF000000;
                
            case 5: // Green - зелёные волны
                return animatedGradient(phase, new float[]{0.27f, 0.33f, 0.38f, 0.27f}, 1.0f, 0.9f);
                
            case 6: // Cherry Blossom - сакура
                return animatedGradient(phase, new float[]{0.93f, 0.90f, 0.88f, 0.93f}, 0.55f, 1.0f);
                
            case 7: { // Matrix - матричный зелёный
                float intensity = (float) Math.abs(Math.sin(phase * Math.PI * 2));
                int green = (int)(intensity * 255);
                return 0xFF000000 | (green << 8);
            }
                
            case 8: // Neon - неоновые огни
                return animatedGradient(phase, new float[]{0.85f, 0.50f, 0.33f, 0.85f}, 1.0f, 1.0f);
                
            case 9: // Gold - золотой блеск
                return animatedGradient(phase, new float[]{0.13f, 0.11f, 0.09f, 0.13f}, 1.0f, 1.0f);
                
            case 10: { // Ice - ледяное дыхание
                float breathe = (float) Math.abs(Math.sin(phase * Math.PI));
                float saturation = breathe * 0.6f;
                return Color.HSBtoRGB(0.57f, saturation, 1.0f) | 0xFF000000;
            }
                
            case 11: // Vaporwave - вапорвейв
                return animatedGradient(phase, new float[]{0.83f, 0.75f, 0.50f, 0.83f}, 0.8f, 1.0f);
                
            case 12: // Candy - конфетная радуга
                return animatedGradient(phase, new float[]{0.00f, 0.90f, 0.75f, 0.55f, 0.00f}, 0.8f, 1.0f);
                
            case 13: // Toxic - токсичные отходы
                return animatedGradient(phase, new float[]{0.30f, 0.24f, 0.18f, 0.30f}, 1.0f, 1.0f);
                
            case 14: // Aqua - водная гладь
                return animatedGradient(phase, new float[]{0.48f, 0.54f, 0.60f, 0.48f}, 1.0f, 1.0f);
                
            default:
                return Color.HSBtoRGB(phase, 1.0f, 1.0f) | 0xFF000000;
        }
    }

    /**
     * Создаёт плавный анимированный градиент через несколько оттенков
     */
    private static int animatedGradient(float phase, float[] hues, float saturation, float brightness) {
        int segments = hues.length - 1;
        float segmentSize = 1.0f / segments;
        
        int currentSegment = (int)(phase / segmentSize);
        if (currentSegment >= segments) currentSegment = segments - 1;
        
        float localPhase = (phase - currentSegment * segmentSize) / segmentSize;
        
        float h1 = hues[currentSegment];
        float h2 = hues[currentSegment + 1];
        
        return lerpHSB(h1, h2, localPhase, saturation, brightness);
    }

    /**
     * Интерполяция между двумя оттенками с учётом кольцевой природы HSB
     */
    private static int lerpHSB(float h1, float h2, float t, float s, float b) {
        float diff = h2 - h1;
        
        if (diff > 0.5f) diff -= 1.0f;
        if (diff < -0.5f) diff += 1.0f;
        
        float h = h1 + diff * t;
        
        while (h < 0) h += 1.0f;
        while (h >= 1.0f) h -= 1.0f;
        
        return Color.HSBtoRGB(h, s, b) | 0xFF000000;
    }

    // Getters / setters
    public int     getStyleIndex()          { return styleIndex; }
    public void    setStyleIndexCustom(int i){ styleIndex = Math.max(0, Math.min(i, STYLE_NAMES.length-1)); }
    public String  getCustomLabel()         { return customLabel.isEmpty() ? "AMETHYST USER" : customLabel; }
    public void    setCustomLabel(String l) { customLabel = l == null ? "" : l; }
    public int     getCustomColor()         { return customColor; }
    public void    setCustomColor(int c)    { customColor = c | 0xFF000000; }

    // Legacy compatibility
    public int getDisplayColor()          { return getCharColor(0, 1); }
    public void nextStyle()               { styleIndex = (styleIndex+1) % STYLE_NAMES.length; }
    public void previousStyle()           { styleIndex = (styleIndex+STYLE_NAMES.length-1) % STYLE_NAMES.length; }
}