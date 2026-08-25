package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.prizowo.enchantmentlevelbreak.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    private void onEnchant(Holder<Enchantment> enchantment, int level, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isEmpty() || level <= 0) {
            return;
        }

        int clampedLevel = Math.min(level, Config.maxEnchantmentLevel);
        boolean storedOnBook = stack.is(Items.ENCHANTED_BOOK);
        ItemEnchantments current = stack.getOrDefault(
                storedOnBook ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS,
                ItemEnchantments.EMPTY);

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);
        mutable.set(enchantment, clampedLevel);
        stack.set(storedOnBook ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS, mutable.toImmutable());
        ci.cancel();
    }
}
