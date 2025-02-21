package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(EnchantmentMenu.class)
public class EnchantmentMenuListMixin {
    
    @Inject(method = "getEnchantmentList", at = @At("HEAD"), cancellable = true)
    private void onGetEnchantmentList(RegistryAccess registryAccess, ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (!stack.getEnchantments().isEmpty()) {
            return;
        }
        
        if (isVanillaEnchantableItem(stack)) {
            return;
        }
        
        List<EnchantmentInstance> list = new ArrayList<>();
        
        long seed = level * 31L + slot;
        RandomSource random = RandomSource.create(seed);
        
        List<Holder<Enchantment>> allEnchantments = new ArrayList<>();
        registryAccess.registryOrThrow(Registries.ENCHANTMENT).holders().forEach(allEnchantments::add);
        
        Collections.shuffle(allEnchantments, new Random(seed));
        
        int enchantCount = random.nextInt(3, 6);
        
        int displayLevel = Math.max(1, level / 8);
        int minLevel = Math.max(1, displayLevel - 2);
        int maxLevel = displayLevel + 2;
        
        for (int i = 0; i < Math.min(enchantCount, allEnchantments.size()); i++) {
            long enchantSeed = seed + i * 7919L;
            RandomSource enchantRandom = RandomSource.create(enchantSeed);
            int enchantLevel = enchantRandom.nextInt(minLevel, maxLevel + 1);
            list.add(new EnchantmentInstance(allEnchantments.get(i), enchantLevel));
        }
        
        cir.setReturnValue(list);
    }

    // 排除下面的几个物品可以继续附魔，其余的物品只能附魔一次
    private boolean isVanillaEnchantableItem(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SwordItem ||
               item instanceof AxeItem ||
               item instanceof PickaxeItem ||
               item instanceof ShovelItem ||
               item instanceof HoeItem ||
               item instanceof BowItem ||
               item instanceof CrossbowItem ||
               item instanceof FishingRodItem ||
               item instanceof TridentItem ||
               item instanceof ArmorItem ||
               item instanceof ElytraItem ||
               item instanceof ShieldItem;
    }
} 