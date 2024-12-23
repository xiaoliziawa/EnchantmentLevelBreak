package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Mixin({EnchantmentHelper.class})
public class EnchantmentHelperEnchantMixin {
    @Inject(method = {"getEnchantmentCost"}, at = {@At("HEAD")}, cancellable = true)
    private static void onGetEnchantmentCost(RandomSource random, int enchantNum, int power, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (power > 0) {
            int baseCost = power * (enchantNum + 1) * 2;
            cir.setReturnValue(Integer.valueOf(Math.min(baseCost, 50000)));
        }
    }

    @Inject(method = {"getAvailableEnchantmentResults"}, at = {@At("HEAD")}, cancellable = true)
    private static void onGetAvailableEnchantmentResults(int level, ItemStack stack, Stream<Holder<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        List<EnchantmentInstance> list = new ArrayList<>();
        RandomSource random = RandomSource.create();
        boolean hasMainEnchant = false;
        Objects.requireNonNull(possibleEnchantments);
        for (Holder<Enchantment> holder : (Iterable<Holder<Enchantment>>)possibleEnchantments::iterator) {
            if (stack.isPrimaryItemFor(holder)) {
                if (!hasMainEnchant) {
                    int i = Math.max(1, level / 8);
                    list.add(new EnchantmentInstance(holder, i));
                    hasMainEnchant = true;
                    continue;
                }
                int displayLevel = Math.max(1, level / 8);
                int minLevel = Math.max(1, displayLevel - 2);
                int maxLevel = displayLevel;
                int enchantLevel = random.nextInt(minLevel, maxLevel + 1);
                list.add(new EnchantmentInstance(holder, enchantLevel));
            }
        }
        cir.setReturnValue(list);
    }

    @Inject(method = {"selectEnchantment"}, at = {@At("HEAD")})
    private static void onSelectEnchantment(RandomSource random, ItemStack stack, int level, Stream<Holder<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        level = Math.min((int)(level * 1.5), 2147483647);
    }
}
