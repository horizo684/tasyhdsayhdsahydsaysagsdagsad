package com.amethyst.client;

import com.amethyst.client.modules.AutoText;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AutoTextPickerGUI extends GuiScreen {

    private final GuiScreen parent;
    private final AutoText autoText;
    
    private List<BindEntry> binds = new ArrayList<>();
    private int selectedBind = -1;
    private boolean waitingForKey = false;
    private boolean editingCommand = false;
    private String editingText = "";
    
    private int scrollOffset = 0;
    private int maxScroll = 0;
    
    private static final int ENTRY_HEIGHT = 50;
    private static final int ENTRY_GAP = 8;
    private static final int PADDING = 14;
    
    private static class BindEntry {
        int keyCode;
        String command;
        
        BindEntry(int keyCode, String command) {
            this.keyCode = keyCode;
            this.command = command;
        }
    }

    public AutoTextPickerGUI(GuiScreen parent, AutoText autoText) {
        this.parent = parent;
        this.autoText = autoText;
        loadBinds();
    }
    
    private void loadBinds() {
        binds.clear();
        for (Map.Entry<Integer, String> entry : autoText.getBinds().entrySet()) {
            binds.add(new BindEntry(entry.getKey(), entry.getValue()));
        }
    }
    
    private void saveBinds() {
        autoText.clearBinds();
        for (BindEntry entry : binds) {
            autoText.addBind(entry.keyCode, entry.command);
        }
    }

    @Override
    public void initGui() {
        calculateMaxScroll();
    }
    
    private void calculateMaxScroll() {
        int totalHeight = binds.size() * (ENTRY_HEIGHT + ENTRY_GAP);
        ScaledResolution sr = new ScaledResolution(mc);
        int availableHeight = sr.getScaledHeight() - 100;
        maxScroll = Math.max(0, totalHeight - availableHeight);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();

        drawGradientRect(0, 0, w, h, 0xF2060410, 0xF2010008);

        String title = waitingForKey ? "Press any key..." : (editingCommand ? "Type command..." : "AutoText Settings");
        mc.fontRendererObj.drawStringWithShadow(title,
            w / 2 - mc.fontRendererObj.getStringWidth(title) / 2, 7, 
            waitingForKey ? 0xFFFFFF44 : (editingCommand ? 0xFF44FFFF : 0xFFCCDDFF));

        if (!waitingForKey && !editingCommand) {
            String desc = "Bind commands to keys • Click '+' to add new";
            mc.fontRendererObj.drawString(desc,
                w / 2 - mc.fontRendererObj.getStringWidth(desc) / 2, 20, 0xFF667788);
        }

        // Поле ввода команды (если редактируем)
        if (editingCommand) {
            drawInputField(w, h);
        }

        drawAddButton(mx, my, w);
        drawBindList(mx, my, w, h);
        drawBackButton(mx, my, h);

        super.drawScreen(mx, my, pt);
    }
    
    private void drawInputField(int w, int h) {
        int fieldW = 400;
        int fieldH = 30;
        int fieldX = w / 2 - fieldW / 2;
        int fieldY = 35;
        
        // Фон поля
        drawRect(fieldX, fieldY, fieldX + fieldW, fieldY + fieldH, 0xFF0A1420);
        drawHollowRect(fieldX, fieldY, fieldX + fieldW, fieldY + fieldH, 0xFF44AAFF);
        
        // Мигающий курсор
        String displayText = editingText;
        if (System.currentTimeMillis() % 1000 < 500) {
            displayText += "_";
        }
        
        // Текст
        mc.fontRendererObj.drawString(displayText.isEmpty() ? "§8Type command and press Enter..." : displayText,
            fieldX + 8, fieldY + 10, displayText.isEmpty() ? 0xFF556677 : 0xFFCCDDEE);
        
        // Подсказка
        mc.fontRendererObj.drawString("§8Press §fEnter §8to save • §fESC §8to cancel",
            w / 2 - mc.fontRendererObj.getStringWidth("Press Enter to save • ESC to cancel") / 2,
            fieldY + fieldH + 6, 0xFF445566);
    }
    
    private void drawAddButton(int mx, int my, int w) {
        if (editingCommand) return; // Скрываем когда редактируем
        
        int bw = 120, bh = 24;
        int bx = w / 2 - bw / 2;
        int by = 35;
        
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        
        drawGradientRect(bx, by, bx + bw, by + bh, 
            hov ? 0xFF1A3555 : 0xFF0F2540, 
            hov ? 0xFF0F2540 : 0xFF0A1830);
        
        drawHollowRect(bx, by, bx + bw, by + bh, 0xFF2A5580);
        
        String text = "+ Add Bind";
        mc.fontRendererObj.drawStringWithShadow(text,
            bx + bw / 2 - mc.fontRendererObj.getStringWidth(text) / 2,
            by + bh / 2 - 4,
            hov ? 0xFF66BBFF : 0xFF5599DD);
    }
    
    private void drawBindList(int mx, int my, int w, int h) {
        int listY = editingCommand ? 90 : 70;
        int listH = h - listY - 40;
        int listW = Math.min(500, w - 40);
        int listX = w / 2 - listW / 2;
        
        drawRect(listX, listY, listX + listW, listY + listH, 0x88000000);
        drawHollowRect(listX, listY, listX + listW, listY + listH, 0xFF1A2A3A);
        
        enableScissor(listX, listY, listW, listH);
        
        int yPos = listY + PADDING - scrollOffset;
        
        for (int i = 0; i < binds.size(); i++) {
            BindEntry bind = binds.get(i);
            
            if (yPos + ENTRY_HEIGHT > listY && yPos < listY + listH) {
                drawBindEntry(bind, i, listX + PADDING, yPos, listW - PADDING * 2, mx, my);
            }
            
            yPos += ENTRY_HEIGHT + ENTRY_GAP;
        }
        
        if (binds.isEmpty()) {
            String msg = "No binds yet. Click '+' to add one!";
            mc.fontRendererObj.drawString(msg,
                w / 2 - mc.fontRendererObj.getStringWidth(msg) / 2,
                listY + listH / 2 - 4,
                0xFF556677);
        }
        
        disableScissor();
    }
    
    private void drawBindEntry(BindEntry bind, int index, int x, int y, int w, int mx, int my) {
        boolean selected = index == selectedBind;
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + ENTRY_HEIGHT && !editingCommand;
        
        int bgColor = selected ? 0xFF1A2A40 : (hovered ? 0xFF0E1825 : 0xFF0A1420);
        drawRect(x, y, x + w, y + ENTRY_HEIGHT, bgColor);
        
        int borderColor = selected ? 0xFF3A6A9A : (hovered ? 0xFF2A4A6A : 0xFF1A2A3A);
        drawHollowRect(x, y, x + w, y + ENTRY_HEIGHT, borderColor);
        
        String keyName = Keyboard.getKeyName(bind.keyCode);
        int keyBoxW = mc.fontRendererObj.getStringWidth(keyName) + 12;
        int keyBoxH = 20;
        int keyBoxX = x + 8;
        int keyBoxY = y + ENTRY_HEIGHT / 2 - keyBoxH / 2;
        
        drawRect(keyBoxX, keyBoxY, keyBoxX + keyBoxW, keyBoxY + keyBoxH, 0xFF1A3050);
        drawHollowRect(keyBoxX, keyBoxY, keyBoxX + keyBoxW, keyBoxY + keyBoxH, 0xFF3A6A9A);
        mc.fontRendererObj.drawStringWithShadow(keyName,
            keyBoxX + 6, keyBoxY + 6, 0xFF66BBFF);
        
        String cmd = bind.command;
        if (cmd.length() > 40) cmd = cmd.substring(0, 37) + "...";
        mc.fontRendererObj.drawString(cmd,
            keyBoxX + keyBoxW + 12, y + 8, 0xFFCCDDEE);
        
        mc.fontRendererObj.drawString("§8LMB: edit key • RMB: edit command",
            keyBoxX + keyBoxW + 12, y + 20, 0xFF445566);
        
        int delSize = 18;
        int delX = x + w - delSize - 8;
        int delY = y + ENTRY_HEIGHT / 2 - delSize / 2;
        boolean delHov = mx >= delX && mx <= delX + delSize && my >= delY && my <= delY + delSize && !editingCommand;
        
        drawRect(delX, delY, delX + delSize, delY + delSize, 
            delHov ? 0xFFAA3333 : 0xFF662222);
        drawHollowRect(delX, delY, delX + delSize, delY + delSize, 
            delHov ? 0xFFFF5555 : 0xFF883333);
        mc.fontRendererObj.drawString("×", delX + 6, delY + 4, 0xFFFFFFFF);
    }
    
    private void drawBackButton(int mx, int my, int h) {
        int bx = 10, by = h - 22, bw = 70, bh = 16;
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
        drawRect(bx, by, bx + bw, by + bh, hov ? 0xFF0D1825 : 0xFF080E18);
        drawHollowRect(bx, by, bx + bw, by + bh, 0xFF1A3045);
        String t = "◀ Back";
        mc.fontRendererObj.drawStringWithShadow(t, 
            bx + bw/2 - mc.fontRendererObj.getStringWidth(t)/2, by + 4, 0xFF5599BB);
    }
    
    private void enableScissor(int x, int y, int w, int h) {
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
            x * scale,
            mc.displayHeight - (y + h) * scale,
            w * scale,
            h * scale
        );
    }
    
    private void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
    
    private void drawHollowRect(int l, int t, int r, int b, int col) {
        drawRect(l, t, r, t + 1, col);
        drawRect(l, b - 1, r, b, col);
        drawRect(l, t, l + 1, b, col);
        drawRect(r - 1, t, r, b, col);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        if (waitingForKey || editingCommand) return;
        
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();

        if (mx >= 10 && mx <= 80 && my >= h - 22 && my <= h - 6) {
            saveBinds();
            mc.displayGuiScreen(parent);
            return;
        }
        
        int bw = 120, bh = 24;
        int bx = w / 2 - bw / 2;
        int by = 35;
        if (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh) {
            binds.add(new BindEntry(Keyboard.KEY_NONE, ""));
            selectedBind = binds.size() - 1;
            waitingForKey = true;
            calculateMaxScroll();
            return;
        }
        
        int listY = 70;
        int listH = h - listY - 40;
        int listW = Math.min(500, w - 40);
        int listX = w / 2 - listW / 2;
        
        int yPos = listY + PADDING - scrollOffset;
        
        for (int i = 0; i < binds.size(); i++) {
            if (yPos + ENTRY_HEIGHT > listY && yPos < listY + listH) {
                int ex = listX + PADDING;
                int ey = yPos;
                int ew = listW - PADDING * 2;
                
                int delSize = 18;
                int delX = ex + ew - delSize - 8;
                int delY = ey + ENTRY_HEIGHT / 2 - delSize / 2;
                
                if (mx >= delX && mx <= delX + delSize && my >= delY && my <= delY + delSize) {
                    binds.remove(i);
                    if (selectedBind == i) selectedBind = -1;
                    else if (selectedBind > i) selectedBind--;
                    calculateMaxScroll();
                    return;
                }
                
                if (mx >= ex && mx <= ex + ew && my >= ey && my <= ey + ENTRY_HEIGHT) {
                    selectedBind = i;
                    if (btn == 0) {
                        waitingForKey = true;
                    } else if (btn == 1) {
                        editingCommand = true;
                        editingText = binds.get(i).command;
                    }
                    return;
                }
            }
            yPos += ENTRY_HEIGHT + ENTRY_GAP;
        }

        super.mouseClicked(mx, my, btn);
    }

    @Override
    protected void keyTyped(char ch, int key) throws IOException {
        if (waitingForKey) {
            if (key == Keyboard.KEY_ESCAPE) {
                if (selectedBind >= 0 && selectedBind < binds.size()) {
                    if (binds.get(selectedBind).keyCode == Keyboard.KEY_NONE) {
                        binds.remove(selectedBind);
                        selectedBind = -1;
                    }
                }
                waitingForKey = false;
            } else if (key != Keyboard.KEY_NONE) {
                if (selectedBind >= 0 && selectedBind < binds.size()) {
                    binds.get(selectedBind).keyCode = key;
                    waitingForKey = false;
                    if (binds.get(selectedBind).command.isEmpty()) {
                        editingCommand = true;
                        editingText = "";
                    }
                }
            }
            return;
        }
        
        if (editingCommand) {
            if (key == Keyboard.KEY_ESCAPE) {
                if (selectedBind >= 0 && selectedBind < binds.size()) {
                    if (binds.get(selectedBind).command.isEmpty() && editingText.isEmpty()) {
                        binds.remove(selectedBind);
                        selectedBind = -1;
                    }
                }
                editingCommand = false;
                editingText = "";
            } else if (key == Keyboard.KEY_RETURN) {
                if (selectedBind >= 0 && selectedBind < binds.size()) {
                    binds.get(selectedBind).command = editingText;
                }
                editingCommand = false;
                editingText = "";
            } else if (key == Keyboard.KEY_BACK) {
                if (editingText.length() > 0) {
                    editingText = editingText.substring(0, editingText.length() - 1);
                }
            } else if (ch >= 32 && ch < 127) {
                editingText += ch;
            }
            return;
        }
        
        if (key == Keyboard.KEY_ESCAPE) {
            saveBinds();
            mc.displayGuiScreen(parent);
            return;
        }
        
        super.keyTyped(ch, key);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = org.lwjgl.input.Mouse.getEventDWheel();
        if (scroll != 0) {
            scrollOffset = Math.max(0, Math.min(scrollOffset + (scroll > 0 ? -25 : 25), maxScroll));
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
