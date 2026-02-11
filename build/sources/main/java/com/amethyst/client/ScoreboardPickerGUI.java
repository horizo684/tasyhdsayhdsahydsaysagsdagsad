package com.amethyst.client;

import com.amethyst.client.modules.Scoreboard;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.awt.Color;
import java.io.IOException;

public class ScoreboardPickerGUI extends GuiScreen {

    private final GuiScreen parent;
    private final Scoreboard mod;

    // Слайдеры
    private boolean draggingBg    = false;
    private boolean draggingScale = false;

    private static final int PANEL_W = 280;
    private static final int PANEL_H = 260;

    public ScoreboardPickerGUI(GuiScreen parent, Scoreboard mod) {
        this.parent = parent;
        this.mod    = mod;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth(), H = sr.getScaledHeight();
        int px = W / 2 - PANEL_W / 2;
        int py = H / 2 - PANEL_H / 2;

        // Затемнение фона
        drawGradientRect(0, 0, W, H, 0xBB000000, 0xBB020208);

        // Панель
        drawPanel(px, py, PANEL_W, PANEL_H);

        // Заголовок
        String title = "Scoreboard Settings";
        int tw = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawStringWithShadow(title, px + PANEL_W / 2 - tw / 2, py + 10, 0xFFFFAAAA);

        // Разделитель
        drawGradientRect(px + 15, py + 22, px + PANEL_W - 15, py + 23, 0x00FFFFFF, 0xAAFF6666);
        drawGradientRect(px + PANEL_W - 15, py + 22, px + PANEL_W - 15, py + 23, 0xAAFF6666, 0x00FFFFFF);

        int iy = py + 32; // текущая Y-позиция для элементов

        // ── Toggle: Show Numbers ──────────────────────────────────────────────
        iy = drawToggle(mx, my, px + 15, iy, "Show score numbers", mod.isShowNumbers());
        iy += 6;

        // ── Toggle: Show Background ───────────────────────────────────────────
        iy = drawToggle(mx, my, px + 15, iy, "Show background", mod.isShowBackground());
        iy += 6;

        // ── Slider: Background alpha ──────────────────────────────────────────
        iy = drawSlider(mx, my, px + 15, iy, PANEL_W - 30,
                "Background opacity", mod.getBgAlpha(), draggingBg, 0xFFAAAAAA);
        iy += 10;

        // ── Slider: Scale ─────────────────────────────────────────────────────
        float scaleNorm = (mod.getScale() - 0.5f) / 1.5f; // 0.5-2.0 → 0-1
        iy = drawSlider(mx, my, px + 15, iy, PANEL_W - 30,
                "Scale  " + String.format("%.1fx", mod.getScale()),
                scaleNorm, draggingScale, 0xFF99CCFF);
        iy += 10;

        // ── Preview mini ──────────────────────────────────────────────────────
        drawPreview(px + 15, iy, PANEL_W - 30);

        // ── Back button ───────────────────────────────────────────────────────
        drawBackButton(mx, my, px, py);

        super.drawScreen(mx, my, pt);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int drawToggle(int mx, int my, int x, int y, String label, boolean state) {
        int toggleW = 32, toggleH = 14;
        int tx = x + 160;
        boolean hov = mx >= tx - 2 && mx <= tx + toggleW + 2 && my >= y - 2 && my <= y + toggleH + 2;

        mc.fontRendererObj.drawString(label, x, y + 3, 0xFFCCDDEE);

        // Фон тоггла
        drawRoundRect(tx, y, toggleW, toggleH, state ? 0xFF2D6B2D : 0xFF333344);
        // Кружок
        int knobX = state ? tx + toggleW - 12 : tx + 2;
        drawCircleFill(knobX + 5, y + 7, 5, state ? 0xFF55FF55 : 0xFF888899);

        String stateStr = state ? "ON" : "OFF";
        mc.fontRendererObj.drawString(stateStr, tx + toggleW + 4, y + 3,
                state ? 0xFF55FF55 : 0xFF666677);
        return y + toggleH + 2;
    }

    private int drawSlider(int mx, int my, int x, int y, int w,
                            String label, float value, boolean dragging, int accentColor) {
        int sliderH = 12;
        mc.fontRendererObj.drawString(label, x, y, 0xFF99AABB);
        y += 11;

        // Track
        drawRoundRect(x, y, w, sliderH, 0xFF1A1A2A);
        // Fill
        int fillW = (int)(value * w);
        if (fillW > 0) drawRoundRect(x, y, fillW, sliderH, accentColor & 0x00FFFFFF | 0x88000000);
        // Handle
        int hx = x + fillW;
        drawCircleFill(hx, y + sliderH / 2, 7, dragging ? 0xFFFFFFFF : accentColor);
        drawCircleFill(hx, y + sliderH / 2, 4, 0xFF0A0A14);

        return y + sliderH + 2;
    }

    private void drawPreview(int x, int y, int w) {
        mc.fontRendererObj.drawString("§8Preview:", x, y, 0xFF445566);
        y += 10;

        int previewBg = mod.isShowBackground()
                ? ((int)(mod.getBgAlpha() * 180) << 24) | 0x000000
                : 0x00000000;

        if (mod.isShowBackground()) drawRect(x, y, x + w, y + 60, previewBg);

        // Title
        String title = "§eObjective";
        mc.fontRendererObj.drawStringWithShadow(title, x + w / 2 - mc.fontRendererObj.getStringWidth(title) / 2, y + 2, mod.getTitleColor());

        // Lines
        String[] names = {"PlayerOne", "PlayerTwo", "PlayerThree"};
        int[] scores   = {1500, 980, 340};
        for (int i = 0; i < 3; i++) {
            mc.fontRendererObj.drawStringWithShadow(names[i], x + 3, y + 12 + i * 10, mod.getTextColor());
            if (mod.isShowNumbers()) {
                String num = String.valueOf(scores[i]);
                mc.fontRendererObj.drawStringWithShadow(num, x + w - mc.fontRendererObj.getStringWidth(num) - 3,
                        y + 12 + i * 10, mod.getNumberColor());
            }
        }
    }

    private void drawBackButton(int mx, int my, int px, int py) {
        int bx = px + 10, by = py + PANEL_H - 26, bw = 80, bh = 16;
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        drawRoundRect(bx, by, bw, bh, hov ? 0xFF1A1228 : 0xFF110D1E);
        drawHollowRect(bx, by, bx + bw, by + bh, 0xFF3A1A5A);
        String t = "◀ Back";
        mc.fontRendererObj.drawStringWithShadow(t, bx + bw / 2 - mc.fontRendererObj.getStringWidth(t) / 2,
                by + 4, 0xFFAA66CC);
    }

    private void drawPanel(int x, int y, int w, int h) {
        // Основной фон
        drawRoundRectGradient(x, y, w, h, 0xEE0A0B18, 0xEE060810);
        // Рамка
        drawHollowRect(x + 1, y + 1, x + w - 1, y + h - 1, 0x33FF6666);
        drawHollowRect(x + 2, y + 2, x + w - 2, y + h - 2, 0x15FF4444);
    }

    // ── GL drawing utils ──────────────────────────────────────────────────────

    private void drawRoundRect(int x, int y, int w, int h, int color) {
        drawRect(x + 2, y, x + w - 2, y + h, color);
        drawRect(x, y + 2, x + w, y + h - 2, color);
    }

    private void drawRoundRectGradient(int x, int y, int w, int h, int top, int bottom) {
        drawGradientRect(x + 2, y, x + w - 2, y + h, top, bottom);
        drawGradientRect(x, y + 2, x + w, y + h - 2, top, bottom);
    }

    private void drawHollowRect(int l, int t, int r, int b, int c) {
        drawRect(l, t, r, t + 1, c); drawRect(l, b - 1, r, b, c);
        drawRect(l, t, l + 1, b, c); drawRect(r - 1, t, r, b, c);
    }

    private void drawCircleFill(int cx, int cy, int r, int color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator ts = Tessellator.getInstance();
        WorldRenderer wr = ts.getWorldRenderer();
        int rv=(color>>16)&0xFF, gv=(color>>8)&0xFF, bv=color&0xFF, av=(color>>24)&0xFF;
        wr.begin(9, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 20; i++) {
            double a = 2 * Math.PI * i / 20;
            wr.pos(cx + Math.cos(a) * r, cy + Math.sin(a) * r, 0).color(rv, gv, bv, av).endVertex();
        }
        ts.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth(), H = sr.getScaledHeight();
        int px = W / 2 - PANEL_W / 2;
        int py = H / 2 - PANEL_H / 2;

        // Back
        if (mx >= px + 10 && mx <= px + 90 && my >= py + PANEL_H - 26 && my <= py + PANEL_H - 10) {
            mc.displayGuiScreen(parent); return;
        }

        // Toggles
        int iy = py + 32;
        // Show numbers toggle
        int toggleX = px + 15 + 160;
        if (mx >= toggleX && mx <= toggleX + 32 && my >= iy && my <= iy + 14) {
            mod.setShowNumbers(!mod.isShowNumbers()); return;
        }
        iy += 14 + 2 + 6;
        // Show background toggle
        if (mx >= toggleX && mx <= toggleX + 32 && my >= iy && my <= iy + 14) {
            mod.setShowBackground(!mod.isShowBackground()); return;
        }
        iy += 14 + 2 + 6;

        // Bg alpha slider click
        int sliderX = px + 15, sliderW = PANEL_W - 30;
        iy += 11; // label height
        if (mx >= sliderX && mx <= sliderX + sliderW && my >= iy && my <= iy + 12) {
            draggingBg = true;
            mod.setBgAlpha((float)(mx - sliderX) / sliderW);
            return;
        }
        iy += 12 + 2 + 10;

        // Scale slider
        iy += 11;
        if (mx >= sliderX && mx <= sliderX + sliderW && my >= iy && my <= iy + 12) {
            draggingScale = true;
            float n = (float)(mx - sliderX) / sliderW;
            mod.setScale(0.5f + n * 1.5f);
            return;
        }

        super.mouseClicked(mx, my, btn);
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        draggingBg = false;
        draggingScale = false;
        super.mouseReleased(mx, my, state);
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth();
        int px = W / 2 - PANEL_W / 2;
        int sliderX = px + 15, sliderW = PANEL_W - 30;

        if (draggingBg) {
            float n = Math.max(0f, Math.min(1f, (float)(mx - sliderX) / sliderW));
            mod.setBgAlpha(n);
        }
        if (draggingScale) {
            float n = Math.max(0f, Math.min(1f, (float)(mx - sliderX) / sliderW));
            mod.setScale(0.5f + n * 1.5f);
        }
        super.mouseClickMove(mx, my, btn, time);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}