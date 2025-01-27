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
                ItemEnchantments rightEnchants = right.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                if (!rightEnchants.isEmpty() && (Config.allowAnyEnchantment || canEnchant(left, rightEnchants))) {
                    if (!Config.allowLevelStacking) {
                        if (Config.allowAnyEnchantment) {
                            ItemEnchantments leftEnchants = left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(leftEnchants);
                            
                            int totalCost = 0;
                            for (var entry : rightEnchants.entrySet()) {
                                Holder<Enchantment> enchantment = entry.getKey();
                                int rightLevel = entry.getValue();
                                int leftLevel = mutable.getLevel(enchantment);
                                
                                mutable.set(enchantment, Math.max(leftLevel, rightLevel));
                                totalCost += Math.max(leftLevel, rightLevel);
                            }
                            
                            ItemStack result = left.copy();
                            result.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
                            this.resultSlots.setItem(0, result);
                            
                            this.repairItemCountCost = Math.min(totalCost, 50);
                            this.cost.set(this.repairItemCountCost);
                            ci.cancel();
                        }
                        return;
                    }

                    ItemEnchantments leftEnchants = left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                    ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(leftEnchants);
                    
                    int totalCost = 0;
                    for (var entry : rightEnchants.entrySet()) {
                        Holder<Enchantment> enchantment = entry.getKey();
                        int rightLevel = entry.getValue();
                        int leftLevel = mutable.getLevel(enchantment);
                        
                        int newLevel = leftLevel + rightLevel;
                        mutable.set(enchantment, newLevel);
                        totalCost += newLevel;
                    }
                    
                    ItemStack result = left.copy();
                    result.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
                    this.resultSlots.setItem(0, result);
                    
                    this.repairItemCountCost = Math.min(totalCost, 50);
                    this.cost.set(this.repairItemCountCost);
                    ci.cancel();
                }
            }
        } finally {
            IS_PROCESSING.set(false);
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