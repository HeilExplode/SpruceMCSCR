/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.world.level.storage.loot.predicates.CompositeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class AllOfCondition
extends CompositeLootItemCondition {
    public static final MapCodec<AllOfCondition> CODEC = AllOfCondition.createCodec(AllOfCondition::new);
    public static final Codec<AllOfCondition> INLINE_CODEC = AllOfCondition.createInlineCodec(AllOfCondition::new);

    AllOfCondition(List<LootItemCondition> list) {
        super(list, Util.allOf(list));
    }

    public static AllOfCondition allOf(List<LootItemCondition> list) {
        return new AllOfCondition(List.copyOf(list));
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ALL_OF;
    }

    public static Builder allOf(LootItemCondition.Builder ... builderArray) {
        return new Builder(builderArray);
    }

    public static class Builder
    extends CompositeLootItemCondition.Builder {
        public Builder(LootItemCondition.Builder ... builderArray) {
            super(builderArray);
        }

        @Override
        public Builder and(LootItemCondition.Builder builder) {
            this.addTerm(builder);
            return this;
        }

        @Override
        protected LootItemCondition create(List<LootItemCondition> list) {
            return new AllOfCondition(list);
        }
    }
}

