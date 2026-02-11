package com.amethyst.client.modules;

import com.amethyst.client.AmethystClient;
import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

public class Friends extends Module {
    private Minecraft mc = Minecraft.getMinecraft();
    private boolean middleClickPressed = false;
    
    public Friends() {
        super("Friends", "Shows [FRIEND] tag above friends");
        this.setEnabled(true);
    }
    
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (!isEnabled() || mc.thePlayer == null) {
            return;
        }
        
        // Check for middle mouse button (button 2)
        if (Mouse.isButtonDown(2)) {
            if (!middleClickPressed) {
                middleClickPressed = true;
                handleMiddleClick();
            }
        } else {
            middleClickPressed = false;
        }
    }
    
    private void handleMiddleClick() {
        // Check what the player is looking at
        MovingObjectPosition mop = mc.objectMouseOver;
        
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            if (mop.entityHit instanceof EntityPlayer) {
                EntityPlayer targetPlayer = (EntityPlayer) mop.entityHit;
                String playerName = targetPlayer.getName();
                
                // Toggle friend status
                if (AmethystClient.friendManager.isFriend(playerName)) {
                    AmethystClient.friendManager.removeFriend(playerName);
                    if (mc.thePlayer != null) {
                        mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                            "§cRemoved §f" + playerName + " §cfrom friends"));
                    }
                } else {
                    AmethystClient.friendManager.addFriend(playerName);
                    if (mc.thePlayer != null) {
                        mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                            "§aAdded §f" + playerName + " §ato friends"));
                    }
                }
            }
        }
    }
}
