package com.amethyst.client;

import com.amethyst.client.modules.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModernClickGUI extends GuiScreen {

    private static final int SIDEBAR_W  = 110;
    private static final int CARD_W     = 130;
    private static final int CARD_H     = 72;
    private static final int CARD_GAP   = 8;
    private static final int GRID_PAD_X = 14;
    private static final int HEADER_H   = 32;

    private int selectedCategory = 0;
    private int scrollOffset     = 0;
    private int maxScroll        = 0;
    private int hoveredCard      = -1;

    // ── Animation ─────────────────────────────────────────────────────────────
    // Single linear progress 0→1 (opening) then 1→0 (closing)
    // Easing is applied at render time, not stored.
    private float progress  = 0f;   // raw linear [0,1]
    private boolean closing = false;

    // ─────────────────────────────────────────────────────────────────────────

    private List<ModuleCategory> categories = new ArrayList<>();

    private static class ModuleCategory {
        final String icon, name;
        final List<Module> modules = new ArrayList<>();
        final boolean isHome;
        ModuleCategory(String icon, String name) { 
            this.icon = icon; 
            this.name = name; 
            this.isHome = false;
        }
        ModuleCategory(String icon, String name, boolean isHome) { 
            this.icon = icon; 
            this.name = name; 
            this.isHome = isHome;
        }
    }

    private static final int[]    CAT_ACCENT = { 0xFF7B68EE, 0xFF9966FF, 0xFFFF4455, 0xFF44AAFF, 0xFFFFAA33, 0xFF44FF99 };
    private static final String[] CAT_ICONS  = { "◆", "◈", "⚔", "✦", "⚙", "▣" };
    
    private String searchQuery = "";
    private boolean searchFocused = false;

    @Override
    public void initGui() {
        buildCategories();
        scrollOffset = 0;
        progress = 0f;
        closing  = false;
    }

    private void buildCategories() {
        categories.clear();
        ModuleCategory home   = new ModuleCategory(CAT_ICONS[0], "HOME", true);
        ModuleCategory all    = new ModuleCategory(CAT_ICONS[1], "ALL");
        ModuleCategory combat = new ModuleCategory(CAT_ICONS[2], "COMBAT");
        ModuleCategory visual = new ModuleCategory(CAT_ICONS[3], "VISUAL");
        ModuleCategory misc   = new ModuleCategory(CAT_ICONS[4], "MISC");
        ModuleCategory hud    = new ModuleCategory(CAT_ICONS[5], "HUD");

        for (Module m : AmethystClient.moduleManager.getModules()) {
            if      (m instanceof HitDelayFix || m instanceof AutoSoup || m instanceof Refill)
                combat.modules.add(m);
            else if (m instanceof ModuleList || m instanceof ColorChanger || m instanceof Nametag
                  || m instanceof Friends    || m instanceof FullBright || m instanceof Animations)
                visual.modules.add(m);
            else if (m instanceof NoJumpDelay || m instanceof CopyChat || m instanceof NoHurtCam
                  || m instanceof AutoSprint  || m instanceof AsyncScreenshot || m instanceof ClickGUI)
                misc.modules.add(m);
            else if (m instanceof SoupCounter || m instanceof FPSCounter || m instanceof PingCounter
                  || m instanceof Clock       || m instanceof CPSCounter  || m instanceof Saturation
                  || m instanceof Scoreboard  || m instanceof CustomChat)
                hud.modules.add(m);
            
            all.modules.add(m);
        }
        categories.add(home);   
        categories.add(all);
        categories.add(combat); 
        categories.add(visual); 
        categories.add(misc);   
        categories.add(hud);
    }

    // ── Easing functions ──────────────────────────────────────────────────────

    /** Cubic ease-out: fast start, smooth landing */
    private float easeOut(float t) {
        return 1f - (1f - t) * (1f - t) * (1f - t);
    }

    /** Cubic ease-in: slow start, fast end — for closing */
    private float easeIn(float t) {
        return t * t * t;
    }

    /** Bounce ease-out: overshoots slightly then settles */
    private float easeOutBounce(float t) {
        if (t < 0.5f) {
            float s = 2f * t;
            return 0.5f * (4*s*s*s);
        } else {
            float s = 2f * t - 1f;
            float cubic = 1f - (float)Math.pow(-2f*s+2f,3)/2f;
            // tiny extra overshoot
            return 0.5f + 0.5f * cubic + (float)Math.sin(t * Math.PI) * 0.025f;
        }
    }

    private ClickGUI getClickGUI() {
        return (ClickGUI) AmethystClient.moduleManager.getModuleByName("ClickGUI");
    }

    private ClickGUI.AnimType getAnimType() {
        ClickGUI cfg = getClickGUI();
        return cfg != null ? cfg.getAnimType() : ClickGUI.AnimType.ZOOM;
    }

    private float getSpeed() {
        ClickGUI cfg = getClickGUI();
        // speed in [0.05, 0.40] → per-tick delta in [0.022, 0.10]
        // We want very smooth open (~18 ticks) at default 0.17
        return cfg != null ? cfg.getAnimSpeed() * 0.55f : 0.094f;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public void updateScreen() {
        float spd = getSpeed();

        if (!closing) {
            progress = Math.min(1f, progress + spd);
        } else {
            // Close is 2.5x faster for snappy feel
            progress = Math.max(0f, progress - spd * 2.5f);
            if (progress <= 0f) {
                mc.displayGuiScreen(null);
            }
        }
    }

    // ── Compute transform for current anim type and progress ──────────────────
    // Returns float[3]: { scaleXY, alphaMultiplier, translateY }

    private float[] getTransform() {
        float p = closing ? easeIn(1f - progress) : easeOut(progress);
        // p goes 0→1 open, and for close ease we flip it so 1→0

        switch (getAnimType()) {
            case ZOOM:
                float scaleZ = 0.80f + (closing ? (1f - p) : p) * 0.20f;
                float alphaZ = closing ? (1f - p) : p;
                return new float[]{ scaleZ, alphaZ, 0f };

            case SLIDE_DOWN: {
                float ep = closing ? easeIn(progress) : easeOut(progress);
                // open: slide from above (-30) → 0
                // close: slide to below (+30)
                float offY = closing
                    ? ep * 30f
                    : (1f - easeOut(progress)) * -30f;
                float al = closing ? (1f - progress * 2f) : Math.min(1f, progress * 2f);
                return new float[]{ 1f, al, offY };
            }

            case SLIDE_UP: {
                float ep = closing ? easeIn(progress) : easeOut(progress);
                float offY = closing
                    ? ep * -30f
                    : (1f - easeOut(progress)) * 30f;
                float al = closing ? (1f - progress * 2f) : Math.min(1f, progress * 2f);
                return new float[]{ 1f, al, offY };
            }

            case FADE:
                float alphaF = closing ? easeIn(1f - progress) : easeOut(progress);
                return new float[]{ 1f, alphaF, 0f };

            case BOUNCE: {
                float raw = closing ? easeIn(1f - progress) : easeOutBounce(progress);
                float scaleB = 0.78f + Math.min(raw, 1.04f) * 0.22f;
                float alphaB = closing ? (1f - progress) : Math.min(1f, progress * 1.5f);
                return new float[]{ scaleB, alphaB, 0f };
            }

            default:
                return new float[]{ 1f, progress, 0f };
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth(), H = sr.getScaledHeight();

        float[] tr = getTransform();
        float scale  = tr[0];
        float alpha  = Math.max(0f, Math.min(1f, tr[1]));
        float transY = tr[2];

        // Dim background
        int bgA = (int)(alpha * 0xCC);
        drawGradientRect(0, 0, W, H, bgA << 24, (bgA << 24) | 0x050510);

        GlStateManager.pushMatrix();
        GlStateManager.translate(W / 2f, H / 2f + transY, 0);
        GlStateManager.scale(scale, scale, 1f);
        GlStateManager.translate(-W / 2f, -H / 2f, 0);
        GlStateManager.color(1f, 1f, 1f, alpha);

        int panelX = W / 2 - 230, panelY = H / 2 - 140;
        int panelW = 460,         panelH = 280;

        drawRoundedPanel(panelX, panelY, panelW, panelH);
        drawSidebar(panelX, panelY, panelH, mx, my);

        int contentX = panelX + SIDEBAR_W;
        int contentW = panelW - SIDEBAR_W;
        drawContentAreaClipped(contentX, panelY, contentW, panelH, mx, my);

        drawRect(panelX + SIDEBAR_W - 1, panelY + 8, panelX + SIDEBAR_W, panelY + panelH - 8, 0x22FFFFFF);

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();

        super.drawScreen(mx, my, pt);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private void drawSidebar(int px, int py, int pH, int mx, int my) {
        ColorChanger cc = getColorChanger();

        String logo = "AMETHYST";
        int lx = px + SIDEBAR_W / 2 - mc.fontRendererObj.getStringWidth(logo) / 2;
        for (int i = 0; i < logo.length(); i++) {
            int col = cc != null && cc.isEnabled()
                    ? ColorChanger.getPresetColor(cc.getPresetIndex(), i, logo.length(), cc.getRainbowSpeed())
                    : 0xFF9966FF;
            mc.fontRendererObj.drawStringWithShadow(String.valueOf(logo.charAt(i)), lx, py + 8, col);
            lx += mc.fontRendererObj.getCharWidth(logo.charAt(i));
        }
        mc.fontRendererObj.drawString("CLIENT",
                px + SIDEBAR_W / 2 - mc.fontRendererObj.getStringWidth("CLIENT") / 2, py + 18, 0x66FFFFFF);

        int accentCol = cc != null && cc.isEnabled()
                ? ColorChanger.getPresetColor(cc.getPresetIndex(), 0, 1, cc.getRainbowSpeed())
                : CAT_ACCENT[selectedCategory];
        drawGradientRect(px + 12, py + 27, px + SIDEBAR_W - 12, py + 28,
                accentCol & 0x00FFFFFF, accentCol);

        int btnY = py + 38, btnH = 22, btnGap = 3;
        for (int i = 0; i < categories.size(); i++) {
            ModuleCategory cat = categories.get(i);
            boolean sel = i == selectedCategory;
            boolean hov = mx >= px+6 && mx <= px+SIDEBAR_W-6 && my >= btnY && my <= btnY+btnH;
            int accent = CAT_ACCENT[i];

            if (sel) {
                drawRect(px+6, btnY, px+SIDEBAR_W-6, btnY+btnH, (accent & 0x00FFFFFF) | 0x30000000);
                drawRect(px+6, btnY, px+8, btnY+btnH, accent);
                drawRect(px+6, btnY, px+SIDEBAR_W-6, btnY+1, (accent & 0x00FFFFFF) | 0x88000000);
            } else if (hov) {
                drawRect(px+6, btnY, px+SIDEBAR_W-6, btnY+btnH, 0x18FFFFFF);
            }

            String label = cat.icon + " " + cat.name;
            int textCol = sel ? 0xFFFFFFFF : (hov ? 0xCCFFFFFF : 0x77AABBCC);
            mc.fontRendererObj.drawStringWithShadow(label, px+14, btnY+7, textCol);
            
            String count;
            if (!searchQuery.isEmpty() && !cat.isHome) {
                int filtered = getFilteredModules(cat).size();
                count = filtered + "/" + cat.modules.size();
            } else {
                count = String.valueOf(cat.modules.size());
            }
            mc.fontRendererObj.drawString(count,
                    px+SIDEBAR_W-16, btnY+7, sel ? accent : 0x44AAAAAA);

            btnY += btnH + btnGap;
        }

        mc.fontRendererObj.drawString("beta",
                px + SIDEBAR_W/2 - mc.fontRendererObj.getStringWidth("beta")/2,
                py + pH - 12, 0x33FFFFFF);
    }

    // ── Content area with GL scissor clipping ─────────────────────────────────

    private void drawContentAreaClipped(int cx, int cy, int cw, int ch, int mx, int my) {
        ModuleCategory cat = categories.get(selectedCategory);
        int accent = CAT_ACCENT[selectedCategory];
        ColorChanger cc = getColorChanger();

        // Special rendering for HOME category
        if (cat.isHome) {
            drawHomeCategory(cx, cy, cw, ch, accent, mx, my);
            return;
        }

        // Draw header for normal categories
        mc.fontRendererObj.drawStringWithShadow(cat.name, cx + GRID_PAD_X, cy + 10, accent);
        
        // Filter modules by search if needed
        List<Module> displayModules = getFilteredModules(cat);
        String moduleCountText = searchQuery.isEmpty() ? 
            displayModules.size() + " modules" : 
            displayModules.size() + " / " + cat.modules.size() + " modules";
        mc.fontRendererObj.drawString(moduleCountText, cx + GRID_PAD_X, cy + 21, 0x44AABBCC);

        int gridTop = cy + HEADER_H;
        int gridH   = ch - HEADER_H - 4;
        int cols    = Math.max(1, (cw - GRID_PAD_X*2 + CARD_GAP) / (CARD_W + CARD_GAP));

        ScaledResolution sr = new ScaledResolution(mc);
        int sf = sr.getScaleFactor();

        // GL scissor clips content — cards never overflow
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(cx * sf,
                       mc.displayHeight - (gridTop + gridH) * sf,
                       cw * sf,
                       gridH * sf);

        hoveredCard = -1;
        int row = 0, col = 0;

        for (int i = 0; i < displayModules.size(); i++) {
            Module m = displayModules.get(i);
            int cardX = cx + GRID_PAD_X + col * (CARD_W + CARD_GAP);
            int cardY = gridTop + row * (CARD_H + CARD_GAP) - scrollOffset;

            boolean hov = mx >= cardX && mx <= cardX + CARD_W
                       && my >= cardY && my <= cardY + CARD_H
                       && my >= gridTop && my <= gridTop + gridH;
            if (hov) hoveredCard = i;
            drawModuleCard(cardX, cardY, m, i, displayModules.size(), accent, cc, hov);

            col++;
            if (col >= cols) { col = 0; row++; }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        int totalRows = (displayModules.size() + cols - 1) / cols;
        maxScroll = Math.max(0, totalRows * (CARD_H + CARD_GAP) - gridH - CARD_GAP);

        if (maxScroll > 0) {
            int sbX = cx + cw - 5;
            drawRect(sbX, gridTop, sbX+3, gridTop+gridH, 0x22FFFFFF);
            float frac = (float) scrollOffset / maxScroll;
            int   tH   = Math.max(20, gridH - (int)(frac * (gridH - 30)));
            int   tY   = gridTop + (int)(frac * (gridH - tH));
            drawRect(sbX, tY, sbX+3, tY+tH, (accent & 0x00FFFFFF) | 0xAA000000);
        }
    }

    private List<Module> getFilteredModules(ModuleCategory cat) {
        if (searchQuery.isEmpty()) {
            return cat.modules;
        }
        
        List<Module> filtered = new ArrayList<>();
        String query = searchQuery.toLowerCase();
        for (Module m : cat.modules) {
            if (m.getName().toLowerCase().contains(query) || 
                m.getDescription().toLowerCase().contains(query)) {
                filtered.add(m);
            }
        }
        return filtered;
    }

    private void drawHomeCategory(int cx, int cy, int cw, int ch, int accent, int mx, int my) {
        // Draw title
        mc.fontRendererObj.drawStringWithShadow("◆ HOME", cx + GRID_PAD_X, cy + 10, accent);
        
        int contentY = cy + 40;
        
        // Draw client info card
        int cardX = cx + GRID_PAD_X;
        int cardW = cw - GRID_PAD_X * 2;
        int cardH = 95;
        
        // Card background
        drawRect(cardX, contentY, cardX + cardW, contentY + cardH, 0x18FFFFFF);
        drawRect(cardX, contentY, cardX + cardW, contentY + 3, (accent & 0x00FFFFFF) | 0xFF000000);
        drawHollowRect(cardX, contentY, cardX + cardW, contentY + cardH, 0x22FFFFFF);
        
        // Client logo
        String logo = "AMETHYST CLIENT";
        ColorChanger cc = getColorChanger();
        int logoX = cardX + 12;
        int logoY = contentY + 12;
        
        for (int i = 0; i < logo.length(); i++) {
            int col = cc != null && cc.isEnabled()
                    ? ColorChanger.getPresetColor(cc.getPresetIndex(), i, logo.length(), cc.getRainbowSpeed())
                    : accent;
            mc.fontRendererObj.drawStringWithShadow(String.valueOf(logo.charAt(i)), logoX, logoY, col);
            logoX += mc.fontRendererObj.getCharWidth(logo.charAt(i));
        }
        
        // Version
        mc.fontRendererObj.drawString("v1.1 Beta", cardX + 12, contentY + 27, 0x66AABBCC);
        
        // Description
        String desc1 = "Мощный PvP клиент для Minecraft 1.8.9";
        String desc2 = "с современным интерфейсом и множеством функций";
        String desc3 = "для улучшения игрового опыта.";
        
        mc.fontRendererObj.drawString(desc1, cardX + 12, contentY + 42, 0x99FFFFFF);
        mc.fontRendererObj.drawString(desc2, cardX + 12, contentY + 54, 0x99FFFFFF);
        mc.fontRendererObj.drawString(desc3, cardX + 12, contentY + 66, 0x99FFFFFF);
        
        // Module count
        int totalModules = AmethystClient.moduleManager.getModules().size();
        int enabledModules = 0;
        for (Module m : AmethystClient.moduleManager.getModules()) {
            if (m.isEnabled()) enabledModules++;
        }
        String stats = enabledModules + " / " + totalModules + " модулей активно";
        mc.fontRendererObj.drawString(stats, cardX + 12, contentY + 80, accent);
        
        // Search box
        int searchY = contentY + cardH + 14;
        int searchW = cardW;
        int searchH = 26;
        
        // Search background
        boolean searchHovered = mx >= cardX && mx <= cardX + searchW && 
                               my >= searchY && my <= searchY + searchH;
        drawRect(cardX, searchY, cardX + searchW, searchY + searchH, 
                searchFocused ? 0x30FFFFFF : (searchHovered ? 0x22FFFFFF : 0x18FFFFFF));
        drawRect(cardX, searchY, cardX + searchW, searchY + 2, 
                searchFocused ? ((accent & 0x00FFFFFF) | 0xAA000000) : 0x22FFFFFF);
        drawHollowRect(cardX, searchY, cardX + searchW, searchY + searchH, 
                searchFocused ? ((accent & 0x00FFFFFF) | 0x77000000) : 0x22FFFFFF);
        
        // Search icon
        mc.fontRendererObj.drawString("◎", cardX + 10, searchY + 9, searchFocused ? accent : 0x66AABBCC);
        
        // Search text
        String searchText = searchQuery.isEmpty() ? "Поиск модулей..." : searchQuery;
        int textColor = searchQuery.isEmpty() ? 0x55AABBCC : 0xFFFFFFFF;
        mc.fontRendererObj.drawString(searchText, cardX + 26, searchY + 9, textColor);
        
        // Cursor for focused search
        if (searchFocused && System.currentTimeMillis() % 1000 < 500) {
            int cursorX = cardX + 26 + mc.fontRendererObj.getStringWidth(searchQuery);
            drawRect(cursorX, searchY + 8, cursorX + 1, searchY + searchH - 8, accent);
        }
        
        // Quick stats below search
        int statsY = searchY + searchH + 12;
        
        String[] hints = {
            "◈ Используй категории слева для навигации",
            "⚔ Клик на карточку для активации модуля",
            "✦ ПКМ на карточку для настроек (если доступно)"
        };
        
        for (int i = 0; i < hints.length; i++) {
            mc.fontRendererObj.drawString(hints[i], cardX + 12, statsY + i * 12, 0x66AABBCC);
        }
    }

    private void drawModuleCard(int x, int y, Module m, int idx, int total,
                                int catAccent, ColorChanger cc, boolean hov) {
        boolean enabled = m.isEnabled();
        int cardAccent = cc != null && cc.isEnabled()
                ? ColorChanger.getPresetColor(cc.getPresetIndex(), idx, total, cc.getRainbowSpeed())
                : catAccent;

        if (m instanceof AsyncScreenshot) cardAccent = cc != null && cc.isEnabled() ? cardAccent : 0xFFDD44FF;
        if (m instanceof ClickGUI)        cardAccent = cc != null && cc.isEnabled() ? cardAccent : 0xFF44DDFF;

        drawRect(x, y, x+CARD_W, y+CARD_H,
                enabled ? ((cardAccent & 0x00FFFFFF) | 0x28000000) : 0x18FFFFFF);
        if (hov) drawRect(x, y, x+CARD_W, y+CARD_H, 0x14FFFFFF);

        if (enabled) {
            for (int i = 0; i < CARD_W; i++) {
                int c = cc != null && cc.isEnabled()
                        ? ColorChanger.getPresetColor(cc.getPresetIndex(), idx*CARD_W+i, total*CARD_W, cc.getRainbowSpeed())
                        : cardAccent;
                drawRect(x+i, y, x+i+1, y+3, c | 0xFF000000);
            }
        } else {
            drawRect(x, y, x+CARD_W, y+3, 0x33FFFFFF);
        }

        drawHollowRect(x, y, x+CARD_W, y+CARD_H,
                enabled ? ((cardAccent & 0x00FFFFFF) | 0x55000000) : 0x22FFFFFF);

        mc.fontRendererObj.drawStringWithShadow(m.getName(), x+8, y+8,
                enabled ? 0xFFFFFFFF : 0xAABBCCDD);

        String desc = m.getDescription();
        while (mc.fontRendererObj.getStringWidth(desc + "..") > CARD_W - 16 && desc.length() > 0)
            desc = desc.substring(0, desc.length()-1);
        if (!desc.equals(m.getDescription())) desc += "..";
        mc.fontRendererObj.drawString(desc, x+8, y+22, 0x55AABBCC);

        if (m instanceof AsyncScreenshot)
            mc.fontRendererObj.drawString("§5F2 §8→ §7screenshot", x+8, y+33, 0xFF777788);

        String status = enabled ? "ON" : "OFF";
        int pillColor = enabled ? cardAccent : 0x44AAAAAA;
        int pillW = mc.fontRendererObj.getStringWidth(status) + 8;
        drawRect(x+8, y+CARD_H-18, x+8+pillW, y+CARD_H-6,
                (pillColor & 0x00FFFFFF) | (enabled ? 0x44000000 : 0x22000000));
        drawHollowRect(x+8, y+CARD_H-18, x+8+pillW, y+CARD_H-6,
                (pillColor & 0x00FFFFFF) | 0xBB000000);
        mc.fontRendererObj.drawString(status, x+12, y+CARD_H-15,
                enabled ? pillColor | 0xFF000000 : 0x66AAAAAA);

        if (hasSettingsPopup(m)) {
            String hint = "RMB ▸";
            mc.fontRendererObj.drawString(hint,
                    x + CARD_W - mc.fontRendererObj.getStringWidth(hint) - 6,
                    y + CARD_H - 15,
                    hov ? 0xAAFFFFFF : 0x44FFFFFF);
        }
    }

    private boolean hasSettingsPopup(Module m) {
        return m instanceof ColorChanger || m instanceof Nametag
            || m instanceof Scoreboard  || m instanceof CustomChat
            || m instanceof ClickGUI    || m instanceof Animations;
    }

    // ── Rounded panel ─────────────────────────────────────────────────────────

    private void drawRoundedPanel(int x, int y, int w, int h) {
        drawRect(x+3, y,   x+w-3, y+h,   0xEE0A0C14);
        drawRect(x,   y+3, x+w,   y+h-3, 0xEE0A0C14);
        drawHollowRect(x+1, y+1, x+w-1, y+h-1, 0x18FFFFFF);
        drawHollowRect(x+2, y+2, x+w-2, y+h-2, 0x10FFFFFF);
    }

    private void drawHollowRect(int l, int t, int r, int b, int color) {
        drawRect(l, t, r, t+1, color); drawRect(l, b-1, r, b, color);
        drawRect(l, t, l+1, b, color); drawRect(r-1, t, r, b, color);
    }

    private ColorChanger getColorChanger() {
        return (ColorChanger) AmethystClient.moduleManager.getModuleByName("ColorChanger");
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth(), H = sr.getScaledHeight();
        int panelX = W/2-230, panelY = H/2-140;
        int panelW = 460,     panelH = 280;

        int btnY = panelY+38, btnH = 22;
        for (int i = 0; i < categories.size(); i++) {
            if (mx >= panelX+6 && mx <= panelX+SIDEBAR_W-6 && my >= btnY && my <= btnY+btnH) {
                selectedCategory = i; scrollOffset = 0; searchFocused = false; return;
            }
            btnY += btnH + 3;
        }

        ModuleCategory cat = categories.get(selectedCategory);
        
        // Handle HOME category clicks (search box)
        if (cat.isHome) {
            int cx = panelX + SIDEBAR_W;
            int cw = panelW - SIDEBAR_W;
            int cardX = cx + GRID_PAD_X;
            int cardW = cw - GRID_PAD_X * 2;
            int searchY = panelY + 40 + 95 + 14;
            int searchH = 26;
            
            if (mx >= cardX && mx <= cardX + cardW && my >= searchY && my <= searchY + searchH) {
                searchFocused = true;
                return;
            } else {
                searchFocused = false;
            }
            super.mouseClicked(mx, my, btn);
            return;
        }
        
        searchFocused = false;
        int cx      = panelX + SIDEBAR_W;
        int cw      = panelW - SIDEBAR_W;
        int gridTop = panelY + HEADER_H;
        int gridH   = panelH - HEADER_H - 4;
        int cols    = Math.max(1, (cw - GRID_PAD_X*2 + CARD_GAP) / (CARD_W + CARD_GAP));

        List<Module> displayModules = getFilteredModules(cat);
        int row = 0, col = 0;
        for (int i = 0; i < displayModules.size(); i++) {
            Module m = displayModules.get(i);
            int cardX = cx + GRID_PAD_X + col * (CARD_W + CARD_GAP);
            int cardY = gridTop + row * (CARD_H + CARD_GAP) - scrollOffset;

            if (mx >= cardX && mx <= cardX+CARD_W && my >= cardY && my <= cardY+CARD_H
             && my >= gridTop && my <= gridTop+gridH) {
                if (btn == 0) {
                    m.toggle();
                    AmethystClient.moduleManager.saveConfig();
                } else if (btn == 1) {
                    if      (m instanceof ColorChanger) mc.displayGuiScreen(new ColorPickerGUI(this, (ColorChanger) m));
                    else if (m instanceof Nametag)      mc.displayGuiScreen(new NametagPickerGUI(this, (Nametag) m));
                    else if (m instanceof Scoreboard)   mc.displayGuiScreen(new ScoreboardPickerGUI(this, (Scoreboard) m));
                    else if (m instanceof CustomChat)   mc.displayGuiScreen(new CustomChatPickerGUI(this, (CustomChat) m));
                    else if (m instanceof ClickGUI)     mc.displayGuiScreen(new ClickGUIPickerGUI(this, (ClickGUI) m));
                    else if (m instanceof Animations)   mc.displayGuiScreen(new AnimationsPickerGUI(this, (Animations) m));
                }
                return;
            }
            col++;
            if (col >= cols) { col = 0; row++; }
        }

        super.mouseClicked(mx, my, btn);
    }

    @Override
    protected void keyTyped(char ch, int key) throws IOException {
        if (key == 1) { 
            if (searchFocused) {
                searchFocused = false;
                return;
            }
            closing = true; 
            return; 
        }
        
        if (searchFocused) {
            if (key == 14) { // Backspace
                if (searchQuery.length() > 0) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                }
            } else if (key == 28) { // Enter
                searchFocused = false;
            } else if (ch >= 32 && ch < 127) { // Printable characters
                searchQuery += ch;
            }
            return;
        }
        
        super.keyTyped(ch, key);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0)
            scrollOffset = Math.max(0, Math.min(scrollOffset + (scroll > 0 ? -25 : 25), maxScroll));
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}