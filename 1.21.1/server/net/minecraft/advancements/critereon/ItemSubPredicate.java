/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public interface ItemSubPredicate {
    public static final Codec<Map<Type<?>, ItemSubPredicate>> CODEC = Codec.dispatchedMap(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE.byNameCodec(), Type::codec);

    public boolean matches(ItemStack var1);

    public record Type<T extends ItemSubPredicate>(Codec<T> codec) {
    }
}

