/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.CollectionContentsPredicate;
import net.minecraft.advancements.critereon.CollectionCountsPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;

public record CollectionPredicate<T, P extends Predicate<T>>(Optional<CollectionContentsPredicate<T, P>> contains, Optional<CollectionCountsPredicate<T, P>> counts, Optional<MinMaxBounds.Ints> size) implements Predicate<Iterable<T>>
{
    public static <T, P extends Predicate<T>> Codec<CollectionPredicate<T, P>> codec(Codec<P> codec) {
        return RecordCodecBuilder.create(instance -> instance.group((App)CollectionContentsPredicate.codec(codec).optionalFieldOf("contains").forGetter(CollectionPredicate::contains), (App)CollectionCountsPredicate.codec(codec).optionalFieldOf("count").forGetter(CollectionPredicate::counts), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("size").forGetter(CollectionPredicate::size)).apply((Applicative)instance, CollectionPredicate::new));
    }

    @Override
    public boolean test(Iterable<T> iterable) {
        if (this.contains.isPresent() && !this.contains.get().test(iterable)) {
            return false;
        }
        if (this.counts.isPresent() && !this.counts.get().test(iterable)) {
            return false;
        }
        return !this.size.isPresent() || this.size.get().matches(Iterables.size(iterable));
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((Iterable)object);
    }
}

