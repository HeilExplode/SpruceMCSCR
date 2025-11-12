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
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record SetBlockProperties(BlockItemStateProperties properties, Vec3i offset, Optional<Holder<GameEvent>> triggerGameEvent) implements EnchantmentEntityEffect
{
    public static final MapCodec<SetBlockProperties> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockItemStateProperties.CODEC.fieldOf("properties").forGetter(SetBlockProperties::properties), (App)Vec3i.CODEC.optionalFieldOf("offset", (Object)Vec3i.ZERO).forGetter(SetBlockProperties::offset), (App)GameEvent.CODEC.optionalFieldOf("trigger_game_event").forGetter(SetBlockProperties::triggerGameEvent)).apply((Applicative)instance, SetBlockProperties::new));

    public SetBlockProperties(BlockItemStateProperties blockItemStateProperties) {
        this(blockItemStateProperties, Vec3i.ZERO, Optional.of(GameEvent.BLOCK_CHANGE));
    }

    @Override
    public void apply(ServerLevel serverLevel, int n, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
        BlockState blockState;
        BlockPos blockPos = BlockPos.containing(vec3).offset(this.offset);
        BlockState blockState2 = entity.level().getBlockState(blockPos);
        if (!blockState2.equals(blockState = this.properties.apply(blockState2)) && entity.level().setBlock(blockPos, blockState, 3)) {
            this.triggerGameEvent.ifPresent(holder -> serverLevel.gameEvent(entity, (Holder<GameEvent>)holder, blockPos));
        }
    }

    public MapCodec<SetBlockProperties> codec() {
        return CODEC;
    }
}

