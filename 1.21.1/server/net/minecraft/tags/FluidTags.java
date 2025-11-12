/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class FluidTags {
    public static final TagKey<Fluid> WATER = FluidTags.create("water");
    public static final TagKey<Fluid> LAVA = FluidTags.create("lava");

    private FluidTags() {
    }

    private static TagKey<Fluid> create(String string) {
        return TagKey.create(Registries.FLUID, ResourceLocation.withDefaultNamespace(string));
    }
}

