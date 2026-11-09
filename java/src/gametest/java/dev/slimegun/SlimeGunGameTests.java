package dev.slimegun;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.*;
import net.minecraft.util.math.*;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;
import net.minecraft.block.entity.ChestBlockEntity;

/** Actual Minecraft tests, not fake world stubs. Every test uses a temporary structure. */
public final class SlimeGunGameTests implements FabricGameTest {
    private static HuskEntity mob(TestContext context) {
        context.setBlockState(new BlockPos(3, 0, 3), Blocks.STONE);
        HuskEntity mob = context.spawnEntity(EntityType.HUSK, new Vec3d(3.5, 1, 3.5));
        mob.setInvulnerable(true);
        return mob;
    }
    private static void check(boolean value, String message) { if (!value) throw new GameTestException(message); }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 220)
    public void freezesAndThawsWithoutPermanentAiFlags(TestContext context) {
        HuskEntity target = mob(context);
        Vec3d anchor = target.getPos();
        check(CocoonService.encase(context.getWorld(), target), "Cocoon should be placed");
        check(target.hasStatusEffect(SlimeGunMod.SLIMEBOUND), "Mob must receive the freeze effect");
        check(!target.isAiDisabled() && !target.hasNoGravity(), "Persistent flags must remain unchanged");
        target.setVelocity(1, 1, 1);
        context.runAtTick(30, () -> check(target.getPos().squaredDistanceTo(anchor) < .001, "Mob must stay frozen despite velocity"));
        context.runAtTick(180, () -> {
            check(!target.hasStatusEffect(SlimeGunMod.SLIMEBOUND), "Freeze should expire");
            check(!CocoonService.hasSlimeAt(target), "Temporary slime should disappear");
            check(context.getWorld().getBlockState(context.getAbsolutePos(new BlockPos(3,0,3))).isOf(Blocks.STONE), "Floor must be preserved");
            context.complete();
        });
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 80)
    public void breakingTheCocoonReleasesTheMob(TestContext context) {
        HuskEntity target = mob(context); CocoonService.encase(context.getWorld(), target);
        context.runAtTick(5, () -> {
            Box box = target.getBoundingBox();
            for (FreezeRules.Cell c : FreezeRules.cells(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)) {
                BlockPos pos = new BlockPos(c.x(), c.y(), c.z());
                if (context.getWorld().getBlockState(pos).isOf(SlimeGunMod.COCOON)) context.getWorld().breakBlock(pos, false);
            }
        });
        context.runAtTick(10, () -> { check(!target.hasStatusEffect(SlimeGunMod.SLIMEBOUND), "Breaking all slime must release early"); context.complete(); });
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 330)
    public void repeatedShotsExtendTheTimer(TestContext context) {
        HuskEntity target = mob(context); CocoonService.encase(context.getWorld(), target);
        context.runAtTick(100, () -> CocoonService.encase(context.getWorld(), target));
        context.runAtTick(180, () -> check(CocoonService.hasSlimeAt(target) && target.hasStatusEffect(SlimeGunMod.SLIMEBOUND), "Re-shot cocoon must survive the first deadline"));
        context.runAtTick(285, () -> { check(!CocoonService.hasSlimeAt(target) && !target.hasStatusEffect(SlimeGunMod.SLIMEBOUND), "Refreshed cocoon must eventually expire"); context.complete(); });
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void expiryAndCaveAirSurviveNbtRoundTrip(TestContext context) {
        BlockPos pos = context.getAbsolutePos(new BlockPos(2, 2, 2));
        context.getWorld().setBlockState(pos, SlimeGunMod.COCOON.getDefaultState());
        var original = (SlimeCocoonBlockEntity) context.getWorld().getBlockEntity(pos);
        original.rememberAir(Blocks.CAVE_AIR.getDefaultState()); original.extend(context.getWorld().getTime());
        NbtCompound nbt = original.createNbt(context.getWorld().getRegistryManager());
        var restored = new SlimeCocoonBlockEntity(pos, SlimeGunMod.COCOON.getDefaultState());
        restored.read(nbt, context.getWorld().getRegistryManager());
        check(restored.expiresAt() == original.expiresAt(), "Expiry must persist");
        check(restored.originalAir().isOf(Blocks.CAVE_AIR), "Original air type must persist"); context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
    public void primedCreeperCannotExplodeWhileFrozen(TestContext context) {
        context.setBlockState(new BlockPos(3,0,3), Blocks.STONE);
        CreeperEntity creeper = context.spawnEntity(EntityType.CREEPER, new Vec3d(3.5,1,3.5));
        creeper.ignite();
        check(CocoonService.encase(context.getWorld(), creeper), "Creeper should be encased");
        context.runAtTick(65, () -> { check(creeper.isAlive() && !creeper.isRemoved(), "Frozen creeper fuse must pause"); context.complete(); });
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void existingChestAndItsContentsAreNotReplaced(TestContext context) {
        HuskEntity target = mob(context);
        BlockPos chestPos = context.getAbsolutePos(new BlockPos(3,2,3));
        context.getWorld().setBlockState(chestPos, Blocks.CHEST.getDefaultState());
        ChestBlockEntity chest = (ChestBlockEntity) context.getWorld().getBlockEntity(chestPos);
        chest.setStack(0, new ItemStack(Items.DIAMOND, 16));
        check(CocoonService.encase(context.getWorld(), target), "Available air should still form a cocoon");
        check(context.getWorld().getBlockEntity(chestPos) == chest && chest.getStack(0).getCount() == 16, "Chest data must remain intact");
        context.complete();
    }

    private static PlayerEntity shooter(TestContext context, boolean ammunition) {
        PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
        Vec3d position = Vec3d.ofBottomCenter(context.getAbsolutePos(new BlockPos(3,1,6)));
        player.refreshPositionAndAngles(position.x, position.y, position.z, 180, 0);
        // LivingEntity's ray direction uses head yaw, which mock players do not
        // synchronize from body rotation through the normal player tick.
        player.setHeadYaw(180); player.setBodyYaw(180);
        player.getInventory().setStack(0, new ItemStack(SlimeGunMod.GUN));
        if (ammunition) player.getInventory().setStack(1, new ItemStack(Items.SLIME_BALL, 3));
        return player;
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void gunHitsConsumesAmmoAndEnforcesCooldown(TestContext context) {
        HuskEntity target = mob(context); PlayerEntity player = shooter(context, true);
        // Newly spawned entities enter the world's spatial index at the end of
        // the tick. Exercise the gun once a real ray query can discover the mob.
        context.runAtTick(3, () -> {
            SlimeGunMod.GUN.use(context.getWorld(), player, Hand.MAIN_HAND);
            check(target.hasStatusEffect(SlimeGunMod.SLIMEBOUND), "Aimed gun must freeze the target. Eye=" + player.getEyePos() + " aim=" + player.getRotationVec(1) + " target=" + target.getPos());
            check(player.getInventory().getStack(1).getCount() == 2, "One slimeball consumed");
            check(player.getMainHandStack().getDamage() == 1, "One durability consumed");
            SlimeGunMod.GUN.use(context.getWorld(), player, Hand.MAIN_HAND);
            check(player.getInventory().getStack(1).getCount() == 2, "Cooldown blocks immediate repeat shot");
        });
        context.runAtTick(30, () -> {
            player.getItemCooldownManager().remove(SlimeGunMod.GUN);
            SlimeGunMod.GUN.use(context.getWorld(), player, Hand.MAIN_HAND);
            check(player.getInventory().getStack(1).getCount() == 1, "A later shot may pass through existing cocoon");
            check(target.hasStatusEffect(SlimeGunMod.SLIMEBOUND)
                && target.getStatusEffect(SlimeGunMod.SLIMEBOUND).getDuration() >= 159, "Gun must refresh through existing slime");
            context.complete();
        });
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void wallsBlockShots(TestContext context) {
        HuskEntity target = mob(context); PlayerEntity player = shooter(context, true);
        context.setBlockState(new BlockPos(3,2,5), Blocks.STONE);
        context.runAtTick(3, () -> {
            SlimeGunMod.GUN.use(context.getWorld(), player, Hand.MAIN_HAND);
            check(!target.hasStatusEffect(SlimeGunMod.SLIMEBOUND), "Gun must not shoot through stone"); context.complete();
        });
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void emptyGunDoesNotFire(TestContext context) {
        HuskEntity target = mob(context); PlayerEntity player = shooter(context, false);
        SlimeGunMod.GUN.use(context.getWorld(), player, Hand.MAIN_HAND);
        check(!target.hasStatusEffect(SlimeGunMod.SLIMEBOUND) && player.getMainHandStack().getDamage() == 0, "No ammo means no shot or wear"); context.complete();
    }
}
