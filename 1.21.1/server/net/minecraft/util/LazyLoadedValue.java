/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 */
package net.minecraft.util;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;

@Deprecated
public class LazyLoadedValue<T> {
    private final Supplier<T> factory = Suppliers.memoize(supplier::get);

    public LazyLoadedValue(Supplier<T> supplier) {
    }

    public T get() {
        return this.factory.get();
    }
}

