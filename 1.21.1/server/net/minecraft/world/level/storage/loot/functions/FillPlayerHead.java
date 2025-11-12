/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FillPlayerHead
extends LootItemConditionalFunction {
    public static final MapCodec<FillPlayerHead> CODEC = RecordCodecBuilder.mapCodec(instance -> FillPlayerHead.commonFields(instance).and((App)LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(fillPlayerHead -> fillPlayerHead.entityTarget)).apply((Applicative)instance, FillPlayerHead::new));
    private final LootContext.EntityTarget entityTarget;

    public FillPlayerHead(List<LootItemCondition> list, LootContext.EntityTarget entityTarget) {
        super(list);
        this.entityTarget = entityTarget;
    }

    public LootItemFunctionType<FillPlayerHead> getType() {
        return LootItemFunctions.FILL_PLAYER_HEAD;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.entityTarget.getParam());
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Entity entity;
        if (itemStack.is(Items.PLAYER_HEAD) && (entity = lootContext.getParamOrNull(this.entityTarget.getParam())) instanceof Player) {
            Player player = (Player)entity;
            itemStack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> fillPlayerHead(LootContext.EntityTarget entityTarget) {
        return FillPlayerHead.simpleBuilder(list -> new FillPlayerHead((List<LootItemCondition>)list, entityTarget));
    }
}

