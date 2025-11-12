/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.alchemy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.FastColor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record PotionContents(Optional<Holder<Potion>> potion, Optional<Integer> customColor, List<MobEffectInstance> customEffects) {
    private final List<MobEffectInstance> customEffects;
    public static final PotionContents EMPTY = new PotionContents(Optional.empty(), Optional.empty(), List.of());
    private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);
    private static final int BASE_POTION_COLOR = -13083194;
    private static final Codec<PotionContents> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Potion.CODEC.optionalFieldOf("potion").forGetter(PotionContents::potion), (App)Codec.INT.optionalFieldOf("custom_color").forGetter(PotionContents::customColor), (App)MobEffectInstance.CODEC.listOf().optionalFieldOf("custom_effects", List.of()).forGetter(PotionContents::customEffects)).apply((Applicative)instance, PotionContents::new));
    public static final Codec<PotionContents> CODEC = Codec.withAlternative(FULL_CODEC, Potion.CODEC, PotionContents::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, PotionContents> STREAM_CODEC = StreamCodec.composite(Potion.STREAM_CODEC.apply(ByteBufCodecs::optional), PotionContents::potion, ByteBufCodecs.INT.apply(ByteBufCodecs::optional), PotionContents::customColor, MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list()), PotionContents::customEffects, PotionContents::new);

    public PotionContents(Holder<Potion> holder) {
        this(Optional.of(holder), Optional.empty(), List.of());
    }

    public static ItemStack createItemStack(Item item, Holder<Potion> holder) {
        ItemStack itemStack = new ItemStack(item);
        itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
        return itemStack;
    }

    public boolean is(Holder<Potion> holder) {
        return this.potion.isPresent() && this.potion.get().is(holder) && this.customEffects.isEmpty();
    }

    public Iterable<MobEffectInstance> getAllEffects() {
        if (this.potion.isEmpty()) {
            return this.customEffects;
        }
        if (this.customEffects.isEmpty()) {
            return this.potion.get().value().getEffects();
        }
        return Iterables.concat(this.potion.get().value().getEffects(), this.customEffects);
    }

    public void forEachEffect(Consumer<MobEffectInstance> consumer) {
        if (this.potion.isPresent()) {
            for (MobEffectInstance mobEffectInstance : this.potion.get().value().getEffects()) {
                consumer.accept(new MobEffectInstance(mobEffectInstance));
            }
        }
        for (MobEffectInstance mobEffectInstance : this.customEffects) {
            consumer.accept(new MobEffectInstance(mobEffectInstance));
        }
    }

    public PotionContents withPotion(Holder<Potion> holder) {
        return new PotionContents(Optional.of(holder), this.customColor, this.customEffects);
    }

    public PotionContents withEffectAdded(MobEffectInstance mobEffectInstance) {
        return new PotionContents(this.potion, this.customColor, Util.copyAndAdd(this.customEffects, mobEffectInstance));
    }

    public int getColor() {
        if (this.customColor.isPresent()) {
            return this.customColor.get();
        }
        return PotionContents.getColor(this.getAllEffects());
    }

    public static int getColor(Holder<Potion> holder) {
        return PotionContents.getColor(holder.value().getEffects());
    }

    public static int getColor(Iterable<MobEffectInstance> iterable) {
        return PotionContents.getColorOptional(iterable).orElse(-13083194);
    }

    public static OptionalInt getColorOptional(Iterable<MobEffectInstance> iterable) {
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        for (MobEffectInstance mobEffectInstance : iterable) {
            if (!mobEffectInstance.isVisible()) continue;
            int n5 = mobEffectInstance.getEffect().value().getColor();
            int n6 = mobEffectInstance.getAmplifier() + 1;
            n += n6 * FastColor.ARGB32.red(n5);
            n2 += n6 * FastColor.ARGB32.green(n5);
            n3 += n6 * FastColor.ARGB32.blue(n5);
            n4 += n6;
        }
        if (n4 == 0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(FastColor.ARGB32.color(n / n4, n2 / n4, n3 / n4));
    }

    public boolean hasEffects() {
        if (!this.customEffects.isEmpty()) {
            return true;
        }
        return this.potion.isPresent() && !this.potion.get().value().getEffects().isEmpty();
    }

    public List<MobEffectInstance> customEffects() {
        return Lists.transform(this.customEffects, MobEffectInstance::new);
    }

    public void addPotionTooltip(Consumer<Component> consumer, float f, float f2) {
        PotionContents.addPotionTooltip(this.getAllEffects(), consumer, f, f2);
    }

    public static void addPotionTooltip(Iterable<MobEffectInstance> iterable, Consumer<Component> consumer, float f, float f2) {
        Object object;
        ArrayList arrayList = Lists.newArrayList();
        boolean bl = true;
        for (MobEffectInstance mobEffectInstance : iterable) {
            bl = false;
            object = Component.translatable(mobEffectInstance.getDescriptionId());
            Holder<MobEffect> holder2 = mobEffectInstance.getEffect();
            holder2.value().createModifiers(mobEffectInstance.getAmplifier(), (holder, attributeModifier) -> arrayList.add(new Pair(holder, attributeModifier)));
            if (mobEffectInstance.getAmplifier() > 0) {
                object = Component.translatable("potion.withAmplifier", object, Component.translatable("potion.potency." + mobEffectInstance.getAmplifier()));
            }
            if (!mobEffectInstance.endsWithin(20)) {
                object = Component.translatable("potion.withDuration", object, MobEffectUtil.formatDuration(mobEffectInstance, f, f2));
            }
            consumer.accept(((MutableComponent)object).withStyle(holder2.value().getCategory().getTooltipFormatting()));
        }
        if (bl) {
            consumer.accept(NO_EFFECT);
        }
        if (!arrayList.isEmpty()) {
            consumer.accept(CommonComponents.EMPTY);
            consumer.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            for (MobEffectInstance mobEffectInstance : arrayList) {
                object = (AttributeModifier)mobEffectInstance.getSecond();
                double d = ((AttributeModifier)object).amount();
                double d2 = ((AttributeModifier)object).operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || ((AttributeModifier)object).operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL ? ((AttributeModifier)object).amount() * 100.0 : ((AttributeModifier)object).amount();
                if (d > 0.0) {
                    consumer.accept(Component.translatable("attribute.modifier.plus." + ((AttributeModifier)object).operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d2), Component.translatable(((Attribute)((Holder)mobEffectInstance.getFirst()).value()).getDescriptionId())).withStyle(ChatFormatting.BLUE));
                    continue;
                }
                if (!(d < 0.0)) continue;
                consumer.accept(Component.translatable("attribute.modifier.take." + ((AttributeModifier)object).operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d2 *= -1.0), Component.translatable(((Attribute)((Holder)mobEffectInstance.getFirst()).value()).getDescriptionId())).withStyle(ChatFormatting.RED));
            }
        }
    }
}

