package com.amethyst.client.modules;

import com.amethyst.client.HUDConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class CustomChat extends Module {

    private boolean showBackground = true;
    private float bgAlpha = 0.5f;

    public CustomChat() {
        super("CustomChat", "Movable chat with smooth animations", Keyboard.KEY_NONE, Category.RENDER);
    }

    @Override
    public void onEnable() {
        // Регистрируем обработчик
    }

    @Override
    public void onDisable() {
        // Восстанавливаем ванильный чат
        resetChatPosition();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderChat(RenderGameOverlayEvent.Chat event) {
        if (!this.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        GuiNewChat chat = mc.ingameGUI.getChatGUI();
        ScaledResolution sr = new ScaledResolution(mc);

        // Получаем сохраненные координаты
        int x = HUDConfig.getChatX();
        int y = HUDConfig.getChatY();

        // Если координаты не установлены, используем дефолтные (как в ванильном майнкрафте)
        if (x == -1) x = 2; // Ванильная позиция X
        if (y == -1) y = sr.getScaledHeight() - 48; // Ванильная позиция Y

        // ВАЖНО: Отменяем стандартную отрисовку чата
        event.setCanceled(true);

        // Рисуем чат в кастомной позиции
        net.minecraft.client.gui.Gui.drawRect(x, y, x + 320, y + 180, 
            showBackground ? (int)(bgAlpha * 255) << 24 : 0);

        // Сдвигаем рендер чата
        org.lwjgl.opengl.GL11.glPushMatrix();
        org.lwjgl.opengl.GL11.glTranslatef(x, y, 0);
        
        // Отрисовываем сам чат (всегда видим сообщения)
        chat.drawChat(mc.ingameGUI.getUpdateCounter());
        
        org.lwjgl.opengl.GL11.glPopMatrix();
    }

    private void resetChatPosition() {
        // Сбрасываем на ванильную позицию при отключении
        HUDConfig.setChatX(-1);
        HUDConfig.setChatY(-1);
    }

    public boolean isShowBackground() {
        return showBackground;
    }

    public void setShowBackground(boolean showBackground) {
        this.showBackground = showBackground;
    }

    public float getBgAlpha() {
        return bgAlpha;
    }

    public void setBgAlpha(float bgAlpha) {
        this.bgAlpha = bgAlpha;
    }
}