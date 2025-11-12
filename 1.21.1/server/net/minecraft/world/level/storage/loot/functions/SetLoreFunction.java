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
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetLoreFunction
extends LootItemConditionalFunction {
    public static final MapCodec<SetLoreFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetLoreFunction.commonFields(instance).and(instance.group((App)ComponentSerialization.CODEC.sizeLimitedListOf(256).fieldOf("lore").forGetter(setLoreFunction -> setLoreFunction.lore), (App)ListOperation.codec(256).forGetter(setLoreFunction -> setLoreFunction.mode), (App)LootContext.EntityTarget.CODEC.optionalFieldOf("entity").forGetter(setLoreFunction -> setLoreFunction.resolutionContext))).apply((Applicative)instance, SetLoreFunction::new));
    private final List<Component> lore;
    private final ListOperation mode;
    private final Optional<LootContext.EntityTarget> resolutionContext;

    public SetLoreFunction(List<LootItemCondition> list, List<Component> list2, ListOperation listOperation, Optional<LootContext.EntityTarget> optional) {
        super(list);
        this.lore = List.copyOf(list2);
        this.mode = listOperation;
        this.resolutionContext = optional;
    }

    public LootItemFunctionType<SetLoreFunction> getType() {
        return LootItemFunctions.SET_LORE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.resolutionContext.map(entityTarget -> Set.of(entityTarget.getParam())).orElseGet(Set::of);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.update(DataComponents.LORE, ItemLore.EMPTY, itemLore -> new ItemLore(this.updateLore((ItemLore)itemLore, lootContext)));
        return itemStack;
    }

    private List<Component> updateLore(@Nullable ItemLore itemLore, LootContext lootContext) {
        if (itemLore == null && this.lore.isEmpty()) {
            return List.of();
        }
        UnaryOperator<Component> unaryOperator = SetNameFunction.createResolver(lootContext, this.resolutionContext.orElse(null));
        List list = this.lore.stream().map(unaryOperator).toList();
        List<Component> list2 = this.mode.apply(itemLore.lines(), list, 256);
        return list2;
    }

    public static Builder setLore() {
        return new Builder();
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private Optional<LootContext.EntityTarget> resolutionContext = Optional.empty();
        private final ImmutableList.Builder<Component> lore = ImmutableList.builder();
        private ListOperation mode = ListOperation.Append.INSTANCE;

        public Builder setMode(ListOperation listOperation) {
            this.mode = listOperation;
            return this;
        }

        public Builder setResolutionContext(LootContext.EntityTarget entityTarget) {
            this.resolutionContext = Optional.of(entityTarget);
            return this;
        }

        public Builder addLine(Component component) {
            this.lore.add((Object)component);
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetLoreFunction(this.getConditions(), (List<Component>)this.lore.build(), this.mode, this.resolutionContext);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

