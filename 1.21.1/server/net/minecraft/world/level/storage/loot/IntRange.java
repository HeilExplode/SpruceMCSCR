/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class IntRange {
    private static final Codec<IntRange> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NumberProviders.CODEC.optionalFieldOf("min").forGetter(intRange -> Optional.ofNullable(intRange.min)), (App)NumberProviders.CODEC.optionalFieldOf("max").forGetter(intRange -> Optional.ofNullable(intRange.max))).apply((Applicative)instance, IntRange::new));
    public static final Codec<IntRange> CODEC = Codec.either((Codec)Codec.INT, RECORD_CODEC).xmap(either -> (IntRange)either.map(IntRange::exact, Function.identity()), intRange -> {
        OptionalInt optionalInt = intRange.unpackExact();
        if (optionalInt.isPresent()) {
            return Either.left((Object)optionalInt.getAsInt());
        }
        return Either.right((Object)intRange);
    });
    @Nullable
    private final NumberProvider min;
    @Nullable
    private final NumberProvider max;
    private final IntLimiter limiter;
    private final IntChecker predicate;

    public Set<LootContextParam<?>> getReferencedContextParams() {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        if (this.min != null) {
            builder.addAll(this.min.getReferencedContextParams());
        }
        if (this.max != null) {
            builder.addAll(this.max.getReferencedContextParams());
        }
        return builder.build();
    }

    private IntRange(Optional<NumberProvider> optional, Optional<NumberProvider> optional2) {
        this((NumberProvider)optional.orElse(null), (NumberProvider)optional2.orElse(null));
    }

    private IntRange(@Nullable NumberProvider numberProvider, @Nullable NumberProvider numberProvider2) {
        this.min = numberProvider;
        this.max = numberProvider2;
        if (numberProvider == null) {
            if (numberProvider2 == null) {
                this.limiter = (lootContext, n) -> n;
                this.predicate = (lootContext, n) -> true;
            } else {
                this.limiter = (lootContext, n) -> Math.min(numberProvider2.getInt(lootContext), n);
                this.predicate = (lootContext, n) -> n <= numberProvider2.getInt(lootContext);
            }
        } else if (numberProvider2 == null) {
            this.limiter = (lootContext, n) -> Math.max(numberProvider.getInt(lootContext), n);
            this.predicate = (lootContext, n) -> n >= numberProvider.getInt(lootContext);
        } else {
            this.limiter = (lootContext, n) -> Mth.clamp(n, numberProvider.getInt(lootContext), numberProvider2.getInt(lootContext));
            this.predicate = (lootContext, n) -> n >= numberProvider.getInt(lootContext) && n <= numberProvider2.getInt(lootContext);
        }
    }

    public static IntRange exact(int n) {
        ConstantValue constantValue = ConstantValue.exactly(n);
        return new IntRange(Optional.of(constantValue), Optional.of(constantValue));
    }

    public static IntRange range(int n, int n2) {
        return new IntRange(Optional.of(ConstantValue.exactly(n)), Optional.of(ConstantValue.exactly(n2)));
    }

    public static IntRange lowerBound(int n) {
        return new IntRange(Optional.of(ConstantValue.exactly(n)), Optional.empty());
    }

    public static IntRange upperBound(int n) {
        return new IntRange(Optional.empty(), Optional.of(ConstantValue.exactly(n)));
    }

    public int clamp(LootContext lootContext, int n) {
        return this.limiter.apply(lootContext, n);
    }

    public boolean test(LootContext lootContext, int n) {
        return this.predicate.test(lootContext, n);
    }

    private OptionalInt unpackExact() {
        ConstantValue constantValue;
        NumberProvider numberProvider;
        if (Objects.equals(this.min, this.max) && (numberProvider = this.min) instanceof ConstantValue && Math.floor((constantValue = (ConstantValue)numberProvider).value()) == (double)constantValue.value()) {
            return OptionalInt.of((int)constantValue.value());
        }
        return OptionalInt.empty();
    }

    @FunctionalInterface
    static interface IntLimiter {
        public int apply(LootContext var1, int var2);
    }

    @FunctionalInterface
    static interface IntChecker {
        public boolean test(LootContext var1, int var2);
    }
}

