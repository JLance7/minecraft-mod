package dev.slimegun;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

public final class SlimeboundEffect extends StatusEffect {
    public SlimeboundEffect() { super(StatusEffectCategory.HARMFUL, 0x72db46); }
    @Override public boolean canApplyUpdateEffect(int duration, int amplifier) { return true; }
    @Override public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) return true;
        // Returning false expires the effect through vanilla's normal effect
        // iterator. Removing it directly here would mutate that iterator.
        if (!(entity instanceof MobEntity) || !CocoonService.hasSlimeAt(entity)) return false;
        entity.setVelocity(Vec3d.ZERO); entity.fallDistance = 0;
        return true;
    }
}
