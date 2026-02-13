package com.amethyst.client;

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

public class FriendTagRenderer {
    private final Minecraft mc = Minecraft.getMinecraft();
    
    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        EntityPlayer player = event.entityPlayer;
        
        if (player == mc.thePlayer) {
            return;
        }
        
        if (AmethystClient.friendManager.isFriend(player.getName())) {
            renderFriendTag(player, event.x, event.y, event.z);
        }
    }
    
    private void renderFriendTag(EntityPlayer player, double x, double y, double z) {
        FontRenderer fontRenderer = mc.fontRendererObj;
        RenderManager renderManager = mc.getRenderManager();
        
        String leftBracket = "[";
        String text = "FRIEND";
        String rightBracket = "]";
        
        float scale = 0.027f;
        
        GlStateManager.pushMatrix();
        
        // Position ABOVE player's head (увеличено с 0.8 до 1.1 чтобы было выше)
        GlStateManager.translate(x, y + player.height + 1.1, z);
        
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        int leftBracketWidth = fontRenderer.getStringWidth(leftBracket);
        int textWidth = fontRenderer.getStringWidth(text);
        int rightBracketWidth = fontRenderer.getStringWidth(rightBracket);
        int totalWidth = leftBracketWidth + textWidth + rightBracketWidth;
        int xOffset = -totalWidth / 2;
        
        // Draw semi-transparent background
        drawRect(xOffset - 2, -2, xOffset + totalWidth + 2, 10, 0x80000000);
        
        // Draw left bracket in aqua
        fontRenderer.drawString(leftBracket, xOffset, 0, 0xFF55FFFF);
        
        // Draw FRIEND in white
        fontRenderer.drawString(text, xOffset + leftBracketWidth, 0, 0xFFFFFFFF);
        
        // Draw right bracket in aqua
        fontRenderer.drawString(rightBracket, xOffset + leftBracketWidth + textWidth, 0, 0xFF55FFFF);
        
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        GlStateManager.popMatrix();
    }
    
    private void drawRect(int left, int top, int right, int bottom, int color) {
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;
        
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, alpha);
        
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}