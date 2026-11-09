package dev.slimegun;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

/** Kept behind a client-only entry point, so dedicated servers never load render classes. */
public final class SlimeGunClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(SlimeGunMod.COCOON, RenderLayer.getTranslucent());
    }
}
