/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerPatterns {
    public static final ResourceKey<BannerPattern> BASE = BannerPatterns.create("base");
    public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_LEFT = BannerPatterns.create("square_bottom_left");
    public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_RIGHT = BannerPatterns.create("square_bottom_right");
    public static final ResourceKey<BannerPattern> SQUARE_TOP_LEFT = BannerPatterns.create("square_top_left");
    public static final ResourceKey<BannerPattern> SQUARE_TOP_RIGHT = BannerPatterns.create("square_top_right");
    public static final ResourceKey<BannerPattern> STRIPE_BOTTOM = BannerPatterns.create("stripe_bottom");
    public static final ResourceKey<BannerPattern> STRIPE_TOP = BannerPatterns.create("stripe_top");
    public static final ResourceKey<BannerPattern> STRIPE_LEFT = BannerPatterns.create("stripe_left");
    public static final ResourceKey<BannerPattern> STRIPE_RIGHT = BannerPatterns.create("stripe_right");
    public static final ResourceKey<BannerPattern> STRIPE_CENTER = BannerPatterns.create("stripe_center");
    public static final ResourceKey<BannerPattern> STRIPE_MIDDLE = BannerPatterns.create("stripe_middle");
    public static final ResourceKey<BannerPattern> STRIPE_DOWNRIGHT = BannerPatterns.create("stripe_downright");
    public static final ResourceKey<BannerPattern> STRIPE_DOWNLEFT = BannerPatterns.create("stripe_downleft");
    public static final ResourceKey<BannerPattern> STRIPE_SMALL = BannerPatterns.create("small_stripes");
    public static final ResourceKey<BannerPattern> CROSS = BannerPatterns.create("cross");
    public static final ResourceKey<BannerPattern> STRAIGHT_CROSS = BannerPatterns.create("straight_cross");
    public static final ResourceKey<BannerPattern> TRIANGLE_BOTTOM = BannerPatterns.create("triangle_bottom");
    public static final ResourceKey<BannerPattern> TRIANGLE_TOP = BannerPatterns.create("triangle_top");
    public static final ResourceKey<BannerPattern> TRIANGLES_BOTTOM = BannerPatterns.create("triangles_bottom");
    public static final ResourceKey<BannerPattern> TRIANGLES_TOP = BannerPatterns.create("triangles_top");
    public static final ResourceKey<BannerPattern> DIAGONAL_LEFT = BannerPatterns.create("diagonal_left");
    public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT = BannerPatterns.create("diagonal_up_right");
    public static final ResourceKey<BannerPattern> DIAGONAL_LEFT_MIRROR = BannerPatterns.create("diagonal_up_left");
    public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT_MIRROR = BannerPatterns.create("diagonal_right");
    public static final ResourceKey<BannerPattern> CIRCLE_MIDDLE = BannerPatterns.create("circle");
    public static final ResourceKey<BannerPattern> RHOMBUS_MIDDLE = BannerPatterns.create("rhombus");
    public static final ResourceKey<BannerPattern> HALF_VERTICAL = BannerPatterns.create("half_vertical");
    public static final ResourceKey<BannerPattern> HALF_HORIZONTAL = BannerPatterns.create("half_horizontal");
    public static final ResourceKey<BannerPattern> HALF_VERTICAL_MIRROR = BannerPatterns.create("half_vertical_right");
    public static final ResourceKey<BannerPattern> HALF_HORIZONTAL_MIRROR = BannerPatterns.create("half_horizontal_bottom");
    public static final ResourceKey<BannerPattern> BORDER = BannerPatterns.create("border");
    public static final ResourceKey<BannerPattern> CURLY_BORDER = BannerPatterns.create("curly_border");
    public static final ResourceKey<BannerPattern> GRADIENT = BannerPatterns.create("gradient");
    public static final ResourceKey<BannerPattern> GRADIENT_UP = BannerPatterns.create("gradient_up");
    public static final ResourceKey<BannerPattern> BRICKS = BannerPatterns.create("bricks");
    public static final ResourceKey<BannerPattern> GLOBE = BannerPatterns.create("globe");
    public static final ResourceKey<BannerPattern> CREEPER = BannerPatterns.create("creeper");
    public static final ResourceKey<BannerPattern> SKULL = BannerPatterns.create("skull");
    public static final ResourceKey<BannerPattern> FLOWER = BannerPatterns.create("flower");
    public static final ResourceKey<BannerPattern> MOJANG = BannerPatterns.create("mojang");
    public static final ResourceKey<BannerPattern> PIGLIN = BannerPatterns.create("piglin");
    public static final ResourceKey<BannerPattern> FLOW = BannerPatterns.create("flow");
    public static final ResourceKey<BannerPattern> GUSTER = BannerPatterns.create("guster");

    private static ResourceKey<BannerPattern> create(String string) {
        return ResourceKey.create(Registries.BANNER_PATTERN, ResourceLocation.withDefaultNamespace(string));
    }

    public static void bootstrap(BootstrapContext<BannerPattern> bootstrapContext) {
        BannerPatterns.register(bootstrapContext, BASE);
        BannerPatterns.register(bootstrapContext, SQUARE_BOTTOM_LEFT);
        BannerPatterns.register(bootstrapContext, SQUARE_BOTTOM_RIGHT);
        BannerPatterns.register(bootstrapContext, SQUARE_TOP_LEFT);
        BannerPatterns.register(bootstrapContext, SQUARE_TOP_RIGHT);
        BannerPatterns.register(bootstrapContext, STRIPE_BOTTOM);
        BannerPatterns.register(bootstrapContext, STRIPE_TOP);
        BannerPatterns.register(bootstrapContext, STRIPE_LEFT);
        BannerPatterns.register(bootstrapContext, STRIPE_RIGHT);
        BannerPatterns.register(bootstrapContext, STRIPE_CENTER);
        BannerPatterns.register(bootstrapContext, STRIPE_MIDDLE);
        BannerPatterns.register(bootstrapContext, STRIPE_DOWNRIGHT);
        BannerPatterns.register(bootstrapContext, STRIPE_DOWNLEFT);
        BannerPatterns.register(bootstrapContext, STRIPE_SMALL);
        BannerPatterns.register(bootstrapContext, CROSS);
        BannerPatterns.register(bootstrapContext, STRAIGHT_CROSS);
        BannerPatterns.register(bootstrapContext, TRIANGLE_BOTTOM);
        BannerPatterns.register(bootstrapContext, TRIANGLE_TOP);
        BannerPatterns.register(bootstrapContext, TRIANGLES_BOTTOM);
        BannerPatterns.register(bootstrapContext, TRIANGLES_TOP);
        BannerPatterns.register(bootstrapContext, DIAGONAL_LEFT);
        BannerPatterns.register(bootstrapContext, DIAGONAL_RIGHT);
        BannerPatterns.register(bootstrapContext, DIAGONAL_LEFT_MIRROR);
        BannerPatterns.register(bootstrapContext, DIAGONAL_RIGHT_MIRROR);
        BannerPatterns.register(bootstrapContext, CIRCLE_MIDDLE);
        BannerPatterns.register(bootstrapContext, RHOMBUS_MIDDLE);
        BannerPatterns.register(bootstrapContext, HALF_VERTICAL);
        BannerPatterns.register(bootstrapContext, HALF_HORIZONTAL);
        BannerPatterns.register(bootstrapContext, HALF_VERTICAL_MIRROR);
        BannerPatterns.register(bootstrapContext, HALF_HORIZONTAL_MIRROR);
        BannerPatterns.register(bootstrapContext, BORDER);
        BannerPatterns.register(bootstrapContext, CURLY_BORDER);
        BannerPatterns.register(bootstrapContext, GRADIENT);
        BannerPatterns.register(bootstrapContext, GRADIENT_UP);
        BannerPatterns.register(bootstrapContext, BRICKS);
        BannerPatterns.register(bootstrapContext, GLOBE);
        BannerPatterns.register(bootstrapContext, CREEPER);
        BannerPatterns.register(bootstrapContext, SKULL);
        BannerPatterns.register(bootstrapContext, FLOWER);
        BannerPatterns.register(bootstrapContext, MOJANG);
        BannerPatterns.register(bootstrapContext, PIGLIN);
        BannerPatterns.register(bootstrapContext, FLOW);
        BannerPatterns.register(bootstrapContext, GUSTER);
    }

    public static void register(BootstrapContext<BannerPattern> bootstrapContext, ResourceKey<BannerPattern> resourceKey) {
        bootstrapContext.register(resourceKey, new BannerPattern(resourceKey.location(), "block.minecraft.banner." + resourceKey.location().toShortLanguageKey()));
    }
}

