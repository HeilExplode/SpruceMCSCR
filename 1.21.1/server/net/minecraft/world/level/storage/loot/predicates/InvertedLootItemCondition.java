/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public record InvertedLootItemCondition(LootItemCondition term) implements LootItemCondition
{
    public static final MapCodec<InvertedLootItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootItemCondition.DIRECT_CODEC.fieldOf("term").forGetter(InvertedLootItemCondition::term)).apply((Applicative)instance, InvertedLootItemCondition::new));

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.INVERTED;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return !this.term.test(lootContext);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.term.getReferencedContextParams();
    }

    @Override
    public void validate(ValidationContext validationContext) {
        LootItemCondition.super.validate(validationContext);
        this.term.validate(validationContext);
    }

    public static LootItemCondition.Builder invert(LootItemCondition.Builder builder) {
        InvertedLootItemCondition invertedLootItemCondition = new InvertedLootItemCondition(builder.build());
        return () -> invertedLootItemCondition;
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }
}

