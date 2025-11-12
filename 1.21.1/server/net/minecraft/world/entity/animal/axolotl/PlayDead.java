/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public class PlayDead
extends Behavior<Axolotl> {
    public PlayDead() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.PLAY_DEAD_TICKS, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.HURT_BY_ENTITY, (Object)((Object)MemoryStatus.VALUE_PRESENT)), 200);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Axolotl axolotl) {
        return axolotl.isInWaterOrBubble();
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Axolotl axolotl, long l) {
        return axolotl.isInWaterOrBubble() && axolotl.getBrain().hasMemoryValue(MemoryModuleType.PLAY_DEAD_TICKS);
    }

    @Override
    protected void start(ServerLevel serverLevel, Axolotl axolotl, long l) {
        Brain<Axolotl> brain = axolotl.getBrain();
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        axolotl.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Axolotl)livingEntity, l);
    }
}

