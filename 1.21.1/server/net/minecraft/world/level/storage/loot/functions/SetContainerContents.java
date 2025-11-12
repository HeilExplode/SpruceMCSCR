/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerContents
extends LootItemConditionalFunction {
    public static final MapCodec<SetContainerContents> CODEC = RecordCodecBuilder.mapCodec(instance -> SetContainerContents.commonFields(instance).and(instance.group((App)ContainerComponentManipulators.CODEC.fieldOf("component").forGetter(setContainerContents -> setContainerContents.component), (App)LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter(setContainerContents -> setContainerContents.entries))).apply((Applicative)instance, SetContainerContents::new));
    private final ContainerComponentManipulator<?> component;
    private final List<LootPoolEntryContainer> entries;

    SetContainerContents(List<LootItemCondition> list, ContainerComponentManipulator<?> containerComponentManipulator, List<LootPoolEntryContainer> list2) {
        super(list);
        this.component = containerComponentManipulator;
        this.entries = List.copyOf(list2);
    }

    public LootItemFunctionType<SetContainerContents> getType() {
        return LootItemFunctions.SET_CONTENTS;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.isEmpty()) {
            return itemStack;
        }
        Stream.Builder builder = Stream.builder();
        this.entries.forEach(lootPoolEntryContainer -> lootPoolEntryContainer.expand(lootContext, lootPoolEntry -> lootPoolEntry.createItemStack(LootTable.createStackSplitter(lootContext.getLevel(), builder::add), lootContext)));
        this.component.setContents(itemStack, builder.build());
        return itemStack;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        for (int i = 0; i < this.entries.size(); ++i) {
            this.entries.get(i).validate(validationContext.forChild(".entry[" + i + "]"));
        }
    }

    public static Builder setContents(ContainerComponentManipulator<?> containerComponentManipulator) {
        return new Builder(containerComponentManipulator);
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
        private final ContainerComponentManipulator<?> component;

        public Builder(ContainerComponentManipulator<?> containerComponentManipulator) {
            this.component = containerComponentManipulator;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder withEntry(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add((Object)builder.build());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetContainerContents(this.getConditions(), this.component, (List<LootPoolEntryContainer>)this.entries.build());
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

