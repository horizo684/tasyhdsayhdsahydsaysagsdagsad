package com.amethyst.client;

import com.amethyst.client.irc.IRCManager;
import com.amethyst.client.irc.IRCNametagRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = AmethystClient.MODID, name = AmethystClient.NAME, version = AmethystClient.VERSION)
public class AmethystClient {
    public static final String MODID   = "amethystclient";
    public static final String NAME    = "AmethystClient";
    public static final String VERSION = "1.2";

    public static ModuleManager     moduleManager;
    public static FriendManager     friendManager;
    public static AmethystConfig    config;
    public static CustomChatRenderer customChatRenderer;

    public static IRCManager        ircManager;
    public static IRCNametagRenderer ircNametagRenderer;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config        = new AmethystConfig(event.getSuggestedConfigurationFile());
        friendManager = new FriendManager();
        HUDConfig.load();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        moduleManager = new ModuleManager();
        KeyBindings.register();

        // IRC
        ircManager = new IRCManager(mc);
        ircManager.loadSettings();
        ircNametagRenderer = new IRCNametagRenderer(mc, ircManager);
        MinecraftForge.EVENT_BUS.register(ircManager);
        MinecraftForge.EVENT_BUS.register(ircNametagRenderer);

        // Core renderers
        MinecraftForge.EVENT_BUS.register(new HUDRenderer());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new NametagRenderer());
        MinecraftForge.EVENT_BUS.register(new FriendTagRenderer());
        MinecraftForge.EVENT_BUS.register(new GuiModifier());
        MinecraftForge.EVENT_BUS.register(new CopyChatClickHandler());
        MinecraftForge.EVENT_BUS.register(new ScreenshotClickHandler());
        MinecraftForge.EVENT_BUS.register(new GuiNewChatHook());

        // Scoreboard + Chat
        customChatRenderer = new CustomChatRenderer();
        MinecraftForge.EVENT_BUS.register(new ScoreboardRenderer());
        MinecraftForge.EVENT_BUS.register(customChatRenderer);
        MinecraftForge.EVENT_BUS.register(new VanillaScoreboardSuppressor());

        // Module listeners
        registerModuleListener("CPS Counter");
        registerModuleListener("Refill");
        registerModuleListener("Friends");
        registerModuleListener("Saturation");
        registerModuleListener("CopyChat");
        registerModuleListener("NoJumpDelay");
        registerModuleListener("NoHurtCam");
        registerModuleListener("AutoSprint");
        registerModuleListener("AsyncScreenshot");
        registerModuleListener("IRC");
        // Animations module registers Orange's listeners itself via onEnable()
    }

    private void registerModuleListener(String name) {
        Module m = moduleManager.getModuleByName(name);
        if (m != null) MinecraftForge.EVENT_BUS.register(m);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CopyMessageCommand());
    }
}
