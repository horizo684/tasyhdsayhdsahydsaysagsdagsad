package com.amethyst.client;

import com.amethyst.client.modules.Nametag;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;

public class NametagPickerGUI extends GuiScreen {

    private final GuiScreen parent;
    private final Nametag nametag;

    private String labelText;
    private boolean editingLabel = false;

    // Custom colour wheel (style index 14)
    private float customHue = 0f, customSat = 1f, customBri = 1f;
    private boolean draggingWheel = false, draggingBri = false;
    private int wheelCX, wheelCY;
    private static final int WHEEL_R = 50;
    private int briX, briY;
    private static final int BRI_W = 14, BRI_H = 105;

    // Grid layout
    private static final int COLS = 3, BTN_W = 88, BTN_H = 18, BTN_GAP = 4;

    public NametagPickerGUI(GuiScreen parent, Nametag nametag) {
        this.parent = parent;
        this.nametag = nametag;
        this.labelText = nametag.getCustomLabel().equals("AMETHYST USER") ? "" : nametag.getCustomLabel();
    }

    @Override
    public void initGui() {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();
        wheelCX = w - 100;
        wheelCY = h / 2 + 10;
        briX = wheelCX + WHEEL_R + 15;
        briY = wheelCY - BRI_H / 2;
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();

        drawGradientRect(0, 0, w, h, 0xF2060310, 0xF2010006);

        String title = "Nametag Style";
        mc.fontRendererObj.drawStringWithShadow(title,
            w/2 - mc.fontRendererObj.getStringWidth(title)/2, 7, 0xFFDDBBFF);

        drawStyleGrid(mx, my, w);
        drawLabelField(mx, my, w, h);

        if (nametag.getStyleIndex() == 14) drawCustomWheel(mx, my);

        drawPreview(w, h);
        drawBack(mx, my, h);

        super.drawScreen(mx, my, pt);
    }

    // ─── Style grid ───────────────────────────────────────────────────────────
    private void drawStyleGrid(int mx, int my, int w) {
        int names = Nametag.STYLE_NAMES.length;
        int gridW = COLS*BTN_W + (COLS-1)*BTN_GAP;
        int gx = w/2 - gridW/2 - 30, gy = 22;

        mc.fontRendererObj.drawString("§8Style preset:", gx, gy - 8, 0xFF445566);

        for (int i = 0; i < names; i++) {
            int col = i % COLS, row = i / COLS;
            int bx = gx + col*(BTN_W+BTN_GAP), by = gy + row*(BTN_H+BTN_GAP);
            boolean sel = i == nametag.getStyleIndex();
            boolean hov = mx>=bx && mx<=bx+BTN_W && my>=by && my<=by+BTN_H;

            // Animated gradient fill
            if (i == 12) {
                drawRect(bx, by, bx+BTN_W, by+BTN_H, 0xFFE0E0E0); // solid white
            } else if (i == 13) {
                drawRect(bx, by, bx+BTN_W, by+BTN_H, 0xFFFFAA00); // solid gold
            } else if (i == 14) {
                int cc = Color.HSBtoRGB(customHue, customSat, customBri);
                drawRect(bx, by, bx+BTN_W, by+BTN_H, cc | 0xFF000000);
            } else {
                for (int px = 0; px < BTN_W; px++) {
                    // Animated: uses real time! drawScreen runs every frame
                    int c = Nametag.getStyleColor(i, px, BTN_W, 0xFFFFFFFF);
                    drawRect(bx+px, by, bx+px+1, by+BTN_H, c);
                }
            }
            drawRect(bx, by, bx+BTN_W, by+BTN_H, 0x55000000);

            int border = sel ? 0xFFFFFFFF : (hov ? 0x99FFFFFF : 0x44FFFFFF);
            drawHollowRect(bx, by, bx+BTN_W, by+BTN_H, border);

            String name = Nametag.STYLE_NAMES[i];
            int nw = mc.fontRendererObj.getStringWidth(name);
            mc.fontRendererObj.drawStringWithShadow(name, bx+BTN_W/2-nw/2, by+BTN_H/2-4, 0xFFFFFFFF);
        }
    }

    // ─── Label field ──────────────────────────────────────────────────────────
    private void drawLabelField(int mx, int my, int w, int h) {
        int rows = (Nametag.STYLE_NAMES.length + COLS - 1) / COLS;
        int gridW = COLS*BTN_W + (COLS-1)*BTN_GAP;
        int gx = w/2 - gridW/2 - 30;
        int fy = 22 + rows*(BTN_H+BTN_GAP) + 8;

        mc.fontRendererObj.drawString("§8Label text:", gx, fy, 0xFF445566);

        int fw = gridW + 60;
        boolean hov = mx>=gx && mx<=gx+fw && my>=fy+10 && my<=fy+24;
        drawRect(gx, fy+10, gx+fw, fy+24, editingLabel ? 0xFF0D1525 : 0xFF080D18);
        drawHollowRect(gx, fy+10, gx+fw, fy+24, editingLabel ? 0xFF3366AA : (hov ? 0xFF223344 : 0xFF1A2530));

        String display = labelText.isEmpty() && !editingLabel
                ? "§8AMETHYST USER (default)"
                : labelText;
        if (editingLabel && (System.currentTimeMillis() % 1000) < 500) display += "§f|";
        mc.fontRendererObj.drawString(display, gx+4, fy+14, editingLabel ? 0xFFCCDDEE : 0xFF778899);

        mc.fontRendererObj.drawString("§8Click to edit — shown above your name in F5", gx, fy+27, 0xFF334455);
    }

    // ─── Custom colour wheel ──────────────────────────────────────────────────
    private void drawCustomWheel(int mx, int my) {
        drawColorWheel();
        int sx = (int)(wheelCX + Math.cos(customHue*2*Math.PI)*customSat*WHEEL_R);
        int sy = (int)(wheelCY + Math.sin(customHue*2*Math.PI)*customSat*WHEEL_R);
        drawCircle(sx, sy, 5, 0xFFFFFFFF);
        drawCircle(sx, sy, 3, Color.HSBtoRGB(customHue, customSat, customBri) | 0xFF000000);
        drawBriBar();
        int bhy = (int)(briY + (1f - customBri) * BRI_H);
        drawRect(briX-3, bhy-2, briX+BRI_W+3, bhy+2, 0xFFFFFFFF);
    }

    // ─── Animated preview ─────────────────────────────────────────────────────
    private void drawPreview(int w, int h) {
        int rows = (Nametag.STYLE_NAMES.length + COLS - 1) / COLS;
        int gridW = COLS*BTN_W + (COLS-1)*BTN_GAP;
        int gx = w/2 - gridW/2 - 30;
        int py = 22 + rows*(BTN_H+BTN_GAP) + 45;

        mc.fontRendererObj.drawString("§8Preview:", gx, py, 0xFF445566);

        String label = labelText.isEmpty() ? "AMETHYST USER" : labelText;
        int si = nametag.getStyleIndex();
        int charX = gx;
        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);
            int col = Nametag.getStyleColor(si, i, label.length(), nametag.getCustomColor());
            mc.fontRendererObj.drawStringWithShadow(String.valueOf(c), charX, py+11, col);
            charX += mc.fontRendererObj.getCharWidth(c);
        }
    }

    // ─── Back ─────────────────────────────────────────────────────────────────
    private void drawBack(int mx, int my, int h) {
        int bx=10, by=h-22, bw=70, bh=16;
        boolean hov = mx>=bx && mx<=bx+bw && my>=by && my<=by+bh;
        drawRect(bx, by, bx+bw, by+bh, hov ? 0xFF100D25 : 0xFF080812);
        drawHollowRect(bx, by, bx+bw, by+bh, 0xFF2A1A45);
        String t = "◀ Back";
        mc.fontRendererObj.drawStringWithShadow(t, bx+bw/2-mc.fontRendererObj.getStringWidth(t)/2, by+4, 0xFF8855CC);
    }

    // ─── GL helpers ───────────────────────────────────────────────────────────
    private void drawColorWheel() {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        Tessellator ts = Tessellator.getInstance();
        WorldRenderer wr = ts.getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(wheelCX, wheelCY, 0).color(255,255,255,255).endVertex();
        for (int i = 0; i <= 64; i++) {
            double a = 2*Math.PI*i/64;
            int x=(int)(wheelCX+Math.cos(a)*WHEEL_R), y=(int)(wheelCY+Math.sin(a)*WHEEL_R);
            int rgb = Color.HSBtoRGB((float)i/64, 1f, customBri);
            wr.pos(x,y,0).color((rgb>>16)&0xFF,(rgb>>8)&0xFF,rgb&0xFF,255).endVertex();
        }
        ts.draw();
        GlStateManager.enableTexture2D(); GlStateManager.disableBlend();
    }

    private void drawBriBar() {
        for (int i = 0; i < BRI_H; i++) {
            float b = 1f-(float)i/BRI_H;
            drawRect(briX, briY+i, briX+BRI_W, briY+i+1, Color.HSBtoRGB(customHue,customSat,b)|0xFF000000);
        }
        drawHollowRect(briX, briY, briX+BRI_W, briY+BRI_H, 0x55FFFFFF);
    }

    private void drawCircle(int cx,int cy,int r,int color) {
        GlStateManager.enableBlend(); GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA,1,0);
        Tessellator ts=Tessellator.getInstance(); WorldRenderer wr=ts.getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        int rv=(color>>16)&0xFF,gv=(color>>8)&0xFF,bv=color&0xFF,av=(color>>24)&0xFF;
        wr.pos(cx,cy,0).color(rv,gv,bv,av).endVertex();
        for (int i=0;i<=20;i++){double a=2*Math.PI*i/20;wr.pos(cx+Math.cos(a)*r,cy+Math.sin(a)*r,0).color(rv,gv,bv,av).endVertex();}
        ts.draw(); GlStateManager.enableTexture2D(); GlStateManager.disableBlend();
    }

    private void drawHollowRect(int l,int t,int r,int b,int c){
        drawRect(l,t,r,t+1,c);drawRect(l,b-1,r,b,c);drawRect(l,t,l+1,b,c);drawRect(r-1,t,r,b,c);
    }

    // ─── Input ────────────────────────────────────────────────────────────────
    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();

        if (mx>=10 && mx<=80 && my>=h-22 && my<=h-6) { mc.displayGuiScreen(parent); return; }

        int rows = (Nametag.STYLE_NAMES.length + COLS - 1) / COLS;
        int gridW = COLS*BTN_W + (COLS-1)*BTN_GAP;
        int gx = w/2 - gridW/2 - 30, gy = 22;

        for (int i = 0; i < Nametag.STYLE_NAMES.length; i++) {
            int col = i%COLS, row = i/COLS;
            int bx = gx+col*(BTN_W+BTN_GAP), by = gy+row*(BTN_H+BTN_GAP);
            if (mx>=bx && mx<=bx+BTN_W && my>=by && my<=by+BTN_H) {
                nametag.setStyleIndexCustom(i); editingLabel=false; return;
            }
        }

        // Label field
        int fy = 22 + rows*(BTN_H+BTN_GAP) + 18;
        int fw = gridW + 60;
        if (mx>=gx && mx<=gx+fw && my>=fy && my<=fy+14) {
            editingLabel = true; return;
        } else { editingLabel = false; }

        // Custom colour wheel
        if (nametag.getStyleIndex() == 14) {
            int dx=mx-wheelCX, dy=my-wheelCY;
            if (Math.sqrt(dx*dx+dy*dy)<=WHEEL_R) { draggingWheel=true; updateWheel(mx,my); return; }
            if (mx>=briX && mx<=briX+BRI_W && my>=briY && my<=briY+BRI_H) { draggingBri=true; updateBri(my); return; }
        }
        super.mouseClicked(mx, my, btn);
    }

    @Override
    protected void mouseReleased(int mx,int my,int state) { draggingWheel=false; draggingBri=false; super.mouseReleased(mx,my,state); }
    @Override
    protected void mouseClickMove(int mx,int my,int btn,long t) { if(draggingWheel)updateWheel(mx,my); if(draggingBri)updateBri(my); super.mouseClickMove(mx,my,btn,t); }

    @Override
    protected void keyTyped(char ch, int key) throws IOException {
        if (editingLabel) {
            if (key==14) { if(!labelText.isEmpty()) labelText=labelText.substring(0,labelText.length()-1); }
            else if (key==28||key==1) { editingLabel=false; }
            else if (ch>=32 && labelText.length()<32) { labelText+=ch; }
            nametag.setCustomLabel(labelText);
            return;
        }
        if (key==1) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(ch, key);
    }

    private void updateWheel(int mx,int my) {
        int dx=mx-wheelCX,dy=my-wheelCY;
        customHue=(float)((Math.atan2(dy,dx)/(2*Math.PI)+1.0)%1.0);
        customSat=Math.min(1f,(float)(Math.sqrt(dx*dx+dy*dy)/WHEEL_R));
        nametag.setCustomColor(Color.HSBtoRGB(customHue,customSat,customBri));
    }
    private void updateBri(int my) {
        customBri=1f-Math.max(0,Math.min(1f,(float)(my-briY)/BRI_H));
        nametag.setCustomColor(Color.HSBtoRGB(customHue,customSat,customBri));
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}