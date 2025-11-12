/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.CelebrateVillagersSurvivedRaid;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.behavior.GoToClosestVillage;
import net.minecraft.world.entity.ai.behavior.GoToPotentialJobSite;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.behavior.InsideBrownianWalk;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.JumpOnBed;
import net.minecraft.world.entity.ai.behavior.LocateHidingPlace;
import net.minecraft.world.entity.ai.behavior.LookAndFollowTradingPlayerSink;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToSkySeeingSpot;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PlayTagWithOtherKids;
import net.minecraft.world.entity.ai.behavior.PoiCompetitorScan;
import net.minecraft.world.entity.ai.behavior.ReactToBell;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.ai.behavior.ResetRaidStatus;
import net.minecraft.world.entity.ai.behavior.RingBell;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetClosestHomeAsWalkTarget;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetHiddenState;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetRaidStatus;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromBlockMemory;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.ai.behavior.SleepInBed;
import net.minecraft.world.entity.ai.behavior.SocializeAtBell;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoiList;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.TradeWithVillager;
import net.minecraft.world.entity.ai.behavior.TriggerGate;
import net.minecraft.world.entity.ai.behavior.UpdateActivityFromSchedule;
import net.minecraft.world.entity.ai.behavior.UseBonemeal;
import net.minecraft.world.entity.ai.behavior.ValidateNearbyPoi;
import net.minecraft.world.entity.ai.behavior.VillageBoundRandomStroll;
import net.minecraft.world.entity.ai.behavior.VillagerCalmDown;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.ai.behavior.VillagerPanicTrigger;
import net.minecraft.world.entity.ai.behavior.WakeUp;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import net.minecraft.world.entity.ai.behavior.YieldJobSite;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.raid.Raid;

public class VillagerGoalPackages {
    private static final float STROLL_SPEED_MODIFIER = 0.4f;
    public static final int INTERACT_DIST_SQR = 5;
    public static final int INTERACT_WALKUP_DIST = 2;
    public static final float INTERACT_SPEED_MODIFIER = 0.5f;

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getCorePackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of((Object)Pair.of((Object)0, (Object)new Swim(0.8f)), (Object)Pair.of((Object)0, InteractWithDoor.create()), (Object)Pair.of((Object)0, (Object)new LookAtTargetSink(45, 90)), (Object)Pair.of((Object)0, (Object)new VillagerPanicTrigger()), (Object)Pair.of((Object)0, WakeUp.create()), (Object)Pair.of((Object)0, ReactToBell.create()), (Object)Pair.of((Object)0, SetRaidStatus.create()), (Object)Pair.of((Object)0, ValidateNearbyPoi.create(villagerProfession.heldJobSite(), MemoryModuleType.JOB_SITE)), (Object)Pair.of((Object)0, ValidateNearbyPoi.create(villagerProfession.acquirableJobSite(), MemoryModuleType.POTENTIAL_JOB_SITE)), (Object)Pair.of((Object)1, (Object)new MoveToTargetSink()), (Object)Pair.of((Object)2, PoiCompetitorScan.create()), (Object)Pair.of((Object)3, (Object)new LookAndFollowTradingPlayerSink(f)), (Object[])new Pair[]{Pair.of((Object)5, GoToWantedItem.create(f, false, 4)), Pair.of((Object)6, AcquirePoi.create(villagerProfession.acquirableJobSite(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())), Pair.of((Object)7, (Object)new GoToPotentialJobSite(f)), Pair.of((Object)8, YieldJobSite.create(f)), Pair.of((Object)10, AcquirePoi.create(holder -> holder.is(PoiTypes.HOME), MemoryModuleType.HOME, false, Optional.of((byte)14))), Pair.of((Object)10, AcquirePoi.create(holder -> holder.is(PoiTypes.MEETING), MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))), Pair.of((Object)10, AssignProfessionFromJobSite.create()), Pair.of((Object)10, ResetProfession.create())});
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getWorkPackage(VillagerProfession villagerProfession, float f) {
        WorkAtPoi workAtPoi = villagerProfession == VillagerProfession.FARMER ? new WorkAtComposter() : new WorkAtPoi();
        return ImmutableList.of(VillagerGoalPackages.getMinimalLookBehavior(), (Object)Pair.of((Object)5, new RunOne(ImmutableList.of((Object)Pair.of((Object)workAtPoi, (Object)7), (Object)Pair.of(StrollAroundPoi.create(MemoryModuleType.JOB_SITE, 0.4f, 4), (Object)2), (Object)Pair.of(StrollToPoi.create(MemoryModuleType.JOB_SITE, 0.4f, 1, 10), (Object)5), (Object)Pair.of(StrollToPoiList.create(MemoryModuleType.SECONDARY_JOB_SITE, f, 1, 6, MemoryModuleType.JOB_SITE), (Object)5), (Object)Pair.of((Object)new HarvestFarmland(), (Object)(villagerProfession == VillagerProfession.FARMER ? 2 : 5)), (Object)Pair.of((Object)new UseBonemeal(), (Object)(villagerProfession == VillagerProfession.FARMER ? 4 : 7))))), (Object)Pair.of((Object)10, (Object)new ShowTradesToPlayer(400, 1600)), (Object)Pair.of((Object)10, SetLookAndInteract.create(EntityType.PLAYER, 4)), (Object)Pair.of((Object)2, SetWalkTargetFromBlockMemory.create(MemoryModuleType.JOB_SITE, f, 9, 100, 1200)), (Object)Pair.of((Object)3, (Object)new GiveGiftToHero(100)), (Object)Pair.of((Object)99, UpdateActivityFromSchedule.create()));
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getPlayPackage(float f) {
        return ImmutableList.of((Object)Pair.of((Object)0, (Object)new MoveToTargetSink(80, 120)), VillagerGoalPackages.getFullLookBehavior(), (Object)Pair.of((Object)5, PlayTagWithOtherKids.create()), (Object)Pair.of((Object)5, new RunOne((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, (Object)((Object)MemoryStatus.VALUE_ABSENT)), ImmutableList.of((Object)Pair.of(InteractWith.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, f, 2), (Object)2), (Object)Pair.of(InteractWith.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, f, 2), (Object)1), (Object)Pair.of(VillageBoundRandomStroll.create(f), (Object)1), (Object)Pair.of(SetWalkTargetFromLookTarget.create(f, 2), (Object)1), (Object)Pair.of((Object)new JumpOnBed(f), (Object)2), (Object)Pair.of((Object)new DoNothing(20, 40), (Object)2)))), (Object)Pair.of((Object)99, UpdateActivityFromSchedule.create()));
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getRestPackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of((Object)Pair.of((Object)2, SetWalkTargetFromBlockMemory.create(MemoryModuleType.HOME, f, 1, 150, 1200)), (Object)Pair.of((Object)3, ValidateNearbyPoi.create(holder -> holder.is(PoiTypes.HOME), MemoryModuleType.HOME)), (Object)Pair.of((Object)3, (Object)new SleepInBed()), (Object)Pair.of((Object)5, new RunOne((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.HOME, (Object)((Object)MemoryStatus.VALUE_ABSENT)), ImmutableList.of((Object)Pair.of(SetClosestHomeAsWalkTarget.create(f), (Object)1), (Object)Pair.of(InsideBrownianWalk.create(f), (Object)4), (Object)Pair.of(GoToClosestVillage.create(f, 4), (Object)2), (Object)Pair.of((Object)new DoNothing(20, 40), (Object)2)))), VillagerGoalPackages.getMinimalLookBehavior(), (Object)Pair.of((Object)99, UpdateActivityFromSchedule.create()));
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getMeetPackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of((Object)Pair.of((Object)2, TriggerGate.triggerOneShuffled(ImmutableList.of((Object)Pair.of(StrollAroundPoi.create(MemoryModuleType.MEETING_POINT, 0.4f, 40), (Object)2), (Object)Pair.of(SocializeAtBell.create(), (Object)2)))), (Object)Pair.of((Object)10, (Object)new ShowTradesToPlayer(400, 1600)), (Object)Pair.of((Object)10, SetLookAndInteract.create(EntityType.PLAYER, 4)), (Object)Pair.of((Object)2, SetWalkTargetFromBlockMemory.create(MemoryModuleType.MEETING_POINT, f, 6, 100, 200)), (Object)Pair.of((Object)3, (Object)new GiveGiftToHero(100)), (Object)Pair.of((Object)3, ValidateNearbyPoi.create(holder -> holder.is(PoiTypes.MEETING), MemoryModuleType.MEETING_POINT)), (Object)Pair.of((Object)3, new GateBehavior((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(), (Set<MemoryModuleType<?>>)ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of((Object)Pair.of((Object)new TradeWithVillager(), (Object)1)))), VillagerGoalPackages.getFullLookBehavior(), (Object)Pair.of((Object)99, UpdateActivityFromSchedule.create()));
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getIdlePackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of((Object)Pair.of((Object)2, new RunOne(ImmutableList.of((Object)Pair.of(InteractWith.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, f, 2), (Object)2), (Object)Pair.of(InteractWith.of(EntityType.VILLAGER, 8, AgeableMob::canBreed, AgeableMob::canBreed, MemoryModuleType.BREED_TARGET, f, 2), (Object)1), (Object)Pair.of(InteractWith.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, f, 2), (Object)1), (Object)Pair.of(VillageBoundRandomStroll.create(f), (Object)1), (Object)Pair.of(SetWalkTargetFromLookTarget.create(f, 2), (Object)1), (Object)Pair.of((Object)new JumpOnBed(f), (Object)1), (Object)Pair.of((Object)new DoNothing(30, 60), (Object)1)))), (Object)Pair.of((Object)3, (Object)new GiveGiftToHero(100)), (Object)Pair.of((Object)3, SetLookAndInteract.create(EntityType.PLAYER, 4)), (Object)Pair.of((Object)3, (Object)new ShowTradesToPlayer(400, 1600)), (Object)Pair.of((Object)3, new GateBehavior((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(), (Set<MemoryModuleType<?>>)ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of((Object)Pair.of((Object)new TradeWithVillager(), (Object)1)))), (Object)Pair.of((Object)3, new GateBehavior((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(), (Set<MemoryModuleType<?>>)ImmutableSet.of(MemoryModuleType.BREED_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of((Object)Pair.of((Object)new VillagerMakeLove(), (Object)1)))), VillagerGoalPackages.getFullLookBehavior(), (Object)Pair.of((Object)99, UpdateActivityFromSchedule.create()));
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getPanicPackage(VillagerProfession villagerProfession, float f) {
        float f2 = f * 1.5f;
        return ImmutableList.of((Object)Pair.of((Object)0, VillagerCalmDown.create()), (Object)Pair.of((Object)1, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_HOSTILE, f2, 6, false)), (Object)Pair.of((Object)1, SetWalkTargetAwayFrom.entity(MemoryModuleType.HURT_BY_ENTITY, f2, 6, false)), (Object)Pair.of((Object)3, VillageBoundRandomStroll.create(f2, 2, 2)), VillagerGoalPackages.getMinimalLookBehavior());
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getPreRaidPackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of((Object)Pair.of((Object)0, RingBell.create()), (Object)Pair.of((Object)0, TriggerGate.triggerOneShuffled(ImmutableList.of((Object)Pair.of(SetWalkTargetFromBlockMemory.create(MemoryModuleType.MEETING_POINT, f * 1.5f, 2, 150, 200), (Object)6), (Object)Pair.of(VillageBoundRandomStroll.create(f * 1.5f), (Object)2)))), VillagerGoalPackages.getMinimalLookBehavior(), (Object)Pair.of((Object)99, ResetRaidStatus.create()));
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getRaidPackage(VillagerProfession villagerProfession, float f) {
        return ImmutableList.of((Object)Pair.of((Object)0, BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(VillagerGoalPackages::raidExistsAndNotVictory), TriggerGate.triggerOneShuffled(ImmutableList.of((Object)Pair.of(MoveToSkySeeingSpot.create(f), (Object)5), (Object)Pair.of(VillageBoundRandomStroll.create(f * 1.1f), (Object)2))))), (Object)Pair.of((Object)0, (Object)new CelebrateVillagersSurvivedRaid(600, 600)), (Object)Pair.of((Object)2, BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(VillagerGoalPackages::raidExistsAndActive), LocateHidingPlace.create(24, f * 1.4f, 1))), VillagerGoalPackages.getMinimalLookBehavior(), (Object)Pair.of((Object)99, ResetRaidStatus.create()));
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getHidePackage(VillagerProfession villagerProfession, float f) {
        int n = 2;
        return ImmutableList.of((Object)Pair.of((Object)0, SetHiddenState.create(15, 3)), (Object)Pair.of((Object)1, LocateHidingPlace.create(32, f * 1.25f, 2)), VillagerGoalPackages.getMinimalLookBehavior());
    }

    private static Pair<Integer, BehaviorControl<LivingEntity>> getFullLookBehavior() {
        return Pair.of((Object)5, new RunOne(ImmutableList.of((Object)Pair.of(SetEntityLookTarget.create(EntityType.CAT, 8.0f), (Object)8), (Object)Pair.of(SetEntityLookTarget.create(EntityType.VILLAGER, 8.0f), (Object)2), (Object)Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0f), (Object)2), (Object)Pair.of(SetEntityLookTarget.create(MobCategory.CREATURE, 8.0f), (Object)1), (Object)Pair.of(SetEntityLookTarget.create(MobCategory.WATER_CREATURE, 8.0f), (Object)1), (Object)Pair.of(SetEntityLookTarget.create(MobCategory.AXOLOTLS, 8.0f), (Object)1), (Object)Pair.of(SetEntityLookTarget.create(MobCategory.UNDERGROUND_WATER_CREATURE, 8.0f), (Object)1), (Object)Pair.of(SetEntityLookTarget.create(MobCategory.WATER_AMBIENT, 8.0f), (Object)1), (Object)Pair.of(SetEntityLookTarget.create(MobCategory.MONSTER, 8.0f), (Object)1), (Object)Pair.of((Object)new DoNothing(30, 60), (Object)2))));
    }

    private static Pair<Integer, BehaviorControl<LivingEntity>> getMinimalLookBehavior() {
        return Pair.of((Object)5, new RunOne(ImmutableList.of((Object)Pair.of(SetEntityLookTarget.create(EntityType.VILLAGER, 8.0f), (Object)2), (Object)Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0f), (Object)2), (Object)Pair.of((Object)new DoNothing(30, 60), (Object)8))));
    }

    private static boolean raidExistsAndActive(ServerLevel serverLevel, LivingEntity livingEntity) {
        Raid raid = serverLevel.getRaidAt(livingEntity.blockPosition());
        return raid != null && raid.isActive() && !raid.isVictory() && !raid.isLoss();
    }

    private static boolean raidExistsAndNotVictory(ServerLevel serverLevel, LivingEntity livingEntity) {
        Raid raid = serverLevel.getRaidAt(livingEntity.blockPosition());
        return raid != null && raid.isVictory();
    }
}

