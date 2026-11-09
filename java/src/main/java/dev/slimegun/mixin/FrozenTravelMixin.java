package dev.slimegun.mixin;

import dev.slimegun.SlimeGunMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class FrozenTravelMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void slimegun$freezeMovement(Vec3d movementInput, CallbackInfo info) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof MobEntity && entity.hasStatusEffect(SlimeGunMod.SLIMEBOUND)) {
            entity.setVelocity(Vec3d.ZERO); entity.fallDistance = 0;
            // Never persist NoAI or NoGravity flags. Normal behavior returns
            // automatically when vanilla removes the timed status effect.
            info.cancel();
        }
    }
}
