package com.amethyst.client.modules;

import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

public class Hitbox extends Module {
    
    private int red = 255;
    private int green = 0;
    private int blue = 0;
    private int alpha = 180;
    
    private boolean showPlayers = true;
    private boolean showMobs = true;
    private boolean showAnimals = true;
    
    public Hitbox() {
        super("Hitbox", "Show entity hitboxes", 0, Category.RENDER);
    }
    
    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }
    
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer) continue;
            
            if (!shouldRenderEntity(entity)) continue;
            
            AxisAlignedBB box = entity.getEntityBoundingBox();
            
            double viewX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * event.partialTicks;
            double viewY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * event.partialTicks;
            double viewZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * event.partialTicks;
            
            double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks;
            double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks;
            double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks;
            
            AxisAlignedBB renderBox = new AxisAlignedBB(
                box.minX - entity.posX + entityX - viewX,
                box.minY - entity.posY + entityY - viewY,
                box.minZ - entity.posZ + entityZ - viewZ,
                box.maxX - entity.posX + entityX - viewX,
                box.maxY - entity.posY + entityY - viewY,
                box.maxZ - entity.posZ + entityZ - viewZ
            );
            
            renderHitbox(renderBox);
        }
    }
    
    private boolean shouldRenderEntity(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return showPlayers;
        } else if (entity instanceof EntityMob) {
            return showMobs;
        } else if (entity instanceof EntityAnimal) {
            return showAnimals;
        }
        return false;
    }
    
    private void renderHitbox(AxisAlignedBB box) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        
        float r = red / 255.0f;
        float g = green / 255.0f;
        float b = blue / 255.0f;
        float a = alpha / 255.0f;
        
        GL11.glColor4f(r, g, b, a);
        GL11.glLineWidth(2.0f);
        
        RenderGlobal.drawSelectionBoundingBox(box);
        
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    
    public int getRed() { return red; }
    public void setRed(int red) { this.red = Math.max(0, Math.min(255, red)); }
    
    public int getGreen() { return green; }
    public void setGreen(int green) { this.green = Math.max(0, Math.min(255, green)); }
    
    public int getBlue() { return blue; }
    public void setBlue(int blue) { this.blue = Math.max(0, Math.min(255, blue)); }
    
    public int getAlpha() { return alpha; }
    public void setAlpha(int alpha) { this.alpha = Math.max(0, Math.min(255, alpha)); }
    
    public boolean isShowPlayers() { return showPlayers; }
    public void setShowPlayers(boolean showPlayers) { this.showPlayers = showPlayers; }
    
    public boolean isShowMobs() { return showMobs; }
    public void setShowMobs(boolean showMobs) { this.showMobs = showMobs; }
    
    public boolean isShowAnimals() { return showAnimals; }
    public void setShowAnimals(boolean showAnimals) { this.showAnimals = showAnimals; }
    
    @Override
    public void saveSettings() {}
    
    @Override
    public void loadSettings() {}
}
