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
package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record ItemAttributeModifiers(List<Entry> modifiers, boolean showInTooltip) {
    public static final ItemAttributeModifiers EMPTY = new ItemAttributeModifiers(List.of(), true);
    private static final Codec<ItemAttributeModifiers> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Entry.CODEC.listOf().fieldOf("modifiers").forGetter(ItemAttributeModifiers::modifiers), (App)Codec.BOOL.optionalFieldOf("show_in_tooltip", (Object)true).forGetter(ItemAttributeModifiers::showInTooltip)).apply((Applicative)instance, ItemAttributeModifiers::new));
    public static final Codec<ItemAttributeModifiers> CODEC = Codec.withAlternative(FULL_CODEC, (Codec)Entry.CODEC.listOf(), list -> new ItemAttributeModifiers((List<Entry>)list, true));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers> STREAM_CODEC = StreamCodec.composite(Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), ItemAttributeModifiers::modifiers, ByteBufCodecs.BOOL, ItemAttributeModifiers::showInTooltip, ItemAttributeModifiers::new);
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(new DecimalFormat("#.##"), decimalFormat -> decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));

    public ItemAttributeModifiers withTooltip(boolean bl) {
        return new ItemAttributeModifiers(this.modifiers, bl);
    }

    public static Builder builder() {
        return new Builder();
    }

    public ItemAttributeModifiers withModifierAdded(Holder<Attribute> holder, AttributeModifier attributeModifier, EquipmentSlotGroup equipmentSlotGroup) {
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)(this.modifiers.size() + 1));
        for (Entry entry : this.modifiers) {
            if (entry.matches(holder, attributeModifier.id())) continue;
            builder.add((Object)entry);
        }
        builder.add((Object)new Entry(holder, attributeModifier, equipmentSlotGroup));
        return new ItemAttributeModifiers((List<Entry>)builder.build(), this.showInTooltip);
    }

    public void forEach(EquipmentSlotGroup equipmentSlotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
        for (Entry entry : this.modifiers) {
            if (!entry.slot.equals(equipmentSlotGroup)) continue;
            biConsumer.accept(entry.attribute, entry.modifier);
        }
    }

    public void forEach(EquipmentSlot equipmentSlot, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
        for (Entry entry : this.modifiers) {
            if (!entry.slot.test(equipmentSlot)) continue;
            biConsumer.accept(entry.attribute, entry.modifier);
        }
    }

    public double compute(double d, EquipmentSlot equipmentSlot) {
        double d2 = d;
        for (Entry entry : this.modifiers) {
            if (!entry.slot.test(equipmentSlot)) continue;
            double d3 = entry.modifier.amount();
            d2 += (switch (entry.modifier.operation()) {
                default -> throw new MatchException(null, null);
                case AttributeModifier.Operation.ADD_VALUE -> d3;
                case AttributeModifier.Operation.ADD_MULTIPLIED_BASE -> d3 * d;
                case AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL -> d3 * d2;
            });
        }
        return d2;
    }

    public static class Builder {
        private final ImmutableList.Builder<Entry> entries = ImmutableList.builder();

        Builder() {
        }

        public Builder add(Holder<Attribute> holder, AttributeModifier attributeModifier, EquipmentSlotGroup equipmentSlotGroup) {
            this.entries.add((Object)new Entry(holder, attributeModifier, equipmentSlotGroup));
            return this;
        }

        public ItemAttributeModifiers build() {
            return new ItemAttributeModifiers((List<Entry>)this.entries.build(), true);
        }
    }

    public static final class Entry
    extends Record {
        final Holder<Attribute> attribute;
        final AttributeModifier modifier;
        final EquipmentSlotGroup slot;
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Attribute.CODEC.fieldOf("type").forGetter(Entry::attribute), (App)AttributeModifier.MAP_CODEC.forGetter(Entry::modifier), (App)EquipmentSlotGroup.CODEC.optionalFieldOf("slot", (Object)EquipmentSlotGroup.ANY).forGetter(Entry::slot)).apply((Applicative)instance, Entry::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(Attribute.STREAM_CODEC, Entry::attribute, AttributeModifier.STREAM_CODEC, Entry::modifier, EquipmentSlotGroup.STREAM_CODEC, Entry::slot, Entry::new);

        public Entry(Holder<Attribute> holder, AttributeModifier attributeModifier, EquipmentSlotGroup equipmentSlotGroup) {
            this.attribute = holder;
            this.modifier = attributeModifier;
            this.slot = equipmentSlotGroup;
        }

        public boolean matches(Holder<Attribute> holder, ResourceLocation resourceLocation) {
            return holder.equals(this.attribute) && this.modifier.is(resourceLocation);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "attribute;modifier;slot", "attribute", "modifier", "slot"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "attribute;modifier;slot", "attribute", "modifier", "slot"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "attribute;modifier;slot", "attribute", "modifier", "slot"}, this, object);
        }

        public Holder<Attribute> attribute() {
            return this.attribute;
        }

        public AttributeModifier modifier() {
            return this.modifier;
        }

        public EquipmentSlotGroup slot() {
            return this.slot;
        }
    }
}

