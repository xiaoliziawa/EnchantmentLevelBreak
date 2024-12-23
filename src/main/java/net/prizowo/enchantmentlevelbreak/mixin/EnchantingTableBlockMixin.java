package net.prizowo.enchantmentlevelbreak.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin({EnchantingTableBlock.class})
public class EnchantingTableBlockMixin {
    @Shadow
    @Final
    @Mutable
    public static List<BlockPos> BOOKSHELF_OFFSETS;

    @Inject(method = {"isValidBookShelf"}, at = {@At("HEAD")}, cancellable = true)
    private static void onIsValidBookShelf(Level level, BlockPos enchantingTablePos, BlockPos bookshelfPos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(enchantingTablePos.offset((Vec3i)bookshelfPos));
        cir.setReturnValue(Boolean.valueOf((state.getEnchantPowerBonus((LevelReader)level, enchantingTablePos.offset((Vec3i)bookshelfPos)) > 0.0F)));
    }

    @Inject(method = {"<clinit>"}, at = {@At("RETURN")})
    private static void onClinit(CallbackInfo ci) {
        BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-6, 0, -6, 6, 5, 6).filter(pos -> {
            int x = Math.abs(pos.getX());
            int z = Math.abs(pos.getZ());
            return ((x == 2 || x == 3 || x == 4 || x == 5 || x == 6 || z == 2 || z == 3 || z == 4 || z == 5 || z == 6) && (x <= 2 || z <= 2));
        }).map(BlockPos::immutable).toList();
    }
}
