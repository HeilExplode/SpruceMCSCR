/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.alchemy;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

public class PotionBrewing {
    public static final int BREWING_TIME_SECONDS = 20;
    public static final PotionBrewing EMPTY = new PotionBrewing(List.of(), List.of(), List.of());
    private final List<Ingredient> containers;
    private final List<Mix<Potion>> potionMixes;
    private final List<Mix<Item>> containerMixes;

    PotionBrewing(List<Ingredient> list, List<Mix<Potion>> list2, List<Mix<Item>> list3) {
        this.containers = list;
        this.potionMixes = list2;
        this.containerMixes = list3;
    }

    public boolean isIngredient(ItemStack itemStack) {
        return this.isContainerIngredient(itemStack) || this.isPotionIngredient(itemStack);
    }

    private boolean isContainer(ItemStack itemStack) {
        for (Ingredient ingredient : this.containers) {
            if (!ingredient.test(itemStack)) continue;
            return true;
        }
        return false;
    }

    public boolean isContainerIngredient(ItemStack itemStack) {
        for (Mix<Item> mix : this.containerMixes) {
            if (!mix.ingredient.test(itemStack)) continue;
            return true;
        }
        return false;
    }

    public boolean isPotionIngredient(ItemStack itemStack) {
        for (Mix<Potion> mix : this.potionMixes) {
            if (!mix.ingredient.test(itemStack)) continue;
            return true;
        }
        return false;
    }

    public boolean isBrewablePotion(Holder<Potion> holder) {
        for (Mix<Potion> mix : this.potionMixes) {
            if (!mix.to.is(holder)) continue;
            return true;
        }
        return false;
    }

    public boolean hasMix(ItemStack itemStack, ItemStack itemStack2) {
        if (!this.isContainer(itemStack)) {
            return false;
        }
        return this.hasContainerMix(itemStack, itemStack2) || this.hasPotionMix(itemStack, itemStack2);
    }

    public boolean hasContainerMix(ItemStack itemStack, ItemStack itemStack2) {
        for (Mix<Item> mix : this.containerMixes) {
            if (!itemStack.is(mix.from) || !mix.ingredient.test(itemStack2)) continue;
            return true;
        }
        return false;
    }

    public boolean hasPotionMix(ItemStack itemStack, ItemStack itemStack2) {
        Optional<Holder<Potion>> optional = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
        if (optional.isEmpty()) {
            return false;
        }
        for (Mix<Potion> mix : this.potionMixes) {
            if (!mix.from.is(optional.get()) || !mix.ingredient.test(itemStack2)) continue;
            return true;
        }
        return false;
    }

    public ItemStack mix(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack2.isEmpty()) {
            return itemStack2;
        }
        Optional<Holder<Potion>> optional = itemStack2.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
        if (optional.isEmpty()) {
            return itemStack2;
        }
        for (Mix<Item> mix : this.containerMixes) {
            if (!itemStack2.is(mix.from) || !mix.ingredient.test(itemStack)) continue;
            return PotionContents.createItemStack((Item)mix.to.value(), optional.get());
        }
        for (Mix<FeatureElement> mix : this.potionMixes) {
            if (!mix.from.is(optional.get()) || !mix.ingredient.test(itemStack)) continue;
            return PotionContents.createItemStack(itemStack2.getItem(), mix.to);
        }
        return itemStack2;
    }

    public static PotionBrewing bootstrap(FeatureFlagSet featureFlagSet) {
        Builder builder = new Builder(featureFlagSet);
        PotionBrewing.addVanillaMixes(builder);
        return builder.build();
    }

    public static void addVanillaMixes(Builder builder) {
        builder.addContainer(Items.POTION);
        builder.addContainer(Items.SPLASH_POTION);
        builder.addContainer(Items.LINGERING_POTION);
        builder.addContainerRecipe(Items.POTION, Items.GUNPOWDER, Items.SPLASH_POTION);
        builder.addContainerRecipe(Items.SPLASH_POTION, Items.DRAGON_BREATH, Items.LINGERING_POTION);
        builder.addMix(Potions.WATER, Items.GLOWSTONE_DUST, Potions.THICK);
        builder.addMix(Potions.WATER, Items.REDSTONE, Potions.MUNDANE);
        builder.addMix(Potions.WATER, Items.NETHER_WART, Potions.AWKWARD);
        builder.addStartMix(Items.BREEZE_ROD, Potions.WIND_CHARGED);
        builder.addStartMix(Items.SLIME_BLOCK, Potions.OOZING);
        builder.addStartMix(Items.STONE, Potions.INFESTED);
        builder.addStartMix(Items.COBWEB, Potions.WEAVING);
        builder.addMix(Potions.AWKWARD, Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
        builder.addMix(Potions.NIGHT_VISION, Items.REDSTONE, Potions.LONG_NIGHT_VISION);
        builder.addMix(Potions.NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.INVISIBILITY);
        builder.addMix(Potions.LONG_NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.LONG_INVISIBILITY);
        builder.addMix(Potions.INVISIBILITY, Items.REDSTONE, Potions.LONG_INVISIBILITY);
        builder.addStartMix(Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
        builder.addMix(Potions.FIRE_RESISTANCE, Items.REDSTONE, Potions.LONG_FIRE_RESISTANCE);
        builder.addStartMix(Items.RABBIT_FOOT, Potions.LEAPING);
        builder.addMix(Potions.LEAPING, Items.REDSTONE, Potions.LONG_LEAPING);
        builder.addMix(Potions.LEAPING, Items.GLOWSTONE_DUST, Potions.STRONG_LEAPING);
        builder.addMix(Potions.LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        builder.addMix(Potions.LONG_LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        builder.addMix(Potions.SLOWNESS, Items.REDSTONE, Potions.LONG_SLOWNESS);
        builder.addMix(Potions.SLOWNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SLOWNESS);
        builder.addMix(Potions.AWKWARD, Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
        builder.addMix(Potions.TURTLE_MASTER, Items.REDSTONE, Potions.LONG_TURTLE_MASTER);
        builder.addMix(Potions.TURTLE_MASTER, Items.GLOWSTONE_DUST, Potions.STRONG_TURTLE_MASTER);
        builder.addMix(Potions.SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        builder.addMix(Potions.LONG_SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        builder.addStartMix(Items.SUGAR, Potions.SWIFTNESS);
        builder.addMix(Potions.SWIFTNESS, Items.REDSTONE, Potions.LONG_SWIFTNESS);
        builder.addMix(Potions.SWIFTNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SWIFTNESS);
        builder.addMix(Potions.AWKWARD, Items.PUFFERFISH, Potions.WATER_BREATHING);
        builder.addMix(Potions.WATER_BREATHING, Items.REDSTONE, Potions.LONG_WATER_BREATHING);
        builder.addStartMix(Items.GLISTERING_MELON_SLICE, Potions.HEALING);
        builder.addMix(Potions.HEALING, Items.GLOWSTONE_DUST, Potions.STRONG_HEALING);
        builder.addMix(Potions.HEALING, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        builder.addMix(Potions.STRONG_HEALING, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        builder.addMix(Potions.HARMING, Items.GLOWSTONE_DUST, Potions.STRONG_HARMING);
        builder.addMix(Potions.POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        builder.addMix(Potions.LONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        builder.addMix(Potions.STRONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        builder.addStartMix(Items.SPIDER_EYE, Potions.POISON);
        builder.addMix(Potions.POISON, Items.REDSTONE, Potions.LONG_POISON);
        builder.addMix(Potions.POISON, Items.GLOWSTONE_DUST, Potions.STRONG_POISON);
        builder.addStartMix(Items.GHAST_TEAR, Potions.REGENERATION);
        builder.addMix(Potions.REGENERATION, Items.REDSTONE, Potions.LONG_REGENERATION);
        builder.addMix(Potions.REGENERATION, Items.GLOWSTONE_DUST, Potions.STRONG_REGENERATION);
        builder.addStartMix(Items.BLAZE_POWDER, Potions.STRENGTH);
        builder.addMix(Potions.STRENGTH, Items.REDSTONE, Potions.LONG_STRENGTH);
        builder.addMix(Potions.STRENGTH, Items.GLOWSTONE_DUST, Potions.STRONG_STRENGTH);
        builder.addMix(Potions.WATER, Items.FERMENTED_SPIDER_EYE, Potions.WEAKNESS);
        builder.addMix(Potions.WEAKNESS, Items.REDSTONE, Potions.LONG_WEAKNESS);
        builder.addMix(Potions.AWKWARD, Items.PHANTOM_MEMBRANE, Potions.SLOW_FALLING);
        builder.addMix(Potions.SLOW_FALLING, Items.REDSTONE, Potions.LONG_SLOW_FALLING);
    }

    static final class Mix<T>
    extends Record {
        final Holder<T> from;
        final Ingredient ingredient;
        final Holder<T> to;

        Mix(Holder<T> holder, Ingredient ingredient, Holder<T> holder2) {
            this.from = holder;
            this.ingredient = ingredient;
            this.to = holder2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Mix.class, "from;ingredient;to", "from", "ingredient", "to"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Mix.class, "from;ingredient;to", "from", "ingredient", "to"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Mix.class, "from;ingredient;to", "from", "ingredient", "to"}, this, object);
        }

        public Holder<T> from() {
            return this.from;
        }

        public Ingredient ingredient() {
            return this.ingredient;
        }

        public Holder<T> to() {
            return this.to;
        }
    }

    public static class Builder {
        private final List<Ingredient> containers = new ArrayList<Ingredient>();
        private final List<Mix<Potion>> potionMixes = new ArrayList<Mix<Potion>>();
        private final List<Mix<Item>> containerMixes = new ArrayList<Mix<Item>>();
        private final FeatureFlagSet enabledFeatures;

        public Builder(FeatureFlagSet featureFlagSet) {
            this.enabledFeatures = featureFlagSet;
        }

        private static void expectPotion(Item item) {
            if (!(item instanceof PotionItem)) {
                throw new IllegalArgumentException("Expected a potion, got: " + String.valueOf(BuiltInRegistries.ITEM.getKey(item)));
            }
        }

        public void addContainerRecipe(Item item, Item item2, Item item3) {
            if (!(item.isEnabled(this.enabledFeatures) && item2.isEnabled(this.enabledFeatures) && item3.isEnabled(this.enabledFeatures))) {
                return;
            }
            Builder.expectPotion(item);
            Builder.expectPotion(item3);
            this.containerMixes.add(new Mix<Item>(item.builtInRegistryHolder(), Ingredient.of(item2), item3.builtInRegistryHolder()));
        }

        public void addContainer(Item item) {
            if (!item.isEnabled(this.enabledFeatures)) {
                return;
            }
            Builder.expectPotion(item);
            this.containers.add(Ingredient.of(item));
        }

        public void addMix(Holder<Potion> holder, Item item, Holder<Potion> holder2) {
            if (holder.value().isEnabled(this.enabledFeatures) && item.isEnabled(this.enabledFeatures) && holder2.value().isEnabled(this.enabledFeatures)) {
                this.potionMixes.add(new Mix<Potion>(holder, Ingredient.of(item), holder2));
            }
        }

        public void addStartMix(Item item, Holder<Potion> holder) {
            if (holder.value().isEnabled(this.enabledFeatures)) {
                this.addMix(Potions.WATER, item, Potions.MUNDANE);
                this.addMix(Potions.AWKWARD, item, holder);
            }
        }

        public PotionBrewing build() {
            return new PotionBrewing(List.copyOf(this.containers), List.copyOf(this.potionMixes), List.copyOf(this.containerMixes));
        }
    }
}

