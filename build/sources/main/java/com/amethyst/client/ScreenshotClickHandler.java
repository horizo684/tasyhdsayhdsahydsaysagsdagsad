package com.amethyst.client;

import com.amethyst.client.modules.AsyncScreenshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public class ScreenshotClickHandler {

    @SubscribeEvent
    public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();

        // Только левая кнопка мыши
        if (!Mouse.getEventButtonState() || Mouse.getEventButton() != 0) return;

        AsyncScreenshot mod = (AsyncScreenshot) AmethystClient.moduleManager.getModuleByName("AsyncScreenshot");
        if (mod == null || !mod.isEnabled()) return;

        int mouseX = Mouse.getEventX() * event.gui.width / mc.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / mc.displayHeight - 1;

        GuiNewChat chat = mc.ingameGUI.getChatGUI();
        IChatComponent component = chat.getChatComponent(mouseX, mouseY);
        if (component == null) return;

        // Смотрим что написано в hover-тексте компонента под курсором —
        // там спрятан маркер нужной кнопки
        if (component.getChatStyle() == null) return;

        net.minecraft.event.HoverEvent hover = component.getChatStyle().getChatHoverEvent();
        if (hover == null) return;

        IChatComponent hoverValue = hover.getValue();
        if (hoverValue == null) return;

        String hoverText = hoverValue.getFormattedText();

        if (hoverText.contains(AsyncScreenshot.MARKER_OPEN)) {
            mod.openScreenshot();
            event.setCanceled(true);
        } else if (hoverText.contains(AsyncScreenshot.MARKER_COPY)) {
            mod.copyScreenshot();
            event.setCanceled(true);
        } else if (hoverText.contains(AsyncScreenshot.MARKER_EXPORT)) {
            mod.exportScreenshot();
            event.setCanceled(true);
        }
    }
}