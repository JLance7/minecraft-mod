package dev.slimegun.mixin;

import dev.slimegun.SlimeGunMod;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreeperEntity.class)
public abstract class FrozenCreeperMixin {
    // Creeper fuses advance outside the ordinary AI step. Pause only the fuse,
    // allowing status-effect duration and normal entity bookkeeping to tick.
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/CreeperEntity;getFuseSpeed()I"))
    private int slimegun$pauseFuse(CreeperEntity creeper) {
        return creeper.hasStatusEffect(SlimeGunMod.SLIMEBOUND) ? 0 : creeper.getFuseSpeed();
    }
}
