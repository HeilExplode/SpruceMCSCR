/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public class LocateHidingPlace {
    public static OneShot<LivingEntity> create(int n, float f, int n2) {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleType.HOME), instance.registered(MemoryModuleType.HIDING_PLACE), instance.registered(MemoryModuleType.PATH), instance.registered(MemoryModuleType.LOOK_TARGET), instance.registered(MemoryModuleType.BREED_TARGET), instance.registered(MemoryModuleType.INTERACTION_TARGET)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4, memoryAccessor5, memoryAccessor6, memoryAccessor7) -> (serverLevel, livingEntity, l) -> {
            serverLevel.getPoiManager().find(holder -> holder.is(PoiTypes.HOME), blockPos -> true, livingEntity.blockPosition(), n2 + 1, PoiManager.Occupancy.ANY).filter(blockPos -> blockPos.closerToCenterThan(livingEntity.position(), n2)).or(() -> serverLevel.getPoiManager().getRandom(holder -> holder.is(PoiTypes.HOME), blockPos -> true, PoiManager.Occupancy.ANY, livingEntity.blockPosition(), n, livingEntity.getRandom())).or(() -> instance.tryGet(memoryAccessor2).map(GlobalPos::pos)).ifPresent(blockPos -> {
                memoryAccessor4.erase();
                memoryAccessor5.erase();
                memoryAccessor6.erase();
                memoryAccessor7.erase();
                memoryAccessor3.set(GlobalPos.of(serverLevel.dimension(), blockPos));
                if (!blockPos.closerToCenterThan(livingEntity.position(), n2)) {
                    memoryAccessor.set(new WalkTarget((BlockPos)blockPos, f, n2));
                }
            });
            return true;
        }));
    }
}

