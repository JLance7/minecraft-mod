package dev.slimegun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class SlimeGunMod implements ModInitializer {
    public static final String ID = "slimegun";
    public static final SlimeCocoonBlock COCOON = new SlimeCocoonBlock(AbstractBlock.Settings.create()
        .strength(.2f).sounds(BlockSoundGroup.SLIME).nonOpaque().noCollision().dropsNothing()
        .pistonBehavior(PistonBehavior.DESTROY).suffocates((state, world, pos) -> false)
        .solidBlock((state, world, pos) -> false));
    public static final BlockEntityType<SlimeCocoonBlockEntity> COCOON_ENTITY = FabricBlockEntityTypeBuilder
        .create(SlimeCocoonBlockEntity::new, COCOON).build();
    public static final RegistryEntry<StatusEffect> SLIMEBOUND = Registry.registerReference(
        Registries.STATUS_EFFECT, id("slimebound"), new SlimeboundEffect());
    public static final Item GUN = new SlimeGunItem(new Item.Settings().maxDamage(384));
    public static final TagKey<EntityType<?>> IMMUNE = TagKey.of(RegistryKeys.ENTITY_TYPE, id("immune_to_slime"));

    public static Identifier id(String path) { return Identifier.of(ID, path); }

    @Override public void onInitialize() {
        Registry.register(Registries.BLOCK, id("slime_cocoon"), COCOON);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, id("slime_cocoon"), COCOON_ENTITY);
        Registry.register(Registries.ITEM, id("slime_gun"), GUN);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> entries.add(GUN));
    }
}
