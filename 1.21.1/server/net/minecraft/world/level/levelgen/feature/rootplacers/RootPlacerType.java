/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.rootplacers.MangroveRootPlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;

public class RootPlacerType<P extends RootPlacer> {
    public static final RootPlacerType<MangroveRootPlacer> MANGROVE_ROOT_PLACER = RootPlacerType.register("mangrove_root_placer", MangroveRootPlacer.CODEC);
    private final MapCodec<P> codec;

    private static <P extends RootPlacer> RootPlacerType<P> register(String string, MapCodec<P> mapCodec) {
        return Registry.register(BuiltInRegistries.ROOT_PLACER_TYPE, string, new RootPlacerType<P>(mapCodec));
    }

    private RootPlacerType(MapCodec<P> mapCodec) {
        this.codec = mapCodec;
    }

    public MapCodec<P> codec() {
        return this.codec;
    }
}

