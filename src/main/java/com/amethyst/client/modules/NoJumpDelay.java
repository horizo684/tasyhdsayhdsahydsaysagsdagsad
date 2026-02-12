package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.Module.Category;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoJumpDelay extends Module {
    private Minecraft mc = Minecraft.getMinecraft();
    
    public NoJumpDelay() {
        super("NoJumpDelay", "Smart jump - only spam in 2-block spaces", 0, Category.MISC);
    }
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isEnabled() || mc.thePlayer == null) {
            return;
        }
        
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        if (!mc.gameSettings.keyBindJump.isKeyDown() || !mc.thePlayer.onGround) {
            return;
        }
        
        BlockPos playerPos = mc.thePlayer.getPosition();
        BlockPos headPos = playerPos.up(2);
        Block blockAbove = mc.theWorld.getBlockState(headPos).getBlock();
        
        if (!blockAbove.isAir(mc.theWorld, headPos)) {
            mc.thePlayer.jump();
        }
    }
}