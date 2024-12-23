package net.prizowo.enchantmentlevelbreak.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.prizowo.enchantmentlevelbreak.Enchantmentlevelbreak;

@EventBusSubscriber(modid = Enchantmentlevelbreak.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    private static final ModConfigSpec.BooleanValue USE_ROMAN_NUMERALS_VALUE = BUILDER
            .comment("Use roman numerals for enchantment levels instead of arabic numbers")
            .define("useRomanNumerals", true);
            
    public static final ModConfigSpec SPEC = BUILDER.build();
    
    public static boolean useRomanNumerals;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        useRomanNumerals = USE_ROMAN_NUMERALS_VALUE.get();
    }
} 