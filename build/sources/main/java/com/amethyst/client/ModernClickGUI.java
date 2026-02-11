package com.amethyst.client;

import com.amethyst.client.modules.*;
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
import java.util.ArrayList;
import java.util.List;

/**
 * AmethystClient — ClickGUI v2
 *
 * Aesthetic: Dark glassmorphism with neon accent lines.
 * Layout:
 *   ┌─────────────────────────────────────────────┐
 *   │  SIDEBAR (left)  │  MODULE GRID (right)     │
 *   │                  │                           │
 *   │  [logo]          │  ┌────┐ ┌────┐ ┌────┐  │
 *   │  ─────────       │  │mod │ │mod │ │mod │  │
 *   │  > COMBAT        │  └────┘ └────┘ └────┘  │
 *   │    VISUAL        │  ┌────┐ ┌────┐ ...     │
 *   │    MISC          │                           │
 *   │    HUD           │                           │
 *   │                  │                           │
 *   │  [version]       │                           │
 *   └─────────────────────────────────────────────┘
 */
public class ModernClickGUI extends GuiScreen {

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int SIDEBAR_W   = 110;
    private static final int CARD_W      = 130;
    private static final int CARD_H      = 72;
    private static final int CARD_GAP    = 8;
    private static final int GRID_PAD_X  = 14;
    private static final int GRID_PAD_Y  = 10;
    private static final int HEADER_H    = 32;  // top bar height in content area

    // ── State ─────────────────────────────────────────────────────────────────
    private int selectedCategory = 0;
    private int scrollOffset     = 0;
    private int maxScroll        = 0;

    // Hover/click animation: track which card is hovered
    private int hoveredCard = -1;

    // ── Data ──────────────────────────────────────────────────────────────────
    private List<ModuleCategory> categories = new ArrayList<>();

    private static class ModuleCategory {
        final String icon;
        final String name;
        final List<Module> modules = new ArrayList<>();
        ModuleCategory(String icon, String name) { this.icon = icon; this.name = name; }
    }

    // ── Category accent colours ───────────────────────────────────────────────
    private static final int[] CAT_ACCENT = {
        0xFFFF4455,   // Combat  — red
        0xFF44AAFF,   // Visual  — blue
        0xFFFFAA33,   // Misc    — amber
        0xFF44FF99,   // HUD     — green
    };

    // ── Category icons (using Minecraft's limited § colour + ASCII art) ───────
    private static final String[] CAT_ICONS = { "⚔", "✦", "⚙", "▣" };

    @Override
    public void initGui() {
        buildCategories();
        scrollOffset = 0;
    }

    private void buildCategories() {
        categories.clear();
        ModuleCategory combat = new ModuleCategory(CAT_ICONS[0], "COMBAT");
        ModuleCategory visual = new ModuleCategory(CAT_ICONS[1], "VISUAL");
        ModuleCategory misc   = new ModuleCategory(CAT_ICONS[2], "MISC");
        ModuleCategory hud    = new ModuleCategory(CAT_ICONS[3], "HUD");

        for (Module m : AmethystClient.moduleManager.getModules()) {
            if      (m instanceof HitDelayFix || m instanceof AutoSoup || m instanceof Refill)
                combat.modules.add(m);
            else if (m instanceof ModuleList || m instanceof ColorChanger || m instanceof Nametag
                  || m instanceof Friends    || m instanceof FullBright)
                visual.modules.add(m);
            else if (m instanceof NoJumpDelay || m instanceof CopyChat || m instanceof NoHurtCam)
				  || m instanceof AutoSprint); 
                misc.modules.add(m);
            else if (m instanceof SoupCounter || m instanceof FPSCounter || m instanceof PingCounter
                  || m instanceof Clock       || m instanceof CPSCounter  || m instanceof Saturation)
                hud.modules.add(m);
        }
        categories.add(combat); categories.add(visual);
        categories.add(misc);   categories.add(hud);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DRAW
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth(), H = sr.getScaledHeight();

        // Dim the world behind
        drawGradientRect(0, 0, W, H, 0xCC000000, 0xDD050510);

        // ── Panel background (full) ───────────────────────────────────────────
        int panelX = W / 2 - 230, panelY = H / 2 - 140;
        int panelW = 460,         panelH = 280;
        drawRoundedPanel(panelX, panelY, panelW, panelH);

        // ── Sidebar ───────────────────────────────────────────────────────────
        drawSidebar(panelX, panelY, panelH, mx, my);

        // ── Content area ──────────────────────────────────────────────────────
        int contentX = panelX + SIDEBAR_W;
        int contentW = panelW - SIDEBAR_W;
        drawContentArea(contentX, panelY, contentW, panelH, mx, my);

        // ── Divider line ──────────────────────────────────────────────────────
        drawRect(panelX + SIDEBAR_W - 1, panelY + 8, panelX + SIDEBAR_W, panelY + panelH - 8, 0x22FFFFFF);

        super.drawScreen(mx, my, pt);
    }

    // ─── Sidebar ─────────────────────────────────────────────────────────────
    private void drawSidebar(int px, int py, int pH, int mx, int my) {
        ColorChanger cc = getColorChanger();

        // Logo / title
        String logo = "AMETHYST";
        int lx = px + SIDEBAR_W / 2 - mc.fontRendererObj.getStringWidth(logo) / 2;
        // Draw each char with animated colour
        for (int i = 0; i < logo.length(); i++) {
            int col = cc != null && cc.isEnabled()
                    ? ColorChanger.getPresetColor(cc.getPresetIndex(), i, logo.length(), cc.getRainbowSpeed())
                    : 0xFF9966FF;
            mc.fontRendererObj.drawStringWithShadow(String.valueOf(logo.charAt(i)), lx, py + 8, col);
            lx += mc.fontRendererObj.getCharWidth(logo.charAt(i));
        }
        String sub = "CLIENT";
        mc.fontRendererObj.drawString(sub, px + SIDEBAR_W / 2 - mc.fontRendererObj.getStringWidth(sub) / 2, py + 18, 0x66FFFFFF);

        // Thin accent line under title
        int accentCol = cc != null && cc.isEnabled()
                ? ColorChanger.getPresetColor(cc.getPresetIndex(), 0, 1, cc.getRainbowSpeed())
                : CAT_ACCENT[selectedCategory];
        drawGradientRect(px + 12, py + 27, px + SIDEBAR_W - 12, py + 28,
                accentCol & 0x00FFFFFF | 0x00000000, accentCol);

        // Category buttons
        int btnY = py + 38;
        int btnH = 22;
        int btnGap = 3;
        for (int i = 0; i < categories.size(); i++) {
            ModuleCategory cat = categories.get(i);
            boolean sel = i == selectedCategory;
            boolean hov = mx >= px + 6 && mx <= px + SIDEBAR_W - 6
                       && my >= btnY   && my <= btnY + btnH;

            int accent = CAT_ACCENT[i];
            if (sel) {
                // Filled pill
                drawRect(px + 6, btnY, px + SIDEBAR_W - 6, btnY + btnH, accent & 0x00FFFFFF | 0x30000000);
                drawRect(px + 6, btnY, px + 8, btnY + btnH, accent);  // left accent bar
                // Glow effect
                drawRect(px + 6, btnY, px + SIDEBAR_W - 6, btnY + 1, accent & 0x00FFFFFF | 0x88000000);
                drawRect(px + 6, btnY + btnH - 1, px + SIDEBAR_W - 6, btnY + btnH, accent & 0x00FFFFFF | 0x44000000);
            } else if (hov) {
                drawRect(px + 6, btnY, px + SIDEBAR_W - 6, btnY + btnH, 0x18FFFFFF);
            }

            String label = cat.icon + " " + cat.name;
            int textCol = sel ? 0xFFFFFFFF : (hov ? 0xCCFFFFFF : 0x77AABBCC);
            mc.fontRendererObj.drawStringWithShadow(label, px + 14, btnY + 7, textCol);

            // Module count badge
            String count = String.valueOf(cat.modules.size());
            int badgeX = px + SIDEBAR_W - 16;
            mc.fontRendererObj.drawString(count, badgeX, btnY + 7, sel ? accent : 0x44AAAAAA);

            btnY += btnH + btnGap;
        }

        // Version at bottom
        String ver = "beta";
        mc.fontRendererObj.drawString(ver,
            px + SIDEBAR_W / 2 - mc.fontRendererObj.getStringWidth(ver) / 2,
            py + pH - 12, 0x33FFFFFF);
    }

    // ─── Content area ─────────────────────────────────────────────────────────
    private void drawContentArea(int cx, int cy, int cw, int ch, int mx, int my) {
        ModuleCategory cat = categories.get(selectedCategory);
        int accent = CAT_ACCENT[selectedCategory];
        ColorChanger cc = getColorChanger();

        // Category name header
        String catName = cat.name;
        mc.fontRendererObj.drawStringWithShadow(catName, cx + GRID_PAD_X, cy + 10, accent);
        String modCount = cat.modules.size() + " modules";
        mc.fontRendererObj.drawString(modCount, cx + GRID_PAD_X, cy + 21, 0x44AABBCC);

        // Scroll clip region (approximate via offset)
        int gridTop   = cy + HEADER_H;
        int gridH     = ch - HEADER_H - 4;
        int cols      = Math.max(1, (cw - GRID_PAD_X * 2 + CARD_GAP) / (CARD_W + CARD_GAP));
        int cardAndGap = CARD_W + CARD_GAP;

        hoveredCard = -1;
        int row = 0, col = 0;

        for (int i = 0; i < cat.modules.size(); i++) {
            Module m = cat.modules.get(i);
            int cardX = cx + GRID_PAD_X + col * cardAndGap;
            int cardY = gridTop + row * (CARD_H + CARD_GAP) - scrollOffset;

            // Clip: only draw if visible
            if (cardY + CARD_H >= gridTop && cardY <= gridTop + gridH) {
                boolean hov = mx >= cardX && mx <= cardX + CARD_W
                           && my >= cardY && my <= cardY + CARD_H
                           && my >= gridTop && my <= gridTop + gridH;
                if (hov) hoveredCard = i;
                drawModuleCard(cardX, cardY, m, i, cat.modules.size(), accent, cc, hov);
            }

            col++;
            if (col >= cols) { col = 0; row++; }
        }

        int totalRows = (cat.modules.size() + cols - 1) / cols;
        maxScroll = Math.max(0, totalRows * (CARD_H + CARD_GAP) - gridH - CARD_GAP);

        // Scrollbar
        if (maxScroll > 0) {
            int sbX = cx + cw - 5;
            int sbH = gridH;
            drawRect(sbX, gridTop, sbX + 3, gridTop + sbH, 0x22FFFFFF);
            float frac = (float) scrollOffset / maxScroll;
            int   tH   = Math.max(20, sbH - (int)(frac * (sbH - 30)));
            int   tY   = gridTop + (int)(frac * (sbH - tH));
            drawRect(sbX, tY, sbX + 3, tY + tH, accent & 0x00FFFFFF | 0xAA000000);
        }
    }

    // ─── Module card ──────────────────────────────────────────────────────────
    private void drawModuleCard(int x, int y, Module m, int idx, int total,
                                int catAccent, ColorChanger cc, boolean hov) {
        boolean enabled = m.isEnabled();

        // Determine accent for this card
        int cardAccent = cc != null && cc.isEnabled()
                ? ColorChanger.getPresetColor(cc.getPresetIndex(), idx, total, cc.getRainbowSpeed())
                : catAccent;

        // ── Card background ──
        int bgAlpha = enabled ? 0x28 : 0x18;
        int bgColor = enabled
                ? (cardAccent & 0x00FFFFFF) | (bgAlpha << 24)
                : 0x18FFFFFF;
        drawRect(x, y, x + CARD_W, y + CARD_H, bgColor);

        // Hover lift effect
        if (hov) drawRect(x, y, x + CARD_W, y + CARD_H, 0x14FFFFFF);

        // ── Top accent bar (3 px, full width) ──
        if (enabled) {
            for (int i = 0; i < CARD_W; i++) {
                float t = (float) i / CARD_W;
                int c = cc != null && cc.isEnabled()
                        ? ColorChanger.getPresetColor(cc.getPresetIndex(), idx * CARD_W + i, total * CARD_W, cc.getRainbowSpeed())
                        : cardAccent;
                drawRect(x + i, y, x + i + 1, y + 3, c | 0xFF000000);
            }
        } else {
            drawRect(x, y, x + CARD_W, y + 3, 0x33FFFFFF);
        }

        // ── Border ──
        int borderCol = enabled ? (cardAccent & 0x00FFFFFF | 0x55000000) : 0x22FFFFFF;
        drawHollowRect(x, y, x + CARD_W, y + CARD_H, borderCol);

        // ── Module name ──
        String name = m.getName();
        mc.fontRendererObj.drawStringWithShadow(name, x + 8, y + 8, enabled ? 0xFFFFFFFF : 0xAABBCCDD);

        // ── Description (truncated) ──
        String desc = m.getDescription();
        if (mc.fontRendererObj.getStringWidth(desc) > CARD_W - 16) {
            while (mc.fontRendererObj.getStringWidth(desc + "..") > CARD_W - 16 && desc.length() > 0)
                desc = desc.substring(0, desc.length() - 1);
            desc += "..";
        }
        mc.fontRendererObj.drawString(desc, x + 8, y + 22, 0x55AABBCC);

        // ── Status pill ──
        String status = enabled ? "ON" : "OFF";
        int pillColor = enabled ? cardAccent : 0x44AAAAAA;
        int pillW = mc.fontRendererObj.getStringWidth(status) + 8;
        drawRect(x + 8, y + CARD_H - 18, x + 8 + pillW, y + CARD_H - 6,
                (pillColor & 0x00FFFFFF) | (enabled ? 0x44000000 : 0x22000000));
        drawHollowRect(x + 8, y + CARD_H - 18, x + 8 + pillW, y + CARD_H - 6,
                (pillColor & 0x00FFFFFF) | 0xBB000000);
        mc.fontRendererObj.drawString(status, x + 12, y + CARD_H - 15, enabled ? pillColor | 0xFF000000 : 0x66AAAAAA);

        // ── Config hint (RMB) ──
        if (m instanceof ColorChanger || m instanceof Nametag) {
            String hint = "RMB ▸";
            mc.fontRendererObj.drawString(hint,
                    x + CARD_W - mc.fontRendererObj.getStringWidth(hint) - 6,
                    y + CARD_H - 15,
                    hov ? 0xAAFFFFFF : 0x44FFFFFF);
        }
    }

    // ─── Rounded panel ────────────────────────────────────────────────────────
    private void drawRoundedPanel(int x, int y, int w, int h) {
        // Main body
        drawRect(x + 3, y,     x + w - 3, y + h,     0xEE0A0C14);
        drawRect(x,     y + 3, x + w,     y + h - 3, 0xEE0A0C14);
        // Corners (tiny pixel squares)
        drawRect(x + 1, y + 1, x + 3,     y + 3,     0xEE0A0C14);
        drawRect(x + w - 3, y + 1, x + w - 1, y + 3, 0xEE0A0C14);
        drawRect(x + 1, y + h - 3, x + 3, y + h - 1, 0xEE0A0C14);
        drawRect(x + w - 3, y + h - 3, x + w - 1, y + h - 1, 0xEE0A0C14);

        // Subtle outer glow border
        drawHollowRect(x + 1, y + 1, x + w - 1, y + h - 1, 0x18FFFFFF);
        drawHollowRect(x + 2, y + 2, x + w - 2, y + h - 2, 0x10FFFFFF);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private void drawHollowRect(int l, int t, int r, int b, int color) {
        drawRect(l, t, r, t + 1, color);
        drawRect(l, b - 1, r, b, color);
        drawRect(l, t, l + 1, b, color);
        drawRect(r - 1, t, r, b, color);
    }

    private ColorChanger getColorChanger() {
        return (ColorChanger) AmethystClient.moduleManager.getModuleByName("ColorChanger");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  INPUT
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        ScaledResolution sr = new ScaledResolution(mc);
        int W = sr.getScaledWidth(), H = sr.getScaledHeight();
        int panelX = W / 2 - 230, panelY = H / 2 - 140;
        int panelW = 460,         panelH = 280;

        // ── Sidebar category click ────────────────────────────────────────────
        int btnY = panelY + 38, btnH = 22;
        for (int i = 0; i < categories.size(); i++) {
            if (mx >= panelX + 6 && mx <= panelX + SIDEBAR_W - 6
             && my >= btnY       && my <= btnY + btnH) {
                selectedCategory = i;
                scrollOffset = 0;
                return;
            }
            btnY += btnH + 3;
        }

        // ── Module card click ─────────────────────────────────────────────────
        ModuleCategory cat = categories.get(selectedCategory);
        int cx      = panelX + SIDEBAR_W;
        int cw      = panelW - SIDEBAR_W;
        int gridTop = panelY + HEADER_H;
        int gridH   = panelH - HEADER_H - 4;
        int cols    = Math.max(1, (cw - GRID_PAD_X * 2 + CARD_GAP) / (CARD_W + CARD_GAP));

        int row = 0, col = 0;
        for (int i = 0; i < cat.modules.size(); i++) {
            Module m = cat.modules.get(i);
            int cardX = cx + GRID_PAD_X + col * (CARD_W + CARD_GAP);
            int cardY = gridTop + row * (CARD_H + CARD_GAP) - scrollOffset;

            if (mx >= cardX && mx <= cardX + CARD_W
             && my >= cardY && my <= cardY + CARD_H
             && my >= gridTop && my <= gridTop + gridH) {
                if (btn == 0) {
                    m.toggle();
                    AmethystClient.moduleManager.saveConfig();
                } else if (btn == 1) {
                    if (m instanceof ColorChanger)
                        mc.displayGuiScreen(new ColorPickerGUI(this, (ColorChanger) m));
                    else if (m instanceof Nametag)
                        mc.displayGuiScreen(new NametagPickerGUI(this, (Nametag) m));
                }
                return;
            }
            col++;
            if (col >= cols) { col = 0; row++; }
        }

        super.mouseClicked(mx, my, btn);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            scrollOffset = Math.max(0, Math.min(scrollOffset + (scroll > 0 ? -25 : 25), maxScroll));
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}