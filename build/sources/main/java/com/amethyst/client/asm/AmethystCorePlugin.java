package com.amethyst.client.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * FML Core Plugin для регистрации ASM трансформера
 * Этот класс загружается ДО всех модов и регистрирует наш трансформер
 */
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.TransformerExclusions({"com.amethyst.client.asm"})
@IFMLLoadingPlugin.Name("AmethystCore")
public class AmethystCorePlugin implements IFMLLoadingPlugin {
    
    @Override
    public String[] getASMTransformerClass() {
        // Возвращаем класс нашего трансформера
        System.out.println("[AmethystCore] Registering ASM transformer: BlockhitSwingFixer");
        return new String[]{"com.amethyst.client.asm.BlockhitSwingFixer"};
    }
    
    @Override
    public String getModContainerClass() {
        return null;
    }
    
    @Override
    public String getSetupClass() {
        return null;
    }
    
    @Override
    public void injectData(Map<String, Object> data) {
        // Не используется
    }
    
    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}