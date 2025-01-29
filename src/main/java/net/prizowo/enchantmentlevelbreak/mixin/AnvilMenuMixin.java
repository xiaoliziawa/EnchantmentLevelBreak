package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
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
        if (IS_PROCESSING.get()) {
            return;
        }

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
        // 获取两种类型的附魔
        ItemEnchantments leftEnchants = left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments rightEnchants = right.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments leftStoredEnchants = left.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments rightStoredEnchants = right.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        // 获取实际有效的附魔
        ItemEnchantments effectiveLeftEnchants = !leftStoredEnchants.isEmpty() ? leftStoredEnchants : leftEnchants;
        ItemEnchantments effectiveRightEnchants = !rightStoredEnchants.isEmpty() ? rightStoredEnchants : rightEnchants;

        // 如果是相同的物品，处理附魔合并
        if (left.getItem() == right.getItem()) {
            if (!effectiveLeftEnchants.isEmpty() || !effectiveRightEnchants.isEmpty()) {
                handleEnchantmentMerge(left, effectiveLeftEnchants, effectiveRightEnchants, leftStoredEnchants.isEmpty(), rightStoredEnchants.isEmpty(), ci);
            }
            return; // 让原版处理没有附魔的物品合并
        }

        // 只处理附魔书的情况，其他情况让原版处理
        if (!effectiveRightEnchants.isEmpty() && isEnchantedBook(right)) {
            // 如果不允许任意物品附魔，检查是否为原版支持的附魔
            if (!Config.allowAnyEnchantment && !canEnchant(left, effectiveRightEnchants)) {
                return; // 让原版处理
            }
            handleEnchantmentMerge(left, effectiveLeftEnchants, effectiveRightEnchants, leftStoredEnchants.isEmpty(), rightStoredEnchants.isEmpty(), ci);
        }
    }

    @Unique
    private boolean isEnchantedBook(ItemStack stack) {
        return stack.getItem().toString().contains("enchanted_book");
    }

    @Unique
    private void handleEnchantmentMerge(ItemStack target, ItemEnchantments leftEnchants, ItemEnchantments rightEnchants, 
                                      boolean isLeftNormal, boolean isRightNormal, CallbackInfo ci) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(leftEnchants);
        int totalCost = 0;

        for (var entry : rightEnchants.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            int rightLevel = entry.getValue();
            int leftLevel = mutable.getLevel(enchantment);
            
            int newLevel = calculateNewLevel(leftLevel, rightLevel);
            mutable.set(enchantment, newLevel);
            totalCost += newLevel;
        }

        ItemStack result = target.copy();
        // 决定使用哪种类型的附魔
        if (isLeftNormal && !isRightNormal) {
            // 如果左边是普通附魔，右边是存储附魔，使用普通附魔
            result.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        } else if (!isLeftNormal) {
            // 如果左边是存储附魔，继续使用存储附魔
            result.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        } else {
            // 其他情况使用普通附魔
            result.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }

        this.resultSlots.setItem(0, result);
        this.repairItemCountCost = Math.min(totalCost, 50);
        this.cost.set(this.repairItemCountCost);
        ci.cancel();
    }

    @Unique
    private int calculateNewLevel(int leftLevel, int rightLevel) {
        if (Config.allowLevelStacking) {
            return leftLevel + rightLevel;
        } else {
            if (leftLevel == rightLevel) {
                return leftLevel + 1;
            } else {
                return Math.max(leftLevel, rightLevel);
            }
        }
    }

    @Unique
    private boolean canEnchant(ItemStack item, ItemEnchantments enchantments) {
        for (var entry : enchantments.entrySet()) {
            if (!entry.getKey().value().canEnchant(item)) {
                return false;
            }
        }
        return true;
    }
} 