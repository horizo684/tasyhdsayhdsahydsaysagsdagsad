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
        modules.add(new Scoreboard());    // ← NEW
        modules.add(new CustomChat());    // ← NEW

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
        modules.add(new AsyncScreenshot());
        modules.add(new ClickGUI());

        loadConfig();
    }

    public List<Module> getModules() { return modules; }

    public Module getModuleByName(String name) {
        for (Module m : modules)
            if (m.getName().equalsIgnoreCase(name)) return m;
        return null;
    }

    public List<Module> getEnabledModules() {
        List<Module> enabled = new java.util.ArrayList<>();
        for (Module m : modules) if (m.isEnabled()) enabled.add(m);
        return enabled;
    }

    public void saveConfig() {
        for (Module m : modules) {
            AmethystClient.config.set(m.getName() + ".enabled", m.isEnabled());
            m.saveSettings();  // Сохраняем специфические настройки модуля
        }
        AmethystClient.config.save();
    }

    public void loadConfig() {
        for (Module m : modules) {
            boolean en = AmethystClient.config.getBoolean(m.getName() + ".enabled", false);
            if (en) m.setEnabled(true);
            m.loadSettings();  // Загружаем специфические настройки модуля
        }
    }
}