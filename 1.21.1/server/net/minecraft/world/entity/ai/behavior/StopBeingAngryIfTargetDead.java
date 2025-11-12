/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.GameRules;

public class StopBeingAngryIfTargetDead {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.ANGRY_AT)).apply((Applicative)instance, memoryAccessor -> (serverLevel, livingEntity2, l) -> {
            Optional.ofNullable(serverLevel.getEntity((UUID)instance.get(memoryAccessor))).map(entity -> {
                LivingEntity livingEntity;
                return entity instanceof LivingEntity ? (livingEntity = (LivingEntity)entity) : null;
            }).filter(LivingEntity::isDeadOrDying).filter(livingEntity -> livingEntity.getType() != EntityType.PLAYER || serverLevel.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)).ifPresent(livingEntity -> memoryAccessor.erase());
            return true;
        }));
    }
}

