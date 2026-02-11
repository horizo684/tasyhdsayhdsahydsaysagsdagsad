package com.amethyst.client;

import com.amethyst.client.modules.Nametag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class NametagRenderer {
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        Nametag mod = (Nametag) AmethystClient.moduleManager.getModuleByName("Nametag");
        if (mod == null || !mod.isEnabled()) return;
        EntityPlayer p = event.entityPlayer;
        if (p != mc.thePlayer || mc.gameSettings.thirdPersonView == 0) return;
        render(p, event.x, event.y, event.z, mod);
    }

    private void render(EntityPlayer p, double x, double y, double z, Nametag mod) {
        FontRenderer fr = mc.fontRendererObj;
        RenderManager rm = mc.getRenderManager();

        String label = mod.getCustomLabel();
        String name  = p.getDisplayNameString();
        float scale  = 0.027f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + p.height + 0.5, z);
        GL11.glNormal3f(0f, 1f, 0f);
        GlStateManager.rotate(-rm.playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate(rm.playerViewX, 1f, 0f, 0f);
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        // Label row
        int lw = fr.getStringWidth(label);
        drawRect(-lw/2 - 2, -14, lw/2 + 2, -4, 0x80000000);
        int cx = -lw / 2;
        int total = label.length();
        
        // КРИТИЧЕСКИ ВАЖНО: Вызываем getCharColor() каждый кадр!
        // Функция внутри использует System.currentTimeMillis()
        for (int i = 0; i < total; i++) {
            char c = label.charAt(i);
            // getCharColor вычисляет цвет на основе ТЕКУЩЕГО времени - анимация!
            int color = mod.getCharColor(i, total);
            fr.drawString(String.valueOf(c), cx, -13, color);
            cx += fr.getCharWidth(c);
        }

        // Player name row
        int nw = fr.getStringWidth(name);
        drawRect(-nw/2 - 2, -2, nw/2 + 2, 10, 0x80000000);
        fr.drawString(name, -nw/2, 0, 0xFFFFFFFF);

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }

    private void drawRect(int l, int t, int r, int b, int color) {
        float a=(float)(color>>24&255)/255f, rv=(float)(color>>16&255)/255f,
              gv=(float)(color>>8&255)/255f,  bv=(float)(color&255)/255f;
        Tessellator ts=Tessellator.getInstance(); WorldRenderer wr=ts.getWorldRenderer();
        GlStateManager.enableBlend(); GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770,771,1,0);
        GlStateManager.color(rv,gv,bv,a);
        wr.begin(7, DefaultVertexFormats.POSITION);
        wr.pos(l,b,0).endVertex(); wr.pos(r,b,0).endVertex();
        wr.pos(r,t,0).endVertex(); wr.pos(l,t,0).endVertex();
        ts.draw();
        GlStateManager.enableTexture2D(); GlStateManager.disableBlend();
    }
}