package com.amethyst.client;

import com.amethyst.client.modules.ClickGUI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;

public class ClickGUIPickerGUI extends GuiScreen {

    private static final int PANEL_W = 280;
    private static final int PANEL_H = 290;

    private final GuiScreen parent;
    private final ClickGUI  mod;

    private boolean draggingSpeed = false;

    // preview
    private float previewAnim = 0f;
    private boolean previewDir = true;

    public ClickGUIPickerGUI(GuiScreen parent, ClickGUI mod) {
        this.parent = parent;
        this.mod    = mod;
    }

    // ── Layout constants ──────────────────────────────────────────────────────
    // All Y coords are stored relative to panelY at layout time.
    // We compute them ONCE per frame, both for drawing and for hit-testing.

    private int panelX, panelY;

    // Absolute Y positions of interactive elements (set during layout)
    private int[] animBtnY   = new int[1]; // AnimType row Y
    private int[] styleBtnY  = new int[1]; // Style row Y
    private int   sliderY;
    private int   toggle1Y;
    private int   toggle2Y;
    private int   backY;

    // widths computed once
    private int animBtnW, styleBtnW;

    private void computeLayout() {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth(), H = sr.getScaledHeight();
        panelX = W / 2 - PANEL_W / 2;
        panelY = H / 2 - PANEL_H / 2;

        ClickGUI.AnimType[] animTypes = ClickGUI.AnimType.values();
        ClickGUI.GUIStyle[] styles    = ClickGUI.GUIStyle.values();
        animBtnW  = (PANEL_W - 32 - (animTypes.length - 1) * 4) / animTypes.length;
        styleBtnW = (PANEL_W - 32 - (styles.length    - 1) * 4) / styles.length;

        int cy = panelY + 12;   // title
        cy += 16;               // divider
        cy += 8;                // gap after divider → cy = panelY+36

        // Section: Animation Type
        cy += 10;               // section label height
        animBtnY[0] = cy;       // ← buttons drawn here
        cy += 16;               // button height
        cy += 8;                // gap below buttons → total cy += 22 from section start

        // Section: GUI Style
        cy += 10;               // label
        styleBtnY[0] = cy;
        cy += 16;
        cy += 8;

        // Section: Speed slider
        cy += 10;               // label
        sliderY = cy;
        cy += 5;                // track height
        cy += 14;               // gap

        // Section: Extras
        cy += 10;               // label
        toggle1Y = cy;
        cy += 15;
        cy += 2;
        toggle2Y = cy;

        // Back button
        backY = panelY + PANEL_H - 22;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public void updateScreen() {
        float speed = mod.getAnimSpeed() * 1.5f;
        if (previewDir) {
            previewAnim = Math.min(1f, previewAnim + speed);
            if (previewAnim >= 1f) previewDir = false;
        } else {
            previewAnim = Math.max(0f, previewAnim - speed * 0.7f);
            if (previewAnim <= 0f) previewDir = true;
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth(), H = sr.getScaledHeight();
        drawGradientRect(0, 0, W, H, 0xEE020409, 0xEE010208);

        computeLayout();
        int px = panelX, py = panelY;

        drawPanel(px, py, PANEL_W, PANEL_H);

        // Title
        String title = "✦  ClickGUI Settings";
        mc.fontRendererObj.drawStringWithShadow(title,
            px + PANEL_W/2 - mc.fontRendererObj.getStringWidth(title)/2,
            py + 12, 0xFF99CCFF);
        drawRect(px + 16, py + 28, px + PANEL_W - 16, py + 29, 0x33FFFFFF);

        // Animation Type section
        drawSectionLabel("Animation Type", px + 16, animBtnY[0] - 10);
        ClickGUI.AnimType[] animTypes = ClickGUI.AnimType.values();
        for (int i = 0; i < animTypes.length; i++) {
            int bx = px + 16 + i * (animBtnW + 4);
            boolean sel = mod.getAnimType() == animTypes[i];
            boolean hov = mx >= bx && mx < bx + animBtnW
                       && my >= animBtnY[0] && my < animBtnY[0] + 16;
            drawOptionBtn(bx, animBtnY[0], animBtnW, 16, animTypes[i].label, sel, hov);
        }

        // GUI Style section
        drawSectionLabel("GUI Style", px + 16, styleBtnY[0] - 10);
        ClickGUI.GUIStyle[] styles = ClickGUI.GUIStyle.values();
        for (int i = 0; i < styles.length; i++) {
            int bx = px + 16 + i * (styleBtnW + 4);
            boolean sel = mod.getGUIStyle() == styles[i];
            boolean hov = mx >= bx && mx < bx + styleBtnW
                       && my >= styleBtnY[0] && my < styleBtnY[0] + 16;
            drawOptionBtn(bx, styleBtnY[0], styleBtnW, 16, styles[i].label, sel, hov);
        }

        // Speed slider section
        drawSectionLabel("Animation Speed", px + 16, sliderY - 10);
        float norm = (mod.getAnimSpeed() - 0.05f) / 0.35f;
        drawSlider(px + 16, sliderY, PANEL_W - 32, norm,
            String.format("%.0f%%", norm * 100f));

        // Extras section
        drawSectionLabel("Extras", px + 16, toggle1Y - 10);
        drawToggleRow(mx, my, px + 16, toggle1Y, PANEL_W - 32, "Background Blur", mod.isBlur());
        drawToggleRow(mx, my, px + 16, toggle2Y, PANEL_W - 32, "Open Particles",  mod.isParticles());

        // Preview
        drawPreview(px + PANEL_W - 78, py + PANEL_H - 70, previewAnim);

        // Back button
        boolean backHov = mx >= px + 16 && mx < px + 88 && my >= backY && my < backY + 14;
        drawRect(px + 16, backY, px + 88, backY + 14, backHov ? 0xFF0E1825 : 0xFF080E18);
        drawHollowRect(px + 16, backY, px + 88, backY + 14, 0xFF1A3045);
        String back = "◀ Back";
        mc.fontRendererObj.drawStringWithShadow(back,
            px + 16 + 36 - mc.fontRendererObj.getStringWidth(back) / 2,
            backY + 3, 0xFF5599BB);

        super.drawScreen(mx, my, pt);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void drawPanel(int x, int y, int w, int h) {
        drawRect(x+3, y,   x+w-3, y+h,   0xF20A0D18);
        drawRect(x,   y+3, x+w,   y+h-3, 0xF20A0D18);
        drawHollowRect(x+1, y+1, x+w-1, y+h-1, 0x22FFFFFF);
        drawHollowRect(x,   y,   x+w,   y+h,   0x11FFFFFF);
    }

    private void drawSectionLabel(String label, int x, int y) {
        mc.fontRendererObj.drawString("§8" + label, x, y, 0xFF445566);
    }

    private void drawOptionBtn(int x, int y, int w, int h, String label, boolean sel, boolean hov) {
        int bg  = sel ? 0xFF112244 : (hov ? 0xFF0D1A2E : 0xFF090D14);
        int brd = sel ? 0xFF3366BB : (hov ? 0xFF1A3355 : 0xFF1A2233);
        int txt = sel ? 0xFFAADDFF : (hov ? 0xFF8899BB : 0xFF445566);
        drawRect(x, y, x+w, y+h, bg);
        drawHollowRect(x, y, x+w, y+h, brd);
        mc.fontRendererObj.drawString(label,
            x + w/2 - mc.fontRendererObj.getStringWidth(label)/2, y+4, txt);
    }

    private void drawSlider(int x, int y, int w, float norm, String display) {
        drawRect(x, y, x+w, y+5, 0xFF0D1525);
        int filled = (int)(norm * w);
        drawRect(x, y, x+filled, y+5, 0xFF3366AA);
        int hx = x + filled;
        drawRect(hx-3, y-2, hx+3, y+7, 0xFFCCDDEE);
        drawRect(hx-2, y-1, hx+2, y+6, 0xFF090E18);
        mc.fontRendererObj.drawString(display, x+w+6, y, 0xFF6699BB);
    }

    private void drawToggleRow(int mx, int my, int x, int y, int w, String label, boolean state) {
        mc.fontRendererObj.drawString(label, x, y+2, state ? 0xFFCCDDEE : 0xFF445566);
        int bx = x + w - 42, bw = 38, bh = 13;
        boolean hov = mx >= bx && mx < bx+bw && my >= y && my < y+bh;
        drawRect(bx, y, bx+bw, y+bh, state ? 0xFF0A2E18 : 0xFF1A0A0A);
        drawHollowRect(bx, y, bx+bw, y+bh, state ? 0xFF1E6B38 : 0xFF552222);
        String txt = state ? "ON" : "OFF";
        mc.fontRendererObj.drawString(txt,
            bx + bw/2 - mc.fontRendererObj.getStringWidth(txt)/2, y+2,
            state ? 0xFF55FF77 : 0xFFFF5544);
    }

    private void drawPreview(int x, int y, float anim) {
        int pw = 64, ph = 44;
        mc.fontRendererObj.drawString("§8Preview", x, y-10, 0xFF334455);

        float scale = 1f, alpha = anim;
        float offY = 0f;

        switch (mod.getAnimType()) {
            case ZOOM:
                scale = 0.55f + anim * 0.45f;
                break;
            case SLIDE_DOWN:
                offY  = (1f - anim) * -22f;
                break;
            case SLIDE_UP:
                offY  = (1f - anim) * 22f;
                break;
            case FADE:
                // pure alpha, scale 1
                break;
            case BOUNCE:
                // cubic ease-out overshoot
                float t = anim;
                float s = t < 0.5f
                    ? 4*t*t*t
                    : 1f - (float)Math.pow(-2*t+2, 3)/2f;
                scale = 0.65f + s * 0.35f;
                // tiny overshoot past 1 at peak
                if (anim > 0.85f) scale = 1f + (float)Math.sin((anim-0.85f)/(0.15f)*Math.PI) * 0.06f;
                break;
        }

        int cx = x + pw/2, cy2 = (int)(y + ph/2 + offY);
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(cx, cy2, 0);
        net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1f);
        net.minecraft.client.renderer.GlStateManager.translate(-pw/2f, -ph/2f, 0);

        int a  = (int)(alpha * 210);
        int ta = (int)(alpha * 255);
        drawRect(0, 0, pw, ph, (a << 24) | 0x0A0D18);
        drawHollowRect(0, 0, pw, ph, (a << 24) | 0x334466);
        drawRect(0, 0, pw, 3, (a << 24) | 0x3366BB);
        mc.fontRendererObj.drawString("GUI",
            pw/2 - mc.fontRendererObj.getStringWidth("GUI")/2, 5, (ta << 24) | 0xAABBCC);
        drawRect(5, 14, pw-5, 15, (ta << 24) | 0x223344);
        drawRect(5, 20, pw-16, 21, (ta << 24) | 0x1A2A3A);
        drawRect(5, 26, pw-10, 27, (ta << 24) | 0x223344);
        drawRect(5, 32, pw-20, 33, (ta << 24) | 0x1A2A3A);
        drawRect(5, 38, pw-8,  39, (ta << 24) | 0x223344);

        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }

    private void drawHollowRect(int l, int t, int r, int b, int c) {
        drawRect(l, t, r, t+1, c); drawRect(l, b-1, r, b, c);
        drawRect(l, t, l+1, b, c); drawRect(r-1, t, r, b, c);
    }

    // ── Mouse input ───────────────────────────────────────────────────────────

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        // Layout must be computed before hit-testing
        computeLayout();
        int px = panelX;

        // Back button
        if (mx >= px+16 && mx < px+88 && my >= backY && my < backY+14) {
            mc.displayGuiScreen(parent);
            return;
        }

        // Animation Type
        ClickGUI.AnimType[] animTypes = ClickGUI.AnimType.values();
        for (int i = 0; i < animTypes.length; i++) {
            int bx = px + 16 + i * (animBtnW + 4);
            if (mx >= bx && mx < bx + animBtnW
             && my >= animBtnY[0] && my < animBtnY[0] + 16) {
                mod.setAnimType(animTypes[i]);
                return;
            }
        }

        // GUI Style
        ClickGUI.GUIStyle[] styles = ClickGUI.GUIStyle.values();
        for (int i = 0; i < styles.length; i++) {
            int bx = px + 16 + i * (styleBtnW + 4);
            if (mx >= bx && mx < bx + styleBtnW
             && my >= styleBtnY[0] && my < styleBtnY[0] + 16) {
                mod.setGUIStyle(styles[i]);
                return;
            }
        }

        // Speed slider
        int sx = px + 16, sw = PANEL_W - 32;
        if (mx >= sx && mx <= sx+sw && my >= sliderY-3 && my <= sliderY+8) {
            draggingSpeed = true;
            updateSpeed(mx, sx, sw);
            return;
        }

        // Extras toggles
        int tx = px + 16, tw = PANEL_W - 32;
        int togBx = tx + tw - 42;
        if (mx >= togBx && mx < togBx+38 && my >= toggle1Y && my < toggle1Y+13) {
            mod.setBlur(!mod.isBlur());
            return;
        }
        if (mx >= togBx && mx < togBx+38 && my >= toggle2Y && my < toggle2Y+13) {
            mod.setParticles(!mod.isParticles());
            return;
        }

        super.mouseClicked(mx, my, btn);
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        draggingSpeed = false;
        super.mouseReleased(mx, my, state);
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        if (draggingSpeed) {
            computeLayout();
            updateSpeed(mx, panelX + 16, PANEL_W - 32);
        }
        super.mouseClickMove(mx, my, btn, time);
    }

    private void updateSpeed(int mx, int sx, int sw) {
        float n = Math.max(0f, Math.min(1f, (float)(mx - sx) / sw));
        mod.setAnimSpeed(0.05f + n * 0.35f);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
