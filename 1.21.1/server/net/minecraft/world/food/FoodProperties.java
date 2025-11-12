/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.food;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record FoodProperties(int nutrition, float saturation, boolean canAlwaysEat, float eatSeconds, Optional<ItemStack> usingConvertsTo, List<PossibleEffect> effects) {
    private static final float DEFAULT_EAT_SECONDS = 1.6f;
    public static final Codec<FoodProperties> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(FoodProperties::nutrition), (App)Codec.FLOAT.fieldOf("saturation").forGetter(FoodProperties::saturation), (App)Codec.BOOL.optionalFieldOf("can_always_eat", (Object)false).forGetter(FoodProperties::canAlwaysEat), (App)ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("eat_seconds", (Object)Float.valueOf(1.6f)).forGetter(FoodProperties::eatSeconds), (App)ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("using_converts_to").forGetter(FoodProperties::usingConvertsTo), (App)PossibleEffect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(FoodProperties::effects)).apply((Applicative)instance, FoodProperties::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodProperties> DIRECT_STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, FoodProperties::nutrition, ByteBufCodecs.FLOAT, FoodProperties::saturation, ByteBufCodecs.BOOL, FoodProperties::canAlwaysEat, ByteBufCodecs.FLOAT, FoodProperties::eatSeconds, ItemStack.STREAM_CODEC.apply(ByteBufCodecs::optional), FoodProperties::usingConvertsTo, PossibleEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), FoodProperties::effects, FoodProperties::new);

    public int eatDurationTicks() {
        return (int)(this.eatSeconds * 20.0f);
    }

    public record PossibleEffect(MobEffectInstance effect, float probability) {
        private final MobEffectInstance effect;
        public static final Codec<PossibleEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MobEffectInstance.CODEC.fieldOf("effect").forGetter(PossibleEffect::effect), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("probability", (Object)Float.valueOf(1.0f)).forGetter(PossibleEffect::probability)).apply((Applicative)instance, PossibleEffect::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, PossibleEffect> STREAM_CODEC = StreamCodec.composite(MobEffectInstance.STREAM_CODEC, PossibleEffect::effect, ByteBufCodecs.FLOAT, PossibleEffect::probability, PossibleEffect::new);

        public MobEffectInstance effect() {
            return new MobEffectInstance(this.effect);
        }
    }

    public static class Builder {
        private int nutrition;
        private float saturationModifier;
        private boolean canAlwaysEat;
        private float eatSeconds = 1.6f;
        private Optional<ItemStack> usingConvertsTo = Optional.empty();
        private final ImmutableList.Builder<PossibleEffect> effects = ImmutableList.builder();

        public Builder nutrition(int n) {
            this.nutrition = n;
            return this;
        }

        public Builder saturationModifier(float f) {
            this.saturationModifier = f;
            return this;
        }

        public Builder alwaysEdible() {
            this.canAlwaysEat = true;
            return this;
        }

        public Builder fast() {
            this.eatSeconds = 0.8f;
            return this;
        }

        public Builder effect(MobEffectInstance mobEffectInstance, float f) {
            this.effects.add((Object)new PossibleEffect(mobEffectInstance, f));
            return this;
        }

        public Builder usingConvertsTo(ItemLike itemLike) {
            this.usingConvertsTo = Optional.of(new ItemStack(itemLike));
            return this;
        }

        public FoodProperties build() {
            float f = FoodConstants.saturationByModifier(this.nutrition, this.saturationModifier);
            return new FoodProperties(this.nutrition, f, this.canAlwaysEat, this.eatSeconds, this.usingConvertsTo, (List<PossibleEffect>)this.effects.build());
        }
    }
}

