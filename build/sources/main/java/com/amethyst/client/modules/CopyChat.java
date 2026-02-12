package com.amethyst.client.modules;

import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import com.amethyst.client.Module.Category;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

public class CopyChat extends Module {
    private Minecraft mc = Minecraft.getMinecraft();

    public CopyChat() {
        super("CopyChat", "Click [+] next to chat messages to copy", 0, Category.MISC);
    }

    // Удалили добавление [+] - теперь иконка рисуется только в CustomChatRenderer

    public void copyToClipboard(String message) {
        try {
            // Убираем форматирование Minecraft и маркер [+]
            String clean = message.replaceAll("§[0-9a-fk-or]", "")
                                 .replace("[+]", "")
                                 .trim();
            
            StringSelection selection = new StringSelection(clean);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§a✔ §7Copied to clipboard!"));
            }
        } catch (Exception e) {
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§c✘ Failed to copy message!"));
            }
            e.printStackTrace();
        }
    }
}