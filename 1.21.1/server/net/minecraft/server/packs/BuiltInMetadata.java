/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs;

import java.util.Map;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

public class BuiltInMetadata {
    private static final BuiltInMetadata EMPTY = new BuiltInMetadata(Map.of());
    private final Map<MetadataSectionSerializer<?>, ?> values;

    private BuiltInMetadata(Map<MetadataSectionSerializer<?>, ?> map) {
        this.values = map;
    }

    public <T> T get(MetadataSectionSerializer<T> metadataSectionSerializer) {
        return (T)this.values.get(metadataSectionSerializer);
    }

    public static BuiltInMetadata of() {
        return EMPTY;
    }

    public static <T> BuiltInMetadata of(MetadataSectionSerializer<T> metadataSectionSerializer, T t) {
        return new BuiltInMetadata(Map.of(metadataSectionSerializer, t));
    }

    public static <T1, T2> BuiltInMetadata of(MetadataSectionSerializer<T1> metadataSectionSerializer, T1 T1, MetadataSectionSerializer<T2> metadataSectionSerializer2, T2 T2) {
        return new BuiltInMetadata(Map.of(metadataSectionSerializer, T1, metadataSectionSerializer2, T2));
    }
}

