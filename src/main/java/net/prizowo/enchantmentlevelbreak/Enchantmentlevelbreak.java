package net.prizowo.enchantmentlevelbreak;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod("enchantmentlevelbreak")
public class Enchantmentlevelbreak {
    public static final String MODID = "enchantmentlevelbreak";

    public Enchantmentlevelbreak() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CEnchantCommand.register(event.getDispatcher());
    }

}
