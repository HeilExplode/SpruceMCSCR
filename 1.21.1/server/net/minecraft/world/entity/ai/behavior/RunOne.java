/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class RunOne<E extends LivingEntity>
extends GateBehavior<E> {
    public RunOne(List<Pair<? extends BehaviorControl<? super E>, Integer>> list) {
        this((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(), (List<Pair<BehaviorControl<E>, Integer>>)list);
    }

    public RunOne(Map<MemoryModuleType<?>, MemoryStatus> map, List<Pair<? extends BehaviorControl<? super E>, Integer>> list) {
        super(map, (Set<MemoryModuleType<?>>)ImmutableSet.of(), GateBehavior.OrderPolicy.SHUFFLED, GateBehavior.RunningPolicy.RUN_ONE, list);
    }
}

