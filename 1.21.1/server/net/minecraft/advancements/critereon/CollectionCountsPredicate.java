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
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.MinMaxBounds;

public interface CollectionCountsPredicate<T, P extends Predicate<T>>
extends Predicate<Iterable<T>> {
    public List<Entry<T, P>> unpack();

    public static <T, P extends Predicate<T>> Codec<CollectionCountsPredicate<T, P>> codec(Codec<P> codec) {
        return Entry.codec(codec).listOf().xmap(CollectionCountsPredicate::of, CollectionCountsPredicate::unpack);
    }

    @SafeVarargs
    public static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> of(Entry<T, P> ... entryArray) {
        return CollectionCountsPredicate.of(List.of(entryArray));
    }

    public static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> of(List<Entry<T, P>> list) {
        return switch (list.size()) {
            case 0 -> new Zero();
            case 1 -> new Single<T, P>(list.getFirst());
            default -> new Multiple<T, P>(list);
        };
    }

    public record Entry<T, P extends Predicate<T>>(P test, MinMaxBounds.Ints count) {
        public static <T, P extends Predicate<T>> Codec<Entry<T, P>> codec(Codec<P> codec) {
            return RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf("test").forGetter(Entry::test), (App)MinMaxBounds.Ints.CODEC.fieldOf("count").forGetter(Entry::count)).apply((Applicative)instance, Entry::new));
        }

        public boolean test(Iterable<T> iterable) {
            int n = 0;
            for (T t : iterable) {
                if (!this.test.test(t)) continue;
                ++n;
            }
            return this.count.matches(n);
        }
    }

    public static class Zero<T, P extends Predicate<T>>
    implements CollectionCountsPredicate<T, P> {
        @Override
        public boolean test(Iterable<T> iterable) {
            return true;
        }

        @Override
        public List<Entry<T, P>> unpack() {
            return List.of();
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }

    public record Single<T, P extends Predicate<T>>(Entry<T, P> entry) implements CollectionCountsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> iterable) {
            return this.entry.test(iterable);
        }

        @Override
        public List<Entry<T, P>> unpack() {
            return List.of(this.entry);
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }

    public record Multiple<T, P extends Predicate<T>>(List<Entry<T, P>> entries) implements CollectionCountsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> iterable) {
            for (Entry<T, P> entry : this.entries) {
                if (entry.test(iterable)) continue;
                return false;
            }
            return true;
        }

        @Override
        public List<Entry<T, P>> unpack() {
            return this.entries;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }
}

