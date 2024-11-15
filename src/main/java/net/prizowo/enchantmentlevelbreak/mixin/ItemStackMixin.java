package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Unique
    private static final ThreadLocal<Boolean> IS_PROCESSING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    private void onEnchant(Holder<Enchantment> enchantment, int level, CallbackInfo ci) {
        if (IS_PROCESSING.get()) {
            return;
        }

        try {
            IS_PROCESSING.set(true);
            ItemStack stack = (ItemStack)(Object)this;
            
            if (!stack.isEmpty() && level > 0) {
                ItemEnchantments currentEnchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(currentEnchantments);
                mutable.set(enchantment, level);
                stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
                ci.cancel();
            }
        } finally {
            IS_PROCESSING.set(false);
        }
    }
}
