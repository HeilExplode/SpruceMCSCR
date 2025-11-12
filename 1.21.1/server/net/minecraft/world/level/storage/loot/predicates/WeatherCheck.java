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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public record WeatherCheck(Optional<Boolean> isRaining, Optional<Boolean> isThundering) implements LootItemCondition
{
    public static final MapCodec<WeatherCheck> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("raining").forGetter(WeatherCheck::isRaining), (App)Codec.BOOL.optionalFieldOf("thundering").forGetter(WeatherCheck::isThundering)).apply((Applicative)instance, WeatherCheck::new));

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.WEATHER_CHECK;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerLevel serverLevel = lootContext.getLevel();
        if (this.isRaining.isPresent() && this.isRaining.get().booleanValue() != serverLevel.isRaining()) {
            return false;
        }
        return !this.isThundering.isPresent() || this.isThundering.get().booleanValue() == serverLevel.isThundering();
    }

    public static Builder weather() {
        return new Builder();
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Builder
    implements LootItemCondition.Builder {
        private Optional<Boolean> isRaining = Optional.empty();
        private Optional<Boolean> isThundering = Optional.empty();

        public Builder setRaining(boolean bl) {
            this.isRaining = Optional.of(bl);
            return this;
        }

        public Builder setThundering(boolean bl) {
            this.isThundering = Optional.of(bl);
            return this;
        }

        @Override
        public WeatherCheck build() {
            return new WeatherCheck(this.isRaining, this.isThundering);
        }

        @Override
        public /* synthetic */ LootItemCondition build() {
            return this.build();
        }
    }
}

