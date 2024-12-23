package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EnchantingTableBlock.class})
public class EnchantmentMenuMixin {
    @Inject(method = {"isValidBookShelf"}, at = {@At("HEAD")}, cancellable = true)
    private static void onIsValidBookShelf(Level level, BlockPos pos, BlockPos offset, CallbackInfoReturnable<Boolean> cir) {
        BlockPos bookshelfPos = pos.offset((Vec3i)offset);
        BlockState state = level.getBlockState(bookshelfPos);
        cir.setReturnValue(Boolean.valueOf(state.is(Blocks.BOOKSHELF)));
    }
}
