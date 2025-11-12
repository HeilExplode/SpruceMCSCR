/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.effect;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class MobEffect
implements FeatureElement {
    public static final Codec<Holder<MobEffect>> CODEC = BuiltInRegistries.MOB_EFFECT.holderByNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<MobEffect>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT);
    private static final int AMBIENT_ALPHA = Mth.floor(38.25f);
    private final Map<Holder<Attribute>, AttributeTemplate> attributeModifiers = new Object2ObjectOpenHashMap();
    private final MobEffectCategory category;
    private final int color;
    private final Function<MobEffectInstance, ParticleOptions> particleFactory;
    @Nullable
    private String descriptionId;
    private int blendDurationTicks;
    private Optional<SoundEvent> soundOnAdded = Optional.empty();
    private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

    protected MobEffect(MobEffectCategory mobEffectCategory, int n) {
        this.category = mobEffectCategory;
        this.color = n;
        this.particleFactory = mobEffectInstance -> {
            int n2 = mobEffectInstance.isAmbient() ? AMBIENT_ALPHA : 255;
            return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(n2, n));
        };
    }

    protected MobEffect(MobEffectCategory mobEffectCategory, int n, ParticleOptions particleOptions) {
        this.category = mobEffectCategory;
        this.color = n;
        this.particleFactory = mobEffectInstance -> particleOptions;
    }

    public int getBlendDurationTicks() {
        return this.blendDurationTicks;
    }

    public boolean applyEffectTick(LivingEntity livingEntity, int n) {
        return true;
    }

    public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity2, LivingEntity livingEntity, int n, double d) {
        this.applyEffectTick(livingEntity, n);
    }

    public boolean shouldApplyEffectTickThisTick(int n, int n2) {
        return false;
    }

    public void onEffectStarted(LivingEntity livingEntity, int n) {
    }

    public void onEffectAdded(LivingEntity livingEntity, int n) {
        this.soundOnAdded.ifPresent(soundEvent -> livingEntity.level().playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), (SoundEvent)soundEvent, livingEntity.getSoundSource(), 1.0f, 1.0f));
    }

    public void onMobRemoved(LivingEntity livingEntity, int n, Entity.RemovalReason removalReason) {
    }

    public void onMobHurt(LivingEntity livingEntity, int n, DamageSource damageSource, float f) {
    }

    public boolean isInstantenous() {
        return false;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("effect", BuiltInRegistries.MOB_EFFECT.getKey(this));
        }
        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public Component getDisplayName() {
        return Component.translatable(this.getDescriptionId());
    }

    public MobEffectCategory getCategory() {
        return this.category;
    }

    public int getColor() {
        return this.color;
    }

    public MobEffect addAttributeModifier(Holder<Attribute> holder, ResourceLocation resourceLocation, double d, AttributeModifier.Operation operation) {
        this.attributeModifiers.put(holder, new AttributeTemplate(resourceLocation, d, operation));
        return this;
    }

    public MobEffect setBlendDuration(int n) {
        this.blendDurationTicks = n;
        return this;
    }

    public void createModifiers(int n, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
        this.attributeModifiers.forEach((holder, attributeTemplate) -> biConsumer.accept((Holder<Attribute>)holder, attributeTemplate.create(n)));
    }

    public void removeAttributeModifiers(AttributeMap attributeMap) {
        for (Map.Entry<Holder<Attribute>, AttributeTemplate> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeInstance = attributeMap.getInstance(entry.getKey());
            if (attributeInstance == null) continue;
            attributeInstance.removeModifier(entry.getValue().id());
        }
    }

    public void addAttributeModifiers(AttributeMap attributeMap, int n) {
        for (Map.Entry<Holder<Attribute>, AttributeTemplate> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeInstance = attributeMap.getInstance(entry.getKey());
            if (attributeInstance == null) continue;
            attributeInstance.removeModifier(entry.getValue().id());
            attributeInstance.addPermanentModifier(entry.getValue().create(n));
        }
    }

    public boolean isBeneficial() {
        return this.category == MobEffectCategory.BENEFICIAL;
    }

    public ParticleOptions createParticleOptions(MobEffectInstance mobEffectInstance) {
        return this.particleFactory.apply(mobEffectInstance);
    }

    public MobEffect withSoundOnAdded(SoundEvent soundEvent) {
        this.soundOnAdded = Optional.of(soundEvent);
        return this;
    }

    public MobEffect requiredFeatures(FeatureFlag ... featureFlagArray) {
        this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlagArray);
        return this;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    record AttributeTemplate(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        public AttributeModifier create(int n) {
            return new AttributeModifier(this.id, this.amount * (double)(n + 1), this.operation);
        }
    }
}

