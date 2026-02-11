package com.amethyst.client;

import com.amethyst.client.modules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class HUDEditorGUI extends GuiScreen {

    private Minecraft mc = Minecraft.getMinecraft();
    private HUDElement draggedElement = null;
    private HUDElement selectedElement = null; // Для изменения размера
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
        String sub = "Press ESC to save and exit  |  Mouse Wheel to resize selected element";
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
        float scale = HUDConfig.getSoupCounterScale();
        int count = m.getSoupCount();

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        
        int scaledX = (int)(x / scale);
        int scaledY = (int)(y / scale);
        
        GlStateManager.enableRescaleNormal(); GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.mushroom_stew), scaledX, scaledY);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend(); GlStateManager.disableRescaleNormal();

        String text = String.valueOf(count);
        mc.fontRendererObj.drawStringWithShadow(text, scaledX + 20, scaledY + 4, 0xFFFFFFFF);
        
        GlStateManager.popMatrix();
        
        int w = (int)((20 + mc.fontRendererObj.getStringWidth(text)) * scale);
        int h = (int)(16 * scale);
        drawBoundingBox(mx, my, x, y, w, h, 0x00FF00, selectedElement == HUDElement.SOUP_COUNTER);
        
        if (selectedElement == HUDElement.SOUP_COUNTER) {
            drawScaleIndicator(x, y, w, h, scale);
        }
    }

    private void drawFPSCounter(int mx, int my) {
        FPSCounter m = (FPSCounter) AmethystClient.moduleManager.getModuleByName("FPS Counter");
        if (m == null || !m.isEnabled()) return;
        int x = HUDConfig.getFPSCounterX(), y = HUDConfig.getFPSCounterY();
        float scale = HUDConfig.getFPSCounterScale();
        String text = m.getText();
        
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        mc.fontRendererObj.drawStringWithShadow(text, (int)(x / scale), (int)(y / scale), 0xFF00FF00);
        GlStateManager.popMatrix();
        
        int w = (int)(mc.fontRendererObj.getStringWidth(text) * scale);
        int h = (int)(10 * scale);
        drawBoundingBox(mx, my, x, y, w, h, 0x00FF00, selectedElement == HUDElement.FPS_COUNTER);
        
        if (selectedElement == HUDElement.FPS_COUNTER) {
            drawScaleIndicator(x, y, w, h, scale);
        }
    }

    private void drawPingCounter(int mx, int my) {
        PingCounter m = (PingCounter) AmethystClient.moduleManager.getModuleByName("Ping Counter");
        if (m == null || !m.isEnabled()) return;
        int x = HUDConfig.getPingCounterX(), y = HUDConfig.getPingCounterY();
        float scale = HUDConfig.getPingCounterScale();
        String text = m.getText();
        
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        mc.fontRendererObj.drawStringWithShadow(text, (int)(x / scale), (int)(y / scale), 0xFFFFFF00);
        GlStateManager.popMatrix();
        
        int w = (int)(mc.fontRendererObj.getStringWidth(text) * scale);
        int h = (int)(10 * scale);
        drawBoundingBox(mx, my, x, y, w, h, 0xFFFF00, selectedElement == HUDElement.PING_COUNTER);
        
        if (selectedElement == HUDElement.PING_COUNTER) {
            drawScaleIndicator(x, y, w, h, scale);
        }
    }

    private void drawClockCounter(int mx, int my) {
        Clock m = (Clock) AmethystClient.moduleManager.getModuleByName("Clock");
        if (m == null || !m.isEnabled()) return;
        int x = HUDConfig.getClockX(), y = HUDConfig.getClockY();
        float scale = HUDConfig.getClockScale();
        String text = m.getText();
        
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        mc.fontRendererObj.drawStringWithShadow(text, (int)(x / scale), (int)(y / scale), 0xFFFFFFFF);
        GlStateManager.popMatrix();
        
        int w = (int)(mc.fontRendererObj.getStringWidth(text) * scale);
        int h = (int)(10 * scale);
        drawBoundingBox(mx, my, x, y, w, h, 0xFFFFFF, selectedElement == HUDElement.CLOCK);
        
        if (selectedElement == HUDElement.CLOCK) {
            drawScaleIndicator(x, y, w, h, scale);
        }
    }

    private void drawCPSCounter(int mx, int my) {
        CPSCounter m = (CPSCounter) AmethystClient.moduleManager.getModuleByName("CPS Counter");
        if (m == null || !m.isEnabled()) return;
        int x = HUDConfig.getCPSCounterX(), y = HUDConfig.getCPSCounterY();
        float scale = HUDConfig.getCPSCounterScale();
        String text = m.getText();
        
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        mc.fontRendererObj.drawStringWithShadow(text, (int)(x / scale), (int)(y / scale), m.getDisplayColor());
        GlStateManager.popMatrix();
        
        int w = (int)(mc.fontRendererObj.getStringWidth(text) * scale);
        int h = (int)(10 * scale);
        drawBoundingBox(mx, my, x, y, w, h, 0xFF44FF, selectedElement == HUDElement.CPS_COUNTER);
        
        if (selectedElement == HUDElement.CPS_COUNTER) {
            drawScaleIndicator(x, y, w, h, scale);
        }
    }

    private void drawScoreboard(int mx, int my, ScaledResolution sr) {
        ScoreboardModule m = (ScoreboardModule) AmethystClient.moduleManager.getModuleByName("Scoreboard");
        if (m == null || !m.isEnabled()) return;

        int x = resolveScoreboardX(sr);
        int y = HUDConfig.getScoreboardY();
        float scale = HUDConfig.getScoreboardScale();

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        
        int scaledX = (int)(x / scale);
        int scaledY = (int)(y / scale);

        // Mock preview
        int w = 110; int h = 70;
        drawRect(scaledX, scaledY, scaledX + w, scaledY + h, 0x55000000);
        drawHollowRect(scaledX, scaledY, scaledX + w, scaledY + h, 0xAAFF5555);
        mc.fontRendererObj.drawStringWithShadow("§eObjective", scaledX + w / 2 - 24, scaledY + 2, 0xFFFFFF55);
        mc.fontRendererObj.drawStringWithShadow("PlayerOne", scaledX + 3, scaledY + 14, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("PlayerTwo", scaledX + 3, scaledY + 24, 0xFFFFFFFF);
        if (m.isShowNumbers()) {
            mc.fontRendererObj.drawStringWithShadow("1500", scaledX + w - 28, scaledY + 14, 0xFFFF5555);
            mc.fontRendererObj.drawStringWithShadow("980",  scaledX + w - 22, scaledY + 24, 0xFFFF5555);
        }
        mc.fontRendererObj.drawStringWithShadow("§7[Scoreboard]", scaledX + 3, scaledY + 56, 0xFF999999);
        
        GlStateManager.popMatrix();
        
        int scaledW = (int)(w * scale);
        int scaledH = (int)(h * scale);
        drawBoundingBox(mx, my, x, y, scaledW, scaledH, 0xFF5555, selectedElement == HUDElement.SCOREBOARD);
        
        if (selectedElement == HUDElement.SCOREBOARD) {
            drawScaleIndicator(x, y, scaledW, scaledH, scale);
        }
    }

    private void drawChat(int mx, int my, ScaledResolution sr) {
        CustomChat m = (CustomChat) AmethystClient.moduleManager.getModuleByName("CustomChat");
        if (m == null || !m.isEnabled()) return;

        int x = HUDConfig.getChatX();
        int y = resolveChatY(sr);
        float scale = HUDConfig.getChatScale();

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        
        int scaledX = (int)(x / scale);
        int scaledY = (int)(y / scale);

        // Ванильные размеры чата: 320x180
        int w = 320; int h = 180;
        if (m.isShowBackground()) {
            drawRect(scaledX, scaledY, scaledX + w, scaledY + h, (int)(m.getBgAlpha() * 180) << 24);
        }
        drawHollowRect(scaledX, scaledY, scaledX + w, scaledY + h, 0xAA55AAFF);
        mc.fontRendererObj.drawStringWithShadow("§7Player: hello world!", scaledX + 2, scaledY + 2, 0xFFCCCCCC);
        mc.fontRendererObj.drawStringWithShadow("§a[Server]§f Welcome!", scaledX + 2, scaledY + 14, 0xFFCCCCCC);
        mc.fontRendererObj.drawStringWithShadow("§cAdmin§f: gg", scaledX + 2, scaledY + 26, 0xFFCCCCCC);
        mc.fontRendererObj.drawStringWithShadow("§8[Chat Preview]", scaledX + 2, scaledY + h - 10, 0xFF666666);
        
        GlStateManager.popMatrix();
        
        int scaledW = (int)(w * scale);
        int scaledH = (int)(h * scale);
        drawBoundingBox(mx, my, x, y, scaledW, scaledH, 0x55AAFF, selectedElement == HUDElement.CHAT);
        
        if (selectedElement == HUDElement.CHAT) {
            drawScaleIndicator(x, y, scaledW, scaledH, scale);
        }
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

    private void drawBoundingBox(int mx, int my, int x, int y, int w, int h, int rgb, boolean selected) {
        boolean hov = isHovered(mx, my, x, y, w, h);
        int color = hov ? (0x80000000 | (rgb & 0xFFFFFF)) : (0x40000000 | (rgb & 0xFFFFFF));
        
        if (selected) {
            // Более яркая граница для выбранного элемента
            color = 0xAA000000 | (rgb & 0xFFFFFF);
        }
        
        drawRect(x - 2, y - 2, x + w + 2, y + h + 2, color);
        drawHollowRect(x - 2, y - 2, x + w + 2, y + h + 2,
                selected ? (0xFF000000 | (rgb & 0xFFFFFF)) : 
                (hov ? (0xFF000000 | (rgb & 0xFFFFFF)) : (0x66000000 | (rgb & 0xFFFFFF))));
    }

    private void drawScaleIndicator(int x, int y, int w, int h, float scale) {
        String scaleText = String.format("%.2fx", scale);
        int textWidth = mc.fontRendererObj.getStringWidth(scaleText);
        
        // Рисуем индикатор масштаба над элементом
        drawRect(x + w / 2 - textWidth / 2 - 2, y - 14, 
                 x + w / 2 + textWidth / 2 + 2, y - 4, 0xAA000000);
        mc.fontRendererObj.drawStringWithShadow(scaleText, 
                x + w / 2 - textWidth / 2, y - 12, 0xFF00FF00);
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
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && selectedElement != null) {
            float delta = wheel > 0 ? 0.05f : -0.05f;
            
            switch (selectedElement) {
                case SOUP_COUNTER:
                    float soupScale = HUDConfig.getSoupCounterScale() + delta;
                    HUDConfig.setSoupCounterScale(Math.max(0.5f, Math.min(3.0f, soupScale)));
                    break;
                case FPS_COUNTER:
                    float fpsScale = HUDConfig.getFPSCounterScale() + delta;
                    HUDConfig.setFPSCounterScale(Math.max(0.5f, Math.min(3.0f, fpsScale)));
                    break;
                case PING_COUNTER:
                    float pingScale = HUDConfig.getPingCounterScale() + delta;
                    HUDConfig.setPingCounterScale(Math.max(0.5f, Math.min(3.0f, pingScale)));
                    break;
                case CLOCK:
                    float clockScale = HUDConfig.getClockScale() + delta;
                    HUDConfig.setClockScale(Math.max(0.5f, Math.min(3.0f, clockScale)));
                    break;
                case CPS_COUNTER:
                    float cpsScale = HUDConfig.getCPSCounterScale() + delta;
                    HUDConfig.setCPSCounterScale(Math.max(0.5f, Math.min(3.0f, cpsScale)));
                    break;
                case SCOREBOARD:
                    float sbScale = HUDConfig.getScoreboardScale() + delta;
                    HUDConfig.setScoreboardScale(Math.max(0.5f, Math.min(3.0f, sbScale)));
                    break;
                case CHAT:
                    float chatScale = HUDConfig.getChatScale() + delta;
                    HUDConfig.setChatScale(Math.max(0.5f, Math.min(3.0f, chatScale)));
                    break;
            }
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        ScaledResolution sr = new ScaledResolution(mc);

        if (btn == 0) {
            // Soup
            SoupCounter soup = (SoupCounter) AmethystClient.moduleManager.getModuleByName("SoupCounter");
            if (soup != null && soup.isEnabled()) {
                int w = (int)((20 + mc.fontRendererObj.getStringWidth(String.valueOf(soup.getSoupCount()))) * HUDConfig.getSoupCounterScale());
                int h = (int)(16 * HUDConfig.getSoupCounterScale());
                if (tryStartDrag(mx, my, HUDConfig.getSoupCounterX(), HUDConfig.getSoupCounterY(), w, h, HUDElement.SOUP_COUNTER)) return;
            }
            
            // FPS
            FPSCounter fps = (FPSCounter) AmethystClient.moduleManager.getModuleByName("FPS Counter");
            if (fps != null && fps.isEnabled()) {
                int w = (int)(mc.fontRendererObj.getStringWidth(fps.getText()) * HUDConfig.getFPSCounterScale());
                int h = (int)(10 * HUDConfig.getFPSCounterScale());
                if (tryStartDrag(mx, my, HUDConfig.getFPSCounterX(), HUDConfig.getFPSCounterY(), w, h, HUDElement.FPS_COUNTER)) return;
            }
            
            // Ping
            PingCounter ping = (PingCounter) AmethystClient.moduleManager.getModuleByName("Ping Counter");
            if (ping != null && ping.isEnabled()) {
                int w = (int)(mc.fontRendererObj.getStringWidth(ping.getText()) * HUDConfig.getPingCounterScale());
                int h = (int)(10 * HUDConfig.getPingCounterScale());
                if (tryStartDrag(mx, my, HUDConfig.getPingCounterX(), HUDConfig.getPingCounterY(), w, h, HUDElement.PING_COUNTER)) return;
            }
            
            // Clock
            Clock clk = (Clock) AmethystClient.moduleManager.getModuleByName("Clock");
            if (clk != null && clk.isEnabled()) {
                int w = (int)(mc.fontRendererObj.getStringWidth(clk.getText()) * HUDConfig.getClockScale());
                int h = (int)(10 * HUDConfig.getClockScale());
                if (tryStartDrag(mx, my, HUDConfig.getClockX(), HUDConfig.getClockY(), w, h, HUDElement.CLOCK)) return;
            }
            
            // CPS
            CPSCounter cps = (CPSCounter) AmethystClient.moduleManager.getModuleByName("CPS Counter");
            if (cps != null && cps.isEnabled()) {
                int w = (int)(mc.fontRendererObj.getStringWidth(cps.getText()) * HUDConfig.getCPSCounterScale());
                int h = (int)(10 * HUDConfig.getCPSCounterScale());
                if (tryStartDrag(mx, my, HUDConfig.getCPSCounterX(), HUDConfig.getCPSCounterY(), w, h, HUDElement.CPS_COUNTER)) return;
            }
            
            // Scoreboard
            ScoreboardModule sb = (ScoreboardModule) AmethystClient.moduleManager.getModuleByName("Scoreboard");
            if (sb != null && sb.isEnabled()) {
                int x = resolveScoreboardX(sr);
                int w = (int)(110 * HUDConfig.getScoreboardScale());
                int h = (int)(70 * HUDConfig.getScoreboardScale());
                if (tryStartDrag(mx, my, x, HUDConfig.getScoreboardY(), w, h, HUDElement.SCOREBOARD)) return;
            }
            
            // Chat
            CustomChat chat = (CustomChat) AmethystClient.moduleManager.getModuleByName("CustomChat");
            if (chat != null && chat.isEnabled()) {
                int y = resolveChatY(sr);
                int w = (int)(320 * HUDConfig.getChatScale());
                int h = (int)(180 * HUDConfig.getChatScale());
                if (tryStartDrag(mx, my, HUDConfig.getChatX(), y, w, h, HUDElement.CHAT)) return;
            }
        }
        super.mouseClicked(mx, my, btn);
    }

    private boolean tryStartDrag(int mx, int my, int x, int y, int w, int h, HUDElement elem) {
        if (isHovered(mx, my, x, y, w, h)) {
            draggedElement = elem;
            selectedElement = elem; // Выбираем элемент для изменения размера
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