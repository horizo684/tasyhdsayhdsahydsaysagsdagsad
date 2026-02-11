package com.amethyst.client.modules;

import com.amethyst.client.Module;
import java.awt.Color;

public class ColorChanger extends Module {

    // Названия пресетов (15 штук как было)
    public static final String[] PRESET_NAMES = {
        "Rainbow", "Sunset", "Ocean", "Fire", "Purple", "Green",
        "Cherry", "Matrix", "Neon", "Gold", "Ice", "Vaporwave",
        "Candy", "Toxic", "Aqua"
    };

    private int presetIndex = 0;
    private float rainbowSpeed = 5.0f;

    public ColorChanger() {
        super("ColorChanger", "Animated color presets");
        this.setEnabled(true);
    }

    /**
     * Главная функция для получения цвета.
     * Вызывается каждый кадр при отрисовке HUD.
     */
    public int getColor(int index, int total) {
        return getPresetColor(presetIndex, index, total, rainbowSpeed);
    }

    /**
     * АНИМИРОВАННЫЕ ГРАДИЕНТЫ - используют System.currentTimeMillis()
     * для автоматической анимации каждый кадр!
     */
    public static int getPresetColor(int preset, int index, int total, float speed) {
        // ВРЕМЯ - основа анимации! Меняется каждый кадр
        float time = (System.currentTimeMillis() % 10000L) / 10000.0f * speed;
        
        // Смещение для каждого элемента списка
        float offset = total > 1 ? (float) index / (float) total : 0f;
        
        // Итоговая фаза (0.0 - 1.0)
        float phase = (time + offset) % 1.0f;
        
        switch (preset) {
            case 0: // Rainbow - классическая радуга (полный круг HSB)
                return Color.HSBtoRGB(phase, 1.0f, 1.0f) | 0xFF000000;
                
            case 1: // Sunset - закат (оранжевый → розовый → фиолетовый → оранжевый)
                return animatedGradient(phase, new float[]{0.08f, 0.95f, 0.83f, 0.08f}, 1.0f, 1.0f);
                
            case 2: // Ocean - океан (голубой → синий → темно-синий → голубой)
                return animatedGradient(phase, new float[]{0.50f, 0.58f, 0.65f, 0.50f}, 1.0f, 1.0f);
                
            case 3: // Fire - огонь (красный → оранжевый → желтый → красный)
                return animatedGradient(phase, new float[]{0.00f, 0.08f, 0.15f, 0.00f}, 1.0f, 1.0f);
                
            case 4: // Purple - фиолетовая аура (пульсация + сдвиг оттенка)
                float pulse = (float) Math.sin(phase * Math.PI * 2) * 0.5f + 0.5f;
                float h4 = 0.73f + pulse * 0.07f; // 0.73-0.80 (фиолетовый спектр)
                return Color.HSBtoRGB(h4, 0.9f, 1.0f) | 0xFF000000;
                
            case 5: // Green - зелёные волны (лайм → изумруд → лайм)
                return animatedGradient(phase, new float[]{0.27f, 0.33f, 0.38f, 0.27f}, 1.0f, 0.9f);
                
            case 6: // Cherry Blossom - сакура (нежно-розовый → яркий розовый → нежно-розовый)
                return animatedGradient(phase, new float[]{0.93f, 0.90f, 0.88f, 0.93f}, 0.55f, 1.0f);
                
            case 7: { // Matrix - матричный зелёный (чёрный → яркий зелёный пульсация)
                float intensity = (float) Math.abs(Math.sin(phase * Math.PI * 2));
                int green = (int)(intensity * 255);
                return 0xFF000000 | (green << 8);
            }
                
            case 8: // Neon - неоновые огни (розовый → голубой → зелёный → розовый)
                return animatedGradient(phase, new float[]{0.85f, 0.50f, 0.33f, 0.85f}, 1.0f, 1.0f);
                
            case 9: // Gold - золотой блеск (светло-жёлтый → янтарь → бронза → светло-жёлтый)
                return animatedGradient(phase, new float[]{0.13f, 0.11f, 0.09f, 0.13f}, 1.0f, 1.0f);
                
            case 10: { // Ice - ледяное дыхание (белый → голубой → белый)
                float breathe = (float) Math.abs(Math.sin(phase * Math.PI));
                float saturation = breathe * 0.6f; // 0.0-0.6 (от белого до голубого)
                return Color.HSBtoRGB(0.57f, saturation, 1.0f) | 0xFF000000;
            }
                
            case 11: // Vaporwave - вапорвейв (пурпур → фиолет → циан → пурпур)
                return animatedGradient(phase, new float[]{0.83f, 0.75f, 0.50f, 0.83f}, 0.8f, 1.0f);
                
            case 12: // Candy - конфетная радуга (красный → розовый → фиолет → голубой → красный)
                return animatedGradient(phase, new float[]{0.00f, 0.90f, 0.75f, 0.55f, 0.00f}, 0.8f, 1.0f);
                
            case 13: // Toxic - токсичные отходы (лайм → кислотный жёлтый → неоново-зелёный → лайм)
                return animatedGradient(phase, new float[]{0.30f, 0.24f, 0.18f, 0.30f}, 1.0f, 1.0f);
                
            case 14: // Aqua - водная гладь (бирюзовый → морской → сапфировый → бирюзовый)
                return animatedGradient(phase, new float[]{0.48f, 0.54f, 0.60f, 0.48f}, 1.0f, 1.0f);
                
            default:
                return Color.HSBtoRGB(phase, 1.0f, 1.0f) | 0xFF000000;
        }
    }

    /**
     * Создаёт плавный анимированный градиент через несколько оттенков
     * @param phase - текущая фаза анимации (0.0 - 1.0)
     * @param hues - массив оттенков HSB для перехода
     * @param saturation - насыщенность цвета
     * @param brightness - яркость цвета
     */
    private static int animatedGradient(float phase, float[] hues, float saturation, float brightness) {
        int segments = hues.length - 1; // Количество переходов (n-1)
        float segmentSize = 1.0f / segments;
        
        // Находим текущий сегмент
        int currentSegment = (int)(phase / segmentSize);
        if (currentSegment >= segments) currentSegment = segments - 1;
        
        // Локальная позиция внутри сегмента (0.0 - 1.0)
        float localPhase = (phase - currentSegment * segmentSize) / segmentSize;
        
        // Интерполируем между двумя соседними оттенками
        float h1 = hues[currentSegment];
        float h2 = hues[currentSegment + 1];
        
        return lerpHSB(h1, h2, localPhase, saturation, brightness);
    }

    /**
     * Интерполяция между двумя оттенками с учётом кольцевой природы HSB
     */
    private static int lerpHSB(float h1, float h2, float t, float s, float b) {
        // Вычисляем разницу оттенков
        float diff = h2 - h1;
        
        // Выбираем кратчайший путь по кругу HSB (важно для плавности!)
        if (diff > 0.5f) diff -= 1.0f;
        if (diff < -0.5f) diff += 1.0f;
        
        // Интерполируем
        float h = h1 + diff * t;
        
        // Нормализуем в диапазон [0, 1)
        while (h < 0) h += 1.0f;
        while (h >= 1.0f) h -= 1.0f;
        
        return Color.HSBtoRGB(h, s, b) | 0xFF000000;
    }

    // Getters and Setters
    public int getPresetIndex() {
        return presetIndex;
    }

    public void setPresetIndex(int index) {
        this.presetIndex = Math.max(0, Math.min(index, PRESET_NAMES.length - 1));
    }

    public float getRainbowSpeed() {
        return rainbowSpeed;
    }

    public void setRainbowSpeed(float speed) {
        this.rainbowSpeed = Math.max(1.0f, Math.min(20.0f, speed));
    }

    // Legacy compatibility
    public boolean getRainbowMode() {
        return true;
    }

    public void setRainbowMode(boolean mode) {
        // Always animated
    }

    public int getStaticColor() {
        return getColor(0, 1);
    }

    public void setStaticColor(int color) {
        // Not used in new version
    }
}