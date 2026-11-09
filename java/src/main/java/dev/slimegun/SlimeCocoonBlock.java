package dev.slimegun;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class SlimeCocoonBlock extends BlockWithEntity {
    public static final MapCodec<SlimeCocoonBlock> CODEC = createCodec(SlimeCocoonBlock::new);
    public SlimeCocoonBlock(Settings settings) { super(settings); }
    @Override protected MapCodec<? extends BlockWithEntity> getCodec() { return CODEC; }
    @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new SlimeCocoonBlockEntity(pos, state); }
    @Override protected BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBlockEntity(pos) instanceof SlimeCocoonBlockEntity cocoon) {
            if (world.getTime() < cocoon.expiresAt()) {
                // Re-shooting extends an existing cocoon. The first scheduled
                // tick must not remove it before the refreshed deadline.
                world.scheduleBlockTick(pos, this, FreezeRules.remainingDelay(cocoon.expiresAt(), world.getTime()));
            } else world.setBlockState(pos, cocoon.originalAir(), Block.NOTIFY_ALL);
        } else world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
    }
}
