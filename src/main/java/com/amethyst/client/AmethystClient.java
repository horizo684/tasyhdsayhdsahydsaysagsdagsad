package com.amethyst.client;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = AmethystClient.MODID, name = AmethystClient.NAME, version = AmethystClient.VERSION)
public class AmethystClient {
    public static final String MODID   = "amethystclient";
    public static final String NAME    = "AmethystClient";
    public static final String VERSION = "1.2";

    public static ModuleManager  moduleManager;
    public static FriendManager  friendManager;
    public static AmethystConfig config;
    public static CustomChatRenderer customChatRenderer;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config       = new AmethystConfig(event.getSuggestedConfigurationFile());
        friendManager = new FriendManager();
        HUDConfig.load();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        moduleManager = new ModuleManager();
        KeyBindings.register();
        
        // Подменяем ItemRenderer на кастомный для анимаций
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            
            // Получаем EntityRenderer
            net.minecraft.client.renderer.EntityRenderer entityRenderer = mc.entityRenderer;
            
            if (entityRenderer == null) {
                System.err.println("[AmethystClient] EntityRenderer is null!");
            } else {
                // Ищем поле itemRenderer в EntityRenderer (пробуем разные варианты)
                java.lang.reflect.Field itemRendererField = null;
                
                // Пробуем обфусцированное имя
                try {
                    itemRendererField = net.minecraft.client.renderer.EntityRenderer.class.getDeclaredField("field_78516_c");
                } catch (NoSuchFieldException e1) {
                    // Пробуем деобфусцированное имя
                    try {
                        itemRendererField = net.minecraft.client.renderer.EntityRenderer.class.getDeclaredField("itemRenderer");
                    } catch (NoSuchFieldException e2) {
                        System.err.println("[AmethystClient] Could not find itemRenderer field!");
                    }
                }
                
                if (itemRendererField != null) {
                    itemRendererField.setAccessible(true);
                    
                    // Подменяем на кастомный
                    CustomItemRenderer customRenderer = new CustomItemRenderer(mc);
                    itemRendererField.set(entityRenderer, customRenderer);
                    
                    System.out.println("[AmethystClient] ✓ Successfully replaced ItemRenderer with CustomItemRenderer");
                } else {
                    System.err.println("[AmethystClient] ✗ Failed to find itemRenderer field");
                }
            }
        } catch (Exception e) {
            System.err.println("[AmethystClient] ✗ Failed to replace ItemRenderer: " + e.getMessage());
            e.printStackTrace();
        }

        // Core renderers
        MinecraftForge.EVENT_BUS.register(new HUDRenderer());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new NametagRenderer());
        MinecraftForge.EVENT_BUS.register(new FriendTagRenderer());
        MinecraftForge.EVENT_BUS.register(new GuiModifier());
        MinecraftForge.EVENT_BUS.register(new CopyChatClickHandler());
        MinecraftForge.EVENT_BUS.register(new ScreenshotClickHandler());
        MinecraftForge.EVENT_BUS.register(new GuiNewChatHook()); // ДОБАВЛЕНО - для обработки кликов по чату

        // NEW: Scoreboard + Chat renderers
        customChatRenderer = new CustomChatRenderer();
        MinecraftForge.EVENT_BUS.register(new ScoreboardRenderer());
        MinecraftForge.EVENT_BUS.register(customChatRenderer);
        // Suppress vanilla scoreboard sidebar when our module is active
        MinecraftForge.EVENT_BUS.register(new VanillaScoreboardSuppressor());
        
        // Animation handler for 1.7 animations
        MinecraftForge.EVENT_BUS.register(new AnimationHandler());
        
        // Damage animation handler for red armor effect
        MinecraftForge.EVENT_BUS.register(new DamageAnimationHandler());

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