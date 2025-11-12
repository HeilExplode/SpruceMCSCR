/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.Vec3;

public record ReplaceBlock(Vec3i offset, Optional<BlockPredicate> predicate, BlockStateProvider blockState, Optional<Holder<GameEvent>> triggerGameEvent) implements EnchantmentEntityEffect
{
    public static final MapCodec<ReplaceBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Vec3i.CODEC.optionalFieldOf("offset", (Object)Vec3i.ZERO).forGetter(ReplaceBlock::offset), (App)BlockPredicate.CODEC.optionalFieldOf("predicate").forGetter(ReplaceBlock::predicate), (App)BlockStateProvider.CODEC.fieldOf("block_state").forGetter(ReplaceBlock::blockState), (App)GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(ReplaceBlock::triggerGameEvent)).apply((Applicative)instance, ReplaceBlock::new));

    @Override
    public void apply(ServerLevel serverLevel, int n, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
        BlockPos blockPos = BlockPos.containing(vec3).offset(this.offset);
        if (this.predicate.map(blockPredicate -> blockPredicate.test(serverLevel, blockPos)).orElse(true).booleanValue() && serverLevel.setBlockAndUpdate(blockPos, this.blockState.getState(entity.getRandom(), blockPos))) {
            this.triggerGameEvent.ifPresent(holder -> serverLevel.gameEvent(entity, (Holder<GameEvent>)holder, blockPos));
        }
    }

    public MapCodec<ReplaceBlock> codec() {
        return CODEC;
    }
}

