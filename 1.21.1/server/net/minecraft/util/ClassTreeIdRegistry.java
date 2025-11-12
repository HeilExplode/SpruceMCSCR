/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;

public class ClassTreeIdRegistry {
    public static final int NO_ID_VALUE = -1;
    private final Object2IntMap<Class<?>> classToLastIdCache = (Object2IntMap)Util.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> object2IntOpenHashMap.defaultReturnValue(-1));

    public int getLastIdFor(Class<?> clazz) {
        int n = this.classToLastIdCache.getInt(clazz);
        if (n != -1) {
            return n;
        }
        Class<?> clazz2 = clazz;
        while ((clazz2 = clazz2.getSuperclass()) != Object.class) {
            int n2 = this.classToLastIdCache.getInt(clazz2);
            if (n2 == -1) continue;
            return n2;
        }
        return -1;
    }

    public int getCount(Class<?> clazz) {
        return this.getLastIdFor(clazz) + 1;
    }

    public int define(Class<?> clazz) {
        int n = this.getLastIdFor(clazz);
        int n2 = n == -1 ? 0 : n + 1;
        this.classToLastIdCache.put(clazz, n2);
        return n2;
    }
}

