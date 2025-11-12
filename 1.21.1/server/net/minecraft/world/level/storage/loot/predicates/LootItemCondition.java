/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public interface LootItemCondition
extends LootContextUser,
Predicate<LootContext> {
    public static final Codec<LootItemCondition> TYPED_CODEC = BuiltInRegistries.LOOT_CONDITION_TYPE.byNameCodec().dispatch("condition", LootItemCondition::getType, LootItemConditionType::codec);
    public static final Codec<LootItemCondition> DIRECT_CODEC = Codec.lazyInitialized(() -> Codec.withAlternative(TYPED_CODEC, AllOfCondition.INLINE_CODEC));
    public static final Codec<Holder<LootItemCondition>> CODEC = RegistryFileCodec.create(Registries.PREDICATE, DIRECT_CODEC);

    public LootItemConditionType getType();

    @FunctionalInterface
    public static interface Builder {
        public LootItemCondition build();

        default public Builder invert() {
            return InvertedLootItemCondition.invert(this);
        }

        default public AnyOfCondition.Builder or(Builder builder) {
            return AnyOfCondition.anyOf(this, builder);
        }

        default public AllOfCondition.Builder and(Builder builder) {
            return AllOfCondition.allOf(this, builder);
        }
    }
}

