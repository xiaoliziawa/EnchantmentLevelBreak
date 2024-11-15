package net.prizowo.enchantmentlevelbreak;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class CEnchantCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ENCHANTMENTS = (context, builder) ->
            SharedSuggestionProvider.suggestResource(context.getSource().registryAccess().registryOrThrow(Registries.ENCHANTMENT).keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cenchant")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enchantment", StringArgumentType.greedyString())
                        .suggests(SUGGEST_ENCHANTMENTS)
                        .executes(context -> enchantItem(context.getSource(),
                                StringArgumentType.getString(context, "enchantment"),
                                1))
                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                .executes(context -> enchantItem(context.getSource(),
                                        StringArgumentType.getString(context, "enchantment"),
                                        IntegerArgumentType.getInteger(context, "level"))))));
    }

    private static int enchantItem(CommandSourceStack source, String enchantmentInput, int level) throws CommandSyntaxException {
        Player player = source.getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();

        if (itemStack.isEmpty()) {
            source.sendFailure(Component.literal("You must be holding an item to enchant"));
            return 0;
        }

        String[] parts = enchantmentInput.split("\\s+", 2);
        String enchantmentName = parts[0];
        if (parts.length > 1) {
            try {
                level = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        ResourceLocation enchantmentId;
        if (!enchantmentName.contains(":")) {
            enchantmentId = ResourceLocation.fromNamespaceAndPath("minecraft", enchantmentName);
        } else {
            enchantmentId = ResourceLocation.parse(enchantmentName);
        }

        Holder<Enchantment> enchantment = source.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(enchantmentId)
                .orElse(null);

        if (enchantment == null) {
            source.sendFailure(Component.literal("Invalid enchantment: " + enchantmentName));
            return 0;
        }

        ItemEnchantments currentEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(currentEnchantments);
        mutable.set(enchantment, level);
        itemStack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());

        int finalLevel = level;
        source.sendSuccess(() -> Component.literal("Applied " + Enchantment.getFullname(enchantment, finalLevel).getString() + " to the item"), true);

        return 1;
    }
}
