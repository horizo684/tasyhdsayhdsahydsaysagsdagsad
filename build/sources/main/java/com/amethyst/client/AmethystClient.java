package com.amethyst.client;

import com.amethyst.client.irc.IRCManager;
import com.amethyst.client.irc.IRCNametagRenderer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraft.client.Minecraft;

@Mod(modid = AmethystClient.MODID, name = AmethystClient.NAME, version = AmethystClient.VERSION)
public class AmethystClient {
    public static final String MODID   = "amethystclient";
    public static final String NAME    = "AmethystClient";
    public static final String VERSION = "1.2";

    public static ModuleManager  moduleManager;
    public static FriendManager  friendManager;
    public static AmethystConfig config;
    public static CustomChatRenderer customChatRenderer;
    
    // IRC System
    public static IRCManager ircManager;
    public static IRCNametagRenderer ircNametagRenderer;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config       = new AmethystConfig(event.getSuggestedConfigurationFile());
        friendManager = new FriendManager();
        HUDConfig.load();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        
        moduleManager = new ModuleManager();
        KeyBindings.register();
        
        // === IRC SYSTEM ===
        System.out.println("[AmethystClient] Initializing IRC system");
        ircManager = new IRCManager(mc);
        ircManager.loadSettings();
        
        ircNametagRenderer = new IRCNametagRenderer(mc, ircManager);
        
        // Регистрируем IRC обработчики
        MinecraftForge.EVENT_BUS.register(ircManager);
        MinecraftForge.EVENT_BUS.register(ircNametagRenderer);
        
        System.out.println("[AmethystClient] IRC system initialized");
        System.out.println("[AmethystClient] Your IRC label: " + ircManager.getMyCustomLabel());
        
        // === ANIMATIONS (через рефлексию) ===
        System.out.println("[AmethystClient] Patching ItemRenderer for 1.7 animations");
        ItemRendererReflectionPatcher.patch(mc);
        if (ItemRendererReflectionPatcher.isPatched()) {
            System.out.println("[AmethystClient] ItemRenderer successfully patched!");
        } else {
            System.err.println("[AmethystClient] Failed to patch ItemRenderer!");
        }

        // Core renderers
        MinecraftForge.EVENT_BUS.register(new HUDRenderer());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new NametagRenderer());
        MinecraftForge.EVENT_BUS.register(new FriendTagRenderer());
        MinecraftForge.EVENT_BUS.register(new GuiModifier());
        MinecraftForge.EVENT_BUS.register(new CopyChatClickHandler());
        MinecraftForge.EVENT_BUS.register(new ScreenshotClickHandler());
        MinecraftForge.EVENT_BUS.register(new GuiNewChatHook());

        // Scoreboard + Chat renderers
        customChatRenderer = new CustomChatRenderer();
        MinecraftForge.EVENT_BUS.register(new ScoreboardRenderer());
        MinecraftForge.EVENT_BUS.register(customChatRenderer);
        MinecraftForge.EVENT_BUS.register(new VanillaScoreboardSuppressor());
        
        // Animation handlers
        MinecraftForge.EVENT_BUS.register(new AnimationHandler());
        MinecraftForge.EVENT_BUS.register(new DamageAnimationHandler());
        MinecraftForge.EVENT_BUS.register(new OrangeBlockhitHandler()); // Orange's blockhit через RenderHandEvent

        // Module-specific event listeners
        registerModuleListener("CPS Counter");
        registerModuleListener("Refill");
        registerModuleListener("Friends");
        registerModuleListener("Saturation");
        registerModuleListener("CopyChat");
        registerModuleListener("NoJumpDelay");
        registerModuleListener("NoHurtCam");
        registerModuleListener("AutoSprint");
        registerModuleListener("AsyncScreenshot");
        registerModuleListener("IRC"); // IRC модуль
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