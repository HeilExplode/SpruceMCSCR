/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeSupplier {
    private final Map<Holder<Attribute>, AttributeInstance> instances;

    AttributeSupplier(Map<Holder<Attribute>, AttributeInstance> map) {
        this.instances = map;
    }

    private AttributeInstance getAttributeInstance(Holder<Attribute> holder) {
        AttributeInstance attributeInstance = this.instances.get(holder);
        if (attributeInstance == null) {
            throw new IllegalArgumentException("Can't find attribute " + holder.getRegisteredName());
        }
        return attributeInstance;
    }

    public double getValue(Holder<Attribute> holder) {
        return this.getAttributeInstance(holder).getValue();
    }

    public double getBaseValue(Holder<Attribute> holder) {
        return this.getAttributeInstance(holder).getBaseValue();
    }

    public double getModifierValue(Holder<Attribute> holder, ResourceLocation resourceLocation) {
        AttributeModifier attributeModifier = this.getAttributeInstance(holder).getModifier(resourceLocation);
        if (attributeModifier == null) {
            throw new IllegalArgumentException("Can't find modifier " + String.valueOf(resourceLocation) + " on attribute " + holder.getRegisteredName());
        }
        return attributeModifier.amount();
    }

    @Nullable
    public AttributeInstance createInstance(Consumer<AttributeInstance> consumer, Holder<Attribute> holder) {
        AttributeInstance attributeInstance = this.instances.get(holder);
        if (attributeInstance == null) {
            return null;
        }
        AttributeInstance attributeInstance2 = new AttributeInstance(holder, consumer);
        attributeInstance2.replaceFrom(attributeInstance);
        return attributeInstance2;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean hasAttribute(Holder<Attribute> holder) {
        return this.instances.containsKey(holder);
    }

    public boolean hasModifier(Holder<Attribute> holder, ResourceLocation resourceLocation) {
        AttributeInstance attributeInstance = this.instances.get(holder);
        return attributeInstance != null && attributeInstance.getModifier(resourceLocation) != null;
    }

    public static class Builder {
        private final ImmutableMap.Builder<Holder<Attribute>, AttributeInstance> builder = ImmutableMap.builder();
        private boolean instanceFrozen;

        private AttributeInstance create(Holder<Attribute> holder) {
            AttributeInstance attributeInstance2 = new AttributeInstance(holder, attributeInstance -> {
                if (this.instanceFrozen) {
                    throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + holder.getRegisteredName());
                }
            });
            this.builder.put(holder, (Object)attributeInstance2);
            return attributeInstance2;
        }

        public Builder add(Holder<Attribute> holder) {
            this.create(holder);
            return this;
        }

        public Builder add(Holder<Attribute> holder, double d) {
            AttributeInstance attributeInstance = this.create(holder);
            attributeInstance.setBaseValue(d);
            return this;
        }

        public AttributeSupplier build() {
            this.instanceFrozen = true;
            return new AttributeSupplier((Map<Holder<Attribute>, AttributeInstance>)this.builder.buildKeepingLast());
        }
    }
}

