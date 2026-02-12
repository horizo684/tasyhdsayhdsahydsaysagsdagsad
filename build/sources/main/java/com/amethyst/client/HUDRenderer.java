package com.amethyst.client;

import com.amethyst.client.modules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Comparator;
import java.util.List;

public class HUDRenderer {
    
    private Minecraft mc = Minecraft.getMinecraft();
    
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        ScaledResolution sr = new ScaledResolution(mc);
        
        // Получаем ColorChanger для анимации
        ColorChanger colorChanger = (ColorChanger) AmethystClient.moduleManager.getModuleByName("ColorChanger");
        
        // Название клиента с анимацией
        String clientName = "AmethystClient";
        if (colorChanger != null && colorChanger.isEnabled()) {
            // АНИМИРОВАННОЕ название - каждая буква свой цвет!
            int x = 5;
            for (int i = 0; i < clientName.length(); i++) {
                char c = clientName.charAt(i);
                int color = colorChanger.getColor(i, clientName.length());
                mc.fontRendererObj.drawStringWithShadow(String.valueOf(c), x, 5, color);
                x += mc.fontRendererObj.getCharWidth(c);
            }
        } else {
            mc.fontRendererObj.drawStringWithShadow(clientName, 5, 5, 0xFF00D9FF);
        }
        
        FPSCounter fpsCounter = (FPSCounter) AmethystClient.moduleManager.getModuleByName("FPS Counter");
        if (fpsCounter != null && fpsCounter.isEnabled()) {
            mc.fontRendererObj.drawStringWithShadow(fpsCounter.getText(), HUDConfig.getFPSCounterX(), HUDConfig.getFPSCounterY(), 0xFFFFFFFF);
        }
        
        PingCounter pingCounter = (PingCounter) AmethystClient.moduleManager.getModuleByName("Ping Counter");
        if (pingCounter != null && pingCounter.isEnabled()) {
            mc.fontRendererObj.drawStringWithShadow(pingCounter.getText(), HUDConfig.getPingCounterX(), HUDConfig.getPingCounterY(), 0xFFFFFFFF);
        }
        
        Clock clock = (Clock) AmethystClient.moduleManager.getModuleByName("Clock");
        if (clock != null && clock.isEnabled()) {
            mc.fontRendererObj.drawStringWithShadow(clock.getText(), HUDConfig.getClockX(), HUDConfig.getClockY(), 0xFFFFFFFF);
        }
        
        CPSCounter cpsCounter = (CPSCounter) AmethystClient.moduleManager.getModuleByName("CPS Counter");
        if (cpsCounter != null && cpsCounter.isEnabled()) {
            mc.fontRendererObj.drawStringWithShadow(cpsCounter.getText(), HUDConfig.getCPSCounterX(), HUDConfig.getCPSCounterY(), 0xFFFFFFFF);
        }
        
        SoupCounter soupCounter = (SoupCounter) AmethystClient.moduleManager.getModuleByName("SoupCounter");
        if (soupCounter != null && soupCounter.isEnabled()) {
            int soupCount = soupCounter.getSoupCount();
            int soupX = HUDConfig.getSoupCounterX();
            int soupY = HUDConfig.getSoupCounterY();
            
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();
            
            ItemStack soupStack = new ItemStack(Items.mushroom_stew);
            mc.getRenderItem().renderItemAndEffectIntoGUI(soupStack, soupX, soupY);
            
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableBlend();
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
            
            mc.fontRendererObj.drawStringWithShadow(String.valueOf(soupCount), soupX + 20, soupY + 4, 0xFFFFFFFF);
        }
        
        ModuleList moduleList = (ModuleList) AmethystClient.moduleManager.getModuleByName("ArrayList");
        if (moduleList != null && moduleList.isEnabled()) {
            List<Module> enabledModules = AmethystClient.moduleManager.getEnabledModules();
            
            enabledModules.sort(new Comparator<Module>() {
                @Override
                public int compare(Module m1, Module m2) {
                    return Integer.compare(
                        mc.fontRendererObj.getStringWidth(m2.getName()),
                        mc.fontRendererObj.getStringWidth(m1.getName())
                    );
                }
            });
            
            int rightX = sr.getScaledWidth() - 5;
            int rightY = 5;
            int index = 0;
            
            for (Module module : enabledModules) {
                if (module.getName().equals("ArrayList") || module.getName().equals("ColorChanger")) {
                    continue;
                }
                
                String moduleName = module.getName();
                int width = mc.fontRendererObj.getStringWidth(moduleName);
                
                // КРИТИЧЕСКИ ВАЖНО: Вычисляем цвет КАЖДЫЙ КАДР!
                int color = 0xFFFFFFFF;
                if (colorChanger != null && colorChanger.isEnabled()) {
                    // Передаём index и total - функция сама пересчитает цвет на основе времени!
                    color = colorChanger.getColor(index, enabledModules.size());
                }
                
                mc.fontRendererObj.drawStringWithShadow(moduleName, rightX - width, rightY, color);
                rightY += 12;
                index++;
            }
        }
    }
}