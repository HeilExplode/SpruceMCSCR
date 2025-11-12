/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.entity;

import javax.annotation.Nullable;

public interface EntityTypeTest<B, T extends B> {
    public static <B, T extends B> EntityTypeTest<B, T> forClass(final Class<T> clazz) {
        return new EntityTypeTest<B, T>(){

            @Override
            @Nullable
            public T tryCast(B b) {
                return clazz.isInstance(b) ? b : null;
            }

            @Override
            public Class<? extends B> getBaseClass() {
                return clazz;
            }
        };
    }

    public static <B, T extends B> EntityTypeTest<B, T> forExactClass(final Class<T> clazz) {
        return new EntityTypeTest<B, T>(){

            @Override
            @Nullable
            public T tryCast(B b) {
                return clazz.equals(b.getClass()) ? b : null;
            }

            @Override
            public Class<? extends B> getBaseClass() {
                return clazz;
            }
        };
    }

    @Nullable
    public T tryCast(B var1);

    public Class<? extends B> getBaseClass();
}

