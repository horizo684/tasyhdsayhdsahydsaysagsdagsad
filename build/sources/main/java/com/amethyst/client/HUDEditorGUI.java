package com.amethyst.client;

import com.amethyst.client.modules.CustomChat;
import com.amethyst.client.Module;
import com.amethyst.client.modules.ScoreboardModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class HUDEditorGUI extends GuiScreen {

    private boolean draggingScoreboard = false;
    private boolean draggingChat = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    private GuiButton doneButton;
    private GuiButton resetAllButton;

    private static final int BUTTON_DONE = 0;
    private static final int BUTTON_RESET_ALL = 1;

    @Override
    public void initGui() {
        this.buttonList.clear();

        int buttonWidth = 100;
        int buttonHeight = 20;
        int spacing = 5;

        int totalWidth = buttonWidth * 2 + spacing;
        int startX = (this.width - totalWidth) / 2;
        int buttonY = this.height - 30;

        this.doneButton = new GuiButton(BUTTON_DONE, startX, buttonY, buttonWidth, buttonHeight, "Done");
        this.resetAllButton = new GuiButton(BUTTON_RESET_ALL, startX + buttonWidth + spacing, buttonY, buttonWidth, buttonHeight, "Reset All");

        this.buttonList.add(this.doneButton);
        this.buttonList.add(this.resetAllButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_DONE) {
            this.mc.displayGuiScreen(null);
        } else if (button.id == BUTTON_RESET_ALL) {
            Module scoreboardObj = AmethystClient.moduleManager.getModuleByName("Scoreboard");
            if (scoreboardObj != null && scoreboardObj instanceof ScoreboardModule) {
                ScoreboardModule sb = (ScoreboardModule) scoreboardObj;
                sb.x = -1;
                sb.y = 5;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        drawScoreboardPreview(mouseX, mouseY);
        drawCustomChatPreview(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);

        String title = "HUD Editor - Click and drag elements to reposition";
        int titleWidth = this.fontRendererObj.getStringWidth(title);
        this.fontRendererObj.drawStringWithShadow(title, (this.width - titleWidth) / 2, 10, 0xFFFFFF);

        String tip = "Green = draggable, White = locked";
        int tipWidth = this.fontRendererObj.getStringWidth(tip);
        this.fontRendererObj.drawStringWithShadow(tip, (this.width - tipWidth) / 2, 25, 0xAAAAAA);
    }

    private void drawScoreboardPreview(int mx, int my) {
        Module moduleObj = AmethystClient.moduleManager.getModuleByName("Scoreboard");
        if (moduleObj == null || !(moduleObj instanceof ScoreboardModule)) return;
        ScoreboardModule m = (ScoreboardModule) moduleObj;
        if (!m.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return;

        net.minecraft.scoreboard.Scoreboard sb = mc.theWorld.getScoreboard();
        net.minecraft.scoreboard.ScoreObjective obj = sb.getObjectiveInDisplaySlot(1);
        if (obj == null) return;

        int x = m.x;
        int y = m.y;

        if (x == -1) {
            ScaledResolution sr = new ScaledResolution(mc);
            x = sr.getScaledWidth() - 115;
        }

        ScoreboardRenderer.renderScoreboard(mc, sb, obj, x, y, m);

        int w = 110;
        int h = 80;

        if (mx >= x && mx <= x + w && my >= y && my <= y + h) {
            drawRect(x, y, x + w, y + h, 0x3000FF00);
        } else {
            drawRect(x, y, x + w, y + h, 0x30FFFFFF);
        }
    }

    private void drawCustomChatPreview(int mx, int my) {
        Module moduleObj = AmethystClient.moduleManager.getModuleByName("CustomChat");
        if (moduleObj == null || !(moduleObj instanceof CustomChat)) return;
        CustomChat m = (CustomChat) moduleObj;
        if (!m.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);

        int baseX = 2;
        int baseY = sr.getScaledHeight() - 40;

        float scale = m.getScale();

        int scaledX = (int)(baseX / scale);
        int scaledY = (int)(baseY / scale);

        int w = (int)(320 / scale);
        int h = (int)(36 / scale);

        if (m.isShowBackground()) {
            drawRect(scaledX, scaledY, scaledX + w, scaledY + h, (int)(m.getBgAlpha() * 180) << 24);
        }

        String[] sampleMsgs = {
            "§7[12:34] §fPlayer1§7: Hello!",
            "§7[12:35] §fPlayer2§7: Hi there",
            "§7[12:36] §fYou§7: How are you?"
        };

        int ty = scaledY + h - 8;
        for (String msg : sampleMsgs) {
            mc.fontRendererObj.drawStringWithShadow(msg, scaledX + 2, ty, 0xFFFFFF);
            ty -= 9;
        }

        if (mx >= scaledX && mx <= scaledX + w && my >= scaledY && my <= scaledY + h) {
            drawRect(scaledX, scaledY, scaledX + w, scaledY + h, 0x3000FF00);
        } else {
            drawRect(scaledX, scaledY, scaledX + w, scaledY + h, 0x30FFFFFF);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            Module moduleObj = AmethystClient.moduleManager.getModuleByName("Scoreboard");
            if (moduleObj != null && moduleObj instanceof ScoreboardModule) {
                ScoreboardModule sb = (ScoreboardModule) moduleObj;
                if (sb.isEnabled()) {
                    int x = sb.x == -1 ? this.width - 115 : sb.x;
                    int y = sb.y;
                    if (mouseX >= x && mouseX <= x + 110 && mouseY >= y && mouseY <= y + 80) {
                        draggingScoreboard = true;
                        dragOffsetX = mouseX - x;
                        dragOffsetY = mouseY - y;
                        return;
                    }
                }
            }

            Module moduleObj2 = AmethystClient.moduleManager.getModuleByName("CustomChat");
            if (moduleObj2 != null && moduleObj2 instanceof CustomChat) {
                CustomChat chat = (CustomChat) moduleObj2;
                if (chat.isEnabled()) {
                    int baseX = 2;
                    int baseY = this.height - 40;
                    float scale = chat.getScale();
                    int scaledX = (int)(baseX / scale);
                    int scaledY = (int)(baseY / scale);
                    int w = (int)(320 / scale);
                    int h = (int)(36 / scale);
                    
                    if (mouseX >= scaledX && mouseX <= scaledX + w && mouseY >= scaledY && mouseY <= scaledY + h) {
                        draggingChat = true;
                        dragOffsetX = mouseX - scaledX;
                        dragOffsetY = mouseY - scaledY;
                    }
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (state == 0) {
            draggingScoreboard = false;
            draggingChat = false;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if (draggingScoreboard || draggingChat) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            if (draggingScoreboard) {
                Module moduleObj = AmethystClient.moduleManager.getModuleByName("Scoreboard");
                if (moduleObj != null && moduleObj instanceof ScoreboardModule) {
                    ScoreboardModule sb = (ScoreboardModule) moduleObj;
                    sb.x = mouseX - dragOffsetX;
                    sb.y = mouseY - dragOffsetY;
                }
            }

            if (draggingChat) {
                // Custom chat dragging logic can be added here if needed
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}