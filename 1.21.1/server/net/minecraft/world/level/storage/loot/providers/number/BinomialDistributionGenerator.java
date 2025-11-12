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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record BinomialDistributionGenerator(NumberProvider n, NumberProvider p) implements NumberProvider
{
    public static final MapCodec<BinomialDistributionGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)NumberProviders.CODEC.fieldOf("n").forGetter(BinomialDistributionGenerator::n), (App)NumberProviders.CODEC.fieldOf("p").forGetter(BinomialDistributionGenerator::p)).apply((Applicative)instance, BinomialDistributionGenerator::new));

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.BINOMIAL;
    }

    @Override
    public int getInt(LootContext lootContext) {
        int n = this.n.getInt(lootContext);
        float f = this.p.getFloat(lootContext);
        RandomSource randomSource = lootContext.getRandom();
        int n2 = 0;
        for (int i = 0; i < n; ++i) {
            if (!(randomSource.nextFloat() < f)) continue;
            ++n2;
        }
        return n2;
    }

    @Override
    public float getFloat(LootContext lootContext) {
        return this.getInt(lootContext);
    }

    public static BinomialDistributionGenerator binomial(int n, float f) {
        return new BinomialDistributionGenerator(ConstantValue.exactly(n), ConstantValue.exactly(f));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.n.getReferencedContextParams(), this.p.getReferencedContextParams());
    }
}

