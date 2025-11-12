/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public record TimeCheck(Optional<Long> period, IntRange value) implements LootItemCondition
{
    public static final MapCodec<TimeCheck> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.LONG.optionalFieldOf("period").forGetter(TimeCheck::period), (App)IntRange.CODEC.fieldOf("value").forGetter(TimeCheck::value)).apply((Applicative)instance, TimeCheck::new));

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.TIME_CHECK;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.value.getReferencedContextParams();
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerLevel serverLevel = lootContext.getLevel();
        long l = serverLevel.getDayTime();
        if (this.period.isPresent()) {
            l %= this.period.get().longValue();
        }
        return this.value.test(lootContext, (int)l);
    }

    public static Builder time(IntRange intRange) {
        return new Builder(intRange);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Builder
    implements LootItemCondition.Builder {
        private Optional<Long> period = Optional.empty();
        private final IntRange value;

        public Builder(IntRange intRange) {
            this.value = intRange;
        }

        public Builder setPeriod(long l) {
            this.period = Optional.of(l);
            return this;
        }

        @Override
        public TimeCheck build() {
            return new TimeCheck(this.period, this.value);
        }

        @Override
        public /* synthetic */ LootItemCondition build() {
            return this.build();
        }
    }
}

