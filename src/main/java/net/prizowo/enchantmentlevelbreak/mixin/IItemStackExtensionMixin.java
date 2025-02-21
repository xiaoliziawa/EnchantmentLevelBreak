package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class IItemStackExtensionMixin implements IItemStackExtension {

    @Override
    public boolean canEquip(EquipmentSlot armorType, LivingEntity entity) {
        ItemStack self = (ItemStack)(Object)this;
        if (!(self.getItem() instanceof ArmorItem)) {
            return armorType == EquipmentSlot.HEAD ||
                    armorType == EquipmentSlot.CHEST ||
                    armorType == EquipmentSlot.LEGS ||
                    armorType == EquipmentSlot.FEET;
        }
        return IItemStackExtension.super.canEquip(armorType, entity);
    }
} 