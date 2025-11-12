/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.functions;

import java.util.Arrays;
import java.util.function.Function;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public interface FunctionUserBuilder<T extends FunctionUserBuilder<T>> {
    public T apply(LootItemFunction.Builder var1);

    default public <E> T apply(Iterable<E> iterable, Function<E, LootItemFunction.Builder> function) {
        T t = this.unwrap();
        for (E e : iterable) {
            t = t.apply(function.apply(e));
        }
        return t;
    }

    default public <E> T apply(E[] EArray, Function<E, LootItemFunction.Builder> function) {
        return this.apply(Arrays.asList(EArray), function);
    }

    public T unwrap();
}

