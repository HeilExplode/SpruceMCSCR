/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeLootItemCondition
implements LootItemCondition {
    protected final List<LootItemCondition> terms;
    private final Predicate<LootContext> composedPredicate;

    protected CompositeLootItemCondition(List<LootItemCondition> list, Predicate<LootContext> predicate) {
        this.terms = list;
        this.composedPredicate = predicate;
    }

    protected static <T extends CompositeLootItemCondition> MapCodec<T> createCodec(Function<List<LootItemCondition>, T> function) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootItemCondition.DIRECT_CODEC.listOf().fieldOf("terms").forGetter(compositeLootItemCondition -> compositeLootItemCondition.terms)).apply((Applicative)instance, function));
    }

    protected static <T extends CompositeLootItemCondition> Codec<T> createInlineCodec(Function<List<LootItemCondition>, T> function) {
        return LootItemCondition.DIRECT_CODEC.listOf().xmap(function, compositeLootItemCondition -> compositeLootItemCondition.terms);
    }

    @Override
    public final boolean test(LootContext lootContext) {
        return this.composedPredicate.test(lootContext);
    }

    @Override
    public void validate(ValidationContext validationContext) {
        LootItemCondition.super.validate(validationContext);
        for (int i = 0; i < this.terms.size(); ++i) {
            this.terms.get(i).validate(validationContext.forChild(".term[" + i + "]"));
        }
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static abstract class Builder
    implements LootItemCondition.Builder {
        private final ImmutableList.Builder<LootItemCondition> terms = ImmutableList.builder();

        protected Builder(LootItemCondition.Builder ... builderArray) {
            for (LootItemCondition.Builder builder : builderArray) {
                this.terms.add((Object)builder.build());
            }
        }

        public void addTerm(LootItemCondition.Builder builder) {
            this.terms.add((Object)builder.build());
        }

        @Override
        public LootItemCondition build() {
            return this.create((List<LootItemCondition>)this.terms.build());
        }

        protected abstract LootItemCondition create(List<LootItemCondition> var1);
    }
}

