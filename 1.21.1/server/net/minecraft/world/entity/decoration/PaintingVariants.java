/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.decoration;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class PaintingVariants {
    public static final ResourceKey<PaintingVariant> KEBAB = PaintingVariants.create("kebab");
    public static final ResourceKey<PaintingVariant> AZTEC = PaintingVariants.create("aztec");
    public static final ResourceKey<PaintingVariant> ALBAN = PaintingVariants.create("alban");
    public static final ResourceKey<PaintingVariant> AZTEC2 = PaintingVariants.create("aztec2");
    public static final ResourceKey<PaintingVariant> BOMB = PaintingVariants.create("bomb");
    public static final ResourceKey<PaintingVariant> PLANT = PaintingVariants.create("plant");
    public static final ResourceKey<PaintingVariant> WASTELAND = PaintingVariants.create("wasteland");
    public static final ResourceKey<PaintingVariant> POOL = PaintingVariants.create("pool");
    public static final ResourceKey<PaintingVariant> COURBET = PaintingVariants.create("courbet");
    public static final ResourceKey<PaintingVariant> SEA = PaintingVariants.create("sea");
    public static final ResourceKey<PaintingVariant> SUNSET = PaintingVariants.create("sunset");
    public static final ResourceKey<PaintingVariant> CREEBET = PaintingVariants.create("creebet");
    public static final ResourceKey<PaintingVariant> WANDERER = PaintingVariants.create("wanderer");
    public static final ResourceKey<PaintingVariant> GRAHAM = PaintingVariants.create("graham");
    public static final ResourceKey<PaintingVariant> MATCH = PaintingVariants.create("match");
    public static final ResourceKey<PaintingVariant> BUST = PaintingVariants.create("bust");
    public static final ResourceKey<PaintingVariant> STAGE = PaintingVariants.create("stage");
    public static final ResourceKey<PaintingVariant> VOID = PaintingVariants.create("void");
    public static final ResourceKey<PaintingVariant> SKULL_AND_ROSES = PaintingVariants.create("skull_and_roses");
    public static final ResourceKey<PaintingVariant> WITHER = PaintingVariants.create("wither");
    public static final ResourceKey<PaintingVariant> FIGHTERS = PaintingVariants.create("fighters");
    public static final ResourceKey<PaintingVariant> POINTER = PaintingVariants.create("pointer");
    public static final ResourceKey<PaintingVariant> PIGSCENE = PaintingVariants.create("pigscene");
    public static final ResourceKey<PaintingVariant> BURNING_SKULL = PaintingVariants.create("burning_skull");
    public static final ResourceKey<PaintingVariant> SKELETON = PaintingVariants.create("skeleton");
    public static final ResourceKey<PaintingVariant> DONKEY_KONG = PaintingVariants.create("donkey_kong");
    public static final ResourceKey<PaintingVariant> EARTH = PaintingVariants.create("earth");
    public static final ResourceKey<PaintingVariant> WIND = PaintingVariants.create("wind");
    public static final ResourceKey<PaintingVariant> WATER = PaintingVariants.create("water");
    public static final ResourceKey<PaintingVariant> FIRE = PaintingVariants.create("fire");
    public static final ResourceKey<PaintingVariant> BAROQUE = PaintingVariants.create("baroque");
    public static final ResourceKey<PaintingVariant> HUMBLE = PaintingVariants.create("humble");
    public static final ResourceKey<PaintingVariant> MEDITATIVE = PaintingVariants.create("meditative");
    public static final ResourceKey<PaintingVariant> PRAIRIE_RIDE = PaintingVariants.create("prairie_ride");
    public static final ResourceKey<PaintingVariant> UNPACKED = PaintingVariants.create("unpacked");
    public static final ResourceKey<PaintingVariant> BACKYARD = PaintingVariants.create("backyard");
    public static final ResourceKey<PaintingVariant> BOUQUET = PaintingVariants.create("bouquet");
    public static final ResourceKey<PaintingVariant> CAVEBIRD = PaintingVariants.create("cavebird");
    public static final ResourceKey<PaintingVariant> CHANGING = PaintingVariants.create("changing");
    public static final ResourceKey<PaintingVariant> COTAN = PaintingVariants.create("cotan");
    public static final ResourceKey<PaintingVariant> ENDBOSS = PaintingVariants.create("endboss");
    public static final ResourceKey<PaintingVariant> FERN = PaintingVariants.create("fern");
    public static final ResourceKey<PaintingVariant> FINDING = PaintingVariants.create("finding");
    public static final ResourceKey<PaintingVariant> LOWMIST = PaintingVariants.create("lowmist");
    public static final ResourceKey<PaintingVariant> ORB = PaintingVariants.create("orb");
    public static final ResourceKey<PaintingVariant> OWLEMONS = PaintingVariants.create("owlemons");
    public static final ResourceKey<PaintingVariant> PASSAGE = PaintingVariants.create("passage");
    public static final ResourceKey<PaintingVariant> POND = PaintingVariants.create("pond");
    public static final ResourceKey<PaintingVariant> SUNFLOWERS = PaintingVariants.create("sunflowers");
    public static final ResourceKey<PaintingVariant> TIDES = PaintingVariants.create("tides");

    public static void bootstrap(BootstrapContext<PaintingVariant> bootstrapContext) {
        PaintingVariants.register(bootstrapContext, KEBAB, 1, 1);
        PaintingVariants.register(bootstrapContext, AZTEC, 1, 1);
        PaintingVariants.register(bootstrapContext, ALBAN, 1, 1);
        PaintingVariants.register(bootstrapContext, AZTEC2, 1, 1);
        PaintingVariants.register(bootstrapContext, BOMB, 1, 1);
        PaintingVariants.register(bootstrapContext, PLANT, 1, 1);
        PaintingVariants.register(bootstrapContext, WASTELAND, 1, 1);
        PaintingVariants.register(bootstrapContext, POOL, 2, 1);
        PaintingVariants.register(bootstrapContext, COURBET, 2, 1);
        PaintingVariants.register(bootstrapContext, SEA, 2, 1);
        PaintingVariants.register(bootstrapContext, SUNSET, 2, 1);
        PaintingVariants.register(bootstrapContext, CREEBET, 2, 1);
        PaintingVariants.register(bootstrapContext, WANDERER, 1, 2);
        PaintingVariants.register(bootstrapContext, GRAHAM, 1, 2);
        PaintingVariants.register(bootstrapContext, MATCH, 2, 2);
        PaintingVariants.register(bootstrapContext, BUST, 2, 2);
        PaintingVariants.register(bootstrapContext, STAGE, 2, 2);
        PaintingVariants.register(bootstrapContext, VOID, 2, 2);
        PaintingVariants.register(bootstrapContext, SKULL_AND_ROSES, 2, 2);
        PaintingVariants.register(bootstrapContext, WITHER, 2, 2);
        PaintingVariants.register(bootstrapContext, FIGHTERS, 4, 2);
        PaintingVariants.register(bootstrapContext, POINTER, 4, 4);
        PaintingVariants.register(bootstrapContext, PIGSCENE, 4, 4);
        PaintingVariants.register(bootstrapContext, BURNING_SKULL, 4, 4);
        PaintingVariants.register(bootstrapContext, SKELETON, 4, 3);
        PaintingVariants.register(bootstrapContext, EARTH, 2, 2);
        PaintingVariants.register(bootstrapContext, WIND, 2, 2);
        PaintingVariants.register(bootstrapContext, WATER, 2, 2);
        PaintingVariants.register(bootstrapContext, FIRE, 2, 2);
        PaintingVariants.register(bootstrapContext, DONKEY_KONG, 4, 3);
        PaintingVariants.register(bootstrapContext, BAROQUE, 2, 2);
        PaintingVariants.register(bootstrapContext, HUMBLE, 2, 2);
        PaintingVariants.register(bootstrapContext, MEDITATIVE, 1, 1);
        PaintingVariants.register(bootstrapContext, PRAIRIE_RIDE, 1, 2);
        PaintingVariants.register(bootstrapContext, UNPACKED, 4, 4);
        PaintingVariants.register(bootstrapContext, BACKYARD, 3, 4);
        PaintingVariants.register(bootstrapContext, BOUQUET, 3, 3);
        PaintingVariants.register(bootstrapContext, CAVEBIRD, 3, 3);
        PaintingVariants.register(bootstrapContext, CHANGING, 4, 2);
        PaintingVariants.register(bootstrapContext, COTAN, 3, 3);
        PaintingVariants.register(bootstrapContext, ENDBOSS, 3, 3);
        PaintingVariants.register(bootstrapContext, FERN, 3, 3);
        PaintingVariants.register(bootstrapContext, FINDING, 4, 2);
        PaintingVariants.register(bootstrapContext, LOWMIST, 4, 2);
        PaintingVariants.register(bootstrapContext, ORB, 4, 4);
        PaintingVariants.register(bootstrapContext, OWLEMONS, 3, 3);
        PaintingVariants.register(bootstrapContext, PASSAGE, 4, 2);
        PaintingVariants.register(bootstrapContext, POND, 3, 4);
        PaintingVariants.register(bootstrapContext, SUNFLOWERS, 3, 3);
        PaintingVariants.register(bootstrapContext, TIDES, 3, 3);
    }

    private static void register(BootstrapContext<PaintingVariant> bootstrapContext, ResourceKey<PaintingVariant> resourceKey, int n, int n2) {
        bootstrapContext.register(resourceKey, new PaintingVariant(n, n2, resourceKey.location()));
    }

    private static ResourceKey<PaintingVariant> create(String string) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, ResourceLocation.withDefaultNamespace(string));
    }
}

