package dev.slimegun;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.*;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import java.util.List;

public final class SlimeGunItem extends Item {
    public SlimeGunItem(Settings settings) { super(settings); }

    @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack gun = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(gun, true);
        if (player.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(gun);
        ItemStack ammo = findAmmo(player);
        if (!player.isCreative() && ammo.isEmpty()) {
            player.sendMessage(Text.translatable("message.slimegun.no_ammo"), true);
            return TypedActionResult.fail(gun);
        }
        ServerWorld server = (ServerWorld) world;
        Vec3d start = player.getEyePos();
        Vec3d fullRange = start.add(player.getRotationVec(1).multiply(FreezeRules.RANGE));
        HitResult blockHit = world.raycast(new RaycastContext(start, fullRange,
            RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player) {
            @Override public VoxelShape getBlockShape(BlockState state, BlockView view, BlockPos pos) {
                // Re-shooting must pass through our own slime to refresh a mob,
                // while ordinary blocks still stop the shot normally.
                return state.isOf(SlimeGunMod.COCOON) ? VoxelShapes.empty() : super.getBlockShape(state, view, pos);
            }
        });
        Vec3d end = blockHit.getPos();
        double maxDistance = start.squaredDistanceTo(end);
        EntityHitResult entityHit = ProjectileUtil.raycast(player, start, end,
            player.getBoundingBox().stretch(player.getRotationVec(1).multiply(FreezeRules.RANGE)).expand(1),
            entity -> entity instanceof MobEntity mob && CocoonService.canFreeze(mob), maxDistance);
        if (entityHit != null) {
            end = entityHit.getPos();
            if (CocoonService.encase(server, (MobEntity) entityHit.getEntity()))
                server.playSound(null, end.x, end.y, end.z, SoundEvents.ENTITY_SLIME_ATTACK, SoundCategory.PLAYERS, 1, .8f);
        }
        // A block ray limits the entity ray, so mobs behind walls cannot be hit.
        Vec3d trail = end.subtract(start); int points = Math.max(1, (int) (trail.length() * 5));
        for (int i = 0; i <= points; i++) {
            Vec3d point = start.add(trail.multiply((double) i / points));
            server.spawnParticles(ParticleTypes.ITEM_SLIME, point.x, point.y, point.z, 1, .015, .015, .015, .01);
        }
        server.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.PLAYERS, .8f, 1.5f);
        player.getItemCooldownManager().set(this, FreezeRules.SHOT_COOLDOWN);
        if (!player.isCreative()) {
            ammo.decrement(1);
            gun.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        return TypedActionResult.success(gun, false);
    }

    private static ItemStack findAmmo(PlayerEntity player) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isOf(Items.SLIME_BALL)) return stack;
        }
        return ItemStack.EMPTY;
    }
    @Override public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.slimegun.fire").formatted(Formatting.GREEN));
        tooltip.add(Text.translatable("tooltip.slimegun.ammo").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.slimegun.release").formatted(Formatting.GRAY));
    }
}
