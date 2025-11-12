/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LootTable EMPTY = new LootTable(LootContextParamSets.EMPTY, Optional.empty(), List.of(), List.of());
    public static final LootContextParamSet DEFAULT_PARAM_SET = LootContextParamSets.ALL_PARAMS;
    public static final long RANDOMIZE_SEED = 0L;
    public static final Codec<LootTable> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)LootContextParamSets.CODEC.lenientOptionalFieldOf("type", (Object)DEFAULT_PARAM_SET).forGetter(lootTable -> lootTable.paramSet), (App)ResourceLocation.CODEC.optionalFieldOf("random_sequence").forGetter(lootTable -> lootTable.randomSequence), (App)LootPool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(lootTable -> lootTable.pools), (App)LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(lootTable -> lootTable.functions)).apply((Applicative)instance, LootTable::new));
    public static final Codec<Holder<LootTable>> CODEC = RegistryFileCodec.create(Registries.LOOT_TABLE, DIRECT_CODEC);
    private final LootContextParamSet paramSet;
    private final Optional<ResourceLocation> randomSequence;
    private final List<LootPool> pools;
    private final List<LootItemFunction> functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

    LootTable(LootContextParamSet lootContextParamSet, Optional<ResourceLocation> optional, List<LootPool> list, List<LootItemFunction> list2) {
        this.paramSet = lootContextParamSet;
        this.randomSequence = optional;
        this.pools = list;
        this.functions = list2;
        this.compositeFunction = LootItemFunctions.compose(list2);
    }

    public static Consumer<ItemStack> createStackSplitter(ServerLevel serverLevel, Consumer<ItemStack> consumer) {
        return itemStack -> {
            if (!itemStack.isItemEnabled(serverLevel.enabledFeatures())) {
                return;
            }
            if (itemStack.getCount() < itemStack.getMaxStackSize()) {
                consumer.accept((ItemStack)itemStack);
            } else {
                ItemStack itemStack2;
                for (int i = itemStack.getCount(); i > 0; i -= itemStack2.getCount()) {
                    itemStack2 = itemStack.copyWithCount(Math.min(itemStack.getMaxStackSize(), i));
                    consumer.accept(itemStack2);
                }
            }
        };
    }

    public void getRandomItemsRaw(LootParams lootParams, Consumer<ItemStack> consumer) {
        this.getRandomItemsRaw(new LootContext.Builder(lootParams).create(this.randomSequence), consumer);
    }

    public void getRandomItemsRaw(LootContext lootContext, Consumer<ItemStack> consumer) {
        LootContext.VisitedEntry<LootTable> visitedEntry = LootContext.createVisitedEntry(this);
        if (lootContext.pushVisitedElement(visitedEntry)) {
            Consumer<ItemStack> consumer2 = LootItemFunction.decorate(this.compositeFunction, consumer, lootContext);
            for (LootPool lootPool : this.pools) {
                lootPool.addRandomItems(consumer2, lootContext);
            }
            lootContext.popVisitedElement(visitedEntry);
        } else {
            LOGGER.warn("Detected infinite loop in loot tables");
        }
    }

    public void getRandomItems(LootParams lootParams, long l, Consumer<ItemStack> consumer) {
        this.getRandomItemsRaw(new LootContext.Builder(lootParams).withOptionalRandomSeed(l).create(this.randomSequence), LootTable.createStackSplitter(lootParams.getLevel(), consumer));
    }

    public void getRandomItems(LootParams lootParams, Consumer<ItemStack> consumer) {
        this.getRandomItemsRaw(lootParams, LootTable.createStackSplitter(lootParams.getLevel(), consumer));
    }

    public void getRandomItems(LootContext lootContext, Consumer<ItemStack> consumer) {
        this.getRandomItemsRaw(lootContext, LootTable.createStackSplitter(lootContext.getLevel(), consumer));
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams lootParams, RandomSource randomSource) {
        return this.getRandomItems(new LootContext.Builder(lootParams).withOptionalRandomSource(randomSource).create(this.randomSequence));
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams lootParams, long l) {
        return this.getRandomItems(new LootContext.Builder(lootParams).withOptionalRandomSeed(l).create(this.randomSequence));
    }

    public ObjectArrayList<ItemStack> getRandomItems(LootParams lootParams) {
        return this.getRandomItems(new LootContext.Builder(lootParams).create(this.randomSequence));
    }

    private ObjectArrayList<ItemStack> getRandomItems(LootContext lootContext) {
        ObjectArrayList objectArrayList = new ObjectArrayList();
        this.getRandomItems(lootContext, arg_0 -> ((ObjectArrayList)objectArrayList).add(arg_0));
        return objectArrayList;
    }

    public LootContextParamSet getParamSet() {
        return this.paramSet;
    }

    public void validate(ValidationContext validationContext) {
        int n;
        for (n = 0; n < this.pools.size(); ++n) {
            this.pools.get(n).validate(validationContext.forChild(".pools[" + n + "]"));
        }
        for (n = 0; n < this.functions.size(); ++n) {
            this.functions.get(n).validate(validationContext.forChild(".functions[" + n + "]"));
        }
    }

    public void fill(Container container, LootParams lootParams, long l) {
        LootContext lootContext = new LootContext.Builder(lootParams).withOptionalRandomSeed(l).create(this.randomSequence);
        ObjectArrayList<ItemStack> objectArrayList = this.getRandomItems(lootContext);
        RandomSource randomSource = lootContext.getRandom();
        List<Integer> list = this.getAvailableSlots(container, randomSource);
        this.shuffleAndSplitItems(objectArrayList, list.size(), randomSource);
        for (ItemStack itemStack : objectArrayList) {
            if (list.isEmpty()) {
                LOGGER.warn("Tried to over-fill a container");
                return;
            }
            if (itemStack.isEmpty()) {
                container.setItem(list.remove(list.size() - 1), ItemStack.EMPTY);
                continue;
            }
            container.setItem(list.remove(list.size() - 1), itemStack);
        }
    }

    private void shuffleAndSplitItems(ObjectArrayList<ItemStack> objectArrayList, int n, RandomSource randomSource) {
        ArrayList arrayList = Lists.newArrayList();
        Object object = objectArrayList.iterator();
        while (object.hasNext()) {
            ItemStack itemStack = (ItemStack)object.next();
            if (itemStack.isEmpty()) {
                object.remove();
                continue;
            }
            if (itemStack.getCount() <= 1) continue;
            arrayList.add(itemStack);
            object.remove();
        }
        while (n - objectArrayList.size() - arrayList.size() > 0 && !arrayList.isEmpty()) {
            object = (ItemStack)arrayList.remove(Mth.nextInt(randomSource, 0, arrayList.size() - 1));
            int n2 = Mth.nextInt(randomSource, 1, ((ItemStack)object).getCount() / 2);
            ItemStack itemStack = ((ItemStack)object).split(n2);
            if (((ItemStack)object).getCount() > 1 && randomSource.nextBoolean()) {
                arrayList.add(object);
            } else {
                objectArrayList.add(object);
            }
            if (itemStack.getCount() > 1 && randomSource.nextBoolean()) {
                arrayList.add(itemStack);
                continue;
            }
            objectArrayList.add((Object)itemStack);
        }
        objectArrayList.addAll((Collection)arrayList);
        Util.shuffle(objectArrayList, randomSource);
    }

    private List<Integer> getAvailableSlots(Container container, RandomSource randomSource) {
        ObjectArrayList objectArrayList = new ObjectArrayList();
        for (int i = 0; i < container.getContainerSize(); ++i) {
            if (!container.getItem(i).isEmpty()) continue;
            objectArrayList.add((Object)i);
        }
        Util.shuffle(objectArrayList, randomSource);
        return objectArrayList;
    }

    public static Builder lootTable() {
        return new Builder();
    }

    public static class Builder
    implements FunctionUserBuilder<Builder> {
        private final ImmutableList.Builder<LootPool> pools = ImmutableList.builder();
        private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
        private LootContextParamSet paramSet = DEFAULT_PARAM_SET;
        private Optional<ResourceLocation> randomSequence = Optional.empty();

        public Builder withPool(LootPool.Builder builder) {
            this.pools.add((Object)builder.build());
            return this;
        }

        public Builder setParamSet(LootContextParamSet lootContextParamSet) {
            this.paramSet = lootContextParamSet;
            return this;
        }

        public Builder setRandomSequence(ResourceLocation resourceLocation) {
            this.randomSequence = Optional.of(resourceLocation);
            return this;
        }

        @Override
        public Builder apply(LootItemFunction.Builder builder) {
            this.functions.add((Object)builder.build());
            return this;
        }

        @Override
        public Builder unwrap() {
            return this;
        }

        public LootTable build() {
            return new LootTable(this.paramSet, this.randomSequence, (List<LootPool>)this.pools.build(), (List<LootItemFunction>)this.functions.build());
        }

        @Override
        public /* synthetic */ FunctionUserBuilder unwrap() {
            return this.unwrap();
        }

        @Override
        public /* synthetic */ FunctionUserBuilder apply(LootItemFunction.Builder builder) {
            return this.apply(builder);
        }
    }
}

