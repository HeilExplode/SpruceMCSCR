/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase
extends LootPoolEntryContainer {
    protected final List<LootPoolEntryContainer> children;
    private final ComposableEntryContainer composedChildren;

    protected CompositeEntryBase(List<LootPoolEntryContainer> list, List<LootItemCondition> list2) {
        super(list2);
        this.children = list;
        this.composedChildren = this.compose(list);
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        if (this.children.isEmpty()) {
            validationContext.reportProblem("Empty children list");
        }
        for (int i = 0; i < this.children.size(); ++i) {
            this.children.get(i).validate(validationContext.forChild(".entry[" + i + "]"));
        }
    }

    protected abstract ComposableEntryContainer compose(List<? extends ComposableEntryContainer> var1);

    @Override
    public final boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (!this.canRun(lootContext)) {
            return false;
        }
        return this.composedChildren.expand(lootContext, consumer);
    }

    public static <T extends CompositeEntryBase> MapCodec<T> createCodec(CompositeEntryConstructor<T> compositeEntryConstructor) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootPoolEntries.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter(compositeEntryBase -> compositeEntryBase.children)).and(CompositeEntryBase.commonFields(instance).t1()).apply((Applicative)instance, compositeEntryConstructor::create));
    }

    @FunctionalInterface
    public static interface CompositeEntryConstructor<T extends CompositeEntryBase> {
        public T create(List<LootPoolEntryContainer> var1, List<LootItemCondition> var2);
    }
}

