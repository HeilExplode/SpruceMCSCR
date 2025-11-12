/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.attributes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeInstance {
    private static final String BASE_FIELD = "base";
    private static final String MODIFIERS_FIELD = "modifiers";
    public static final String ID_FIELD = "id";
    private final Holder<Attribute> attribute;
    private final Map<AttributeModifier.Operation, Map<ResourceLocation, AttributeModifier>> modifiersByOperation = Maps.newEnumMap(AttributeModifier.Operation.class);
    private final Map<ResourceLocation, AttributeModifier> modifierById = new Object2ObjectArrayMap();
    private final Map<ResourceLocation, AttributeModifier> permanentModifiers = new Object2ObjectArrayMap();
    private double baseValue;
    private boolean dirty = true;
    private double cachedValue;
    private final Consumer<AttributeInstance> onDirty;

    public AttributeInstance(Holder<Attribute> holder, Consumer<AttributeInstance> consumer) {
        this.attribute = holder;
        this.onDirty = consumer;
        this.baseValue = holder.value().getDefaultValue();
    }

    public Holder<Attribute> getAttribute() {
        return this.attribute;
    }

    public double getBaseValue() {
        return this.baseValue;
    }

    public void setBaseValue(double d) {
        if (d == this.baseValue) {
            return;
        }
        this.baseValue = d;
        this.setDirty();
    }

    @VisibleForTesting
    Map<ResourceLocation, AttributeModifier> getModifiers(AttributeModifier.Operation operation2) {
        return this.modifiersByOperation.computeIfAbsent(operation2, operation -> new Object2ObjectOpenHashMap());
    }

    public Set<AttributeModifier> getModifiers() {
        return ImmutableSet.copyOf(this.modifierById.values());
    }

    @Nullable
    public AttributeModifier getModifier(ResourceLocation resourceLocation) {
        return this.modifierById.get(resourceLocation);
    }

    public boolean hasModifier(ResourceLocation resourceLocation) {
        return this.modifierById.get(resourceLocation) != null;
    }

    private void addModifier(AttributeModifier attributeModifier) {
        AttributeModifier attributeModifier2 = this.modifierById.putIfAbsent(attributeModifier.id(), attributeModifier);
        if (attributeModifier2 != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        }
        this.getModifiers(attributeModifier.operation()).put(attributeModifier.id(), attributeModifier);
        this.setDirty();
    }

    public void addOrUpdateTransientModifier(AttributeModifier attributeModifier) {
        AttributeModifier attributeModifier2 = this.modifierById.put(attributeModifier.id(), attributeModifier);
        if (attributeModifier == attributeModifier2) {
            return;
        }
        this.getModifiers(attributeModifier.operation()).put(attributeModifier.id(), attributeModifier);
        this.setDirty();
    }

    public void addTransientModifier(AttributeModifier attributeModifier) {
        this.addModifier(attributeModifier);
    }

    public void addOrReplacePermanentModifier(AttributeModifier attributeModifier) {
        this.removeModifier(attributeModifier.id());
        this.addModifier(attributeModifier);
        this.permanentModifiers.put(attributeModifier.id(), attributeModifier);
    }

    public void addPermanentModifier(AttributeModifier attributeModifier) {
        this.addModifier(attributeModifier);
        this.permanentModifiers.put(attributeModifier.id(), attributeModifier);
    }

    protected void setDirty() {
        this.dirty = true;
        this.onDirty.accept(this);
    }

    public void removeModifier(AttributeModifier attributeModifier) {
        this.removeModifier(attributeModifier.id());
    }

    public boolean removeModifier(ResourceLocation resourceLocation) {
        AttributeModifier attributeModifier = this.modifierById.remove(resourceLocation);
        if (attributeModifier == null) {
            return false;
        }
        this.getModifiers(attributeModifier.operation()).remove(resourceLocation);
        this.permanentModifiers.remove(resourceLocation);
        this.setDirty();
        return true;
    }

    public void removeModifiers() {
        for (AttributeModifier attributeModifier : this.getModifiers()) {
            this.removeModifier(attributeModifier);
        }
    }

    public double getValue() {
        if (this.dirty) {
            this.cachedValue = this.calculateValue();
            this.dirty = false;
        }
        return this.cachedValue;
    }

    private double calculateValue() {
        double d = this.getBaseValue();
        for (AttributeModifier attributeModifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
            d += attributeModifier.amount();
        }
        double d2 = d;
        for (AttributeModifier attributeModifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
            d2 += d * attributeModifier.amount();
        }
        for (AttributeModifier attributeModifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
            d2 *= 1.0 + attributeModifier.amount();
        }
        return this.attribute.value().sanitizeValue(d2);
    }

    private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation operation) {
        return this.modifiersByOperation.getOrDefault(operation, Map.of()).values();
    }

    public void replaceFrom(AttributeInstance attributeInstance) {
        this.baseValue = attributeInstance.baseValue;
        this.modifierById.clear();
        this.modifierById.putAll(attributeInstance.modifierById);
        this.permanentModifiers.clear();
        this.permanentModifiers.putAll(attributeInstance.permanentModifiers);
        this.modifiersByOperation.clear();
        attributeInstance.modifiersByOperation.forEach((operation, map) -> this.getModifiers((AttributeModifier.Operation)operation).putAll((Map<ResourceLocation, AttributeModifier>)map));
        this.setDirty();
    }

    public CompoundTag save() {
        CompoundTag compoundTag = new CompoundTag();
        ResourceKey<Attribute> resourceKey = this.attribute.unwrapKey().orElseThrow(() -> new IllegalStateException("Tried to serialize unregistered attribute"));
        compoundTag.putString(ID_FIELD, resourceKey.location().toString());
        compoundTag.putDouble(BASE_FIELD, this.baseValue);
        if (!this.permanentModifiers.isEmpty()) {
            ListTag listTag = new ListTag();
            for (AttributeModifier attributeModifier : this.permanentModifiers.values()) {
                listTag.add(attributeModifier.save());
            }
            compoundTag.put(MODIFIERS_FIELD, listTag);
        }
        return compoundTag;
    }

    public void load(CompoundTag compoundTag) {
        this.baseValue = compoundTag.getDouble(BASE_FIELD);
        if (compoundTag.contains(MODIFIERS_FIELD, 9)) {
            ListTag listTag = compoundTag.getList(MODIFIERS_FIELD, 10);
            for (int i = 0; i < listTag.size(); ++i) {
                AttributeModifier attributeModifier = AttributeModifier.load(listTag.getCompound(i));
                if (attributeModifier == null) continue;
                this.modifierById.put(attributeModifier.id(), attributeModifier);
                this.getModifiers(attributeModifier.operation()).put(attributeModifier.id(), attributeModifier);
                this.permanentModifiers.put(attributeModifier.id(), attributeModifier);
            }
        }
        this.setDirty();
    }
}

