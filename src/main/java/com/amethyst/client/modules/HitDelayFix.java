package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.Module.Category;

public class HitDelayFix extends Module {
    
    public HitDelayFix() {
        super("HitDelayFix", "Removes attack delay", 0, Category.COMBAT);
    }

    @Override
    public void onEnable() {
        // Эта функция работает через reflection в EventHandler
    }

    @Override
    public void onDisable() {
    }
}
