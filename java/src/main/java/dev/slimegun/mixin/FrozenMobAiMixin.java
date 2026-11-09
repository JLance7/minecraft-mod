package dev.slimegun.mixin;

import dev.slimegun.SlimeGunMod;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class FrozenMobAiMixin {
    @Inject(method = "tickNewAi", at = @At("HEAD"), cancellable = true)
    private void slimegun$pauseAi(CallbackInfo info) {
        MobEntity mob = (MobEntity) (Object) this;
        if (mob.hasStatusEffect(SlimeGunMod.SLIMEBOUND)) {
            mob.getNavigation().stop();
            info.cancel();
        }
    }
}
