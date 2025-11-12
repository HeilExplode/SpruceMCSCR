/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public record BlockItemStateProperties(Map<String, String> properties) {
    public static final BlockItemStateProperties EMPTY = new BlockItemStateProperties(Map.of());
    public static final Codec<BlockItemStateProperties> CODEC = Codec.unboundedMap((Codec)Codec.STRING, (Codec)Codec.STRING).xmap(BlockItemStateProperties::new, BlockItemStateProperties::properties);
    private static final StreamCodec<ByteBuf, Map<String, String>> PROPERTIES_STREAM_CODEC = ByteBufCodecs.map(Object2ObjectOpenHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8);
    public static final StreamCodec<ByteBuf, BlockItemStateProperties> STREAM_CODEC = PROPERTIES_STREAM_CODEC.map(BlockItemStateProperties::new, BlockItemStateProperties::properties);

    public <T extends Comparable<T>> BlockItemStateProperties with(Property<T> property, T t) {
        return new BlockItemStateProperties(Util.copyAndPut(this.properties, property.getName(), property.getName(t)));
    }

    public <T extends Comparable<T>> BlockItemStateProperties with(Property<T> property, BlockState blockState) {
        return this.with(property, blockState.getValue(property));
    }

    @Nullable
    public <T extends Comparable<T>> T get(Property<T> property) {
        String string = this.properties.get(property.getName());
        if (string == null) {
            return null;
        }
        return (T)((Comparable)property.getValue(string).orElse(null));
    }

    public BlockState apply(BlockState blockState) {
        StateDefinition<Block, BlockState> stateDefinition = blockState.getBlock().getStateDefinition();
        for (Map.Entry<String, String> entry : this.properties.entrySet()) {
            Property<?> property = stateDefinition.getProperty(entry.getKey());
            if (property == null) continue;
            blockState = BlockItemStateProperties.updateState(blockState, property, entry.getValue());
        }
        return blockState;
    }

    private static <T extends Comparable<T>> BlockState updateState(BlockState blockState, Property<T> property, String string) {
        return property.getValue(string).map(comparable -> (BlockState)blockState.setValue(property, comparable)).orElse(blockState);
    }

    public boolean isEmpty() {
        return this.properties.isEmpty();
    }
}

