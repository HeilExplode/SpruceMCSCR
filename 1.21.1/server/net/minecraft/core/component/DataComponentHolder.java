/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.core.component;

import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;

public interface DataComponentHolder {
    public DataComponentMap getComponents();

    @Nullable
    default public <T> T get(DataComponentType<? extends T> dataComponentType) {
        return this.getComponents().get(dataComponentType);
    }

    default public <T> T getOrDefault(DataComponentType<? extends T> dataComponentType, T t) {
        return this.getComponents().getOrDefault(dataComponentType, t);
    }

    default public boolean has(DataComponentType<?> dataComponentType) {
        return this.getComponents().has(dataComponentType);
    }
}

