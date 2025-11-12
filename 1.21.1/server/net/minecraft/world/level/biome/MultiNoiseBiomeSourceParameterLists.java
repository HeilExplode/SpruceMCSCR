/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

public class MultiNoiseBiomeSourceParameterLists {
    public static final ResourceKey<MultiNoiseBiomeSourceParameterList> NETHER = MultiNoiseBiomeSourceParameterLists.register("nether");
    public static final ResourceKey<MultiNoiseBiomeSourceParameterList> OVERWORLD = MultiNoiseBiomeSourceParameterLists.register("overworld");

    public static void bootstrap(BootstrapContext<MultiNoiseBiomeSourceParameterList> bootstrapContext) {
        HolderGetter<Biome> holderGetter = bootstrapContext.lookup(Registries.BIOME);
        bootstrapContext.register(NETHER, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.NETHER, holderGetter));
        bootstrapContext.register(OVERWORLD, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, holderGetter));
    }

    private static ResourceKey<MultiNoiseBiomeSourceParameterList> register(String string) {
        return ResourceKey.create(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, ResourceLocation.withDefaultNamespace(string));
    }
}

