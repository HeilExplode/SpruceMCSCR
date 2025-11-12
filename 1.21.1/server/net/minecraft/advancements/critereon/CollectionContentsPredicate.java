/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface CollectionContentsPredicate<T, P extends Predicate<T>>
extends Predicate<Iterable<T>> {
    public List<P> unpack();

    public static <T, P extends Predicate<T>> Codec<CollectionContentsPredicate<T, P>> codec(Codec<P> codec) {
        return codec.listOf().xmap(CollectionContentsPredicate::of, CollectionContentsPredicate::unpack);
    }

    @SafeVarargs
    public static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(P ... PArray) {
        return CollectionContentsPredicate.of(List.of(PArray));
    }

    public static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(List<P> list) {
        return switch (list.size()) {
            case 0 -> new Zero();
            case 1 -> new Single((Predicate)list.getFirst());
            default -> new Multiple(list);
        };
    }

    public static class Zero<T, P extends Predicate<T>>
    implements CollectionContentsPredicate<T, P> {
        @Override
        public boolean test(Iterable<T> iterable) {
            return true;
        }

        @Override
        public List<P> unpack() {
            return List.of();
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }

    public record Single<T, P extends Predicate<T>>(P test) implements CollectionContentsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> iterable) {
            for (T t : iterable) {
                if (!this.test.test(t)) continue;
                return true;
            }
            return false;
        }

        @Override
        public List<P> unpack() {
            return List.of(this.test);
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }

    public record Multiple<T, P extends Predicate<T>>(List<P> tests) implements CollectionContentsPredicate<T, P>
    {
        @Override
        public boolean test(Iterable<T> iterable) {
            ArrayList<P> arrayList = new ArrayList<P>(this.tests);
            for (Object t : iterable) {
                arrayList.removeIf(predicate -> predicate.test(t));
                if (!arrayList.isEmpty()) continue;
                return true;
            }
            return false;
        }

        @Override
        public List<P> unpack() {
            return this.tests;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Iterable)object);
        }
    }
}

