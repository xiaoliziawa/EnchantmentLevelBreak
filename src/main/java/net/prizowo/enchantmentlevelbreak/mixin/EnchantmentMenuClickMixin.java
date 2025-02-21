package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class EnchantmentMenuClickMixin {
    
    @Inject(method = "onEnchantmentPerformed", at = @At("HEAD"))
    private void onEnchantmentPerformed(ItemStack enchantedItem, int levelCost, CallbackInfo ci) {
        if (!enchantedItem.getEnchantments().isEmpty()) {
            // 清空附魔
            enchantedItem.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
    }
} 