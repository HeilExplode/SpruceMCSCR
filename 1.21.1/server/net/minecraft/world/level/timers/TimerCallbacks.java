/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.timers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;
import net.minecraft.world.level.timers.TimerCallback;
import org.slf4j.Logger;

public class TimerCallbacks<C> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final TimerCallbacks<MinecraftServer> SERVER_CALLBACKS = new TimerCallbacks<MinecraftServer>().register(new FunctionCallback.Serializer()).register(new FunctionTagCallback.Serializer());
    private final Map<ResourceLocation, TimerCallback.Serializer<C, ?>> idToSerializer = Maps.newHashMap();
    private final Map<Class<?>, TimerCallback.Serializer<C, ?>> classToSerializer = Maps.newHashMap();

    @VisibleForTesting
    public TimerCallbacks() {
    }

    public TimerCallbacks<C> register(TimerCallback.Serializer<C, ?> serializer) {
        this.idToSerializer.put(serializer.getId(), serializer);
        this.classToSerializer.put(serializer.getCls(), serializer);
        return this;
    }

    private <T extends TimerCallback<C>> TimerCallback.Serializer<C, T> getSerializer(Class<?> clazz) {
        return this.classToSerializer.get(clazz);
    }

    public <T extends TimerCallback<C>> CompoundTag serialize(T t) {
        TimerCallback.Serializer<T, T> serializer = this.getSerializer(t.getClass());
        CompoundTag compoundTag = new CompoundTag();
        serializer.serialize(compoundTag, t);
        compoundTag.putString("Type", serializer.getId().toString());
        return compoundTag;
    }

    @Nullable
    public TimerCallback<C> deserialize(CompoundTag compoundTag) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString("Type"));
        TimerCallback.Serializer<C, ?> serializer = this.idToSerializer.get(resourceLocation);
        if (serializer == null) {
            LOGGER.error("Failed to deserialize timer callback: {}", (Object)compoundTag);
            return null;
        }
        try {
            return serializer.deserialize(compoundTag);
        }
        catch (Exception exception) {
            LOGGER.error("Failed to deserialize timer callback: {}", (Object)compoundTag, (Object)exception);
            return null;
        }
    }
}

