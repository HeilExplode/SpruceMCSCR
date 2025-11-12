/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record MangroveRootPlacement(HolderSet<Block> canGrowThrough, HolderSet<Block> muddyRootsIn, BlockStateProvider muddyRootsProvider, int maxRootWidth, int maxRootLength, float randomSkewChance) {
    public static final Codec<MangroveRootPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_grow_through").forGetter(mangroveRootPlacement -> mangroveRootPlacement.canGrowThrough), (App)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("muddy_roots_in").forGetter(mangroveRootPlacement -> mangroveRootPlacement.muddyRootsIn), (App)BlockStateProvider.CODEC.fieldOf("muddy_roots_provider").forGetter(mangroveRootPlacement -> mangroveRootPlacement.muddyRootsProvider), (App)Codec.intRange((int)1, (int)12).fieldOf("max_root_width").forGetter(mangroveRootPlacement -> mangroveRootPlacement.maxRootWidth), (App)Codec.intRange((int)1, (int)64).fieldOf("max_root_length").forGetter(mangroveRootPlacement -> mangroveRootPlacement.maxRootLength), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("random_skew_chance").forGetter(mangroveRootPlacement -> Float.valueOf(mangroveRootPlacement.randomSkewChance))).apply((Applicative)instance, MangroveRootPlacement::new));
}

