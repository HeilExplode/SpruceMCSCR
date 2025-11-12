/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Function;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public interface ConditionUserBuilder<T extends ConditionUserBuilder<T>> {
    public T when(LootItemCondition.Builder var1);

    default public <E> T when(Iterable<E> iterable, Function<E, LootItemCondition.Builder> function) {
        T t = this.unwrap();
        for (E e : iterable) {
            t = t.when(function.apply(e));
        }
        return t;
    }

    public T unwrap();
}

