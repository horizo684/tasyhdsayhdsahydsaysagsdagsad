package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.Module.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class AutoSoup extends Module {
    private Minecraft mc = Minecraft.getMinecraft();
    private long lastSoupTime = 0;
    private int soupDelay = 300;
    private int animationTicks = 0;
    private int previousSlot = 0;
    
    private enum SoupState {
        IDLE,
        EATING,
        DROPPING_BOWL
    }
    
    private SoupState currentState = SoupState.IDLE;
    
    public AutoSoup() {
        super("AutoSoup", "Auto eat soup at 10 HP (5 hearts) or below", 0, Category.COMBAT);
    }
    
    public void onTick() {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        
        switch (currentState) {
            case EATING:
                animationTicks++;
                if (animationTicks >= 5) {
                    // After eating, immediately drop bowl
                    currentState = SoupState.DROPPING_BOWL;
                    animationTicks = 0;
                }
                break;
                
            case DROPPING_BOWL:
                animationTicks++;
                if (animationTicks == 1) {
                    // Drop bowl instantly and switch back
                    dropBowlFromCurrentSlot();
                    mc.thePlayer.inventory.currentItem = previousSlot;
                    currentState = SoupState.IDLE;
                    animationTicks = 0;
                }
                break;
                
            case IDLE:
                float health = mc.thePlayer.getHealth();
                if (health <= 10.0f) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastSoupTime >= soupDelay) {
                        int soupSlot = findSoupInHotbar();
                        if (soupSlot != -1) {
                            startEatingSoup(soupSlot);
                            lastSoupTime = currentTime;
                        }
                    }
                }
                break;
        }
    }
    
    private void startEatingSoup(int soupSlot) {
        previousSlot = mc.thePlayer.inventory.currentItem;
        mc.thePlayer.inventory.currentItem = soupSlot;
        
        ItemStack soupStack = mc.thePlayer.inventory.getStackInSlot(soupSlot);
        if (soupStack != null && soupStack.getItem() == Items.mushroom_stew) {
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, soupStack);
            currentState = SoupState.EATING;
            animationTicks = 0;
        }
    }
    
    private void dropBowlFromCurrentSlot() {
        if (mc.thePlayer == null) return;
        
        int currentSlot = mc.thePlayer.inventory.currentItem;
        ItemStack stack = mc.thePlayer.inventory.mainInventory[currentSlot];
        
        if (stack != null && stack.getItem() == Items.bowl) {
            mc.thePlayer.dropOneItem(false);
        }
    }
    
    private int findSoupInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() == Items.mushroom_stew) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    public void onEnable() {
        lastSoupTime = 0;
        animationTicks = 0;
        currentState = SoupState.IDLE;
    }
    
    @Override
    public void onDisable() {
        animationTicks = 0;
        currentState = SoupState.IDLE;
    }
}