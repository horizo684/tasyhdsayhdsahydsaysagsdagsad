package com.amethyst.client;

import com.amethyst.client.modules.CopyChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public class CopyChatClickHandler {

    @SubscribeEvent
    public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();

        // Проверяем что это нажатие левой кнопки мыши
        if (!Mouse.getEventButtonState() || Mouse.getEventButton() != 0) {
            return;
        }

        CopyChat copyChat = (CopyChat) AmethystClient.moduleManager.getModuleByName("CopyChat");
        if (copyChat == null || !copyChat.isEnabled()) {
            return;
        }

        // Получаем координаты мыши правильно
        int mouseX = Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;

        // Получаем компонент чата под курсором
        GuiNewChat chat = mc.ingameGUI.getChatGUI();
        IChatComponent component = chat.getChatComponent(mouseX, mouseY);
        
        if (component != null) {
            String fullText = component.getFormattedText();
            
            // Проверяем наличие [+] маркера
            if (fullText.contains("§7[§a+§7]")) {
                // Копируем текст всего сообщения
                copyChat.copyToClipboard(fullText);
                event.setCanceled(true); // Отменяем дальнейшую обработку клика
            }
        }
    }
}