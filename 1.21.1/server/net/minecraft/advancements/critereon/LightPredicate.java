/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record LightPredicate(MinMaxBounds.Ints composite) {
    public static final Codec<LightPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("light", (Object)MinMaxBounds.Ints.ANY).forGetter(LightPredicate::composite)).apply((Applicative)instance, LightPredicate::new));

    public boolean matches(ServerLevel serverLevel, BlockPos blockPos) {
        if (!serverLevel.isLoaded(blockPos)) {
            return false;
        }
        return this.composite.matches(serverLevel.getMaxLocalRawBrightness(blockPos));
    }

    public static class Builder {
        private MinMaxBounds.Ints composite = MinMaxBounds.Ints.ANY;

        public static Builder light() {
            return new Builder();
        }

        public Builder setComposite(MinMaxBounds.Ints ints) {
            this.composite = ints;
            return this;
        }

        public LightPredicate build() {
            return new LightPredicate(this.composite);
        }
    }
}

