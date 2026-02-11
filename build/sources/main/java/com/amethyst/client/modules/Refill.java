package com.amethyst.client.modules;

import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

public class Refill extends Module {
    private Minecraft mc = Minecraft.getMinecraft();
    
    // State machine
    private boolean isRefilling = false;
    private int state = 0;
    private int tickCounter = 0;
    private int currentHotbarSlot = 0;
    private int swapAttempts = 0; // Счётчик попыток свапа для предотвращения зависания
    
    // Ускоренные тайминги
    private static final int TICKS_TO_OPEN = 1;      // 1 тик
    private static final int TICKS_BETWEEN_SWAPS = 0; // 0 тиков = мгновенно!
    private static final int TICKS_TO_CLOSE = 1;     // 1 тик для быстрого закрытия
    private static final int MAX_SWAP_ATTEMPTS = 27; // Макс попыток (инвентарь = 27 слотов)
    
    public Refill() {
        super("Refill", "Press Z to refill soups from inventory to hotbar");
        this.setKeyCode(Keyboard.KEY_Z);
    }
    
    public void onTick() {
        if (!isEnabled() || mc.thePlayer == null || !isRefilling) {
            return;
        }
        
        tickCounter++;
        
        switch (state) {
            case 0: // STATE 0: Open inventory
                if (tickCounter >= 1) {
                    pressInventoryKey();
                    state = 1;
                    tickCounter = 0;
                }
                break;
                
            case 1: // STATE 1: Wait for inventory to open
                if (tickCounter >= TICKS_TO_OPEN) {
                    if (mc.currentScreen instanceof GuiInventory) {
                        state = 2;
                        tickCounter = 0;
                        currentHotbarSlot = 0;
                        swapAttempts = 0;
                    } else if (tickCounter > 10) {
                        // Инвентарь не открылся за 10 тиков - отменяем
                        reset();
                    }
                }
                break;
                
            case 2: // STATE 2: Swap soups to hotbar (МГНОВЕННО!)
                if (tickCounter >= TICKS_BETWEEN_SWAPS) {
                    boolean swapped = swapOneSoup();
                    swapAttempts++;
                    tickCounter = 0;
                    
                    // УСЛОВИЯ ДЛЯ ЗАВЕРШЕНИЯ СВАПА:
                    // 1. Не получилось переместить суп (нет супов или нет места)
                    // 2. ИЛИ хотбар полностью заполнен
                    // 3. ИЛИ превышено максимальное количество попыток (защита от зависания)
                    if (!swapped || isHotbarFull() || swapAttempts >= MAX_SWAP_ATTEMPTS) {
                        state = 3;
                        tickCounter = 0;
                    }
                }
                break;
                
            case 3: // STATE 3: Close inventory
                if (tickCounter >= 1) {
                    pressInventoryKey();
                    state = 4;
                    tickCounter = 0;
                }
                break;
                
            case 4: // STATE 4: Wait for inventory to close
                if (tickCounter >= TICKS_TO_CLOSE) {
                    // Проверяем что инвентарь РЕАЛЬНО закрыт
                    if (mc.currentScreen == null || !(mc.currentScreen instanceof GuiInventory)) {
                        // Инвентарь закрылся - завершаем
                        reset();
                    } else if (tickCounter > 10) {
                        // Инвентарь не закрылся за 10 тиков - форсируем закрытие
                        mc.thePlayer.closeScreen();
                        reset();
                    }
                }
                break;
        }
    }
    
    /**
     * Triggers refill sequence when Z is pressed
     */
    public void triggerRefill() {
        if (mc.thePlayer == null || isRefilling) {
            return;
        }
        
        isRefilling = true;
        state = 0;
        tickCounter = 0;
        currentHotbarSlot = 0;
        swapAttempts = 0;
    }
    
    /**
     * Simulates pressing the inventory key (E by default)
     */
    private void pressInventoryKey() {
        int invKeyCode = mc.gameSettings.keyBindInventory.getKeyCode();
        
        // Симулируем нажатие клавиши
        net.minecraft.client.settings.KeyBinding.setKeyBindState(invKeyCode, true);
        net.minecraft.client.settings.KeyBinding.onTick(invKeyCode);
        net.minecraft.client.settings.KeyBinding.setKeyBindState(invKeyCode, false);
    }
    
    /**
     * Swaps one soup from inventory to next empty hotbar slot
     * @return true if swap was successful, false if no soup found or no empty slots
     */
    private boolean swapOneSoup() {
        // Найти следующий пустой слот в хотбаре
        for (int slot = currentHotbarSlot; slot < 9; slot++) {
            ItemStack hotbarItem = mc.thePlayer.inventory.mainInventory[slot];
            
            if (hotbarItem == null) {
                // Слот пустой - ищем суп в инвентаре
                int soupSlot = findSoupInInventory();
                
                if (soupSlot != -1) {
                    // Найден суп - перемещаем через hotbar swap (mode 2)
                    performHotbarSwap(soupSlot, slot);
                    currentHotbarSlot = slot + 1;
                    return true;
                } else {
                    // Супов больше нет в инвентаре
                    return false;
                }
            }
            
            currentHotbarSlot = slot + 1;
        }
        
        // Все слоты хотбара заняты
        return false;
    }
    
    /**
     * Finds first soup in inventory (slots 9-35)
     * @return inventory slot index or -1 if not found
     */
    private int findSoupInInventory() {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() == Items.mushroom_stew) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Performs hotbar swap using windowClick mode 2
     */
    private void performHotbarSwap(int inventorySlot, int hotbarSlot) {
        // Convert inventory slot to window slot
        int windowSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
        
        // Mode 2 = swap with hotbar slot
        mc.playerController.windowClick(
            mc.thePlayer.inventoryContainer.windowId,
            windowSlot,
            hotbarSlot,
            2,
            mc.thePlayer
        );
    }
    
    /**
     * Checks if all hotbar slots are occupied
     * @return true if all slots are full, false if at least one slot is empty
     */
    private boolean isHotbarFull() {
        for (int i = 0; i < 9; i++) {
            if (mc.thePlayer.inventory.mainInventory[i] == null) {
                return false; // Есть пустой слот
            }
        }
        return true; // Все слоты заняты
    }
    
    /**
     * Resets refill state
     */
    private void reset() {
        isRefilling = false;
        state = 0;
        tickCounter = 0;
        currentHotbarSlot = 0;
        swapAttempts = 0;
    }
    
    @Override
    public void onEnable() {
        reset();
    }
    
    @Override
    public void onDisable() {
        // Force close inventory if refilling
        if (isRefilling && mc.currentScreen instanceof GuiInventory) {
            mc.thePlayer.closeScreen();
        }
        reset();
    }
}