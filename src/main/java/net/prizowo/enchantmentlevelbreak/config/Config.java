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

    private static final ModConfigSpec.BooleanValue ALLOW_ANY_ENCHANTMENT_VALUE = BUILDER
            .comment("Allow applying any enchantment book to any item in anvil")
            .define("allowAnyEnchantment", false);

    private static final ModConfigSpec.BooleanValue ALLOW_LEVEL_STACKING_VALUE = BUILDER
            .comment("Allow unlimited enchantment level stacking in anvil (e.g. 4+4=8 instead of vanilla's 4+4=5)")
            .define("allowLevelStacking", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean useRomanNumerals;
    public static boolean allowAnyEnchantment;
    public static boolean allowLevelStacking;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        useRomanNumerals = USE_ROMAN_NUMERALS_VALUE.get();
        allowAnyEnchantment = ALLOW_ANY_ENCHANTMENT_VALUE.get();
        allowLevelStacking = ALLOW_LEVEL_STACKING_VALUE.get();
    }
}