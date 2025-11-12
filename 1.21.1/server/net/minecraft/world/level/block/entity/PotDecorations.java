/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record PotDecorations(Optional<Item> back, Optional<Item> left, Optional<Item> right, Optional<Item> front) {
    public static final PotDecorations EMPTY = new PotDecorations(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    public static final Codec<PotDecorations> CODEC = BuiltInRegistries.ITEM.byNameCodec().sizeLimitedListOf(4).xmap(PotDecorations::new, PotDecorations::ordered);
    public static final StreamCodec<RegistryFriendlyByteBuf, PotDecorations> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.list(4)).map(PotDecorations::new, PotDecorations::ordered);

    private PotDecorations(List<Item> list) {
        this(PotDecorations.getItem(list, 0), PotDecorations.getItem(list, 1), PotDecorations.getItem(list, 2), PotDecorations.getItem(list, 3));
    }

    public PotDecorations(Item item, Item item2, Item item3, Item item4) {
        this(List.of(item, item2, item3, item4));
    }

    private static Optional<Item> getItem(List<Item> list, int n) {
        if (n >= list.size()) {
            return Optional.empty();
        }
        Item item = list.get(n);
        return item == Items.BRICK ? Optional.empty() : Optional.of(item);
    }

    public CompoundTag save(CompoundTag compoundTag) {
        if (this.equals(EMPTY)) {
            return compoundTag;
        }
        compoundTag.put("sherds", (Tag)CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)this).getOrThrow());
        return compoundTag;
    }

    public List<Item> ordered() {
        return Stream.of(this.back, this.left, this.right, this.front).map(optional -> optional.orElse(Items.BRICK)).toList();
    }

    public static PotDecorations load(@Nullable CompoundTag compoundTag) {
        if (compoundTag == null || !compoundTag.contains("sherds")) {
            return EMPTY;
        }
        return CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get("sherds")).result().orElse(EMPTY);
    }
}

