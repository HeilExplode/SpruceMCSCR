/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.armortrim;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.TrimPattern;

public class TrimPatterns {
    public static final ResourceKey<TrimPattern> SENTRY = TrimPatterns.registryKey("sentry");
    public static final ResourceKey<TrimPattern> DUNE = TrimPatterns.registryKey("dune");
    public static final ResourceKey<TrimPattern> COAST = TrimPatterns.registryKey("coast");
    public static final ResourceKey<TrimPattern> WILD = TrimPatterns.registryKey("wild");
    public static final ResourceKey<TrimPattern> WARD = TrimPatterns.registryKey("ward");
    public static final ResourceKey<TrimPattern> EYE = TrimPatterns.registryKey("eye");
    public static final ResourceKey<TrimPattern> VEX = TrimPatterns.registryKey("vex");
    public static final ResourceKey<TrimPattern> TIDE = TrimPatterns.registryKey("tide");
    public static final ResourceKey<TrimPattern> SNOUT = TrimPatterns.registryKey("snout");
    public static final ResourceKey<TrimPattern> RIB = TrimPatterns.registryKey("rib");
    public static final ResourceKey<TrimPattern> SPIRE = TrimPatterns.registryKey("spire");
    public static final ResourceKey<TrimPattern> WAYFINDER = TrimPatterns.registryKey("wayfinder");
    public static final ResourceKey<TrimPattern> SHAPER = TrimPatterns.registryKey("shaper");
    public static final ResourceKey<TrimPattern> SILENCE = TrimPatterns.registryKey("silence");
    public static final ResourceKey<TrimPattern> RAISER = TrimPatterns.registryKey("raiser");
    public static final ResourceKey<TrimPattern> HOST = TrimPatterns.registryKey("host");
    public static final ResourceKey<TrimPattern> FLOW = TrimPatterns.registryKey("flow");
    public static final ResourceKey<TrimPattern> BOLT = TrimPatterns.registryKey("bolt");

    public static void bootstrap(BootstrapContext<TrimPattern> bootstrapContext) {
        TrimPatterns.register(bootstrapContext, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, SENTRY);
        TrimPatterns.register(bootstrapContext, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, DUNE);
        TrimPatterns.register(bootstrapContext, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, COAST);
        TrimPatterns.register(bootstrapContext, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, WILD);
        TrimPatterns.register(bootstrapContext, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, WARD);
        TrimPatterns.register(bootstrapContext, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, EYE);
        TrimPatterns.register(bootstrapContext, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, VEX);
        TrimPatterns.register(bootstrapContext, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, TIDE);
        TrimPatterns.register(bootstrapContext, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, SNOUT);
        TrimPatterns.register(bootstrapContext, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, RIB);
        TrimPatterns.register(bootstrapContext, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, SPIRE);
        TrimPatterns.register(bootstrapContext, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, WAYFINDER);
        TrimPatterns.register(bootstrapContext, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, SHAPER);
        TrimPatterns.register(bootstrapContext, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, SILENCE);
        TrimPatterns.register(bootstrapContext, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, RAISER);
        TrimPatterns.register(bootstrapContext, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, HOST);
        TrimPatterns.register(bootstrapContext, Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, FLOW);
        TrimPatterns.register(bootstrapContext, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, BOLT);
    }

    public static Optional<Holder.Reference<TrimPattern>> getFromTemplate(HolderLookup.Provider provider, ItemStack itemStack) {
        return provider.lookupOrThrow(Registries.TRIM_PATTERN).listElements().filter(reference -> itemStack.is(((TrimPattern)reference.value()).templateItem())).findFirst();
    }

    public static void register(BootstrapContext<TrimPattern> bootstrapContext, Item item, ResourceKey<TrimPattern> resourceKey) {
        TrimPattern trimPattern = new TrimPattern(resourceKey.location(), BuiltInRegistries.ITEM.wrapAsHolder(item), Component.translatable(Util.makeDescriptionId("trim_pattern", resourceKey.location())), false);
        bootstrapContext.register(resourceKey, trimPattern);
    }

    private static ResourceKey<TrimPattern> registryKey(String string) {
        return ResourceKey.create(Registries.TRIM_PATTERN, ResourceLocation.withDefaultNamespace(string));
    }
}

