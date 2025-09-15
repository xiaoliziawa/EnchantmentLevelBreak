package net.prizowo.enchantmentlevelbreak.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.prizowo.enchantmentlevelbreak.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow public int repairItemCountCost;
    @Shadow private final DataSlot cost = DataSlot.standalone();

    protected AnvilMenuMixin(int containerId, ContainerLevelAccess access) {
        super(null, containerId, null, access);
    }

    @Unique
    private static final ThreadLocal<Boolean> IS_PROCESSING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void onCreateResult(CallbackInfo ci) {
        if (IS_PROCESSING.get()) return;

        try {
            IS_PROCESSING.set(true);
            ItemStack left = this.inputSlots.getItem(0);
            ItemStack right = this.inputSlots.getItem(1);

            if (!left.isEmpty() && !right.isEmpty()) {
                handleAnvilOperation(left, right, ci);
            }
        } finally {
            IS_PROCESSING.set(false);
        }
    }

    @Unique
    private void handleAnvilOperation(ItemStack left, ItemStack right, CallbackInfo ci) {
        boolean sameItem = left.is(right.getItem());
        boolean rightIsBook = right.is(Items.ENCHANTED_BOOK);

        ItemEnchantments leftEnchants = left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments rightEnchants = right.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments leftStoredEnchants = left.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments rightStoredEnchants = right.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        ItemEnchantments effectiveLeft = !leftStoredEnchants.isEmpty() ? leftStoredEnchants : leftEnchants;
        ItemEnchantments effectiveRight = !rightStoredEnchants.isEmpty() ? rightStoredEnchants : rightEnchants;

        if (sameItem) {
            if (!effectiveLeft.isEmpty() || !effectiveRight.isEmpty()) {
                handleEnchantmentMerge(left, effectiveLeft, effectiveRight, true, ci);
            }
            return;
        }
        if (!effectiveRight.isEmpty() && (rightIsBook || !rightEnchants.isEmpty())) {
            handleEnchantmentMerge(left, effectiveLeft, effectiveRight, false, ci);
        }
    }

    @Unique
    private void handleEnchantmentMerge(ItemStack target, ItemEnchantments leftEnchants, ItemEnchantments rightEnchants, boolean isSameItemMerge, CallbackInfo ci) {
        ItemStack result = target.copy();
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(leftEnchants);
        boolean anyApplied = false;
        int totalCost = 0;

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : rightEnchants.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            int rightLevel = entry.getIntValue();
            boolean canApply = isSameItemMerge || Config.allowAnyEnchantment || enchantment.value().canEnchant(target);
            if (canApply) {
                int leftLevel = mutable.getLevel(enchantment);
                int newLevel = calculateNewLevel(leftLevel, rightLevel);
                newLevel = Math.min(newLevel, Config.maxEnchantmentLevel);
                mutable.set(enchantment, newLevel);
                totalCost += newLevel;
                anyApplied = true;
            }
        }

        if (anyApplied) {
            if (result.is(Items.ENCHANTED_BOOK)) {
                result.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
                if (result.has(DataComponents.ENCHANTMENTS)) result.remove(DataComponents.ENCHANTMENTS);
            } else {
                result.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
            }
            this.resultSlots.setItem(0, result);
            this.repairItemCountCost = Math.min(totalCost, 50);
            this.cost.set(this.repairItemCountCost);
            ci.cancel();
        }
    }

    @Unique
    private int calculateNewLevel(int leftLevel, int rightLevel) {
        // 死妈东西，我和你爆了😡
        // 优先级: allowLevelStacking > allowVanillaLevelStacking
        if (Config.allowLevelStacking) {
            // allowLevelStacking为true时，直接相加 (5+5=10)
            return leftLevel + rightLevel;
        } else if (Config.allowVanillaLevelStacking && leftLevel == rightLevel) {
            // allowVanillaLevelStacking为true且相同等级时，+1 (5+5=6)
            return leftLevel + 1;
        } else {
            // 两个都为false时，使用原版机制，取最大值 (5+5=5)
            return Math.max(leftLevel, rightLevel);
        }
    }
}
