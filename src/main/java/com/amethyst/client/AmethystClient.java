package com.amethyst.client;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = AmethystClient.MODID, name = AmethystClient.NAME, version = AmethystClient.VERSION)
public class AmethystClient {
    public static final String MODID = "amethystclient";
    public static final String NAME = "AmethystClient";
    public static final String VERSION = "1.1";

    public static ModuleManager moduleManager;
    public static FriendManager friendManager;
    public static AmethystConfig config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new AmethystConfig(event.getSuggestedConfigurationFile());
        friendManager = new FriendManager();
        HUDConfig.load();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        moduleManager = new ModuleManager();
        KeyBindings.register();

        MinecraftForge.EVENT_BUS.register(new HUDRenderer());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new NametagRenderer());
        MinecraftForge.EVENT_BUS.register(new FriendTagRenderer());
        MinecraftForge.EVENT_BUS.register(new GuiModifier());
        MinecraftForge.EVENT_BUS.register(new CopyChatClickHandler());

        Module cpsCounter = moduleManager.getModuleByName("CPS Counter");
        if (cpsCounter != null) MinecraftForge.EVENT_BUS.register(cpsCounter);

        Module refill = moduleManager.getModuleByName("Refill");
        if (refill != null) MinecraftForge.EVENT_BUS.register(refill);

        Module friends = moduleManager.getModuleByName("Friends");
        if (friends != null) MinecraftForge.EVENT_BUS.register(friends);

        Module saturation = moduleManager.getModuleByName("Saturation");
        if (saturation != null) MinecraftForge.EVENT_BUS.register(saturation);

        Module copyChat = moduleManager.getModuleByName("CopyChat");
        if (copyChat != null) MinecraftForge.EVENT_BUS.register(copyChat);

        Module noJumpDelay = moduleManager.getModuleByName("NoJumpDelay");
        if (noJumpDelay != null) MinecraftForge.EVENT_BUS.register(noJumpDelay);

        Module noHurtCam = moduleManager.getModuleByName("NoHurtCam");
        if (noHurtCam != null) MinecraftForge.EVENT_BUS.register(noHurtCam);

        Module autoSprint = moduleManager.getModuleByName("AutoSprint");
        if (autoSprint != null) MinecraftForge.EVENT_BUS.register(autoSprint);
    }
}