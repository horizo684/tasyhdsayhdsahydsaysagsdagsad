package com.amethyst.client.modules;

import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

public class PingCounter extends Module {
    private Minecraft mc = Minecraft.getMinecraft();
    
    public PingCounter() {
        super("Ping Counter", "Shows your current ping");
        this.setEnabled(true);
    }
    
    public String getText() {
        if (mc.thePlayer == null || mc.getCurrentServerData() == null) {
            return "Ping: 0ms";
        }
        
        try {
            NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            if (playerInfo != null) {
                return "Ping: " + playerInfo.getResponseTime() + "ms";
            }
        } catch (Exception e) {
            // Ignore
        }
        
        return "Ping: 0ms";
    }
}
