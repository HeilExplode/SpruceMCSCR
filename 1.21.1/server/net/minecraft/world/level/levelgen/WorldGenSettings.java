/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;

public record WorldGenSettings(WorldOptions options, WorldDimensions dimensions) {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)WorldOptions.CODEC.forGetter(WorldGenSettings::options), (App)WorldDimensions.CODEC.forGetter(WorldGenSettings::dimensions)).apply((Applicative)instance, instance.stable(WorldGenSettings::new)));

    public static <T> DataResult<T> encode(DynamicOps<T> dynamicOps, WorldOptions worldOptions, WorldDimensions worldDimensions) {
        return CODEC.encodeStart(dynamicOps, (Object)new WorldGenSettings(worldOptions, worldDimensions));
    }

    public static <T> DataResult<T> encode(DynamicOps<T> dynamicOps, WorldOptions worldOptions, RegistryAccess registryAccess) {
        return WorldGenSettings.encode(dynamicOps, worldOptions, new WorldDimensions(registryAccess.registryOrThrow(Registries.LEVEL_STEM)));
    }
}

