/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ItemPredicate(Optional<HolderSet<Item>> items, MinMaxBounds.Ints count, DataComponentPredicate components, Map<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates) implements Predicate<ItemStack>
{
    public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items").forGetter(ItemPredicate::items), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("count", (Object)MinMaxBounds.Ints.ANY).forGetter(ItemPredicate::count), (App)DataComponentPredicate.CODEC.optionalFieldOf("components", (Object)DataComponentPredicate.EMPTY).forGetter(ItemPredicate::components), (App)ItemSubPredicate.CODEC.optionalFieldOf("predicates", Map.of()).forGetter(ItemPredicate::subPredicates)).apply((Applicative)instance, ItemPredicate::new));

    @Override
    public boolean test(ItemStack itemStack) {
        if (this.items.isPresent() && !itemStack.is(this.items.get())) {
            return false;
        }
        if (!this.count.matches(itemStack.getCount())) {
            return false;
        }
        if (!this.components.test(itemStack)) {
            return false;
        }
        for (ItemSubPredicate itemSubPredicate : this.subPredicates.values()) {
            if (itemSubPredicate.matches(itemStack)) continue;
            return false;
        }
        return true;
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((ItemStack)object);
    }

    public static class Builder {
        private Optional<HolderSet<Item>> items = Optional.empty();
        private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
        private DataComponentPredicate components = DataComponentPredicate.EMPTY;
        private final ImmutableMap.Builder<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates = ImmutableMap.builder();

        private Builder() {
        }

        public static Builder item() {
            return new Builder();
        }

        public Builder of(ItemLike ... itemLikeArray) {
            this.items = Optional.of(HolderSet.direct(itemLike -> itemLike.asItem().builtInRegistryHolder(), itemLikeArray));
            return this;
        }

        public Builder of(TagKey<Item> tagKey) {
            this.items = Optional.of(BuiltInRegistries.ITEM.getOrCreateTag(tagKey));
            return this;
        }

        public Builder withCount(MinMaxBounds.Ints ints) {
            this.count = ints;
            return this;
        }

        public <T extends ItemSubPredicate> Builder withSubPredicate(ItemSubPredicate.Type<T> type, T t) {
            this.subPredicates.put(type, t);
            return this;
        }

        public Builder hasComponents(DataComponentPredicate dataComponentPredicate) {
            this.components = dataComponentPredicate;
            return this;
        }

        public ItemPredicate build() {
            return new ItemPredicate(this.items, this.count, this.components, (Map<ItemSubPredicate.Type<?>, ItemSubPredicate>)this.subPredicates.build());
        }
    }
}

