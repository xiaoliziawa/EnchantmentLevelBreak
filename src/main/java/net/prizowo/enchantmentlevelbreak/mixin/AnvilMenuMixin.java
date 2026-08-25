package net.prizowo.enchantmentlevelbreak.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
        ItemEnchantments rightEnchants = EnchantmentHelper.getEnchantmentsForCrafting(right);
        if (rightEnchants.isEmpty()) {
            return;
        }

        boolean sameItem = left.is(right.getItem());
        if (!sameItem && !right.is(Items.ENCHANTED_BOOK)) {
            return;
        }

        ItemStack result = accessor.enchantmentLevelBreak$getResultSlots().getItem(0);
        if (result.isEmpty()) {
            result = left.copy();
        }
        if (!EnchantmentHelper.canStoreEnchantments(result)) {
            return;
        }

        ItemEnchantments leftEnchants = EnchantmentHelper.getEnchantmentsForCrafting(left);
        ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(leftEnchants);
        boolean anyApplied = false;
        long totalCost = 0L;

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : rightEnchants.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            boolean canApply = sameItem || Config.allowAnyEnchantment || left.supportsEnchantment(enchantment);
            if (!canApply) {
                continue;
            }
            int newLevel = calculateNewLevel(leftEnchants.getLevel(enchantment), entry.getIntValue());
            merged.set(enchantment, newLevel);
            totalCost += newLevel;
            anyApplied = true;
        }

        if (!anyApplied) {
            return;
        }

        EnchantmentHelper.setEnchantments(result, merged.toImmutable());
        accessor.enchantmentLevelBreak$getResultSlots().setItem(0, result);
        this.cost.set((int) Math.max(1L, Math.min(totalCost, MAX_ANVIL_COST)));
        ((AnvilMenu) (Object) this).broadcastChanges();
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
