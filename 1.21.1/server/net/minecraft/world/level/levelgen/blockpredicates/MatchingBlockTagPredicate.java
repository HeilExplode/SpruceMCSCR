/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.StateTestingPredicate;

public class MatchingBlockTagPredicate
extends StateTestingPredicate {
    final TagKey<Block> tag;
    public static final MapCodec<MatchingBlockTagPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> MatchingBlockTagPredicate.stateTestingCodec(instance).and((App)TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(matchingBlockTagPredicate -> matchingBlockTagPredicate.tag)).apply((Applicative)instance, MatchingBlockTagPredicate::new));

    protected MatchingBlockTagPredicate(Vec3i vec3i, TagKey<Block> tagKey) {
        super(vec3i);
        this.tag = tagKey;
    }

    @Override
    protected boolean test(BlockState blockState) {
        return blockState.is(this.tag);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_BLOCK_TAG;
    }
}

