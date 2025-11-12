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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, ResourceLocation resourceLocation, List<ItemStack> list) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceLocation, list));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceLocation recipeId, List<ItemPredicate> ingredients) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)ResourceLocation.CODEC.fieldOf("recipe_id").forGetter(TriggerInstance::recipeId), (App)ItemPredicate.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(TriggerInstance::ingredients)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> craftedItem(ResourceLocation resourceLocation, List<ItemPredicate.Builder> list) {
            return CriteriaTriggers.RECIPE_CRAFTED.createCriterion(new TriggerInstance(Optional.empty(), resourceLocation, list.stream().map(ItemPredicate.Builder::build).toList()));
        }

        public static Criterion<TriggerInstance> craftedItem(ResourceLocation resourceLocation) {
            return CriteriaTriggers.RECIPE_CRAFTED.createCriterion(new TriggerInstance(Optional.empty(), resourceLocation, List.of()));
        }

        public static Criterion<TriggerInstance> crafterCraftedItem(ResourceLocation resourceLocation) {
            return CriteriaTriggers.CRAFTER_RECIPE_CRAFTED.createCriterion(new TriggerInstance(Optional.empty(), resourceLocation, List.of()));
        }

        boolean matches(ResourceLocation resourceLocation, List<ItemStack> list) {
            if (!resourceLocation.equals(this.recipeId)) {
                return false;
            }
            ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>(list);
            for (ItemPredicate itemPredicate : this.ingredients) {
                boolean bl = false;
                Iterator iterator = arrayList.iterator();
                while (iterator.hasNext()) {
                    if (!itemPredicate.test((ItemStack)iterator.next())) continue;
                    iterator.remove();
                    bl = true;
                    break;
                }
                if (bl) continue;
                return false;
            }
            return true;
        }
    }
}

