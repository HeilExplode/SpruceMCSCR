/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetAttributesFunction
extends LootItemConditionalFunction {
    public static final MapCodec<SetAttributesFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetAttributesFunction.commonFields(instance).and(instance.group((App)Modifier.CODEC.listOf().fieldOf("modifiers").forGetter(setAttributesFunction -> setAttributesFunction.modifiers), (App)Codec.BOOL.optionalFieldOf("replace", (Object)true).forGetter(setAttributesFunction -> setAttributesFunction.replace))).apply((Applicative)instance, SetAttributesFunction::new));
    private final List<Modifier> modifiers;
    private final boolean replace;

    SetAttributesFunction(List<LootItemCondition> list, List<Modifier> list2, boolean bl) {
        super(list);
        this.modifiers = List.copyOf(list2);
        this.replace = bl;
    }

    public LootItemFunctionType<SetAttributesFunction> getType() {
        return LootItemFunctions.SET_ATTRIBUTES;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return (Set)this.modifiers.stream().flatMap(modifier -> modifier.amount.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (this.replace) {
            itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, this.updateModifiers(lootContext, ItemAttributeModifiers.EMPTY));
        } else {
            itemStack.update(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY, itemAttributeModifiers -> {
                if (itemAttributeModifiers.modifiers().isEmpty()) {
                    return this.updateModifiers(lootContext, itemStack.getItem().getDefaultAttributeModifiers());
                }
                return this.updateModifiers(lootContext, (ItemAttributeModifiers)itemAttributeModifiers);
            });
        }
        return itemStack;
    }

    private ItemAttributeModifiers updateModifiers(LootContext lootContext, ItemAttributeModifiers itemAttributeModifiers) {
        RandomSource randomSource = lootContext.getRandom();
        for (Modifier modifier : this.modifiers) {
            EquipmentSlotGroup equipmentSlotGroup = Util.getRandom(modifier.slots, randomSource);
            itemAttributeModifiers = itemAttributeModifiers.withModifierAdded(modifier.attribute, new AttributeModifier(modifier.id, modifier.amount.getFloat(lootContext), modifier.operation), equipmentSlotGroup);
        }
        return itemAttributeModifiers;
    }

    public static ModifierBuilder modifier(ResourceLocation resourceLocation, Holder<Attribute> holder, AttributeModifier.Operation operation, NumberProvider numberProvider) {
        return new ModifierBuilder(resourceLocation, holder, operation, numberProvider);
    }

    public static Builder setAttributes() {
        return new Builder();
    }

    static final class Modifier
    extends Record {
        final ResourceLocation id;
        final Holder<Attribute> attribute;
        final AttributeModifier.Operation operation;
        final NumberProvider amount;
        final List<EquipmentSlotGroup> slots;
        private static final Codec<List<EquipmentSlotGroup>> SLOTS_CODEC = ExtraCodecs.nonEmptyList(Codec.either(EquipmentSlotGroup.CODEC, (Codec)EquipmentSlotGroup.CODEC.listOf()).xmap(either -> (List)either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left((Object)((EquipmentSlotGroup)list.getFirst())) : Either.right((Object)list)));
        public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("id").forGetter(Modifier::id), (App)Attribute.CODEC.fieldOf("attribute").forGetter(Modifier::attribute), (App)AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(Modifier::operation), (App)NumberProviders.CODEC.fieldOf("amount").forGetter(Modifier::amount), (App)SLOTS_CODEC.fieldOf("slot").forGetter(Modifier::slots)).apply((Applicative)instance, Modifier::new));

        Modifier(ResourceLocation resourceLocation, Holder<Attribute> holder, AttributeModifier.Operation operation, NumberProvider numberProvider, List<EquipmentSlotGroup> list) {
            this.id = resourceLocation;
            this.attribute = holder;
            this.operation = operation;
            this.amount = numberProvider;
            this.slots = list;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Modifier.class, "id;attribute;operation;amount;slots", "id", "attribute", "operation", "amount", "slots"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Modifier.class, "id;attribute;operation;amount;slots", "id", "attribute", "operation", "amount", "slots"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Modifier.class, "id;attribute;operation;amount;slots", "id", "attribute", "operation", "amount", "slots"}, this, object);
        }

        public ResourceLocation id() {
            return this.id;
        }

        public Holder<Attribute> attribute() {
            return this.attribute;
        }

        public AttributeModifier.Operation operation() {
            return this.operation;
        }

        public NumberProvider amount() {
            return this.amount;
        }

        public List<EquipmentSlotGroup> slots() {
            return this.slots;
        }
    }

    public static class ModifierBuilder {
        private final ResourceLocation id;
        private final Holder<Attribute> attribute;
        private final AttributeModifier.Operation operation;
        private final NumberProvider amount;
        private final Set<EquipmentSlotGroup> slots = EnumSet.noneOf(EquipmentSlotGroup.class);

        public ModifierBuilder(ResourceLocation resourceLocation, Holder<Attribute> holder, AttributeModifier.Operation operation, NumberProvider numberProvider) {
            this.id = resourceLocation;
            this.attribute = holder;
            this.operation = operation;
            this.amount = numberProvider;
        }

        public ModifierBuilder forSlot(EquipmentSlotGroup equipmentSlotGroup) {
            this.slots.add(equipmentSlotGroup);
            return this;
        }

        public Modifier build() {
            return new Modifier(this.id, this.attribute, this.operation, this.amount, List.copyOf(this.slots));
        }
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final boolean replace;
        private final List<Modifier> modifiers = Lists.newArrayList();

        public Builder(boolean bl) {
            this.replace = bl;
        }

        public Builder() {
            this(false);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder withModifier(ModifierBuilder modifierBuilder) {
            this.modifiers.add(modifierBuilder.build());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetAttributesFunction(this.getConditions(), this.modifiers, this.replace);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

