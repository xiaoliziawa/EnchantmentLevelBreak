package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "getItemEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void onGetItemEnchantmentLevel(Holder<Enchantment> enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() > 0) {
            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            int actualLevel = enchantments.getLevel(enchantment);
            cir.setReturnValue(actualLevel);
        }
    }

    @Inject(method = "getEnchantmentCost", at = @At("RETURN"), cancellable = true)
    private static void onGetEnchantmentCost(RandomSource random, int enchantNum, int power, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (power > 0) {
            int cost = power * 2;
            cir.setReturnValue(Math.min(cost, 50000));
        }
    }
}