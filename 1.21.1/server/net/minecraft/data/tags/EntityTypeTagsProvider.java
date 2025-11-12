/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class EntityTypeTagsProvider
extends IntrinsicHolderTagsProvider<EntityType<?>> {
    public EntityTypeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(packOutput, Registries.ENTITY_TYPE, completableFuture, (T entityType) -> entityType.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.SKELETONS)).add(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.SKELETON_HORSE, EntityType.BOGGED);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.ZOMBIES)).add(EntityType.ZOMBIE_HORSE, EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN, EntityType.DROWNED, EntityType.HUSK);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.RAIDERS)).add(EntityType.EVOKER, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)((IntrinsicHolderTagsProvider.IntrinsicTagAppender)((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.UNDEAD)).addTag((TagKey)EntityTypeTags.SKELETONS)).addTag((TagKey)EntityTypeTags.ZOMBIES)).add(EntityType.WITHER).add(EntityType.PHANTOM);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.BEEHIVE_INHABITORS)).add(EntityType.BEE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.ARROWS)).add(EntityType.ARROW, EntityType.SPECTRAL_ARROW);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.IMPACT_PROJECTILES)).addTag((TagKey)EntityTypeTags.ARROWS)).add(EntityType.FIREWORK_ROCKET).add(EntityType.SNOWBALL, EntityType.FIREBALL, EntityType.SMALL_FIREBALL, EntityType.EGG, EntityType.TRIDENT, EntityType.DRAGON_FIREBALL, EntityType.WITHER_SKULL, EntityType.WIND_CHARGE, EntityType.BREEZE_WIND_CHARGE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)).add(EntityType.RABBIT, EntityType.ENDERMITE, EntityType.SILVERFISH, EntityType.FOX);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.AXOLOTL_HUNT_TARGETS)).add(EntityType.TROPICAL_FISH, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.COD, EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.TADPOLE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)).add(EntityType.DROWNED, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)).add(EntityType.STRAY, EntityType.POLAR_BEAR, EntityType.SNOW_GOLEM, EntityType.WITHER);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)).add(EntityType.STRIDER, EntityType.BLAZE, EntityType.MAGMA_CUBE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.CAN_BREATHE_UNDER_WATER)).addTag((TagKey)EntityTypeTags.UNDEAD)).add(EntityType.AXOLOTL, EntityType.FROG, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.TURTLE, EntityType.GLOW_SQUID, EntityType.COD, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.SQUID, EntityType.TROPICAL_FISH, EntityType.TADPOLE, EntityType.ARMOR_STAND);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.FROG_FOOD)).add(EntityType.SLIME, EntityType.MAGMA_CUBE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.FALL_DAMAGE_IMMUNE)).add(EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.SHULKER, EntityType.ALLAY, EntityType.BAT, EntityType.BEE, EntityType.BLAZE, EntityType.CAT, EntityType.CHICKEN, EntityType.GHAST, EntityType.PHANTOM, EntityType.MAGMA_CUBE, EntityType.OCELOT, EntityType.PARROT, EntityType.WITHER, EntityType.BREEZE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.DISMOUNTS_UNDERWATER)).add(EntityType.CAMEL, EntityType.CHICKEN, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA, EntityType.MULE, EntityType.PIG, EntityType.RAVAGER, EntityType.SPIDER, EntityType.STRIDER, EntityType.TRADER_LLAMA, EntityType.ZOMBIE_HORSE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.NON_CONTROLLING_RIDER)).add(EntityType.SLIME, EntityType.MAGMA_CUBE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.ILLAGER)).add(EntityType.EVOKER).add(EntityType.ILLUSIONER).add(EntityType.PILLAGER).add(EntityType.VINDICATOR);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.AQUATIC)).add(EntityType.TURTLE).add(EntityType.AXOLOTL).add(EntityType.GUARDIAN).add(EntityType.ELDER_GUARDIAN).add(EntityType.COD).add(EntityType.PUFFERFISH).add(EntityType.SALMON).add(EntityType.TROPICAL_FISH).add(EntityType.DOLPHIN).add(EntityType.SQUID).add(EntityType.GLOW_SQUID).add(EntityType.TADPOLE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.ARTHROPOD)).add(EntityType.BEE).add(EntityType.ENDERMITE).add(EntityType.SILVERFISH).add(EntityType.SPIDER).add(EntityType.CAVE_SPIDER);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.IGNORES_POISON_AND_REGEN)).addTag((TagKey)EntityTypeTags.UNDEAD);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.INVERTED_HEALING_AND_HARM)).addTag((TagKey)EntityTypeTags.UNDEAD);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.WITHER_FRIENDS)).addTag((TagKey)EntityTypeTags.UNDEAD);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.ILLAGER_FRIENDS)).addTag((TagKey)EntityTypeTags.ILLAGER);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH)).add(EntityType.TURTLE).add(EntityType.GUARDIAN).add(EntityType.ELDER_GUARDIAN).add(EntityType.COD).add(EntityType.PUFFERFISH).add(EntityType.SALMON).add(EntityType.TROPICAL_FISH).add(EntityType.DOLPHIN).add(EntityType.SQUID).add(EntityType.GLOW_SQUID).add(EntityType.TADPOLE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.SENSITIVE_TO_IMPALING)).addTag((TagKey)EntityTypeTags.AQUATIC);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)).addTag((TagKey)EntityTypeTags.ARTHROPOD);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.SENSITIVE_TO_SMITE)).addTag((TagKey)EntityTypeTags.UNDEAD);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.REDIRECTABLE_PROJECTILE)).add(EntityType.FIREBALL, EntityType.WIND_CHARGE, EntityType.BREEZE_WIND_CHARGE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.DEFLECTS_PROJECTILES)).add(EntityType.BREEZE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.CAN_TURN_IN_BOATS)).add(EntityType.BREEZE);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE)).add(EntityType.BREEZE, EntityType.SKELETON, EntityType.BOGGED, EntityType.STRAY, EntityType.ZOMBIE, EntityType.HUSK, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SLIME);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.IMMUNE_TO_INFESTED)).add(EntityType.SILVERFISH);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)EntityTypeTags.IMMUNE_TO_OOZING)).add(EntityType.SLIME);
    }
}

