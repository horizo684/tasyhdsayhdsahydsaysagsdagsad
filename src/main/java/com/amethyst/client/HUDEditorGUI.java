package com.amethyst.client;

import com.amethyst.client.modules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class HUDEditorGUI extends GuiScreen {

    private Minecraft mc = Minecraft.getMinecraft();
    private HUDElement draggedElement = null;
    private int dragOffsetX = 0, dragOffsetY = 0;

    private enum HUDElement {
        SOUP_COUNTER, FPS_COUNTER, PING_COUNTER, CLOCK, CPS_COUNTER,
        SCOREBOARD, CHAT
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    @Override
    public void drawScreen(int mouseX, int mouseY, float pt) {
        drawDefaultBackground();
        ScaledResolution sr = new ScaledResolution(mc);

        // Header
        String title = "HUD Editor — drag elements to reposition";
        mc.fontRendererObj.drawStringWithShadow(title,
                sr.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(title) / 2,
                8, 0xFFFFFFFF);
        String sub = "Press ESC to save and exit  |  RCtrl to open editor";
        mc.fontRendererObj.drawStringWithShadow(sub,
                sr.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(sub) / 2,
                20, 0xFF888888);

        // Elements
        drawSoupCounter(mouseX, mouseY, sr);
        drawFPSCounter(mouseX, mouseY);
        drawPingCounter(mouseX, mouseY);
        drawClockCounter(mouseX, mouseY);
        drawCPSCounter(mouseX, mouseY);
        drawScoreboard(mouseX, mouseY, sr);
        drawChat(mouseX, mouseY, sr);

        super.drawScreen(mouseX, mouseY, pt);
    }

    // ── Individual element drawers ────────────────────────────────────────────

    private void drawSoupCounter(int mx, int my, ScaledResolution sr) {
        SoupCounter m = (SoupCounter) AmethystClient.moduleManager.getModuleByName("SoupCounter");
        if (m == null || !m.isEnabled()) return;
        int x = HUDConfig.getSoupCounterX(), y = HUDConfig.getSoupCounterY();
        int count = m.getSoupCount();

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal(); GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.mushroom_stew), x, y);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend(); GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();

        String text = String.valueOf(count);
        mc.fontRendererObj.drawStringWithShadow(text, x + 20, y + 4, 0xFFFFFFFF);
        int w = 20 + mc.fontRendererObj.getStringWidth(text);
        drawBoundingBox(mx, my, x, y, w, 16, 0x00FF00);
    }

    private void drawFPSCounter(int mx, int my) {
        FPSCounter m = (FPSCounter) AmethystClient.moduleManager.getModuleByName("FPS Counter");
        if (m == null || !m.isEnabled()) return;
        int x = HUDConfig.getFPSCounterX(), y = HUDConfig.getFPSCounterY();
        String text = m.getText();
        mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFF00FF00);
        drawBoundingBox(mx, my, x, y, mc.fontRendererObj.getStringWidth(text), 10, 0x00FF00);
    }

    private void drawPingCounter(int mx, int my) {
        PingCounter m = (PingCounter) AmethystClient.moduleManager.getModuleByName("Ping Counter");
        if (m == null || !m.isEnabled()) return;
        int x = HUDConfig.getPingCounterX(), y = HUDConfig.getPingCounterY();
        String text = m.getText();
        mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFFFFF00);
        drawBoundingBox(mx, my, x, y, mc.fontRendererObj.getStringWidth(text), 10, 0xFFFF00);
    }

    private void drawClockCounter(int mx, int my) {
        Clock m = (Clock) AmethystClient.moduleManager.getModuleByName("Clock");
        if (m == null || !m.isEnabled()) return;
        int x = HUDConfig.getClockX(), y = HUDConfig.getClockY();
        String text = m.getText();
        mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFFFFFFF);
        drawBoundingBox(mx, my, x, y, mc.fontRendererObj.getStringWidth(text), 10, 0xFFFFFF);
    }

    private void drawCPSCounter(int mx, int my) {
        CPSCounter m = (CPSCounter) AmethystClient.moduleManager.getModuleByName("CPS Counter");
        if (m == null || !m.isEnabled()) return;
        int x = HUDConfig.getCPSCounterX(), y = HUDConfig.getCPSCounterY();
        String text = m.getText();
        mc.fontRendererObj.drawStringWithShadow(text, x, y, m.getDisplayColor());
        drawBoundingBox(mx, my, x, y, mc.fontRendererObj.getStringWidth(text), 10, 0xFF44FF);
    }

    private void drawScoreboard(int mx, int my, ScaledResolution sr) {
        Scoreboard m = (Scoreboard) AmethystClient.moduleManager.getModuleByName("Scoreboard");
        if (m == null || !m.isEnabled()) return;

        int x = resolveScoreboardX(sr);
        int y = HUDConfig.getScoreboardY();

        // Mock preview
        int w = 110; int h = 70;
        drawRect(x, y, x + w, y + h, 0x55000000);
        drawHollowRect(x, y, x + w, y + h, 0xAAFF5555);
        mc.fontRendererObj.drawStringWithShadow("§eObjective", x + w / 2 - 24, y + 2, 0xFFFFFF55);
        mc.fontRendererObj.drawStringWithShadow("PlayerOne", x + 3, y + 14, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("PlayerTwo", x + 3, y + 24, 0xFFFFFFFF);
        if (m.isShowNumbers()) {
            mc.fontRendererObj.drawStringWithShadow("1500", x + w - 28, y + 14, 0xFFFF5555);
            mc.fontRendererObj.drawStringWithShadow("980",  x + w - 22, y + 24, 0xFFFF5555);
        }
        mc.fontRendererObj.drawStringWithShadow("§7[Scoreboard]", x + 3, y + 56, 0xFF999999);
        drawBoundingBox(mx, my, x, y, w, h, 0xFF5555);
    }

    private void drawChat(int mx, int my, ScaledResolution sr) {
        CustomChat m = (CustomChat) AmethystClient.moduleManager.getModuleByName("CustomChat");
        if (m == null || !m.isEnabled()) return;

        int x = HUDConfig.getChatX();
        int y = resolveChatY(sr);

        int w = 200; int h = 44;
        if (m.isShowBackground()) drawRect(x, y, x + w, y + h, (int)(m.getBgAlpha() * 180) << 24);
        drawHollowRect(x, y, x + w, y + h, 0xAA55AAFF);
        mc.fontRendererObj.drawStringWithShadow("§7Player: hello world!", x + 2, y + 2, 0xFFCCCCCC);
        mc.fontRendererObj.drawStringWithShadow("§a[Server]§f Welcome!", x + 2, y + 14, 0xFFCCCCCC);
        mc.fontRendererObj.drawStringWithShadow("§cAdmin§f: gg", x + 2, y + 26, 0xFFCCCCCC);
        mc.fontRendererObj.drawStringWithShadow("§8[Chat]", x + 2, y + h - 10, 0xFF666666);
        drawBoundingBox(mx, my, x, y, w, h, 0x55AAFF);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int resolveScoreboardX(ScaledResolution sr) {
        int saved = HUDConfig.getScoreboardX();
        return saved == -1 ? sr.getScaledWidth() - 115 : saved;
    }

    private int resolveChatY(ScaledResolution sr) {
        int saved = HUDConfig.getChatY();
        return saved == -1 ? sr.getScaledHeight() - 60 : saved;
    }

    private void drawBoundingBox(int mx, int my, int x, int y, int w, int h, int rgb) {
        boolean hov = isHovered(mx, my, x, y, w, h);
        int color = hov ? (0x80000000 | (rgb & 0xFFFFFF)) : (0x40000000 | (rgb & 0xFFFFFF));
        drawRect(x - 2, y - 2, x + w + 2, y + h + 2, color);
        drawHollowRect(x - 2, y - 2, x + w + 2, y + h + 2,
                hov ? (0xFF000000 | (rgb & 0xFFFFFF)) : (0x66000000 | (rgb & 0xFFFFFF)));
    }

    private void drawHollowRect(int l, int t, int r, int b, int c) {
        drawRect(l, t, r, t+1, c); drawRect(l, b-1, r, b, c);
        drawRect(l, t, l+1, b, c); drawRect(r-1, t, r, b, c);
    }

    private boolean isHovered(int mx, int my, int x, int y, int w, int h) {
        return mx >= x-2 && mx <= x+w+2 && my >= y-2 && my <= y+h+2;
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        ScaledResolution sr = new ScaledResolution(mc);

        if (btn == 0) {
            // Soup
            if (tryStartDrag(mx, my, HUDConfig.getSoupCounterX(), HUDConfig.getSoupCounterY(), 36, 16, HUDElement.SOUP_COUNTER)) return;
            // FPS
            FPSCounter fps = (FPSCounter) AmethystClient.moduleManager.getModuleByName("FPS Counter");
            if (fps != null && fps.isEnabled()) {
                int w = mc.fontRendererObj.getStringWidth(fps.getText());
                if (tryStartDrag(mx, my, HUDConfig.getFPSCounterX(), HUDConfig.getFPSCounterY(), w, 10, HUDElement.FPS_COUNTER)) return;
            }
            // Ping
            PingCounter ping = (PingCounter) AmethystClient.moduleManager.getModuleByName("Ping Counter");
            if (ping != null && ping.isEnabled()) {
                int w = mc.fontRendererObj.getStringWidth(ping.getText());
                if (tryStartDrag(mx, my, HUDConfig.getPingCounterX(), HUDConfig.getPingCounterY(), w, 10, HUDElement.PING_COUNTER)) return;
            }
            // Clock
            Clock clk = (Clock) AmethystClient.moduleManager.getModuleByName("Clock");
            if (clk != null && clk.isEnabled()) {
                int w = mc.fontRendererObj.getStringWidth(clk.getText());
                if (tryStartDrag(mx, my, HUDConfig.getClockX(), HUDConfig.getClockY(), w, 10, HUDElement.CLOCK)) return;
            }
            // CPS
            CPSCounter cps = (CPSCounter) AmethystClient.moduleManager.getModuleByName("CPS Counter");
            if (cps != null && cps.isEnabled()) {
                int w = mc.fontRendererObj.getStringWidth(cps.getText());
                if (tryStartDrag(mx, my, HUDConfig.getCPSCounterX(), HUDConfig.getCPSCounterY(), w, 10, HUDElement.CPS_COUNTER)) return;
            }
            // Scoreboard
            Scoreboard sb = (Scoreboard) AmethystClient.moduleManager.getModuleByName("Scoreboard");
            if (sb != null && sb.isEnabled()) {
                int x = resolveScoreboardX(sr);
                if (tryStartDrag(mx, my, x, HUDConfig.getScoreboardY(), 110, 70, HUDElement.SCOREBOARD)) return;
            }
            // Chat
            CustomChat chat = (CustomChat) AmethystClient.moduleManager.getModuleByName("CustomChat");
            if (chat != null && chat.isEnabled()) {
                int y = resolveChatY(sr);
                if (tryStartDrag(mx, my, HUDConfig.getChatX(), y, 200, 44, HUDElement.CHAT)) return;
            }
        }
        super.mouseClicked(mx, my, btn);
    }

    private boolean tryStartDrag(int mx, int my, int x, int y, int w, int h, HUDElement elem) {
        if (isHovered(mx, my, x, y, w, h)) {
            draggedElement = elem;
            dragOffsetX = mx - x;
            dragOffsetY = my - y;
            return true;
        }
        return false;
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        draggedElement = null;
        super.mouseReleased(mx, my, state);
    }

    @Override
    protected void mouseClickMove(int mx, int my, int btn, long time) {
        if (draggedElement == null) { super.mouseClickMove(mx, my, btn, time); return; }
        ScaledResolution sr = new ScaledResolution(mc);
        int newX = mx - dragOffsetX, newY = my - dragOffsetY;

        switch (draggedElement) {
            case SOUP_COUNTER:  HUDConfig.setSoupCounterX(newX);  HUDConfig.setSoupCounterY(newY);  break;
            case FPS_COUNTER:   HUDConfig.setFPSCounterX(newX);   HUDConfig.setFPSCounterY(newY);   break;
            case PING_COUNTER:  HUDConfig.setPingCounterX(newX);  HUDConfig.setPingCounterY(newY);  break;
            case CLOCK:         HUDConfig.setClockX(newX);        HUDConfig.setClockY(newY);        break;
            case CPS_COUNTER:   HUDConfig.setCPSCounterX(newX);   HUDConfig.setCPSCounterY(newY);   break;
            case SCOREBOARD:    HUDConfig.setScoreboardX(newX);   HUDConfig.setScoreboardY(newY);   break;
            case CHAT:          HUDConfig.setChatX(newX);         HUDConfig.setChatY(newY);         break;
        }
        super.mouseClickMove(mx, my, btn, time);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}