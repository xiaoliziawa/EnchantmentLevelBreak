package net.prizowo.enchantmentlevelbreak.mixin;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(EnchantmentMenu.class)
interface EnchantmentMenuAccessor {
    @Invoker("getEnchantmentList")
    List<EnchantmentInstance> invokeGetEnchantmentList(RegistryAccess registryAccess, ItemStack itemStack, int slot, int level);
}

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentTableTooltipMixin extends AbstractContainerScreen<EnchantmentMenu> {
    
    @Unique
    private List<EnchantmentInstance>[] cachedEnchantments = new List[3];
    @Unique
    private ItemStack lastItem = ItemStack.EMPTY;
    @Unique
    private int[] lastCosts = new int[3];
    
    public EnchantmentTableTooltipMixin(EnchantmentMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
    
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EnchantmentScreen;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V"), cancellable = true)
    private void onRenderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        boolean creative = minecraft.player.getAbilities().instabuild;
        int lapisCount = menu.getGoldCount();
        ItemStack currentItem = menu.getSlot(0).getItem();

        boolean needsUpdate = !ItemStack.matches(lastItem, currentItem);
        for(int i = 0; i < 3; i++) {
            if(lastCosts[i] != menu.costs[i]) {
                needsUpdate = true;
                break;
            }
        }
        
        if(needsUpdate) {
            lastItem = currentItem.copy();
            for(int i = 0; i < 3; i++) {
                lastCosts[i] = menu.costs[i];
                if(menu.costs[i] > 0) {
                    cachedEnchantments[i] = ((EnchantmentMenuAccessor)menu).invokeGetEnchantmentList(
                        minecraft.level.registryAccess(),
                        currentItem,
                        i,
                        menu.costs[i]
                    );
                } else {
                    cachedEnchantments[i] = null;
                }
            }
        }

        for(int slot = 0; slot < 3; ++slot) {
            int cost = menu.costs[slot];
            if (isHovering(60, 14 + 19 * slot, 108, 17, mouseX, mouseY) && cost > 0) {
                List<Component> tooltips = Lists.newArrayList();
                
                int enchantmentId = menu.enchantClue[slot];
                int level = menu.levelClue[slot];
                
                Optional<Holder.Reference<Enchantment>> enchantmentOpt = minecraft.level.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolder(enchantmentId);
                
                if (enchantmentOpt.isPresent()) {
                    tooltips.add(Enchantment.getFullname(enchantmentOpt.get(), level)
                        .copy().withStyle(ChatFormatting.GRAY));
                        
                    if (cachedEnchantments[slot] != null) {
                        for (EnchantmentInstance enchantment : cachedEnchantments[slot]) {
                            tooltips.add(Enchantment.getFullname(enchantment.enchantment, enchantment.level)
                                .copy().withStyle(ChatFormatting.GRAY));
                        }
                    }
                } else {
                    tooltips.add(Component.literal("").withStyle(ChatFormatting.GRAY));
                    tooltips.add(Component.translatable("neoforge.container.enchant.limitedEnchantability")
                        .withStyle(ChatFormatting.RED));
                }

                if (!creative) {
                    tooltips.add(CommonComponents.EMPTY);
                    
                    if (minecraft.player.experienceLevel < cost) {
                        tooltips.add(Component.translatable("container.enchant.level.requirement", cost)
                            .withStyle(ChatFormatting.RED));
                    } else {
                        int requiredLapis = slot + 1;
                        
                        MutableComponent lapisText = requiredLapis == 1
                            ? Component.translatable("container.enchant.lapis.one")
                            : Component.translatable("container.enchant.lapis.many", requiredLapis);
                        tooltips.add(lapisText.withStyle(lapisCount >= requiredLapis ? ChatFormatting.GRAY : ChatFormatting.RED));
                        
                        MutableComponent levelText = requiredLapis == 1
                            ? Component.translatable("container.enchant.level.one")
                            : Component.translatable("container.enchant.level.many", requiredLapis);
                        tooltips.add(levelText.withStyle(ChatFormatting.GRAY));
                    }
                }

                guiGraphics.renderComponentTooltip(font, tooltips, mouseX, mouseY);
                ci.cancel();
                break;
            }
        }
    }
} 