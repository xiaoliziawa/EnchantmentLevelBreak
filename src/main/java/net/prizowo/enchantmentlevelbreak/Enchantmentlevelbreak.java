package net.prizowo.enchantmentlevelbreak;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.prizowo.enchantmentlevelbreak.config.Config;

@Mod(Enchantmentlevelbreak.MODID)
public class Enchantmentlevelbreak {
    public static final String MODID = "enchantmentlevelbreak";

    public Enchantmentlevelbreak(ModContainer modContainer, IEventBus modEventBus) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        NeoForge.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CEnchantCommand.register(event.getDispatcher());
    }
}
