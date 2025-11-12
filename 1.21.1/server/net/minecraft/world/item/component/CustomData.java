/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapDecoder
 *  com.mojang.serialization.MapEncoder
 *  com.mojang.serialization.MapLike
 *  io.netty.buffer.ByteBuf
 *  org.slf4j.Logger
 */
package net.minecraft.world.item.component;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

public final class CustomData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CustomData EMPTY = new CustomData(new CompoundTag());
    public static final Codec<CustomData> CODEC = Codec.withAlternative(CompoundTag.CODEC, TagParser.AS_CODEC).xmap(CustomData::new, customData -> customData.tag);
    public static final Codec<CustomData> CODEC_WITH_ID = CODEC.validate(customData -> customData.getUnsafe().contains("id", 8) ? DataResult.success((Object)customData) : DataResult.error(() -> "Missing id for entity in: " + String.valueOf(customData)));
    @Deprecated
    public static final StreamCodec<ByteBuf, CustomData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(CustomData::new, customData -> customData.tag);
    private final CompoundTag tag;

    private CustomData(CompoundTag compoundTag) {
        this.tag = compoundTag;
    }

    public static CustomData of(CompoundTag compoundTag) {
        return new CustomData(compoundTag.copy());
    }

    public static Predicate<ItemStack> itemMatcher(DataComponentType<CustomData> dataComponentType, CompoundTag compoundTag) {
        return itemStack -> {
            CustomData customData = itemStack.getOrDefault(dataComponentType, EMPTY);
            return customData.matchedBy(compoundTag);
        };
    }

    public boolean matchedBy(CompoundTag compoundTag) {
        return NbtUtils.compareNbt(compoundTag, this.tag, true);
    }

    public static void update(DataComponentType<CustomData> dataComponentType, ItemStack itemStack, Consumer<CompoundTag> consumer) {
        CustomData customData = itemStack.getOrDefault(dataComponentType, EMPTY).update(consumer);
        if (customData.tag.isEmpty()) {
            itemStack.remove(dataComponentType);
        } else {
            itemStack.set(dataComponentType, customData);
        }
    }

    public static void set(DataComponentType<CustomData> dataComponentType, ItemStack itemStack, CompoundTag compoundTag) {
        if (!compoundTag.isEmpty()) {
            itemStack.set(dataComponentType, CustomData.of(compoundTag));
        } else {
            itemStack.remove(dataComponentType);
        }
    }

    public CustomData update(Consumer<CompoundTag> consumer) {
        CompoundTag compoundTag = this.tag.copy();
        consumer.accept(compoundTag);
        return new CustomData(compoundTag);
    }

    public void loadInto(Entity entity) {
        CompoundTag compoundTag = entity.saveWithoutId(new CompoundTag());
        UUID uUID = entity.getUUID();
        compoundTag.merge(this.tag);
        entity.load(compoundTag);
        entity.setUUID(uUID);
    }

    public boolean loadInto(BlockEntity blockEntity, HolderLookup.Provider provider) {
        CompoundTag compoundTag = blockEntity.saveCustomOnly(provider);
        CompoundTag compoundTag2 = compoundTag.copy();
        compoundTag.merge(this.tag);
        if (!compoundTag.equals(compoundTag2)) {
            try {
                blockEntity.loadCustomOnly(compoundTag, provider);
                blockEntity.setChanged();
                return true;
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to apply custom data to block entity at {}", (Object)blockEntity.getBlockPos(), (Object)exception);
                try {
                    blockEntity.loadCustomOnly(compoundTag2, provider);
                }
                catch (Exception exception2) {
                    LOGGER.warn("Failed to rollback block entity at {} after failure", (Object)blockEntity.getBlockPos(), (Object)exception2);
                }
            }
        }
        return false;
    }

    public <T> DataResult<CustomData> update(DynamicOps<Tag> dynamicOps, MapEncoder<T> mapEncoder, T t) {
        return mapEncoder.encode(t, dynamicOps, dynamicOps.mapBuilder()).build((Object)this.tag).map(tag -> new CustomData((CompoundTag)tag));
    }

    public <T> DataResult<T> read(MapDecoder<T> mapDecoder) {
        return this.read(NbtOps.INSTANCE, mapDecoder);
    }

    public <T> DataResult<T> read(DynamicOps<Tag> dynamicOps, MapDecoder<T> mapDecoder) {
        MapLike mapLike = (MapLike)dynamicOps.getMap((Object)this.tag).getOrThrow();
        return mapDecoder.decode(dynamicOps, mapLike);
    }

    public int size() {
        return this.tag.size();
    }

    public boolean isEmpty() {
        return this.tag.isEmpty();
    }

    public CompoundTag copyTag() {
        return this.tag.copy();
    }

    public boolean contains(String string) {
        return this.tag.contains(string);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof CustomData) {
            CustomData customData = (CustomData)object;
            return this.tag.equals(customData.tag);
        }
        return false;
    }

    public int hashCode() {
        return this.tag.hashCode();
    }

    public String toString() {
        return this.tag.toString();
    }

    @Deprecated
    public CompoundTag getUnsafe() {
        return this.tag;
    }
}

