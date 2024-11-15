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

    public static String intToRoman(int num) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder sb = new StringBuilder();
        try {
            for (int i = 0; i < values.length; i++) {
                while (num >= values[i]) {
                    sb.append(symbols[i]);
                    num -= values[i];
                }
            }
        }
        catch (Exception e){
            sb = new StringBuilder(String.valueOf(num));
        }
        return sb.toString();
    }
}
