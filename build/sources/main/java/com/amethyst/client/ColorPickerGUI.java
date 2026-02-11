package com.amethyst.client;

import com.amethyst.client.modules.ColorChanger;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;

public class ColorPickerGUI extends GuiScreen {

    private final GuiScreen parent;
    private final ColorChanger colorChanger;

    // Tabs
    private int mode = 0;  // 0 = presets, 1 = static

    // Speed slider
    private int   speedX, speedY;
    private static final int SPEED_W = 180, SPEED_H = 14;
    private boolean draggingSpeed = false;

    // Static colour wheel
    private int   wheelCX, wheelCY;
    private static final int WHEEL_R = 58;
    private boolean draggingWheel = false;
    private boolean draggingBri   = false;
    private float selHue = 0f, selSat = 1f, selBri = 1f;

    private int briX, briY;
    private static final int BRI_W = 16, BRI_H = 120;

    // Grid layout
    private static final int COLS   = 3;
    private static final int BTN_W  = 85, BTN_H  = 18, BTN_GAP = 4;

    public ColorPickerGUI(GuiScreen parent, ColorChanger cc) {
        this.parent = parent;
        this.colorChanger = cc;
        this.mode = cc.getRainbowMode() ? 0 : 1;
    }

    @Override
    public void initGui() {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();
        speedX = w / 2 - SPEED_W / 2;
        speedY = h - 50;
        wheelCX = w / 2 + 95;
        wheelCY = h / 2 + 5;
        briX = wheelCX + WHEEL_R + 18;
        briY = wheelCY - BRI_H / 2;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();

        drawGradientRect(0, 0, w, h, 0xF2060410, 0xF2010008);

        // Title
        String title = "Color Picker";
        mc.fontRendererObj.drawStringWithShadow(title,
            w / 2 - mc.fontRendererObj.getStringWidth(title) / 2, 7, 0xFFCCDDFF);

        // Tabs
        drawTabs(mx, my, w);

        if (mode == 0) {
            drawPresetGrid(mx, my, w, h);
            drawSpeedSlider();
        } else {
            drawStaticSection(mx, my);
        }

        // Preview bar (always shown)
        drawPreviewBar(w, h);

        // Back
        drawBack(mx, my, h);

        super.drawScreen(mx, my, pt);
    }

    // ─── Tabs ─────────────────────────────────────────────────────────────────
    private void drawTabs(int mx, int my, int w) {
        String[] labels = { "Animated Presets", "Static Colour" };
        int tw = 108, th = 16, gap = 4;
        int sx = w / 2 - (labels.length * tw + (labels.length - 1) * gap) / 2;
        int ty = 20;
        for (int i = 0; i < labels.length; i++) {
            int tx = sx + i * (tw + gap);
            boolean sel = i == mode;
            boolean hov = mx >= tx && mx <= tx + tw && my >= ty && my <= ty + th;
            drawRect(tx, ty, tx + tw, ty + th, sel ? 0xFF142030 : (hov ? 0xFF0E1820 : 0xFF090E15));
            drawHollowRect(tx, ty, tx + tw, ty + th, sel ? 0xFF2299CC : 0xFF1A2A3A);
            int lw = mc.fontRendererObj.getStringWidth(labels[i]);
            mc.fontRendererObj.drawStringWithShadow(labels[i], tx + tw/2 - lw/2, ty + 4,
                    sel ? 0xFF44CCFF : 0xFF556677);
        }
    }

    // ─── Preset grid ──────────────────────────────────────────────────────────
    private void drawPresetGrid(int mx, int my, int w, int h) {
        int names = ColorChanger.PRESET_NAMES.length;
        int gridW = COLS * BTN_W + (COLS - 1) * BTN_GAP;
        int gx = w / 2 - gridW / 2 - 35;
        int gy = 42;

        mc.fontRendererObj.drawString("§8Select preset:", gx, gy - 8, 0xFF445566);

        for (int i = 0; i < names; i++) {
            int col = i % COLS, row = i / COLS;
            int bx = gx + col * (BTN_W + BTN_GAP);
            int by = gy + row * (BTN_H + BTN_GAP);
            boolean sel = i == colorChanger.getPresetIndex();
            boolean hov = mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H;

            // Animated gradient fill — uses real time so it moves!
            for (int px = 0; px < BTN_W; px++) {
                int c = ColorChanger.getPresetColor(i, px, BTN_W, 5f);
                drawRect(bx + px, by, bx + px + 1, by + BTN_H, c | 0xFF000000);
            }
            drawRect(bx, by, bx + BTN_W, by + BTN_H, 0x55000000); // darken for readability

            int border = sel ? 0xFFFFFFFF : (hov ? 0x99FFFFFF : 0x44FFFFFF);
            drawHollowRect(bx, by, bx + BTN_W, by + BTN_H, border);
            if (sel) {
                // bright corner pixels
                drawRect(bx - 1, by - 1, bx + 2, by + 2, 0xFFFFFFFF);
                drawRect(bx + BTN_W - 2, by - 1, bx + BTN_W + 1, by + 2, 0xFFFFFFFF);
                drawRect(bx - 1, by + BTN_H - 2, bx + 2, by + BTN_H + 1, 0xFFFFFFFF);
                drawRect(bx + BTN_W - 2, by + BTN_H - 2, bx + BTN_W + 1, by + BTN_H + 1, 0xFFFFFFFF);
            }

            String name = ColorChanger.PRESET_NAMES[i];
            int nw = mc.fontRendererObj.getStringWidth(name);
            mc.fontRendererObj.drawStringWithShadow(name, bx + BTN_W/2 - nw/2, by + BTN_H/2 - 4, 0xFFFFFFFF);
        }
    }

    // ─── Speed slider ─────────────────────────────────────────────────────────
    private void drawSpeedSlider() {
        mc.fontRendererObj.drawString("§8Animation speed", speedX, speedY - 10, 0xFF445566);

        // Rainbow background track
        for (int i = 0; i < SPEED_W; i++) {
            float h = (float) i / SPEED_W;
            drawRect(speedX + i, speedY, speedX + i + 1, speedY + SPEED_H,
                    Color.HSBtoRGB(h, 1f, 1f) | 0xFF000000);
        }
        drawHollowRect(speedX, speedY, speedX + SPEED_W, speedY + SPEED_H, 0x66FFFFFF);

        float norm = (colorChanger.getRainbowSpeed() - 1f) / 19f;
        int hx = (int)(speedX + norm * SPEED_W);
        drawRect(hx - 3, speedY - 2, hx + 3, speedY + SPEED_H + 2, 0xFFFFFFFF);
        drawRect(hx - 2, speedY - 1, hx + 2, speedY + SPEED_H + 1, 0xFF0A1020);

        String sv = "x" + String.format("%.1f", colorChanger.getRainbowSpeed());
        mc.fontRendererObj.drawString(sv, speedX + SPEED_W + 6, speedY + 3, 0xFF88AACC);
    }

    // ─── Static colour ────────────────────────────────────────────────────────
    private void drawStaticSection(int mx, int my) {
        drawColorWheel();
        // Selector dot
        int sx = (int)(wheelCX + Math.cos(selHue * 2*Math.PI) * selSat * WHEEL_R);
        int sy = (int)(wheelCY + Math.sin(selHue * 2*Math.PI) * selSat * WHEEL_R);
        drawCircle(sx, sy, 6, 0xFFFFFFFF);
        drawCircle(sx, sy, 4, Color.HSBtoRGB(selHue, selSat, selBri) | 0xFF000000);

        drawBriBar();
        int bhy = (int)(briY + (1f - selBri) * BRI_H);
        drawRect(briX - 4, bhy - 2, briX + BRI_W + 4, bhy + 2, 0xFFFFFFFF);

        mc.fontRendererObj.drawString("§8Hue/Sat", wheelCX - WHEEL_R, wheelCY + WHEEL_R + 8, 0xFF445566);
        mc.fontRendererObj.drawString("§8Bri", briX, briY - 10, 0xFF445566);

        // Hex label
        int col = Color.HSBtoRGB(selHue, selSat, selBri);
        mc.fontRendererObj.drawString(String.format("#%06X", col & 0xFFFFFF),
            wheelCX - WHEEL_R, wheelCY - WHEEL_R - 15, 0xFF667788);
    }

    // ─── Preview bar ──────────────────────────────────────────────────────────
    private void drawPreviewBar(int w, int h) {
        int bx = 15, by = h - 24, bw = w - 30, bh = 10;
        for (int i = 0; i < bw; i++) {
            int c;
            if (mode == 0) {
                c = ColorChanger.getPresetColor(colorChanger.getPresetIndex(), i, bw, colorChanger.getRainbowSpeed());
            } else {
                c = Color.HSBtoRGB(selHue, selSat, selBri);
            }
            drawRect(bx + i, by, bx + i + 1, by + bh, c | 0xFF000000);
        }
        drawHollowRect(bx, by, bx + bw, by + bh, 0x44FFFFFF);
        mc.fontRendererObj.drawString("§8preview", bx, by - 8, 0xFF334455);
    }

    // ─── Back button ──────────────────────────────────────────────────────────
    private void drawBack(int mx, int my, int h) {
        int bx = 10, by = h - 22, bw = 70, bh = 16;
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        drawRect(bx, by, bx + bw, by + bh, hov ? 0xFF0D1825 : 0xFF080E18);
        drawHollowRect(bx, by, bx + bw, by + bh, 0xFF1A3045);
        String t = "◀ Back";
        mc.fontRendererObj.drawStringWithShadow(t, bx + bw/2 - mc.fontRendererObj.getStringWidth(t)/2, by + 4, 0xFF5599BB);
    }

    // ─── GL helpers ───────────────────────────────────────────────────────────
    private void drawColorWheel() {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        Tessellator ts = Tessellator.getInstance();
        WorldRenderer wr = ts.getWorldRenderer();
        int segs = 64;
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(wheelCX, wheelCY, 0).color(255, 255, 255, 255).endVertex();
        for (int i = 0; i <= segs; i++) {
            double angle = 2 * Math.PI * i / segs;
            int x = (int)(wheelCX + Math.cos(angle) * WHEEL_R);
            int y = (int)(wheelCY + Math.sin(angle) * WHEEL_R);
            int rgb = Color.HSBtoRGB((float)i / segs, 1f, selBri);
            wr.pos(x, y, 0).color((rgb>>16)&0xFF,(rgb>>8)&0xFF,rgb&0xFF,255).endVertex();
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
        int rv=(color>>16)&0xFF, gv=(color>>8)&0xFF, bv=color&0xFF, av=(color>>24)&0xFF;
        wr.pos(cx,cy,0).color(rv,gv,bv,av).endVertex();
        for (int i=0;i<=segs;i++) {
            double a = 2*Math.PI*i/segs;
            wr.pos(cx+Math.cos(a)*r, cy+Math.sin(a)*r, 0).color(rv,gv,bv,av).endVertex();
        }
        ts.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawHollowRect(int l,int t,int r,int b,int col) {
        drawRect(l,t,r,t+1,col); drawRect(l,b-1,r,b,col);
        drawRect(l,t,l+1,b,col); drawRect(r-1,t,r,b,col);
    }

    // ─── Input ────────────────────────────────────────────────────────────────
    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();

        // Back
        if (mx >= 10 && mx <= 80 && my >= h-22 && my <= h-6) { mc.displayGuiScreen(parent); return; }

        // Tabs
        String[] labels = { "Animated Presets", "Static Colour" };
        int tw = 108, th = 16, gap = 4;
        int sx = w/2 - (labels.length*tw+(labels.length-1)*gap)/2;
        for (int i = 0; i < labels.length; i++) {
            int tx = sx + i*(tw+gap);
            if (mx>=tx && mx<=tx+tw && my>=20 && my<=20+th) {
                mode = i; colorChanger.setRainbowMode(i==0); return;
            }
        }

        if (mode == 0) {
            // Preset grid click
            int names = ColorChanger.PRESET_NAMES.length;
            int gridW = COLS*BTN_W+(COLS-1)*BTN_GAP;
            int gx = w/2 - gridW/2 - 35, gy = 42;
            for (int i = 0; i < names; i++) {
                int col2 = i%COLS, row = i/COLS;
                int bx = gx + col2*(BTN_W+BTN_GAP), by = gy + row*(BTN_H+BTN_GAP);
                if (mx>=bx && mx<=bx+BTN_W && my>=by && my<=by+BTN_H) {
                    colorChanger.setPresetIndex(i);
                    colorChanger.setRainbowMode(true);
                    return;
                }
            }
            // Speed slider
            if (mx>=speedX && mx<=speedX+SPEED_W && my>=speedY-3 && my<=speedY+SPEED_H+3) {
                draggingSpeed = true; updateSpeed(mx);
            }
        } else {
            int dx = mx-wheelCX, dy = my-wheelCY;
            if (Math.sqrt(dx*dx+dy*dy) <= WHEEL_R) { draggingWheel=true; updateWheel(mx,my); return; }
            if (mx>=briX && mx<=briX+BRI_W && my>=briY && my<=briY+BRI_H) { draggingBri=true; updateBri(my); return; }
        }
        super.mouseClicked(mx, my, btn);
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        draggingSpeed = false; draggingWheel = false; draggingBri = false;
        super.mouseReleased(mx, my, state);
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        if (draggingSpeed) updateSpeed(mx);
        if (draggingWheel) updateWheel(mx, my);
        if (draggingBri)   updateBri(my);
        super.mouseClickMove(mx, my, btn, time);
    }

    private void updateSpeed(int mx) {
        float n = Math.max(0, Math.min(1f, (float)(mx-speedX)/SPEED_W));
        colorChanger.setRainbowSpeed(1 + n*19);
    }
    private void updateWheel(int mx, int my) {
        int dx=mx-wheelCX, dy=my-wheelCY;
        selHue = (float)((Math.atan2(dy,dx)/(2*Math.PI)+1.0)%1.0);
        selSat = Math.min(1f,(float)(Math.sqrt(dx*dx+dy*dy)/WHEEL_R));
        colorChanger.setStaticColor(Color.HSBtoRGB(selHue,selSat,selBri));
    }
    private void updateBri(int my) {
        selBri = 1f-Math.max(0,Math.min(1f,(float)(my-briY)/BRI_H));
        colorChanger.setStaticColor(Color.HSBtoRGB(selHue,selSat,selBri));
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}