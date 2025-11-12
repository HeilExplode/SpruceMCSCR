/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment.effects;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.phys.Vec3;

public record EnchantmentAttributeEffect(ResourceLocation id, Holder<Attribute> attribute, LevelBasedValue amount, AttributeModifier.Operation operation) implements EnchantmentLocationBasedEffect
{
    public static final MapCodec<EnchantmentAttributeEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("id").forGetter(EnchantmentAttributeEffect::id), (App)Attribute.CODEC.fieldOf("attribute").forGetter(EnchantmentAttributeEffect::attribute), (App)LevelBasedValue.CODEC.fieldOf("amount").forGetter(EnchantmentAttributeEffect::amount), (App)AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(EnchantmentAttributeEffect::operation)).apply((Applicative)instance, EnchantmentAttributeEffect::new));

    private ResourceLocation idForSlot(StringRepresentable stringRepresentable) {
        return this.id.withSuffix("/" + stringRepresentable.getSerializedName());
    }

    public AttributeModifier getModifier(int n, StringRepresentable stringRepresentable) {
        return new AttributeModifier(this.idForSlot(stringRepresentable), this.amount().calculate(n), this.operation());
    }

    @Override
    public void onChangedBlock(ServerLevel serverLevel, int n, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, boolean bl) {
        if (bl && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.getAttributes().addTransientAttributeModifiers((Multimap<Holder<Attribute>, AttributeModifier>)this.makeAttributeMap(n, enchantedItemInUse.inSlot()));
        }
    }

    @Override
    public void onDeactivated(EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, int n) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.getAttributes().removeAttributeModifiers((Multimap<Holder<Attribute>, AttributeModifier>)this.makeAttributeMap(n, enchantedItemInUse.inSlot()));
        }
    }

    private HashMultimap<Holder<Attribute>, AttributeModifier> makeAttributeMap(int n, EquipmentSlot equipmentSlot) {
        HashMultimap hashMultimap = HashMultimap.create();
        hashMultimap.put(this.attribute, (Object)this.getModifier(n, equipmentSlot));
        return hashMultimap;
    }

    public MapCodec<EnchantmentAttributeEffect> codec() {
        return CODEC;
    }
}

