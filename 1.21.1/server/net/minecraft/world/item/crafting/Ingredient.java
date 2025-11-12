/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntComparators
 *  it.unimi.dsi.fastutil.ints.IntList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class Ingredient
implements Predicate<ItemStack> {
    public static final Ingredient EMPTY = new Ingredient(Stream.empty());
    public static final StreamCodec<RegistryFriendlyByteBuf, Ingredient> CONTENTS_STREAM_CODEC = ItemStack.LIST_STREAM_CODEC.map(list -> Ingredient.fromValues(list.stream().map(ItemValue::new)), ingredient -> Arrays.asList(ingredient.getItems()));
    private final Value[] values;
    @Nullable
    private ItemStack[] itemStacks;
    @Nullable
    private IntList stackingIds;
    public static final Codec<Ingredient> CODEC = Ingredient.codec(true);
    public static final Codec<Ingredient> CODEC_NONEMPTY = Ingredient.codec(false);

    private Ingredient(Stream<? extends Value> stream) {
        this.values = (Value[])stream.toArray(Value[]::new);
    }

    private Ingredient(Value[] valueArray) {
        this.values = valueArray;
    }

    public ItemStack[] getItems() {
        if (this.itemStacks == null) {
            this.itemStacks = (ItemStack[])Arrays.stream(this.values).flatMap(value -> value.getItems().stream()).distinct().toArray(ItemStack[]::new);
        }
        return this.itemStacks;
    }

    @Override
    public boolean test(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        if (this.isEmpty()) {
            return itemStack.isEmpty();
        }
        for (ItemStack itemStack2 : this.getItems()) {
            if (!itemStack2.is(itemStack.getItem())) continue;
            return true;
        }
        return false;
    }

    public IntList getStackingIds() {
        if (this.stackingIds == null) {
            ItemStack[] itemStackArray = this.getItems();
            this.stackingIds = new IntArrayList(itemStackArray.length);
            for (ItemStack itemStack : itemStackArray) {
                this.stackingIds.add(StackedContents.getStackingIndex(itemStack));
            }
            this.stackingIds.sort(IntComparators.NATURAL_COMPARATOR);
        }
        return this.stackingIds;
    }

    public boolean isEmpty() {
        return this.values.length == 0;
    }

    public boolean equals(Object object) {
        if (object instanceof Ingredient) {
            Ingredient ingredient = (Ingredient)object;
            return Arrays.equals(this.values, ingredient.values);
        }
        return false;
    }

    private static Ingredient fromValues(Stream<? extends Value> stream) {
        Ingredient ingredient = new Ingredient(stream);
        return ingredient.isEmpty() ? EMPTY : ingredient;
    }

    public static Ingredient of() {
        return EMPTY;
    }

    public static Ingredient of(ItemLike ... itemLikeArray) {
        return Ingredient.of(Arrays.stream(itemLikeArray).map(ItemStack::new));
    }

    public static Ingredient of(ItemStack ... itemStackArray) {
        return Ingredient.of(Arrays.stream(itemStackArray));
    }

    public static Ingredient of(Stream<ItemStack> stream) {
        return Ingredient.fromValues(stream.filter(itemStack -> !itemStack.isEmpty()).map(ItemValue::new));
    }

    public static Ingredient of(TagKey<Item> tagKey) {
        return Ingredient.fromValues(Stream.of(new TagValue(tagKey)));
    }

    private static Codec<Ingredient> codec(boolean bl) {
        Codec codec = Codec.list(Value.CODEC).comapFlatMap(list -> {
            if (!bl && list.size() < 1) {
                return DataResult.error(() -> "Item array cannot be empty, at least one item must be defined");
            }
            return DataResult.success((Object)list.toArray(new Value[0]));
        }, List::of);
        return Codec.either((Codec)codec, Value.CODEC).flatComapMap(either -> (Ingredient)either.map(Ingredient::new, value -> new Ingredient(new Value[]{value})), ingredient -> {
            if (ingredient.values.length == 1) {
                return DataResult.success((Object)Either.right((Object)ingredient.values[0]));
            }
            if (ingredient.values.length == 0 && !bl) {
                return DataResult.error(() -> "Item array cannot be empty, at least one item must be defined");
            }
            return DataResult.success((Object)Either.left((Object)ingredient.values));
        });
    }

    @Override
    public /* synthetic */ boolean test(@Nullable Object object) {
        return this.test((ItemStack)object);
    }

    static interface Value {
        public static final Codec<Value> CODEC = Codec.xor(ItemValue.CODEC, TagValue.CODEC).xmap(either -> (Value)either.map(itemValue -> itemValue, tagValue -> tagValue), value -> {
            if (value instanceof TagValue) {
                TagValue tagValue = (TagValue)value;
                return Either.right((Object)tagValue);
            }
            if (value instanceof ItemValue) {
                ItemValue itemValue = (ItemValue)value;
                return Either.left((Object)itemValue);
            }
            throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
        });

        public Collection<ItemStack> getItems();
    }

    record TagValue(TagKey<Item> tag) implements Value
    {
        static final Codec<TagValue> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(tagValue -> tagValue.tag)).apply((Applicative)instance, TagValue::new));

        @Override
        public boolean equals(Object object) {
            if (object instanceof TagValue) {
                TagValue tagValue = (TagValue)object;
                return tagValue.tag.location().equals(this.tag.location());
            }
            return false;
        }

        @Override
        public Collection<ItemStack> getItems() {
            ArrayList arrayList = Lists.newArrayList();
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
                arrayList.add(new ItemStack(holder));
            }
            return arrayList;
        }
    }

    record ItemValue(ItemStack item) implements Value
    {
        static final Codec<ItemValue> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ItemStack.SIMPLE_ITEM_CODEC.fieldOf("item").forGetter(itemValue -> itemValue.item)).apply((Applicative)instance, ItemValue::new));

        @Override
        public boolean equals(Object object) {
            if (object instanceof ItemValue) {
                ItemValue itemValue = (ItemValue)object;
                return itemValue.item.getItem().equals(this.item.getItem()) && itemValue.item.getCount() == this.item.getCount();
            }
            return false;
        }

        @Override
        public Collection<ItemStack> getItems() {
            return Collections.singleton(this.item);
        }
    }
}

