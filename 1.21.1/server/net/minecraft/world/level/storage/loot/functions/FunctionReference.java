/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference
extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<FunctionReference> CODEC = RecordCodecBuilder.mapCodec(instance -> FunctionReference.commonFields(instance).and((App)ResourceKey.codec(Registries.ITEM_MODIFIER).fieldOf("name").forGetter(functionReference -> functionReference.name)).apply((Applicative)instance, FunctionReference::new));
    private final ResourceKey<LootItemFunction> name;

    private FunctionReference(List<LootItemCondition> list, ResourceKey<LootItemFunction> resourceKey) {
        super(list);
        this.name = resourceKey;
    }

    public LootItemFunctionType<FunctionReference> getType() {
        return LootItemFunctions.REFERENCE;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        if (!validationContext.allowsReferences()) {
            validationContext.reportProblem("Uses reference to " + String.valueOf(this.name.location()) + ", but references are not allowed");
            return;
        }
        if (validationContext.hasVisitedElement(this.name)) {
            validationContext.reportProblem("Function " + String.valueOf(this.name.location()) + " is recursively called");
            return;
        }
        super.validate(validationContext);
        validationContext.resolver().get(Registries.ITEM_MODIFIER, this.name).ifPresentOrElse(reference -> ((LootItemFunction)reference.value()).validate(validationContext.enterElement(".{" + String.valueOf(this.name.location()) + "}", this.name)), () -> validationContext.reportProblem("Unknown function table called " + String.valueOf(this.name.location())));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        LootItemFunction lootItemFunction = lootContext.getResolver().get(Registries.ITEM_MODIFIER, this.name).map(Holder::value).orElse(null);
        if (lootItemFunction == null) {
            LOGGER.warn("Unknown function: {}", (Object)this.name.location());
            return itemStack;
        }
        LootContext.VisitedEntry<LootItemFunction> visitedEntry = LootContext.createVisitedEntry(lootItemFunction);
        if (lootContext.pushVisitedElement(visitedEntry)) {
            try {
                ItemStack itemStack2 = (ItemStack)lootItemFunction.apply(itemStack, lootContext);
                return itemStack2;
            }
            finally {
                lootContext.popVisitedElement(visitedEntry);
            }
        }
        LOGGER.warn("Detected infinite loop in loot tables");
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> functionReference(ResourceKey<LootItemFunction> resourceKey) {
        return FunctionReference.simpleBuilder(list -> new FunctionReference((List<LootItemCondition>)list, resourceKey));
    }
}

