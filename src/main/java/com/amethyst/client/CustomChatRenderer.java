package com.amethyst.client;

import com.amethyst.client.modules.CustomChat;
import com.amethyst.client.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.IChatComponent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CustomChatRenderer {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    public static void renderCustomChat(ScaledResolution sr, int updateCounter) {
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.gameSettings.chatVisibility == net.minecraft.entity.player.EntityPlayer.EnumChatVisibility.HIDDEN) {
            return;
        }

        FontRenderer fontRenderer = mc.fontRendererObj;
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        List<String> chatLines = mc.ingameGUI.getChatGUI().getSentMessages();
        
        if (chatLines.isEmpty()) {
            return;
        }

        Module moduleObj = AmethystClient.moduleManager.getModuleByName("CustomChat");
        if (moduleObj == null || !(moduleObj instanceof CustomChat)) return;
        CustomChat mod = (CustomChat) moduleObj;
        if (!mod.isEnabled()) return;

        int chatX = 2;
        int chatY = screenHeight - 40;

        int chatWidth = 320;
        int chatHeight = 180;

        float scale = mod.getScale();
        int maxLines = mod.getMaxMessages();

        int displayLines = Math.min(chatLines.size(), maxLines);

        org.lwjgl.opengl.GL11.glPushMatrix();
        org.lwjgl.opengl.GL11.glScalef(scale, scale, 1.0f);

        int scaledX = (int) (chatX / scale);
        int scaledY = (int) ((chatY - displayLines * 9) / scale);

        int lineY = scaledY + displayLines * 9;

        for (int i = 0; i < displayLines; i++) {
            String message = chatLines.get(i);
            lineY -= 9;

            int textAlpha = 255;
            if (mod.isFadeMessages()) {
                float alpha = 0.5f + 0.5f * i / (float) displayLines;
                textAlpha = (int) (alpha * 255);
            }

            if (mod.isShowBackground()) {
                int bgAlpha = (int) (mod.getBgAlpha() * textAlpha);
                int bgColor = (bgAlpha << 24) | 0x000000;
                
                int messageWidth = fontRenderer.getStringWidth(message);
                net.minecraft.client.gui.Gui.drawRect(
                    scaledX - 2,
                    lineY - 1,
                    scaledX + messageWidth + 2,
                    lineY + 9,
                    bgColor
                );
            }

            String timestamp = "";
            if (mod.isShowTimestamps()) {
                timestamp = "§7[" + TIME_FORMAT.format(new Date()) + "] §r";
            }

            String fullMessage = timestamp + message;

            fontRenderer.drawStringWithShadow(
                fullMessage,
                scaledX,
                lineY,
                (textAlpha << 24) | (mod.getTextColor() & 0x00FFFFFF)
            );
        }

        org.lwjgl.opengl.GL11.glPopMatrix();
    }
}