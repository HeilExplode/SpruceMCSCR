/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class RandomSequence {
    public static final Codec<RandomSequence> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)XoroshiroRandomSource.CODEC.fieldOf("source").forGetter(randomSequence -> randomSequence.source)).apply((Applicative)instance, RandomSequence::new));
    private final XoroshiroRandomSource source;

    public RandomSequence(XoroshiroRandomSource xoroshiroRandomSource) {
        this.source = xoroshiroRandomSource;
    }

    public RandomSequence(long l, ResourceLocation resourceLocation) {
        this(RandomSequence.createSequence(l, Optional.of(resourceLocation)));
    }

    public RandomSequence(long l, Optional<ResourceLocation> optional) {
        this(RandomSequence.createSequence(l, optional));
    }

    private static XoroshiroRandomSource createSequence(long l, Optional<ResourceLocation> optional) {
        RandomSupport.Seed128bit seed128bit = RandomSupport.upgradeSeedTo128bitUnmixed(l);
        if (optional.isPresent()) {
            seed128bit = seed128bit.xor(RandomSequence.seedForKey(optional.get()));
        }
        return new XoroshiroRandomSource(seed128bit.mixed());
    }

    public static RandomSupport.Seed128bit seedForKey(ResourceLocation resourceLocation) {
        return RandomSupport.seedFromHashOf(resourceLocation.toString());
    }

    public RandomSource random() {
        return this.source;
    }
}

