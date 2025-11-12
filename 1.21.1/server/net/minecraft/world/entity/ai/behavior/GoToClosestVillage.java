/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class GoToClosestVillage {
    public static BehaviorControl<Villager> create(float f, int n) {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET)).apply((Applicative)instance, memoryAccessor -> (serverLevel, villager, l) -> {
            if (serverLevel.isVillage(villager.blockPosition())) {
                return false;
            }
            PoiManager poiManager = serverLevel.getPoiManager();
            int n2 = poiManager.sectionsToVillage(SectionPos.of(villager.blockPosition()));
            Vec3 vec3 = null;
            for (int i = 0; i < 5; ++i) {
                Vec3 vec32 = LandRandomPos.getPos(villager, 15, 7, blockPos -> -poiManager.sectionsToVillage(SectionPos.of(blockPos)));
                if (vec32 == null) continue;
                int n3 = poiManager.sectionsToVillage(SectionPos.of(BlockPos.containing(vec32)));
                if (n3 < n2) {
                    vec3 = vec32;
                    break;
                }
                if (n3 != n2) continue;
                vec3 = vec32;
            }
            if (vec3 != null) {
                memoryAccessor.set(new WalkTarget(vec3, f, n));
            }
            return true;
        }));
    }
}

