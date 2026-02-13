package com.amethyst.client;

import com.amethyst.client.modules.Hitbox;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;

public class HitboxPickerGUI extends GuiScreen {

    private final GuiScreen parent;
    private final Hitbox hitbox;

    private int wheelCX, wheelCY;
    private static final int WHEEL_R = 58;
    private boolean draggingWheel = false;
    private boolean draggingBri = false;
    private float selHue = 0f, selSat = 1f, selBri = 1f;

    private int briX, briY;
    private static final int BRI_W = 16, BRI_H = 120;

    private int alphaX, alphaY;
    private static final int ALPHA_W = 180, ALPHA_H = 14;
    private boolean draggingAlpha = false;

    private static final int CHECK_SIZE = 16;

    public HitboxPickerGUI(GuiScreen parent, Hitbox hitbox) {
        this.parent = parent;
        this.hitbox = hitbox;
        
        // Инициализируем HSB из текущего RGB
        float[] hsb = Color.RGBtoHSB(hitbox.getRed(), hitbox.getGreen(), hitbox.getBlue(), null);
        selHue = hsb[0];
        selSat = hsb[1];
        selBri = hsb[2];
    }

    @Override
    public void initGui() {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();
        
        wheelCX = w / 2 - 60;
        wheelCY = h / 2 - 10;
        
        briX = wheelCX + WHEEL_R + 18;
        briY = wheelCY - BRI_H / 2;
        
        alphaX = w / 2 - ALPHA_W / 2;
        alphaY = h - 80;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();

        drawGradientRect(0, 0, w, h, 0xF2060410, 0xF2010008);

        String title = "Hitbox Settings";
        mc.fontRendererObj.drawStringWithShadow(title,
            w / 2 - mc.fontRendererObj.getStringWidth(title) / 2, 7, 0xFFCCDDFF);

        drawColorSection(mx, my);
        drawEntityFilters(mx, my, w, h);
        drawAlphaSlider();
        drawPreviewBox(w, h);
        drawBackButton(mx, my, h);

        super.drawScreen(mx, my, pt);
    }

    private void drawColorSection(int mx, int my) {
        // Цветовое колесо - ОБНОВЛЯЕТСЯ при изменении яркости!
        drawColorWheel();
        
        // Точка выбора на колесе
        int sx = (int)(wheelCX + Math.cos(selHue * 2 * Math.PI) * selSat * WHEEL_R);
        int sy = (int)(wheelCY + Math.sin(selHue * 2 * Math.PI) * selSat * WHEEL_R);
        drawCircle(sx, sy, 6, 0xFFFFFFFF);
        drawCircle(sx, sy, 4, Color.HSBtoRGB(selHue, selSat, selBri) | 0xFF000000);

        // Бар яркости
        drawBriBar();
        int bhy = (int)(briY + (1f - selBri) * BRI_H);
        drawRect(briX - 4, bhy - 2, briX + BRI_W + 4, bhy + 2, 0xFFFFFFFF);
        drawRect(briX - 3, bhy - 1, briX + BRI_W + 3, bhy + 1, 0xFF0A1020);

        // Лейблы
        mc.fontRendererObj.drawString("§8Hue/Sat", wheelCX - WHEEL_R, wheelCY + WHEEL_R + 8, 0xFF445566);
        mc.fontRendererObj.drawString("§8Brightness", briX - 20, briY - 10, 0xFF445566);

        // Hex код
        int col = Color.HSBtoRGB(selHue, selSat, selBri);
        mc.fontRendererObj.drawString(String.format("#%06X", col & 0xFFFFFF),
            wheelCX - WHEEL_R, wheelCY - WHEEL_R - 15, 0xFF88AACC);
    }

    private void drawEntityFilters(int mx, int my, int w, int h) {
        int startY = h / 2 + 70;
        int centerX = w / 2;
        
        mc.fontRendererObj.drawString("§8Show hitboxes for:", centerX - 50, startY - 15, 0xFF445566);
        
        String[] labels = { "Players", "Mobs", "Animals" };
        boolean[] states = { hitbox.isShowPlayers(), hitbox.isShowMobs(), hitbox.isShowAnimals() };
        
        int checkStartX = centerX - (labels.length * (CHECK_SIZE + 50)) / 2;
        
        for (int i = 0; i < labels.length; i++) {
            int cx = checkStartX + i * (CHECK_SIZE + 50);
            int cy = startY;
            
            boolean hov = mx >= cx && mx <= cx + CHECK_SIZE && my >= cy && my <= cy + CHECK_SIZE;
            
            drawRect(cx, cy, cx + CHECK_SIZE, cy + CHECK_SIZE, 
                states[i] ? 0xFF1A3050 : 0xFF0A1420);
            drawHollowRect(cx, cy, cx + CHECK_SIZE, cy + CHECK_SIZE, 
                hov ? 0xFF5599CC : 0xFF2A4060);
            
            if (states[i]) {
                mc.fontRendererObj.drawString("✓", cx + 3, cy + 2, 0xFF44CCFF);
            }
            
            mc.fontRendererObj.drawString(labels[i], cx + CHECK_SIZE + 4, cy + 4, 0xFFAABBCC);
        }
    }

    private void drawAlphaSlider() {
        mc.fontRendererObj.drawString("§8Opacity", alphaX, alphaY - 10, 0xFF445566);

        int rgb = Color.HSBtoRGB(selHue, selSat, selBri) & 0x00FFFFFF;
        
        // Шахматная доска для фона
        for (int x = 0; x < ALPHA_W; x += 4) {
            for (int y = 0; y < ALPHA_H; y += 4) {
                if ((x / 4 + y / 4) % 2 == 0) {
                    drawRect(alphaX + x, alphaY + y, 
                            alphaX + Math.min(x + 4, ALPHA_W), 
                            alphaY + Math.min(y + 4, ALPHA_H), 
                            0xFF333333);
                }
            }
        }
        
        // Градиент прозрачности
        for (int i = 0; i < ALPHA_W; i++) {
            int alpha = (int)(255 * i / (float)ALPHA_W);
            drawRect(alphaX + i, alphaY, alphaX + i + 1, alphaY + ALPHA_H,
                    rgb | (alpha << 24));
        }
        
        drawHollowRect(alphaX, alphaY, alphaX + ALPHA_W, alphaY + ALPHA_H, 0x66FFFFFF);

        // Ползунок
        float norm = hitbox.getAlpha() / 255f;
        int hx = (int)(alphaX + norm * ALPHA_W);
        drawRect(hx - 3, alphaY - 2, hx + 3, alphaY + ALPHA_H + 2, 0xFFFFFFFF);
        drawRect(hx - 2, alphaY - 1, hx + 2, alphaY + ALPHA_H + 1, 0xFF0A1020);

        String av = String.format("%d%%", (int)(norm * 100));
        mc.fontRendererObj.drawString(av, alphaX + ALPHA_W + 6, alphaY + 3, 0xFF88AACC);
    }

    private void drawPreviewBox(int w, int h) {
        int bx = w / 2 - 80;
        int by = h - 50;
        int bw = 160;
        int bh = 20;
        
        mc.fontRendererObj.drawString("§8Preview", bx, by - 10, 0xFF445566);
        
        int rgb = Color.HSBtoRGB(selHue, selSat, selBri) & 0x00FFFFFF;
        int color = rgb | (hitbox.getAlpha() << 24);
        
        // Шахматная доска
        for (int x = 0; x < bw; x += 8) {
            for (int y = 0; y < bh; y += 8) {
                if ((x / 8 + y / 8) % 2 == 0) {
                    drawRect(bx + x, by + y, 
                            bx + Math.min(x + 8, bw), 
                            by + Math.min(y + 8, bh), 
                            0xFF222222);
                } else {
                    drawRect(bx + x, by + y, 
                            bx + Math.min(x + 8, bw), 
                            by + Math.min(y + 8, bh), 
                            0xFF444444);
                }
            }
        }
        
        drawRect(bx, by, bx + bw, by + bh, color);
        drawHollowRect(bx, by, bx + bw, by + bh, 0x44FFFFFF);
    }

    private void drawBackButton(int mx, int my, int h) {
        int bx = 10, by = h - 22, bw = 70, bh = 16;
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        drawRect(bx, by, bx + bw, by + bh, hov ? 0xFF0D1825 : 0xFF080E18);
        drawHollowRect(bx, by, bx + bw, by + bh, 0xFF1A3045);
        String t = "◀ Back";
        mc.fontRendererObj.drawStringWithShadow(t, 
            bx + bw/2 - mc.fontRendererObj.getStringWidth(t)/2, by + 4, 0xFF5599BB);
    }

    private void drawColorWheel() {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        Tessellator ts = Tessellator.getInstance();
        WorldRenderer wr = ts.getWorldRenderer();
        int segs = 64;
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        
        // Центр - белый/серый в зависимости от яркости
        int centerRGB = Color.HSBtoRGB(0, 0, selBri);
        wr.pos(wheelCX, wheelCY, 0).color(
            (centerRGB>>16)&0xFF, 
            (centerRGB>>8)&0xFF, 
            centerRGB&0xFF, 
            255
        ).endVertex();
        
        // Края - цветной круг с учетом яркости
        for (int i = 0; i <= segs; i++) {
            double angle = 2 * Math.PI * i / segs;
            int x = (int)(wheelCX + Math.cos(angle) * WHEEL_R);
            int y = (int)(wheelCY + Math.sin(angle) * WHEEL_R);
            int rgb = Color.HSBtoRGB((float)i / segs, 1f, selBri);
            wr.pos(x, y, 0).color((rgb>>16)&0xFF, (rgb>>8)&0xFF, rgb&0xFF, 255).endVertex();
        }
        ts.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawBriBar() {
        for (int i = 0; i < BRI_H; i++) {
            float b = 1f - (float) i / BRI_H;
            drawRect(briX, briY + i, briX + BRI_W, briY + i + 1,
                    Color.HSBtoRGB(selHue, selSat, b) | 0xFF000000);
        }
        drawHollowRect(briX, briY, briX + BRI_W, briY + BRI_H, 0x55FFFFFF);
    }

    private void drawCircle(int cx, int cy, int r, int color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        Tessellator ts = Tessellator.getInstance();
        WorldRenderer wr = ts.getWorldRenderer();
        int segs = 20;
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        int rv = (color>>16)&0xFF, gv = (color>>8)&0xFF, bv = color&0xFF, av = (color>>24)&0xFF;
        wr.pos(cx, cy, 0).color(rv, gv, bv, av).endVertex();
        for (int i = 0; i <= segs; i++) {
            double a = 2 * Math.PI * i / segs;
            wr.pos(cx + Math.cos(a) * r, cy + Math.sin(a) * r, 0).color(rv, gv, bv, av).endVertex();
        }
        ts.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawHollowRect(int l, int t, int r, int b, int col) {
        drawRect(l, t, r, t + 1, col);
        drawRect(l, b - 1, r, b, col);
        drawRect(l, t, l + 1, b, col);
        drawRect(r - 1, t, r, b, col);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();

        if (mx >= 10 && mx <= 80 && my >= h - 22 && my <= h - 6) {
            mc.displayGuiScreen(parent);
            return;
        }

        int dx = mx - wheelCX, dy = my - wheelCY;
        if (Math.sqrt(dx * dx + dy * dy) <= WHEEL_R) {
            draggingWheel = true;
            updateWheel(mx, my);
            return;
        }

        if (mx >= briX && mx <= briX + BRI_W && my >= briY && my <= briY + BRI_H) {
            draggingBri = true;
            updateBri(my);
            return;
        }

        if (mx >= alphaX && mx <= alphaX + ALPHA_W && my >= alphaY - 3 && my <= alphaY + ALPHA_H + 3) {
            draggingAlpha = true;
            updateAlpha(mx);
            return;
        }

        int startY = h / 2 + 70;
        int centerX = w / 2;
        String[] labels = { "Players", "Mobs", "Animals" };
        int checkStartX = centerX - (labels.length * (CHECK_SIZE + 50)) / 2;
        
        for (int i = 0; i < labels.length; i++) {
            int cx = checkStartX + i * (CHECK_SIZE + 50);
            int cy = startY;
            
            if (mx >= cx && mx <= cx + CHECK_SIZE && my >= cy && my <= cy + CHECK_SIZE) {
                switch (i) {
                    case 0: hitbox.setShowPlayers(!hitbox.isShowPlayers()); break;
                    case 1: hitbox.setShowMobs(!hitbox.isShowMobs()); break;
                    case 2: hitbox.setShowAnimals(!hitbox.isShowAnimals()); break;
                }
                return;
            }
        }

        super.mouseClicked(mx, my, btn);
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        draggingWheel = false;
        draggingBri = false;
        draggingAlpha = false;
        super.mouseReleased(mx, my, state);
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        if (draggingWheel) updateWheel(mx, my);
        if (draggingBri) updateBri(my);
        if (draggingAlpha) updateAlpha(mx);
        super.mouseClickMove(mx, my, btn, time);
    }

    private void updateWheel(int mx, int my) {
        int dx = mx - wheelCX, dy = my - wheelCY;
        selHue = (float)((Math.atan2(dy, dx) / (2 * Math.PI) + 1.0) % 1.0);
        selSat = Math.min(1f, (float)(Math.sqrt(dx * dx + dy * dy) / WHEEL_R));
        updateColor();
    }

    private void updateBri(int my) {
        selBri = 1f - Math.max(0, Math.min(1f, (float)(my - briY) / BRI_H));
        updateColor();
    }

    private void updateAlpha(int mx) {
        float norm = Math.max(0, Math.min(1f, (float)(mx - alphaX) / ALPHA_W));
        hitbox.setAlpha((int)(norm * 255));
    }

    private void updateColor() {
        int rgb = Color.HSBtoRGB(selHue, selSat, selBri);
        hitbox.setRed((rgb >> 16) & 0xFF);
        hitbox.setGreen((rgb >> 8) & 0xFF);
        hitbox.setBlue(rgb & 0xFF);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
