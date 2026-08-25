package net.prizowo.enchantmentlevelbreak.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.prizowo.enchantmentlevelbreak.config.Config;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    @Unique
    private static final int MAX_ANVIL_COST = 50;

    @Shadow
    @Final
    private DataSlot cost;

    @Inject(method = "createResult", at = @At("RETURN"))
    private void onCreateResult(CallbackInfo ci) {
        ItemCombinerMenuAccessor accessor = (ItemCombinerMenuAccessor) this;
        Container inputSlots = accessor.enchantmentLevelBreak$getInputSlots();
        ItemStack left = inputSlots.getItem(0);
        ItemStack right = inputSlots.getItem(1);

        if (!left.isEmpty() && !right.isEmpty()) {
            handleAnvilOperation(accessor, left, right);
        }
    }

    @Unique
    private void handleAnvilOperation(ItemCombinerMenuAccessor accessor, ItemStack left, ItemStack right) {
        ItemEnchantments leftEnchants = left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments rightEnchants = right.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments leftStored = left.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments rightStored = right.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        ItemEnchantments effectiveLeft = !leftStored.isEmpty() ? leftStored : leftEnchants;
        ItemEnchantments effectiveRight = !rightStored.isEmpty() ? rightStored : rightEnchants;

        if (effectiveRight.isEmpty()) {
            return;
        }

        boolean sameItem = left.is(right.getItem());
        if (!sameItem && !right.is(Items.ENCHANTED_BOOK) && rightEnchants.isEmpty()) {
            return;
        }

        applyMerge(accessor, left, effectiveLeft, effectiveRight, sameItem);
    }

    @Unique
    private void applyMerge(ItemCombinerMenuAccessor accessor, ItemStack target, ItemEnchantments leftEnchants, ItemEnchantments rightEnchants, boolean isSameItemMerge) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(leftEnchants);
        boolean anyApplied = false;
        long totalCost = 0L;

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : rightEnchants.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            boolean canApply = isSameItemMerge || Config.allowAnyEnchantment || target.supportsEnchantment(enchantment);
            if (canApply) {
                int newLevel = calculateNewLevel(mutable.getLevel(enchantment), entry.getIntValue());
                mutable.set(enchantment, newLevel);
                totalCost += newLevel;
                anyApplied = true;
            }
        }

        if (!anyApplied) {
            return;
        }

        ItemStack result = target.copy();
        if (result.is(Items.ENCHANTED_BOOK)) {
            result.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
            result.remove(DataComponents.ENCHANTMENTS);
        } else {
            result.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }

        accessor.enchantmentLevelBreak$getResultSlots().setItem(0, result);
        this.cost.set((int) Math.min(totalCost, MAX_ANVIL_COST));
    }

    @Unique
    private int calculateNewLevel(int leftLevel, int rightLevel) {
        long newLevel;
        if (Config.allowLevelStacking) {
            newLevel = (long) leftLevel + rightLevel;
        } else if (Config.allowVanillaLevelStacking && leftLevel == rightLevel) {
            newLevel = (long) leftLevel + 1;
        } else {
            newLevel = Math.max(leftLevel, rightLevel);
        }
        return (int) Math.min(newLevel, Config.maxEnchantmentLevel);
    }
}
