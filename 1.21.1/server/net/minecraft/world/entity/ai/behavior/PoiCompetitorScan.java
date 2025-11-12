/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class PoiCompetitorScan {
    public static BehaviorControl<Villager> create() {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.JOB_SITE), instance.present(MemoryModuleType.NEAREST_LIVING_ENTITIES)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, villager, l) -> {
            GlobalPos globalPos = (GlobalPos)instance.get(memoryAccessor);
            serverLevel.getPoiManager().getType(globalPos.pos()).ifPresent(holder -> ((List)instance.get(memoryAccessor2)).stream().filter(livingEntity -> livingEntity instanceof Villager && livingEntity != villager).map(livingEntity -> (Villager)livingEntity).filter(LivingEntity::isAlive).filter(villager -> PoiCompetitorScan.competesForSameJobsite(globalPos, holder, villager)).reduce((Villager)villager, PoiCompetitorScan::selectWinner));
            return true;
        }));
    }

    private static Villager selectWinner(Villager villager, Villager villager2) {
        Villager villager3;
        Villager villager4;
        if (villager.getVillagerXp() > villager2.getVillagerXp()) {
            villager4 = villager;
            villager3 = villager2;
        } else {
            villager4 = villager2;
            villager3 = villager;
        }
        villager3.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
        return villager4;
    }

    private static boolean competesForSameJobsite(GlobalPos globalPos, Holder<PoiType> holder, Villager villager) {
        Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        return optional.isPresent() && globalPos.equals(optional.get()) && PoiCompetitorScan.hasMatchingProfession(holder, villager.getVillagerData().getProfession());
    }

    private static boolean hasMatchingProfession(Holder<PoiType> holder, VillagerProfession villagerProfession) {
        return villagerProfession.heldJobSite().test(holder);
    }
}

