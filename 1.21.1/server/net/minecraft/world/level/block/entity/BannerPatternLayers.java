/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.slf4j.Logger;

public final class BannerPatternLayers
extends Record {
    final List<Layer> layers;
    static final Logger LOGGER = LogUtils.getLogger();
    public static final BannerPatternLayers EMPTY = new BannerPatternLayers(List.of());
    public static final Codec<BannerPatternLayers> CODEC = Layer.CODEC.listOf().xmap(BannerPatternLayers::new, BannerPatternLayers::layers);
    public static final StreamCodec<RegistryFriendlyByteBuf, BannerPatternLayers> STREAM_CODEC = Layer.STREAM_CODEC.apply(ByteBufCodecs.list()).map(BannerPatternLayers::new, BannerPatternLayers::layers);

    public BannerPatternLayers(List<Layer> list) {
        this.layers = list;
    }

    public BannerPatternLayers removeLast() {
        return new BannerPatternLayers(List.copyOf(this.layers.subList(0, this.layers.size() - 1)));
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{BannerPatternLayers.class, "layers", "layers"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BannerPatternLayers.class, "layers", "layers"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BannerPatternLayers.class, "layers", "layers"}, this, object);
    }

    public List<Layer> layers() {
        return this.layers;
    }

    public record Layer(Holder<BannerPattern> pattern, DyeColor color) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BannerPattern.CODEC.fieldOf("pattern").forGetter(Layer::pattern), (App)DyeColor.CODEC.fieldOf("color").forGetter(Layer::color)).apply((Applicative)instance, Layer::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Layer> STREAM_CODEC = StreamCodec.composite(BannerPattern.STREAM_CODEC, Layer::pattern, DyeColor.STREAM_CODEC, Layer::color, Layer::new);

        public MutableComponent description() {
            String string = this.pattern.value().translationKey();
            return Component.translatable(string + "." + this.color.getName());
        }
    }

    public static class Builder {
        private final ImmutableList.Builder<Layer> layers = ImmutableList.builder();

        @Deprecated
        public Builder addIfRegistered(HolderGetter<BannerPattern> holderGetter, ResourceKey<BannerPattern> resourceKey, DyeColor dyeColor) {
            Optional<Holder.Reference<BannerPattern>> optional = holderGetter.get(resourceKey);
            if (optional.isEmpty()) {
                LOGGER.warn("Unable to find banner pattern with id: '{}'", (Object)resourceKey.location());
                return this;
            }
            return this.add((Holder<BannerPattern>)optional.get(), dyeColor);
        }

        public Builder add(Holder<BannerPattern> holder, DyeColor dyeColor) {
            return this.add(new Layer(holder, dyeColor));
        }

        public Builder add(Layer layer) {
            this.layers.add((Object)layer);
            return this;
        }

        public Builder addAll(BannerPatternLayers bannerPatternLayers) {
            this.layers.addAll(bannerPatternLayers.layers);
            return this;
        }

        public BannerPatternLayers build() {
            return new BannerPatternLayers((List<Layer>)this.layers.build());
        }
    }
}

