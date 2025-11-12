/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class WolfVariants {
    public static final ResourceKey<WolfVariant> PALE = WolfVariants.createKey("pale");
    public static final ResourceKey<WolfVariant> SPOTTED = WolfVariants.createKey("spotted");
    public static final ResourceKey<WolfVariant> SNOWY = WolfVariants.createKey("snowy");
    public static final ResourceKey<WolfVariant> BLACK = WolfVariants.createKey("black");
    public static final ResourceKey<WolfVariant> ASHEN = WolfVariants.createKey("ashen");
    public static final ResourceKey<WolfVariant> RUSTY = WolfVariants.createKey("rusty");
    public static final ResourceKey<WolfVariant> WOODS = WolfVariants.createKey("woods");
    public static final ResourceKey<WolfVariant> CHESTNUT = WolfVariants.createKey("chestnut");
    public static final ResourceKey<WolfVariant> STRIPED = WolfVariants.createKey("striped");
    public static final ResourceKey<WolfVariant> DEFAULT = PALE;

    private static ResourceKey<WolfVariant> createKey(String string) {
        return ResourceKey.create(Registries.WOLF_VARIANT, ResourceLocation.withDefaultNamespace(string));
    }

    static void register(BootstrapContext<WolfVariant> bootstrapContext, ResourceKey<WolfVariant> resourceKey, String string, ResourceKey<Biome> resourceKey2) {
        WolfVariants.register(bootstrapContext, resourceKey, string, HolderSet.direct(bootstrapContext.lookup(Registries.BIOME).getOrThrow(resourceKey2)));
    }

    static void register(BootstrapContext<WolfVariant> bootstrapContext, ResourceKey<WolfVariant> resourceKey, String string, TagKey<Biome> tagKey) {
        WolfVariants.register(bootstrapContext, resourceKey, string, bootstrapContext.lookup(Registries.BIOME).getOrThrow(tagKey));
    }

    static void register(BootstrapContext<WolfVariant> bootstrapContext, ResourceKey<WolfVariant> resourceKey, String string, HolderSet<Biome> holderSet) {
        ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace("entity/wolf/" + string);
        ResourceLocation resourceLocation2 = ResourceLocation.withDefaultNamespace("entity/wolf/" + string + "_tame");
        ResourceLocation resourceLocation3 = ResourceLocation.withDefaultNamespace("entity/wolf/" + string + "_angry");
        bootstrapContext.register(resourceKey, new WolfVariant(resourceLocation, resourceLocation2, resourceLocation3, holderSet));
    }

    public static Holder<WolfVariant> getSpawnVariant(RegistryAccess registryAccess, Holder<Biome> holder) {
        Registry<WolfVariant> registry = registryAccess.registryOrThrow(Registries.WOLF_VARIANT);
        return registry.holders().filter(reference -> ((WolfVariant)reference.value()).biomes().contains(holder)).findFirst().or(() -> registry.getHolder(DEFAULT)).or(registry::getAny).orElseThrow();
    }

    public static void bootstrap(BootstrapContext<WolfVariant> bootstrapContext) {
        WolfVariants.register(bootstrapContext, PALE, "wolf", Biomes.TAIGA);
        WolfVariants.register(bootstrapContext, SPOTTED, "wolf_spotted", BiomeTags.IS_SAVANNA);
        WolfVariants.register(bootstrapContext, SNOWY, "wolf_snowy", Biomes.GROVE);
        WolfVariants.register(bootstrapContext, BLACK, "wolf_black", Biomes.OLD_GROWTH_PINE_TAIGA);
        WolfVariants.register(bootstrapContext, ASHEN, "wolf_ashen", Biomes.SNOWY_TAIGA);
        WolfVariants.register(bootstrapContext, RUSTY, "wolf_rusty", BiomeTags.IS_JUNGLE);
        WolfVariants.register(bootstrapContext, WOODS, "wolf_woods", Biomes.FOREST);
        WolfVariants.register(bootstrapContext, CHESTNUT, "wolf_chestnut", Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        WolfVariants.register(bootstrapContext, STRIPED, "wolf_striped", BiomeTags.IS_BADLANDS);
    }
}

