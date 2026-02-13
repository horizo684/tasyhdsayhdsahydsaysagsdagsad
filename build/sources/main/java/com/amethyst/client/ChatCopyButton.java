package com.amethyst.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class ChatCopyButton {
    
    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        IChatComponent original = event.message;
        String originalText = original.getUnformattedText();
        
        ChatComponentText copyButton = new ChatComponentText(" §7[§a+§7]");
        
        copyButton.getChatStyle().setChatClickEvent(
            new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/copymsg " + originalText
            )
        );
        
        copyButton.getChatStyle().setChatHoverEvent(
            new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText("§aClick to copy")
            )
        );
        
        original.appendSibling(copyButton);
    }
    
    public static void copyToClipboard(String text) {
        try {
            StringSelection selection = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText("§aCopied to clipboard!")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}