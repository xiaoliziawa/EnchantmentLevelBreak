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
                boolean isRightEnchantedBook = right.is(Items.ENCHANTED_BOOK);

                ItemEnchantments leftEnchants = left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments rightEnchants = right.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments leftStoredEnchants = left.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments rightStoredEnchants = right.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

                ItemEnchantments effectiveLeftEnchants = !leftStoredEnchants.isEmpty() ? leftStoredEnchants : leftEnchants;
                ItemEnchantments effectiveRightEnchants = !rightStoredEnchants.isEmpty() ? rightStoredEnchants : rightEnchants;

                if (!effectiveRightEnchants.isEmpty()) {
                    ItemStack result = left.copy();
                    ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(effectiveLeftEnchants);

                    boolean anyEnchantmentApplied = false;
                    int totalCost = 0;

                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : effectiveRightEnchants.entrySet()) {
                        Holder<Enchantment> enchantment = entry.getKey();
                        int rightLevel = entry.getIntValue();
                        boolean canApply = Config.allowAnyEnchantment || !isRightEnchantedBook || enchantment.value().canEnchant(left);
                        if (canApply) {
                            int leftLevel = mutable.getLevel(enchantment);
                            int newLevel;
                            if (Config.allowLevelStacking) {
                                newLevel = leftLevel + rightLevel;
                            } else {
                                newLevel = Math.max(leftLevel, rightLevel);
                                if (leftLevel == rightLevel) {
                                    newLevel = Math.min(newLevel + 1, Config.maxEnchantmentLevel);
                                }
                            }
                            newLevel = Math.min(newLevel, Config.maxEnchantmentLevel);
                            mutable.set(enchantment, newLevel);
                            totalCost += newLevel;
                            anyEnchantmentApplied = true;
                        }
                    }

                    if (anyEnchantmentApplied) {
                        if (result.is(Items.ENCHANTED_BOOK)) {
                            result.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
                            if (result.has(DataComponents.ENCHANTMENTS)) {
                                result.remove(DataComponents.ENCHANTMENTS);
                            }
                        } else {
                            result.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
                        }
                        this.resultSlots.setItem(0, result);
                        this.repairItemCountCost = Math.min(totalCost, 50);
                        this.cost.set(this.repairItemCountCost);
                        ci.cancel();
                    }
                }
            }
        } finally {
            IS_PROCESSING.set(false);
        }
    }
} 