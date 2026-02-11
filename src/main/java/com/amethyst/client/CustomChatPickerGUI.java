package com.amethyst.client;

import com.amethyst.client.modules.CustomChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.io.IOException;

public class CustomChatPickerGUI extends GuiScreen {

    private final GuiScreen parent;
    private final CustomChat mod;

    private boolean draggingBg    = false;
    private boolean draggingScale = false;
    private boolean draggingMsgs  = false;

    private static final int PANEL_W = 290;
    private static final int PANEL_H = 300;

    public CustomChatPickerGUI(GuiScreen parent, CustomChat mod) {
        this.parent = parent;
        this.mod    = mod;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth(), H = sr.getScaledHeight();
        int px = W / 2 - PANEL_W / 2;
        int py = H / 2 - PANEL_H / 2;

        drawGradientRect(0, 0, W, H, 0xBB000000, 0xBB020210);
        drawPanel(px, py, PANEL_W, PANEL_H);

        String title = "Chat Settings";
        int tw = mc.fontRendererObj.getStringWidth(title);
        mc.fontRendererObj.drawStringWithShadow(title, px + PANEL_W / 2 - tw / 2, py + 10, 0xFFAADDFF);
        drawGradientRect(px + 15, py + 22, px + PANEL_W / 2, py + 23, 0x00FFFFFF, 0xAA55BBFF);
        drawGradientRect(px + PANEL_W / 2, py + 22, px + PANEL_W - 15, py + 23, 0xAA55BBFF, 0x00FFFFFF);

        int iy = py + 32;

        // Toggles
        iy = drawToggle(mx, my, px + 15, iy, "Show background",   mod.isShowBackground());  iy += 6;
        iy = drawToggle(mx, my, px + 15, iy, "Fade-in animation", mod.isFadeMessages());    iy += 6;
        iy = drawToggle(mx, my, px + 15, iy, "Show timestamps",   mod.isShowTimestamps());  iy += 10;

        // Sliders
        float bgNorm    = mod.getBgAlpha();
        float scaleNorm = (mod.getScale() - 0.5f) / 1.5f;
        float msgsNorm  = (mod.getMaxMessages() - 3f) / 17f;

        iy = drawSlider(mx, my, px + 15, iy, PANEL_W - 30,
                "Background opacity", bgNorm, draggingBg, 0xFF55BBFF); iy += 10;
        iy = drawSlider(mx, my, px + 15, iy, PANEL_W - 30,
                "Scale  " + String.format("%.1fx", mod.getScale()),
                scaleNorm, draggingScale, 0xFF99DDFF); iy += 10;
        iy = drawSlider(mx, my, px + 15, iy, PANEL_W - 30,
                "Visible lines: " + mod.getMaxMessages(),
                msgsNorm, draggingMsgs, 0xFF77CCAA); iy += 10;

        // Preview
        drawChatPreview(px + 15, iy, PANEL_W - 30);

        drawBackButton(mx, my, px, py);

        super.drawScreen(mx, my, pt);
    }

    private int drawToggle(int mx, int my, int x, int y, String label, boolean state) {
        int toggleW = 32, toggleH = 14;
        int tx = x + 170;
        mc.fontRendererObj.drawString(label, x, y + 3, 0xFFCCDDEE);
        drawRoundRect(tx, y, toggleW, toggleH, state ? 0xFF1E4A6E : 0xFF1A1A2E);
        int knobX = state ? tx + toggleW - 12 : tx + 2;
        drawCircleFill(knobX + 5, y + 7, 5, state ? 0xFF55AAFF : 0xFF555566);
        mc.fontRendererObj.drawString(state ? "ON" : "OFF", tx + toggleW + 4, y + 3,
                state ? 0xFF55AAFF : 0xFF555566);
        return y + toggleH + 2;
    }

    private int drawSlider(int mx, int my, int x, int y, int w,
                            String label, float value, boolean dragging, int accent) {
        int sliderH = 12;
        mc.fontRendererObj.drawString(label, x, y, 0xFF99AABB);
        y += 11;
        drawRoundRect(x, y, w, sliderH, 0xFF1A1A2A);
        int fillW = (int)(value * w);
        if (fillW > 0) drawRoundRect(x, y, fillW, sliderH, accent & 0x00FFFFFF | 0x66000000);
        int hx = x + fillW;
        drawCircleFill(hx, y + sliderH / 2, 7, dragging ? 0xFFFFFFFF : accent);
        drawCircleFill(hx, y + sliderH / 2, 4, 0xFF080810);
        return y + sliderH + 2;
    }

    private void drawChatPreview(int x, int y, int w) {
        mc.fontRendererObj.drawString("§8Preview:", x, y, 0xFF445566);
        y += 10;

        String[] msgs = {
            "§7Player1§f: hello!",
            "§a[Server]§f Welcome!",
            "§c<Player2>§f gg"
        };

        for (int i = msgs.length - 1; i >= 0; i--) {
            // Fade-in эффект в превью
            float alpha = mod.isFadeMessages() ? 0.5f + 0.5f * i / msgs.length : 1.0f;
            if (mod.isShowBackground()) {
                int bgA = (int)(mod.getBgAlpha() * alpha * 180);
                if (bgA > 0) drawRect(x - 1, y + (msgs.length - 1 - i) * 10 - 1,
                        x + w, y + (msgs.length - 1 - i) * 10 + 9, (bgA << 24) | 0x000000);
            }
            int textA = (int)(alpha * 200);
            mc.fontRendererObj.drawString(msgs[i], x, y + (msgs.length - 1 - i) * 10,
                    (textA << 24) | 0xFFFFFF);
        }
    }

    private void drawBackButton(int mx, int my, int px, int py) {
        int bx = px + 10, by = py + PANEL_H - 26, bw = 80, bh = 16;
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        drawRoundRect(bx, by, bw, bh, hov ? 0xFF0D1A28 : 0xFF08101E);
        drawHollowRect(bx, by, bx + bw, by + bh, 0xFF1A3A5A);
        String t = "◀ Back";
        mc.fontRendererObj.drawStringWithShadow(t,
                bx + bw / 2 - mc.fontRendererObj.getStringWidth(t) / 2, by + 4, 0xFF5599BB);
    }

    private void drawPanel(int x, int y, int w, int h) {
        drawGradientRect(x + 2, y, x + w - 2, y + h, 0xEE080C1A, 0xEE040810);
        drawGradientRect(x, y + 2, x + w, y + h - 2, 0xEE080C1A, 0xEE040810);
        drawHollowRect(x + 1, y + 1, x + w - 1, y + h - 1, 0x3355AAFF);
        drawHollowRect(x + 2, y + 2, x + w - 2, y + h - 2, 0x1533AAFF);
    }

    private void drawRoundRect(int x, int y, int w, int h, int color) {
        drawRect(x + 2, y, x + w - 2, y + h, color);
        drawRect(x, y + 2, x + w, y + h - 2, color);
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
        if (mx >= px+10 && mx <= px+90 && my >= py+PANEL_H-26 && my <= py+PANEL_H-10) {
            mc.displayGuiScreen(parent); return;
        }

        int iy = py + 32;
        int toggleX = px + 15 + 170;

        // Toggle: show background
        if (inToggle(mx, my, toggleX, iy)) { mod.setShowBackground(!mod.isShowBackground()); return; }
        iy += 22;
        // Toggle: fade
        if (inToggle(mx, my, toggleX, iy)) { mod.setFadeMessages(!mod.isFadeMessages()); return; }
        iy += 22;
        // Toggle: timestamps
        if (inToggle(mx, my, toggleX, iy)) { mod.setShowTimestamps(!mod.isShowTimestamps()); return; }
        iy += 20;

        int sliderX = px + 15, sliderW = PANEL_W - 30;
        // BG alpha
        iy += 11;
        if (inSlider(mx, my, sliderX, iy, sliderW)) { draggingBg = true; mod.setBgAlpha((float)(mx-sliderX)/sliderW); return; }
        iy += 22;
        // Scale
        iy += 11;
        if (inSlider(mx, my, sliderX, iy, sliderW)) { draggingScale = true; mod.setScale(0.5f+(float)(mx-sliderX)/sliderW*1.5f); return; }
        iy += 22;
        // Max msgs
        iy += 11;
        if (inSlider(mx, my, sliderX, iy, sliderW)) { draggingMsgs = true; mod.setMaxMessages(3+(int)((float)(mx-sliderX)/sliderW*17)); return; }

        super.mouseClicked(mx, my, btn);
    }

    private boolean inToggle(int mx, int my, int tx, int ty) {
        return mx >= tx && mx <= tx + 32 && my >= ty && my <= ty + 14;
    }
    private boolean inSlider(int mx, int my, int sx, int sy, int sw) {
        return mx >= sx && mx <= sx + sw && my >= sy && my <= sy + 12;
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        draggingBg = false; draggingScale = false; draggingMsgs = false;
        super.mouseReleased(mx, my, state);
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        ScaledResolution sr = new ScaledResolution(mc);
        int px = sr.getScaledWidth() / 2 - PANEL_W / 2;
        int sx = px + 15, sw = PANEL_W - 30;
        float n = Math.max(0f, Math.min(1f, (float)(mx - sx) / sw));
        if (draggingBg)    mod.setBgAlpha(n);
        if (draggingScale) mod.setScale(0.5f + n * 1.5f);
        if (draggingMsgs)  mod.setMaxMessages(3 + (int)(n * 17));
        super.mouseClickMove(mx, my, btn, time);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}