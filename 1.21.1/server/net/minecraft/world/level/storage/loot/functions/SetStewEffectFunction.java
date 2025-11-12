/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetStewEffectFunction
extends LootItemConditionalFunction {
    private static final Codec<List<EffectEntry>> EFFECTS_LIST = EffectEntry.CODEC.listOf().validate(list -> {
        ObjectOpenHashSet objectOpenHashSet = new ObjectOpenHashSet();
        for (EffectEntry effectEntry : list) {
            if (objectOpenHashSet.add(effectEntry.effect())) continue;
            return DataResult.error(() -> "Encountered duplicate mob effect: '" + String.valueOf(effectEntry.effect()) + "'");
        }
        return DataResult.success((Object)list);
    });
    public static final MapCodec<SetStewEffectFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetStewEffectFunction.commonFields(instance).and((App)EFFECTS_LIST.optionalFieldOf("effects", List.of()).forGetter(setStewEffectFunction -> setStewEffectFunction.effects)).apply((Applicative)instance, SetStewEffectFunction::new));
    private final List<EffectEntry> effects;

    SetStewEffectFunction(List<LootItemCondition> list, List<EffectEntry> list2) {
        super(list);
        this.effects = list2;
    }

    public LootItemFunctionType<SetStewEffectFunction> getType() {
        return LootItemFunctions.SET_STEW_EFFECT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return (Set)this.effects.stream().flatMap(effectEntry -> effectEntry.duration().getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (!itemStack.is(Items.SUSPICIOUS_STEW) || this.effects.isEmpty()) {
            return itemStack;
        }
        EffectEntry effectEntry = Util.getRandom(this.effects, lootContext.getRandom());
        Holder<MobEffect> holder = effectEntry.effect();
        int n = effectEntry.duration().getInt(lootContext);
        if (!holder.value().isInstantenous()) {
            n *= 20;
        }
        SuspiciousStewEffects.Entry entry = new SuspiciousStewEffects.Entry(holder, n);
        itemStack.update(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY, entry, SuspiciousStewEffects::withEffectAdded);
        return itemStack;
    }

    public static Builder stewEffect() {
        return new Builder();
    }

    record EffectEntry(Holder<MobEffect> effect, NumberProvider duration) {
        public static final Codec<EffectEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MobEffect.CODEC.fieldOf("type").forGetter(EffectEntry::effect), (App)NumberProviders.CODEC.fieldOf("duration").forGetter(EffectEntry::duration)).apply((Applicative)instance, EffectEntry::new));
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final ImmutableList.Builder<EffectEntry> effects = ImmutableList.builder();

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder withEffect(Holder<MobEffect> holder, NumberProvider numberProvider) {
            this.effects.add((Object)new EffectEntry(holder, numberProvider));
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetStewEffectFunction(this.getConditions(), (List<EffectEntry>)this.effects.build());
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

