/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetAwayFrom {
    public static BehaviorControl<PathfinderMob> pos(MemoryModuleType<BlockPos> memoryModuleType, float f, int n, boolean bl) {
        return SetWalkTargetAwayFrom.create(memoryModuleType, f, n, bl, Vec3::atBottomCenterOf);
    }

    public static OneShot<PathfinderMob> entity(MemoryModuleType<? extends Entity> memoryModuleType, float f, int n, boolean bl) {
        return SetWalkTargetAwayFrom.create(memoryModuleType, f, n, bl, Entity::position);
    }

    private static <T> OneShot<PathfinderMob> create(MemoryModuleType<T> memoryModuleType, float f, int n, boolean bl, Function<T, Vec3> function) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.WALK_TARGET), instance.present(memoryModuleType)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, pathfinderMob, l) -> {
            Vec3 vec3;
            Vec3 vec32;
            Vec3 vec33;
            Optional optional = instance.tryGet(memoryAccessor);
            if (optional.isPresent() && !bl) {
                return false;
            }
            Vec3 vec34 = pathfinderMob.position();
            if (!vec34.closerThan(vec33 = (Vec3)function.apply(instance.get(memoryAccessor2)), n)) {
                return false;
            }
            if (optional.isPresent() && ((WalkTarget)optional.get()).getSpeedModifier() == f && (vec32 = ((WalkTarget)optional.get()).getTarget().currentPosition().subtract(vec34)).dot(vec3 = vec33.subtract(vec34)) < 0.0) {
                return false;
            }
            for (int i = 0; i < 10; ++i) {
                vec3 = LandRandomPos.getPosAway(pathfinderMob, 16, 7, vec33);
                if (vec3 == null) continue;
                memoryAccessor.set(new WalkTarget(vec3, f, 0));
                break;
            }
            return true;
        }));
    }
}

