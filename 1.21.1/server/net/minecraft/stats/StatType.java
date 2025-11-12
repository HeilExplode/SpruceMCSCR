/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.stats;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;

public class StatType<T>
implements Iterable<Stat<T>> {
    private final Registry<T> registry;
    private final Map<T, Stat<T>> map = new IdentityHashMap<T, Stat<T>>();
    private final Component displayName;
    private final StreamCodec<RegistryFriendlyByteBuf, Stat<T>> streamCodec;

    public StatType(Registry<T> registry, Component component) {
        this.registry = registry;
        this.displayName = component;
        this.streamCodec = ByteBufCodecs.registry(registry.key()).map(this::get, Stat::getValue);
    }

    public StreamCodec<RegistryFriendlyByteBuf, Stat<T>> streamCodec() {
        return this.streamCodec;
    }

    public boolean contains(T t) {
        return this.map.containsKey(t);
    }

    public Stat<T> get(T t, StatFormatter statFormatter) {
        return this.map.computeIfAbsent(t, object -> new Stat<Object>(this, object, statFormatter));
    }

    public Registry<T> getRegistry() {
        return this.registry;
    }

    @Override
    public Iterator<Stat<T>> iterator() {
        return this.map.values().iterator();
    }

    public Stat<T> get(T t) {
        return this.get(t, StatFormatter.DEFAULT);
    }

    public Component getDisplayName() {
        return this.displayName;
    }
}

