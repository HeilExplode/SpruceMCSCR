/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record UniformGenerator(NumberProvider min, NumberProvider max) implements NumberProvider
{
    public static final MapCodec<UniformGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)NumberProviders.CODEC.fieldOf("min").forGetter(UniformGenerator::min), (App)NumberProviders.CODEC.fieldOf("max").forGetter(UniformGenerator::max)).apply((Applicative)instance, UniformGenerator::new));

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.UNIFORM;
    }

    public static UniformGenerator between(float f, float f2) {
        return new UniformGenerator(ConstantValue.exactly(f), ConstantValue.exactly(f2));
    }

    @Override
    public int getInt(LootContext lootContext) {
        return Mth.nextInt(lootContext.getRandom(), this.min.getInt(lootContext), this.max.getInt(lootContext));
    }

    @Override
    public float getFloat(LootContext lootContext) {
        return Mth.nextFloat(lootContext.getRandom(), this.min.getFloat(lootContext), this.max.getFloat(lootContext));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.min.getReferencedContextParams(), this.max.getReferencedContextParams());
    }
}

