/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.CatVariant;

public class CatVariantTags {
    public static final TagKey<CatVariant> DEFAULT_SPAWNS = CatVariantTags.create("default_spawns");
    public static final TagKey<CatVariant> FULL_MOON_SPAWNS = CatVariantTags.create("full_moon_spawns");

    private CatVariantTags() {
    }

    private static TagKey<CatVariant> create(String string) {
        return TagKey.create(Registries.CAT_VARIANT, ResourceLocation.withDefaultNamespace(string));
    }
}

