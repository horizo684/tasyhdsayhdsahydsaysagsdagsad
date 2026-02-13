package com.amethyst.client.irc;

import com.amethyst.client.AmethystClient;
import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

/**
 * Рендерит IRC метки над головами игроков
 */
public class IRCNametagRenderer {
    
    private final Minecraft mc;
    private final IRCManager ircManager;
    
    public IRCNametagRenderer(Minecraft mc, IRCManager ircManager) {
        this.mc = mc;
        this.ircManager = ircManager;
    }
    
    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        EntityPlayer player = event.entityPlayer;
        
        // Лог 1: Событие сработало
        System.out.println("[IRC Renderer] Event fired for: " + player.getName());
        
        // Проверяем включен ли IRC модуль
        Module ircModule = AmethystClient.moduleManager.getModuleByName("IRC");
        if (ircModule == null) {
            System.out.println("[IRC Renderer] IRC module is NULL!");
            return;
        }
        if (!ircModule.isEnabled()) {
            System.out.println("[IRC Renderer] IRC module is DISABLED!");
            return;
        }
        System.out.println("[IRC Renderer] IRC module is ENABLED");
        
        // Не рендерим для себя (от первого лица)
        if (player == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) {
            System.out.println("[IRC Renderer] Skipping self (first person)");
            return;
        }
        
        UUID uuid = player.getUniqueID();
        System.out.println("[IRC Renderer] Looking for UUID: " + uuid);
        
        IRCUser ircUser = ircManager.getUser(uuid);
        
        // Лог: Найден ли пользователь
        if (ircUser != null) {
            System.out.println("[IRC Renderer] Found IRC user: " + ircUser.getCustomLabel());
            System.out.println("[IRC Renderer] Rendering tag for: " + player.getName() + " - " + ircUser.getCustomLabel());
        } else {
            System.out.println("[IRC Renderer] IRC user NOT FOUND for UUID: " + uuid);
            System.out.println("[IRC Renderer] Current IRC users:");
            AmethystClient.ircManager.debugPrintUsers();
        }
        
        // Если игрок не использует мод - не рендерим
        if (ircUser == null) {
            return;
        }
        
        // Рендерим кастомную метку
        renderIRCNametag(player, ircUser, event.x, event.y, event.z, event.partialRenderTick);
    }
    
    /**
     * Рендерит IRC метку над игроком
     */
    private void renderIRCNametag(EntityPlayer player, IRCUser ircUser, double x, double y, double z, float partialTicks) {
        System.out.println("[IRC Renderer] renderIRCNametag called for: " + player.getName());
        
        FontRenderer fontRenderer = mc.fontRendererObj;
        String label = ircUser.getCustomLabel();
        int color = ircUser.getColor();
        
        System.out.println("[IRC Renderer] Label: " + label + ", Color: 0x" + Integer.toHexString(color));
        
        // Позиция над головой игрока (немного выше обычного nametag)
        double yOffset = player.height + 0.5; // +0.5 чтобы было выше vanilla nametag
        
        // Расстояние до игрока
        double distance = player.getDistanceToEntity(mc.thePlayer);
        
        System.out.println("[IRC Renderer] Distance: " + distance + " blocks");
        
        // Не рендерим если слишком далеко
        if (distance > 64.0) {
            System.out.println("[IRC Renderer] Too far away, skipping render");
            return;
        }
        
        // Масштаб зависит от расстояния
        float scale = 0.016666668F * 1.6F; // Базовый масштаб nametag
        
        GlStateManager.pushMatrix();
        
        // Позиционирование
        GlStateManager.translate(x, y + yOffset, z);
        
        // Поворот к камере
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        
        // Масштабирование
        GlStateManager.scale(-scale, -scale, scale);
        
        // Отключаем освещение
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        
        // Включаем blend для прозрачности
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        
        // Отключаем текстуры для рисования фона
        GlStateManager.disableTexture2D();
        
        // Размеры
        int labelWidth = fontRenderer.getStringWidth(label);
        int halfWidth = labelWidth / 2;
        
        // Рисуем фон
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        
        int bgAlpha = 64; // Прозрачность фона
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(-halfWidth - 2, -2, 0.0).color(0.0F, 0.0F, 0.0F, bgAlpha / 255.0F).endVertex();
        worldRenderer.pos(-halfWidth - 2, 9, 0.0).color(0.0F, 0.0F, 0.0F, bgAlpha / 255.0F).endVertex();
        worldRenderer.pos(halfWidth + 2, 9, 0.0).color(0.0F, 0.0F, 0.0F, bgAlpha / 255.0F).endVertex();
        worldRenderer.pos(halfWidth + 2, -2, 0.0).color(0.0F, 0.0F, 0.0F, bgAlpha / 255.0F).endVertex();
        tessellator.draw();
        
        // Включаем текстуры для текста
        GlStateManager.enableTexture2D();
        
        // Рисуем текст с тенью
        fontRenderer.drawString(label, -halfWidth, 0, color);
        
        System.out.println("[IRC Renderer] ✓ Successfully rendered tag!");
        
        // Восстанавливаем состояние
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        
        GlStateManager.popMatrix();
    }
}