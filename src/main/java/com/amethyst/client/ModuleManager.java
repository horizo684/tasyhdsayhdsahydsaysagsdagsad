package com.amethyst.client;

import com.amethyst.client.modules.*;
import java.util.List;

public class ModuleManager {
    private List<Module> modules;

    public ModuleManager() {
        modules = new java.util.ArrayList<>();

        // HUD Modules
        modules.add(new SoupCounter());
        modules.add(new FPSCounter());
        modules.add(new PingCounter());
        modules.add(new Clock());
        modules.add(new CPSCounter());
        
        // Visual Modules
        modules.add(new ModuleList());
        modules.add(new ColorChanger());
        modules.add(new Nametag());
        modules.add(new Friends());
        modules.add(new FullBright());
        modules.add(new Saturation());
        
        // Combat Modules
        modules.add(new HitDelayFix());
        modules.add(new AutoSoup());
        modules.add(new Refill());
        
        // Movement Modules
        modules.add(new AutoSprint());
        modules.add(new NoJumpDelay());
        
        // Misc Modules
        modules.add(new CopyChat());
        modules.add(new NoHurtCam());
        modules.add(new DiscordRPC());

        loadConfig();
    }

    public List<Module> getModules() {
        return modules;
    }

    public Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public List<Module> getEnabledModules() {
        List<Module> enabled = new java.util.ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) {
                enabled.add(module);
            }
        }
        return enabled;
    }

    public void saveConfig() {
        for (Module module : modules) {
            AmethystClient.config.set(module.getName() + ".enabled", module.isEnabled());
        }
        AmethystClient.config.save();
    }

    public void loadConfig() {
        for (Module module : modules) {
            boolean enabled = AmethystClient.config.getBoolean(module.getName() + ".enabled", false);
            if (enabled) {
                module.setEnabled(true);
            }
        }
    }
}