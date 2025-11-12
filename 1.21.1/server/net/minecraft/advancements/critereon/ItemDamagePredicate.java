/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public record ItemDamagePredicate(MinMaxBounds.Ints durability, MinMaxBounds.Ints damage) implements SingleComponentItemPredicate<Integer>
{
    public static final Codec<ItemDamagePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("durability", (Object)MinMaxBounds.Ints.ANY).forGetter(ItemDamagePredicate::durability), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("damage", (Object)MinMaxBounds.Ints.ANY).forGetter(ItemDamagePredicate::damage)).apply((Applicative)instance, ItemDamagePredicate::new));

    @Override
    public DataComponentType<Integer> componentType() {
        return DataComponents.DAMAGE;
    }

    @Override
    public boolean matches(ItemStack itemStack, Integer n) {
        if (!this.durability.matches(itemStack.getMaxDamage() - n)) {
            return false;
        }
        return this.damage.matches(n);
    }

    public static ItemDamagePredicate durability(MinMaxBounds.Ints ints) {
        return new ItemDamagePredicate(ints, MinMaxBounds.Ints.ANY);
    }
}

