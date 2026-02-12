package com.amethyst.client;

import com.amethyst.client.modules.AsyncScreenshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ScreenshotClickHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        // Работаем только когда открыт чат
        if (!(event.gui instanceof GuiChat)) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        
        // Проверяем это ли клик левой кнопкой
        if (!org.lwjgl.input.Mouse.getEventButtonState()) return;
        if (org.lwjgl.input.Mouse.getEventButton() != 0) return;
        
        AsyncScreenshot mod = (AsyncScreenshot) AmethystClient.moduleManager.getModuleByName("AsyncScreenshot");
        if (mod == null || !mod.isEnabled()) return;
        
        // Получаем позицию мыши
        int mouseX = org.lwjgl.input.Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - org.lwjgl.input.Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;
        
        // Получаем компонент чата под курсором
        IChatComponent component = mc.ingameGUI.getChatGUI().getChatComponent(mouseX, mouseY);
        if (component == null) return;
        
        // Получаем полный текст компонента (включая невидимые маркеры)
        String fullText = component.getUnformattedText();
        String formattedText = component.getFormattedText();
        
        System.out.println("[AsyncScreenshot] Click on component:");
        System.out.println("  Unformatted: " + fullText);
        System.out.println("  Formatted: " + formattedText);
        
        // Проверяем наличие маркеров
        if (formattedText.contains("[SC_OPEN]") || fullText.contains("[SC_OPEN]")) {
            System.out.println("[AsyncScreenshot] OPEN button clicked");
            mod.openScreenshot();
            event.setCanceled(true);
        } else if (formattedText.contains("[SC_COPY]") || fullText.contains("[SC_COPY]")) {
            System.out.println("[AsyncScreenshot] COPY button clicked");
            mod.copyScreenshot();
            event.setCanceled(true);
        } else if (formattedText.contains("[SC_EXPORT]") || fullText.contains("[SC_EXPORT]")) {
            System.out.println("[AsyncScreenshot] EXPORT button clicked");
            mod.exportScreenshot();
            event.setCanceled(true);
        }
    }
}