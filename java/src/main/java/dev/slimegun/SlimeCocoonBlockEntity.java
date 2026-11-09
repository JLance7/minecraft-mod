package dev.slimegun;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

/** Expiry and the original air variant persist with the chunk. Scheduled block
 * ticks also persist, so a server restart cannot turn temporary slime permanent. */
public final class SlimeCocoonBlockEntity extends BlockEntity {
    private long expiry;
    private int airKind;

    public SlimeCocoonBlockEntity(BlockPos pos, BlockState state) { super(SlimeGunMod.COCOON_ENTITY, pos, state); }
    public void rememberAir(BlockState state) {
        airKind = state.isOf(Blocks.CAVE_AIR) ? 1 : state.isOf(Blocks.VOID_AIR) ? 2 : 0;
        markDirty();
    }
    public BlockState originalAir() { return (airKind == 1 ? Blocks.CAVE_AIR : airKind == 2 ? Blocks.VOID_AIR : Blocks.AIR).getDefaultState(); }
    public long expiresAt() { return expiry; }
    public void extend(long now) { expiry = FreezeRules.refreshedExpiry(expiry, now); markDirty(); }

    @Override protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries); nbt.putLong("ExpiresAt", expiry); nbt.putInt("AirKind", airKind);
    }
    @Override protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries); expiry = nbt.getLong("ExpiresAt"); airKind = nbt.getInt("AirKind");
    }
}
