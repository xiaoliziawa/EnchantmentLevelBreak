package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentDisplayMixin {
    @Inject(method = "getFullname", at = @At("HEAD"), cancellable = true)
    private static void onGetFullname(Holder<Enchantment> enchantment, int level, CallbackInfoReturnable<Component> cir) {
        MutableComponent name = enchantment.value().description().copy();
        if (level != 1) {
            name.append(" ").append(String.valueOf(level));
        }
        cir.setReturnValue(name);
    }
} 