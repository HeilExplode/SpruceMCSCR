/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ItemAttributeModifiersPredicate;
import net.minecraft.advancements.critereon.ItemBundlePredicate;
import net.minecraft.advancements.critereon.ItemContainerPredicate;
import net.minecraft.advancements.critereon.ItemCustomDataPredicate;
import net.minecraft.advancements.critereon.ItemDamagePredicate;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemFireworkExplosionPredicate;
import net.minecraft.advancements.critereon.ItemFireworksPredicate;
import net.minecraft.advancements.critereon.ItemJukeboxPlayablePredicate;
import net.minecraft.advancements.critereon.ItemPotionsPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.ItemTrimPredicate;
import net.minecraft.advancements.critereon.ItemWritableBookPredicate;
import net.minecraft.advancements.critereon.ItemWrittenBookPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ItemSubPredicates {
    public static final ItemSubPredicate.Type<ItemDamagePredicate> DAMAGE = ItemSubPredicates.register("damage", ItemDamagePredicate.CODEC);
    public static final ItemSubPredicate.Type<ItemEnchantmentsPredicate.Enchantments> ENCHANTMENTS = ItemSubPredicates.register("enchantments", ItemEnchantmentsPredicate.Enchantments.CODEC);
    public static final ItemSubPredicate.Type<ItemEnchantmentsPredicate.StoredEnchantments> STORED_ENCHANTMENTS = ItemSubPredicates.register("stored_enchantments", ItemEnchantmentsPredicate.StoredEnchantments.CODEC);
    public static final ItemSubPredicate.Type<ItemPotionsPredicate> POTIONS = ItemSubPredicates.register("potion_contents", ItemPotionsPredicate.CODEC);
    public static final ItemSubPredicate.Type<ItemCustomDataPredicate> CUSTOM_DATA = ItemSubPredicates.register("custom_data", ItemCustomDataPredicate.CODEC);
    public static final ItemSubPredicate.Type<ItemContainerPredicate> CONTAINER = ItemSubPredicates.register("container", ItemContainerPredicate.CODEC);
    public static final ItemSubPredicate.Type<ItemBundlePredicate> BUNDLE_CONTENTS = ItemSubPredicates.register("bundle_contents", ItemBundlePredicate.CODEC);
    public static final ItemSubPredicate.Type<ItemFireworkExplosionPredicate> FIREWORK_EXPLOSION = ItemSubPredicates.register("firework_explosion", ItemFireworkExplosionPredicate.CODEC);
    public static final ItemSubPredicate.Type<ItemFireworksPredicate> FIREWORKS = ItemSubPredicates.register("fireworks", ItemFireworksPredicate.CODEC);
    public static final ItemSubPredicate.Type<ItemWritableBookPredicate> WRITABLE_BOOK = ItemSubPredicates.register("writable_book_content", ItemWritableBookPredicate.CODEC);
    public static final ItemSubPredicate.Type<ItemWrittenBookPredicate> WRITTEN_BOOK = ItemSubPredicates.register("written_book_content", ItemWrittenBookPredicate.CODEC);
    public static final ItemSubPredicate.Type<ItemAttributeModifiersPredicate> ATTRIBUTE_MODIFIERS = ItemSubPredicates.register("attribute_modifiers", ItemAttributeModifiersPredicate.CODEC);
    public static final ItemSubPredicate.Type<ItemTrimPredicate> ARMOR_TRIM = ItemSubPredicates.register("trim", ItemTrimPredicate.CODEC);
    public static final ItemSubPredicate.Type<ItemJukeboxPlayablePredicate> JUKEBOX_PLAYABLE = ItemSubPredicates.register("jukebox_playable", ItemJukeboxPlayablePredicate.CODEC);

    private static <T extends ItemSubPredicate> ItemSubPredicate.Type<T> register(String string, Codec<T> codec) {
        return Registry.register(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE, string, new ItemSubPredicate.Type<T>(codec));
    }

    public static ItemSubPredicate.Type<?> bootstrap(Registry<ItemSubPredicate.Type<?>> registry) {
        return DAMAGE;
    }
}

