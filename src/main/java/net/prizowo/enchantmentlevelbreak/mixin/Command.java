package net.prizowo.enchantmentlevelbreak.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(EnchantCommand.class)
public class Command {
    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    private static void onEnchant(CommandSourceStack source, Collection<? extends Entity> targets, Holder<Enchantment> enchantment, int level, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        int i = 0;
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity livingEntity) {
                ItemStack itemStack = livingEntity.getMainHandItem();
                if (!itemStack.isEmpty()) {
                    itemStack.enchant(enchantment, level);
                    i++;
                }
            }
        }
        cir.setReturnValue(i);
        cir.cancel();
    }
}
