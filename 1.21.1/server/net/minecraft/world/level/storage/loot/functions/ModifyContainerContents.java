/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ModifyContainerContents
extends LootItemConditionalFunction {
    public static final MapCodec<ModifyContainerContents> CODEC = RecordCodecBuilder.mapCodec(instance -> ModifyContainerContents.commonFields(instance).and(instance.group((App)ContainerComponentManipulators.CODEC.fieldOf("component").forGetter(modifyContainerContents -> modifyContainerContents.component), (App)LootItemFunctions.ROOT_CODEC.fieldOf("modifier").forGetter(modifyContainerContents -> modifyContainerContents.modifier))).apply((Applicative)instance, ModifyContainerContents::new));
    private final ContainerComponentManipulator<?> component;
    private final LootItemFunction modifier;

    private ModifyContainerContents(List<LootItemCondition> list, ContainerComponentManipulator<?> containerComponentManipulator, LootItemFunction lootItemFunction) {
        super(list);
        this.component = containerComponentManipulator;
        this.modifier = lootItemFunction;
    }

    public LootItemFunctionType<ModifyContainerContents> getType() {
        return LootItemFunctions.MODIFY_CONTENTS;
    }

    @Override
    public ItemStack run(ItemStack itemStack2, LootContext lootContext) {
        if (itemStack2.isEmpty()) {
            return itemStack2;
        }
        this.component.modifyItems(itemStack2, itemStack -> (ItemStack)this.modifier.apply(itemStack, lootContext));
        return itemStack2;
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        this.modifier.validate(validationContext.forChild(".modifier"));
    }
}

