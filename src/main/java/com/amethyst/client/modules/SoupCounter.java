package com.amethyst.client.modules;

import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;

public class SoupCounter extends Module {
    
    public SoupCounter() {
        super("SoupCounter", "Shows soup count in inventory");
        this.setEnabled(true);
    }

    public int getSoupCount() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return 0;
        
        int count = 0;
        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null && stack.getItem() == Items.mushroom_stew) {
                count += stack.stackSize;
            }
        }
        return count;
    }
}
