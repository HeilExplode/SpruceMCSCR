/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.gson.JsonObject
 */
package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;

public interface ResourceMetadata {
    public static final ResourceMetadata EMPTY = new ResourceMetadata(){

        @Override
        public <T> Optional<T> getSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
            return Optional.empty();
        }
    };
    public static final IoSupplier<ResourceMetadata> EMPTY_SUPPLIER = () -> EMPTY;

    public static ResourceMetadata fromJsonStream(InputStream inputStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
            final JsonObject jsonObject = GsonHelper.parse(bufferedReader);
            ResourceMetadata resourceMetadata = new ResourceMetadata(){

                @Override
                public <T> Optional<T> getSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
                    String string = metadataSectionSerializer.getMetadataSectionName();
                    return jsonObject.has(string) ? Optional.of(metadataSectionSerializer.fromJson(GsonHelper.getAsJsonObject(jsonObject, string))) : Optional.empty();
                }
            };
            return resourceMetadata;
        }
    }

    public <T> Optional<T> getSection(MetadataSectionSerializer<T> var1);

    default public ResourceMetadata copySections(Collection<MetadataSectionSerializer<?>> collection) {
        Builder builder = new Builder();
        for (MetadataSectionSerializer<?> metadataSectionSerializer : collection) {
            this.copySection(builder, metadataSectionSerializer);
        }
        return builder.build();
    }

    private <T> void copySection(Builder builder, MetadataSectionSerializer<T> metadataSectionSerializer) {
        this.getSection(metadataSectionSerializer).ifPresent(object -> builder.put(metadataSectionSerializer, object));
    }

    public static class Builder {
        private final ImmutableMap.Builder<MetadataSectionSerializer<?>, Object> map = ImmutableMap.builder();

        public <T> Builder put(MetadataSectionSerializer<T> metadataSectionSerializer, T t) {
            this.map.put(metadataSectionSerializer, t);
            return this;
        }

        public ResourceMetadata build() {
            final ImmutableMap immutableMap = this.map.build();
            if (immutableMap.isEmpty()) {
                return EMPTY;
            }
            return new ResourceMetadata(){

                @Override
                public <T> Optional<T> getSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
                    return Optional.ofNullable(immutableMap.get(metadataSectionSerializer));
                }
            };
        }
    }
}

