/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  javax.annotation.Nullable
 *  org.jetbrains.annotations.VisibleForTesting
 */
package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.VisibleForTesting;

public interface HolderSet<T>
extends Iterable<Holder<T>> {
    public Stream<Holder<T>> stream();

    public int size();

    public Either<TagKey<T>, List<Holder<T>>> unwrap();

    public Optional<Holder<T>> getRandomElement(RandomSource var1);

    public Holder<T> get(int var1);

    public boolean contains(Holder<T> var1);

    public boolean canSerializeIn(HolderOwner<T> var1);

    public Optional<TagKey<T>> unwrapKey();

    @Deprecated
    @VisibleForTesting
    public static <T> Named<T> emptyNamed(HolderOwner<T> holderOwner, TagKey<T> tagKey) {
        return new Named<T>((HolderOwner)holderOwner, (TagKey)tagKey){

            @Override
            protected List<Holder<T>> contents() {
                throw new UnsupportedOperationException("Tag " + String.valueOf(this.key()) + " can't be dereferenced during construction");
            }
        };
    }

    public static <T> HolderSet<T> empty() {
        return Direct.EMPTY;
    }

    @SafeVarargs
    public static <T> Direct<T> direct(Holder<T> ... holderArray) {
        return new Direct<T>(List.of(holderArray));
    }

    public static <T> Direct<T> direct(List<? extends Holder<T>> list) {
        return new Direct(List.copyOf(list));
    }

    @SafeVarargs
    public static <E, T> Direct<T> direct(Function<E, Holder<T>> function, E ... EArray) {
        return HolderSet.direct(Stream.of(EArray).map(function).toList());
    }

    public static <E, T> Direct<T> direct(Function<E, Holder<T>> function, Collection<E> collection) {
        return HolderSet.direct(collection.stream().map(function).toList());
    }

    public static final class Direct<T>
    extends ListBacked<T> {
        static final Direct<?> EMPTY = new Direct(List.of());
        private final List<Holder<T>> contents;
        @Nullable
        private Set<Holder<T>> contentsSet;

        Direct(List<Holder<T>> list) {
            this.contents = list;
        }

        @Override
        protected List<Holder<T>> contents() {
            return this.contents;
        }

        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() {
            return Either.right(this.contents);
        }

        @Override
        public Optional<TagKey<T>> unwrapKey() {
            return Optional.empty();
        }

        @Override
        public boolean contains(Holder<T> holder) {
            if (this.contentsSet == null) {
                this.contentsSet = Set.copyOf(this.contents);
            }
            return this.contentsSet.contains(holder);
        }

        public String toString() {
            return "DirectSet[" + String.valueOf(this.contents) + "]";
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Direct)) return false;
            Direct direct = (Direct)object;
            if (!this.contents.equals(direct.contents)) return false;
            return true;
        }

        public int hashCode() {
            return this.contents.hashCode();
        }
    }

    public static class Named<T>
    extends ListBacked<T> {
        private final HolderOwner<T> owner;
        private final TagKey<T> key;
        private List<Holder<T>> contents = List.of();

        Named(HolderOwner<T> holderOwner, TagKey<T> tagKey) {
            this.owner = holderOwner;
            this.key = tagKey;
        }

        void bind(List<Holder<T>> list) {
            this.contents = List.copyOf(list);
        }

        public TagKey<T> key() {
            return this.key;
        }

        @Override
        protected List<Holder<T>> contents() {
            return this.contents;
        }

        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() {
            return Either.left(this.key);
        }

        @Override
        public Optional<TagKey<T>> unwrapKey() {
            return Optional.of(this.key);
        }

        @Override
        public boolean contains(Holder<T> holder) {
            return holder.is(this.key);
        }

        public String toString() {
            return "NamedSet(" + String.valueOf(this.key) + ")[" + String.valueOf(this.contents) + "]";
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> holderOwner) {
            return this.owner.canSerializeIn(holderOwner);
        }
    }

    public static abstract class ListBacked<T>
    implements HolderSet<T> {
        protected abstract List<Holder<T>> contents();

        @Override
        public int size() {
            return this.contents().size();
        }

        @Override
        public Spliterator<Holder<T>> spliterator() {
            return this.contents().spliterator();
        }

        @Override
        public Iterator<Holder<T>> iterator() {
            return this.contents().iterator();
        }

        @Override
        public Stream<Holder<T>> stream() {
            return this.contents().stream();
        }

        @Override
        public Optional<Holder<T>> getRandomElement(RandomSource randomSource) {
            return Util.getRandomSafe(this.contents(), randomSource);
        }

        @Override
        public Holder<T> get(int n) {
            return this.contents().get(n);
        }

        @Override
        public boolean canSerializeIn(HolderOwner<T> holderOwner) {
            return true;
        }
    }
}

