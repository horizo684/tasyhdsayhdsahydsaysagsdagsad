package com.amethyst.client;

import com.amethyst.client.modules.CustomChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class CustomChatPickerGUI extends GuiScreen {

    private final GuiScreen parent;
    private final CustomChat chat;

    // Slider states
    private boolean draggingBg = false;
    private boolean draggingLines = false;
    private boolean draggingWidth = false;
    private boolean draggingOpacity = false;
    private boolean draggingScale = false;

    public CustomChatPickerGUI(GuiScreen parent, CustomChat chat) {
        this.parent = parent;
        this.chat = chat;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();

        drawGradientRect(0, 0, w, h, 0xF2050515, 0xF2010008);

        String title = "Chat Settings";
        mc.fontRendererObj.drawStringWithShadow(title,
            w/2 - mc.fontRendererObj.getStringWidth(title)/2, 8, 0xFFAADDFF);

        int cy = 30;
        int gap = 28;

        // ── Toggles ───────────────────────────────────────────────────────────
        cy = drawToggle(mx, my, "Show Background", chat.isShowBackground(), 
            w/2 - 90, cy, () -> chat.setShowBackground(!chat.isShowBackground()));
        cy += gap;

        cy = drawToggle(mx, my, "Fade-in Animation", chat.isFadeMessages(),
            w/2 - 90, cy, () -> chat.setFadeMessages(!chat.isFadeMessages()));
        cy += gap;

        cy = drawToggle(mx, my, "Show Timestamps", chat.isShowTimestamps(),
            w/2 - 90, cy, () -> chat.setShowTimestamps(!chat.isShowTimestamps()));
        cy += gap + 5;

        // ── Sliders ───────────────────────────────────────────────────────────
        cy = drawSlider(mx, my, "Background Opacity", chat.getBgAlpha(), 0f, 1f,
            w/2 - 90, cy, v -> chat.setBgAlpha(v), draggingBg,
            String.format("%.0f%%", chat.getBgAlpha() * 100));
        cy += gap;

        cy = drawSlider(mx, my, "Chat Width", chat.getChatWidth(), 0.5f, 2.0f,
            w/2 - 90, cy, v -> chat.setChatWidth(v), draggingWidth,
            String.format("%.0f%%", chat.getChatWidth() * 100));
        cy += gap;

        cy = drawSlider(mx, my, "Text Opacity", chat.getChatOpacity(), 0f, 1f,
            w/2 - 90, cy, v -> chat.setChatOpacity(v), draggingOpacity,
            String.format("%.0f%%", chat.getChatOpacity() * 100));
        cy += gap;

        cy = drawSlider(mx, my, "Chat Scale", chat.getChatScale(), 0.5f, 2.0f,
            w/2 - 90, cy, v -> chat.setChatScale(v), draggingScale,
            String.format("%.1fx", chat.getChatScale()));
        cy += gap;

        cy = drawSlider(mx, my, "Visible Lines", chat.getMaxMessages() / 20f, 0f, 1f,
            w/2 - 90, cy, v -> chat.setMaxMessages((int)(v * 20) + 3), draggingLines,
            String.valueOf(chat.getMaxMessages()));
        cy += gap + 10;

        // ── Preview ───────────────────────────────────────────────────────────
        drawPreview(w/2 - 100, cy);

        // ── Back ──────────────────────────────────────────────────────────────
        drawBack(mx, my, h);

        super.drawScreen(mx, my, pt);
    }

    private int drawToggle(int mx, int my, String label, boolean state, int x, int y, Runnable onClick) {
        mc.fontRendererObj.drawString("§8" + label, x, y, 0xFF445566);

        int bx = x + 140, by = y - 2, bw = 40, bh = 14;
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        int bgCol = state ? 0xFF1A4D2E : 0xFF331111;
        int borderCol = state ? 0xFF2E8B57 : 0xFF663333;

        drawRect(bx, by, bx + bw, by + bh, hov ? (bgCol | 0x22000000) : bgCol);
        drawHollowRect(bx, by, bx + bw, by + bh, borderCol);

        String txt = state ? "ON" : "OFF";
        mc.fontRendererObj.drawString(txt, bx + bw/2 - mc.fontRendererObj.getStringWidth(txt)/2,
            by + 3, state ? 0xFF77FF77 : 0xFFFF7777);

        return y + 10;
    }

    private int drawSlider(int mx, int my, String label, float value, float min, float max,
                          int x, int y, ValueSetter setter, boolean dragging, String display) {
        mc.fontRendererObj.drawString("§8" + label, x, y, 0xFF445566);

        int sx = x, sy = y + 10, sw = 180, sh = 6;
        drawRect(sx, sy, sx + sw, sy + sh, 0xFF1A2A3A);

        float norm = (value - min) / (max - min);
        int hx = (int)(sx + norm * sw);
        drawRect(sx, sy, hx, sy + sh, 0xFF3366AA);

        drawRect(hx - 3, sy - 2, hx + 3, sy + sh + 2, 0xFFFFFFFF);
        drawRect(hx - 2, sy - 1, hx + 2, sy + sh + 1, 0xFF0A1020);

        mc.fontRendererObj.drawString(display, sx + sw + 6, y + 8, 0xFF88AACC);

        return y + 20;
    }

    private void drawPreview(int x, int y) {
        mc.fontRendererObj.drawString("§8Preview:", x, y, 0xFF334455);
        int py = y + 12;
        int pw = (int)(320 * chat.getChatWidth() * chat.getChatScale());

        if (chat.isShowBackground()) {
            int bgAlpha = (int)(chat.getBgAlpha() * chat.getChatOpacity() * 180) << 24;
            drawRect(x, py, x + pw, py + 27, bgAlpha);
        }

        int textAlpha = (int)(chat.getChatOpacity() * 255) << 24;
        mc.fontRendererObj.drawStringWithShadow("§7Player: hello!", x + 2, py + 2, textAlpha | 0xCCCCCC);
        mc.fontRendererObj.drawStringWithShadow("§a[Server]§f Welcome!", x + 2, py + 11, textAlpha | 0xCCCCCC);
        mc.fontRendererObj.drawStringWithShadow("§cAdmin§f: test", x + 2, py + 20, textAlpha | 0xCCCCCC);
    }

    private void drawBack(int mx, int my, int h) {
        int bx=10, by=h-22, bw=70, bh=16;
        boolean hov = mx>=bx && mx<=bx+bw && my>=by && my<=by+bh;
        drawRect(bx, by, bx+bw, by+bh, hov ? 0xFF0E1825 : 0xFF080E18);
        drawHollowRect(bx, by, bx+bw, by+bh, 0xFF1A3045);
        String t = "◀ Back";
        mc.fontRendererObj.drawStringWithShadow(t, bx+bw/2-mc.fontRendererObj.getStringWidth(t)/2, by+4, 0xFF5599BB);
    }

    private void drawHollowRect(int l,int t,int r,int b,int c){
        drawRect(l,t,r,t+1,c);drawRect(l,b-1,r,b,c);
        drawRect(l,t,l+1,b,c);drawRect(r-1,t,r,b,c);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();

        if (mx>=10 && mx<=80 && my>=h-22 && my<=h-6) { mc.displayGuiScreen(parent); return; }

        int x = w/2 - 90;
        int cy = 30;
        int gap = 28;

        // Toggles - drawToggle повертає y+10, потім додається gap
        if (clickToggle(mx, my, x + 140, cy - 2)) { chat.setShowBackground(!chat.isShowBackground()); return; }
        cy += 10; // як повертає drawToggle
        cy += gap;
        
        if (clickToggle(mx, my, x + 140, cy - 2)) { chat.setFadeMessages(!chat.isFadeMessages()); return; }
        cy += 10;
        cy += gap;
        
        if (clickToggle(mx, my, x + 140, cy - 2)) { chat.setShowTimestamps(!chat.isShowTimestamps()); return; }
        cy += 10;
        cy += gap + 5;

        // Sliders - drawSlider повертає y+20, потім додається gap
        if (clickSlider(mx, my, x, cy + 10)) { draggingBg = true; updateBg(mx, x); return; }
        cy += 20; // як повертає drawSlider
        cy += gap;
        
        if (clickSlider(mx, my, x, cy + 10)) { draggingWidth = true; updateWidth(mx, x); return; }
        cy += 20;
        cy += gap;
        
        if (clickSlider(mx, my, x, cy + 10)) { draggingOpacity = true; updateOpacity(mx, x); return; }
        cy += 20;
        cy += gap;
        
        if (clickSlider(mx, my, x, cy + 10)) { draggingScale = true; updateScale(mx, x); return; }
        cy += 20;
        cy += gap;
        
        if (clickSlider(mx, my, x, cy + 10)) { draggingLines = true; updateLines(mx, x); return; }

        super.mouseClicked(mx, my, btn);
    }

    private boolean clickToggle(int mx, int my, int x, int y) {
        return mx >= x && mx <= x + 40 && my >= y && my <= y + 14;
    }

    private boolean clickSlider(int mx, int my, int x, int y) {
        return mx >= x && mx <= x + 180 && my >= y - 3 && my <= y + 9;
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        draggingBg = draggingLines = draggingWidth = draggingOpacity = draggingScale = false;
        super.mouseReleased(mx, my, state);
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        int x = new ScaledResolution(mc).getScaledWidth()/2 - 90;
        if (draggingBg) updateBg(mx, x);
        if (draggingWidth) updateWidth(mx, x);
        if (draggingOpacity) updateOpacity(mx, x);
        if (draggingScale) updateScale(mx, x);
        if (draggingLines) updateLines(mx, x);
        super.mouseClickMove(mx, my, btn, time);
    }

    private void updateBg(int mx, int x) {
        float n = Math.max(0, Math.min(1f, (float)(mx-x)/180));
        chat.setBgAlpha(n);
    }

    private void updateWidth(int mx, int x) {
        float n = Math.max(0, Math.min(1f, (float)(mx-x)/180));
        chat.setChatWidth(0.5f + n * 1.5f);
    }

    private void updateOpacity(int mx, int x) {
        float n = Math.max(0, Math.min(1f, (float)(mx-x)/180));
        chat.setChatOpacity(n);
    }

    private void updateScale(int mx, int x) {
        float n = Math.max(0, Math.min(1f, (float)(mx-x)/180));
        chat.setChatScale(0.5f + n * 1.5f);
    }

    private void updateLines(int mx, int x) {
        float n = Math.max(0, Math.min(1f, (float)(mx-x)/180));
        chat.setMaxMessages((int)(n * 17) + 3);
    }

    @FunctionalInterface
    private interface ValueSetter {
        void set(float value);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}