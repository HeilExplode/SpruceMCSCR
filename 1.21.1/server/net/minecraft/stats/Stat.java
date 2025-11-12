/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.stats;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.StatType;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Stat<T>
extends ObjectiveCriteria {
    public static final StreamCodec<RegistryFriendlyByteBuf, Stat<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.STAT_TYPE).dispatch(Stat::getType, StatType::streamCodec);
    private final StatFormatter formatter;
    private final T value;
    private final StatType<T> type;

    protected Stat(StatType<T> statType, T t, StatFormatter statFormatter) {
        super(Stat.buildName(statType, t));
        this.type = statType;
        this.formatter = statFormatter;
        this.value = t;
    }

    public static <T> String buildName(StatType<T> statType, T t) {
        return Stat.locationToKey(BuiltInRegistries.STAT_TYPE.getKey(statType)) + ":" + Stat.locationToKey(statType.getRegistry().getKey(t));
    }

    private static <T> String locationToKey(@Nullable ResourceLocation resourceLocation) {
        return resourceLocation.toString().replace(':', '.');
    }

    public StatType<T> getType() {
        return this.type;
    }

    public T getValue() {
        return this.value;
    }

    public String format(int n) {
        return this.formatter.format(n);
    }

    public boolean equals(Object object) {
        return this == object || object instanceof Stat && Objects.equals(this.getName(), ((Stat)object).getName());
    }

    public int hashCode() {
        return this.getName().hashCode();
    }

    public String toString() {
        return "Stat{name=" + this.getName() + ", formatter=" + String.valueOf(this.formatter) + "}";
    }
}

