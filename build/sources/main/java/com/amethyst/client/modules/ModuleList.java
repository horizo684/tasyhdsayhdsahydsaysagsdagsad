package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.Module.Category;

public class ModuleList extends Module {
    
    public ModuleList() {
        super("ArrayList", "Shows active modules list", 0, Category.MISC);
        this.setEnabled(true);
    }
}
