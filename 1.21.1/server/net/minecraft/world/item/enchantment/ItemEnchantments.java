/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item.enchantment;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.enchantment.Enchantment;

public class ItemEnchantments
implements TooltipProvider {
    public static final ItemEnchantments EMPTY = new ItemEnchantments((Object2IntOpenHashMap<Holder<Enchantment>>)new Object2IntOpenHashMap(), true);
    private static final Codec<Integer> LEVEL_CODEC = Codec.intRange((int)0, (int)255);
    private static final Codec<Object2IntOpenHashMap<Holder<Enchantment>>> LEVELS_CODEC = Codec.unboundedMap(Enchantment.CODEC, LEVEL_CODEC).xmap(Object2IntOpenHashMap::new, Function.identity());
    private static final Codec<ItemEnchantments> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)LEVELS_CODEC.fieldOf("levels").forGetter(itemEnchantments -> itemEnchantments.enchantments), (App)Codec.BOOL.optionalFieldOf("show_in_tooltip", (Object)true).forGetter(itemEnchantments -> itemEnchantments.showInTooltip)).apply((Applicative)instance, ItemEnchantments::new));
    public static final Codec<ItemEnchantments> CODEC = Codec.withAlternative(FULL_CODEC, LEVELS_CODEC, object2IntOpenHashMap -> new ItemEnchantments((Object2IntOpenHashMap<Holder<Enchantment>>)object2IntOpenHashMap, true));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemEnchantments> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.map(Object2IntOpenHashMap::new, Enchantment.STREAM_CODEC, ByteBufCodecs.VAR_INT), itemEnchantments -> itemEnchantments.enchantments, ByteBufCodecs.BOOL, itemEnchantments -> itemEnchantments.showInTooltip, ItemEnchantments::new);
    final Object2IntOpenHashMap<Holder<Enchantment>> enchantments;
    final boolean showInTooltip;

    ItemEnchantments(Object2IntOpenHashMap<Holder<Enchantment>> object2IntOpenHashMap, boolean bl) {
        this.enchantments = object2IntOpenHashMap;
        this.showInTooltip = bl;
        for (Object2IntMap.Entry entry : object2IntOpenHashMap.object2IntEntrySet()) {
            int n = entry.getIntValue();
            if (n >= 0 && n <= 255) continue;
            throw new IllegalArgumentException("Enchantment " + String.valueOf(entry.getKey()) + " has invalid level " + n);
        }
    }

    public int getLevel(Holder<Enchantment> holder) {
        return this.enchantments.getInt(holder);
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        if (!this.showInTooltip) {
            return;
        }
        HolderLookup.Provider provider = tooltipContext.registries();
        HolderSet<Enchantment> holderSet = ItemEnchantments.getTagOrEmpty(provider, Registries.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);
        for (Holder holder : holderSet) {
            int n = this.enchantments.getInt((Object)holder);
            if (n <= 0) continue;
            consumer.accept(Enchantment.getFullname(holder, n));
        }
        for (Object2IntMap.Entry entry : this.enchantments.object2IntEntrySet()) {
            Holder holder = (Holder)entry.getKey();
            if (holderSet.contains(holder)) continue;
            consumer.accept(Enchantment.getFullname((Holder)entry.getKey(), entry.getIntValue()));
        }
    }

    private static <T> HolderSet<T> getTagOrEmpty(@Nullable HolderLookup.Provider provider, ResourceKey<Registry<T>> resourceKey, TagKey<T> tagKey) {
        Optional<HolderSet.Named<T>> optional;
        if (provider != null && (optional = provider.lookupOrThrow(resourceKey).get(tagKey)).isPresent()) {
            return optional.get();
        }
        return HolderSet.direct(new Holder[0]);
    }

    public ItemEnchantments withTooltip(boolean bl) {
        return new ItemEnchantments(this.enchantments, bl);
    }

    public Set<Holder<Enchantment>> keySet() {
        return Collections.unmodifiableSet(this.enchantments.keySet());
    }

    public Set<Object2IntMap.Entry<Holder<Enchantment>>> entrySet() {
        return Collections.unmodifiableSet(this.enchantments.object2IntEntrySet());
    }

    public int size() {
        return this.enchantments.size();
    }

    public boolean isEmpty() {
        return this.enchantments.isEmpty();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ItemEnchantments) {
            ItemEnchantments itemEnchantments = (ItemEnchantments)object;
            return this.showInTooltip == itemEnchantments.showInTooltip && this.enchantments.equals(itemEnchantments.enchantments);
        }
        return false;
    }

    public int hashCode() {
        int n = this.enchantments.hashCode();
        n = 31 * n + (this.showInTooltip ? 1 : 0);
        return n;
    }

    public String toString() {
        return "ItemEnchantments{enchantments=" + String.valueOf(this.enchantments) + ", showInTooltip=" + this.showInTooltip + "}";
    }

    public static class Mutable {
        private final Object2IntOpenHashMap<Holder<Enchantment>> enchantments = new Object2IntOpenHashMap();
        private final boolean showInTooltip;

        public Mutable(ItemEnchantments itemEnchantments) {
            this.enchantments.putAll(itemEnchantments.enchantments);
            this.showInTooltip = itemEnchantments.showInTooltip;
        }

        public void set(Holder<Enchantment> holder, int n) {
            if (n <= 0) {
                this.enchantments.removeInt(holder);
            } else {
                this.enchantments.put(holder, Math.min(n, 255));
            }
        }

        public void upgrade(Holder<Enchantment> holder, int n) {
            if (n > 0) {
                this.enchantments.merge(holder, Math.min(n, 255), Integer::max);
            }
        }

        public void removeIf(Predicate<Holder<Enchantment>> predicate) {
            this.enchantments.keySet().removeIf(predicate);
        }

        public int getLevel(Holder<Enchantment> holder) {
            return this.enchantments.getOrDefault(holder, 0);
        }

        public Set<Holder<Enchantment>> keySet() {
            return this.enchantments.keySet();
        }

        public ItemEnchantments toImmutable() {
            return new ItemEnchantments(this.enchantments, this.showInTooltip);
        }
    }
}

