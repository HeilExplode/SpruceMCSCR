/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class AnyOfCondition
extends CompositeLootItemCondition {
    public static final MapCodec<AnyOfCondition> CODEC = AnyOfCondition.createCodec(AnyOfCondition::new);

    AnyOfCondition(List<LootItemCondition> list) {
        super(list, Util.anyOf(list));
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ANY_OF;
    }

    public static Builder anyOf(LootItemCondition.Builder ... builderArray) {
        return new Builder(builderArray);
    }

    public static class Builder
    extends CompositeLootItemCondition.Builder {
        public Builder(LootItemCondition.Builder ... builderArray) {
            super(builderArray);
        }

        @Override
        public Builder or(LootItemCondition.Builder builder) {
            this.addTerm(builder);
            return this;
        }

        @Override
        protected LootItemCondition create(List<LootItemCondition> list) {
            return new AnyOfCondition(list);
        }
    }
}

