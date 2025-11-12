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
import java.util.Optional;
import net.minecraft.advancements.critereon.CollectionPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public record ItemContainerPredicate(Optional<CollectionPredicate<ItemStack, ItemPredicate>> items) implements SingleComponentItemPredicate<ItemContainerContents>
{
    public static final Codec<ItemContainerPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CollectionPredicate.codec(ItemPredicate.CODEC).optionalFieldOf("items").forGetter(ItemContainerPredicate::items)).apply((Applicative)instance, ItemContainerPredicate::new));

    @Override
    public DataComponentType<ItemContainerContents> componentType() {
        return DataComponents.CONTAINER;
    }

    @Override
    public boolean matches(ItemStack itemStack, ItemContainerContents itemContainerContents) {
        return !this.items.isPresent() || this.items.get().test(itemContainerContents.nonEmptyItems());
    }
}

