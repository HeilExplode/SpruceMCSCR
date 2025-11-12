/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item.alchemy;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class Potion
implements FeatureElement {
    public static final Codec<Holder<Potion>> CODEC = BuiltInRegistries.POTION.holderByNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Potion>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.POTION);
    @Nullable
    private final String name;
    private final List<MobEffectInstance> effects;
    private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

    public Potion(MobEffectInstance ... mobEffectInstanceArray) {
        this((String)null, mobEffectInstanceArray);
    }

    public Potion(@Nullable String string, MobEffectInstance ... mobEffectInstanceArray) {
        this.name = string;
        this.effects = List.of(mobEffectInstanceArray);
    }

    public Potion requiredFeatures(FeatureFlag ... featureFlagArray) {
        this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlagArray);
        return this;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    public static String getName(Optional<Holder<Potion>> optional, String string) {
        String string2;
        if (optional.isPresent() && (string2 = optional.get().value().name) != null) {
            return string + string2;
        }
        string2 = optional.flatMap(Holder::unwrapKey).map(resourceKey -> resourceKey.location().getPath()).orElse("empty");
        return string + string2;
    }

    public List<MobEffectInstance> getEffects() {
        return this.effects;
    }

    public boolean hasInstantEffects() {
        if (!this.effects.isEmpty()) {
            for (MobEffectInstance mobEffectInstance : this.effects) {
                if (!mobEffectInstance.getEffect().value().isInstantenous()) continue;
                return true;
            }
        }
        return false;
    }
}

