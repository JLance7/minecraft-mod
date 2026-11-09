package dev.slimegun;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/** Server-authoritative placement: only vanilla air or our own cocoon blocks
 * may be touched. Chests, terrain, plants, liquids and other mods' blocks stay intact. */
public final class CocoonService {
    private CocoonService() {}

    public static boolean canFreeze(MobEntity mob) {
        return mob.isAlive() && !mob.isSpectator() && !mob.getType().isIn(SlimeGunMod.IMMUNE)
            && !(mob instanceof TameableEntity pet && pet.isTamed());
    }

    public static boolean encase(ServerWorld world, MobEntity target) {
        if (!canFreeze(target)) return false;
        boolean placed = false;
        Box box = target.getBoundingBox();
        for (FreezeRules.Cell cell : FreezeRules.cells(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
            BlockPos pos = new BlockPos(cell.x(), cell.y(), cell.z());
            if (!world.getWorldBorder().contains(pos) || world.isOutOfHeightLimit(pos)) continue;
            BlockState state = world.getBlockState(pos);
            boolean existing = state.isOf(SlimeGunMod.COCOON);
            if (!existing && !(state.isOf(Blocks.AIR) || state.isOf(Blocks.CAVE_AIR) || state.isOf(Blocks.VOID_AIR))) continue;
            if (!existing && !world.setBlockState(pos, SlimeGunMod.COCOON.getDefaultState(), Block.NOTIFY_ALL)) continue;
            if (world.getBlockEntity(pos) instanceof SlimeCocoonBlockEntity cocoon) {
                if (!existing) cocoon.rememberAir(state);
                cocoon.extend(world.getTime());
                world.scheduleBlockTick(pos, SlimeGunMod.COCOON, FreezeRules.FREEZE_TICKS);
                placed = true;
            }
        }
        if (!placed) return false;
        target.getNavigation().stop();
        target.setVelocity(0, 0, 0);
        target.fallDistance = 0;
        target.addStatusEffect(new StatusEffectInstance(SlimeGunMod.SLIMEBOUND, FreezeRules.FREEZE_TICKS, 0, false, true, true));
        return true;
    }

    public static boolean hasSlimeAt(LivingEntity entity) {
        Box box = entity.getBoundingBox();
        for (FreezeRules.Cell cell : FreezeRules.cells(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ))
            if (entity.getWorld().getBlockState(new BlockPos(cell.x(), cell.y(), cell.z())).isOf(SlimeGunMod.COCOON)) return true;
        return false;
    }
}
