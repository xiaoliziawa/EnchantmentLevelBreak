package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class Helper {
    @Unique
    private static final ThreadLocal<Boolean> IS_PROCESSING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "getEnchantmentCost", at = @At("HEAD"), cancellable = true)
    private static void onGetEnchantmentCost(RandomSource random, int enchantNum, int power, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (IS_PROCESSING.get()) {
            cir.setReturnValue(0);
            return;
        }
        
        try {
            IS_PROCESSING.set(true);
            int baseCost = Math.max(1, power);
            cir.setReturnValue(Math.min(baseCost * 2, 50000));
        } finally {
            IS_PROCESSING.set(false);
        }
    }
}
