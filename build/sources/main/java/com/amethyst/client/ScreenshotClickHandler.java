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
        // Працюємо тільки коли відкритий чат
        if (!(event.gui instanceof GuiChat)) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        
        // Перевіряємо чи це клік лівою кнопкою
        if (!org.lwjgl.input.Mouse.getEventButtonState()) return;
        if (org.lwjgl.input.Mouse.getEventButton() != 0) return;
        
        AsyncScreenshot mod = (AsyncScreenshot) AmethystClient.moduleManager.getModuleByName("AsyncScreenshot");
        if (mod == null || !mod.isEnabled()) return;
        
        // Отримуємо позицію миші
        int mouseX = org.lwjgl.input.Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - org.lwjgl.input.Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;
        
        // Отримуємо компонент чату під курсором
        IChatComponent component = mc.ingameGUI.getChatGUI().getChatComponent(mouseX, mouseY);
        if (component == null) return;
        
        // Перевіряємо чи є ClickEvent
        if (component.getChatStyle() == null || component.getChatStyle().getChatClickEvent() == null) return;
        
        String clickValue = component.getChatStyle().getChatClickEvent().getValue();
        
        // Перехоплюємо наші команди
        if (clickValue.equals("/screenshot_open")) {
            mod.openScreenshot();
            event.setCanceled(true);
        } else if (clickValue.equals("/screenshot_copy")) {
            mod.copyScreenshot();
            event.setCanceled(true);
        } else if (clickValue.equals("/screenshot_export")) {
            mod.exportScreenshot();
            event.setCanceled(true);
        }
    }
}