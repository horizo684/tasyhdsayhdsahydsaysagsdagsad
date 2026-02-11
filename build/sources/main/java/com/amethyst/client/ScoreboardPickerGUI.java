package com.amethyst.client;

import com.amethyst.client.modules.ScoreboardModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class ScoreboardPickerGUI extends GuiScreen {

    private final GuiScreen parent;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean dragging = false;

    private final ScoreboardModule mod;

    private GuiButton doneButton;
    private GuiButton resetButton;

    private static final int BUTTON_DONE = 0;
    private static final int BUTTON_RESET = 1;

    public ScoreboardPickerGUI(GuiScreen parent, ScoreboardModule mod) {
        this.parent = parent;
        this.mod = mod;
    }

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
        this.resetButton = new GuiButton(BUTTON_RESET, startX + buttonWidth + spacing, buttonY, buttonWidth, buttonHeight, "Reset Position");

        this.buttonList.add(this.doneButton);
        this.buttonList.add(this.resetButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_DONE) {
            this.mc.displayGuiScreen(this.parent);
        } else if (button.id == BUTTON_RESET) {
            mod.x = -1;
            mod.y = 5;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        net.minecraft.scoreboard.Scoreboard scoreboard = mc.theWorld.getScoreboard();
        net.minecraft.scoreboard.ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);

        if (objective != null) {
            int x = mod.x;
            int y = mod.y;

            if (x == -1) {
                x = this.width - 115;
            }

            ScoreboardRenderer.renderScoreboard(mc, scoreboard, objective, x, y, mod);

            int sbWidth = 110;
            int sbHeight = 80;

            if (mouseX >= x && mouseX <= x + sbWidth && mouseY >= y && mouseY <= y + sbHeight) {
                drawRect(x, y, x + sbWidth, y + sbHeight, 0x3000FF00);
            } else {
                drawRect(x, y, x + sbWidth, y + sbHeight, 0x30FFFFFF);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        String title = "Click and drag the scoreboard to reposition it";
        int titleWidth = this.fontRendererObj.getStringWidth(title);
        this.fontRendererObj.drawStringWithShadow(title, (this.width - titleWidth) / 2, 10, 0xFFFFFF);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            int x = mod.x;
            int y = mod.y;

            if (x == -1) {
                x = this.width - 115;
            }

            int sbWidth = 110;
            int sbHeight = 80;

            if (mouseX >= x && mouseX <= x + sbWidth && mouseY >= y && mouseY <= y + sbHeight) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (state == 0) {
            dragging = false;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if (dragging) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            mod.x = mouseX - dragOffsetX;
            mod.y = mouseY - dragOffsetY;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}