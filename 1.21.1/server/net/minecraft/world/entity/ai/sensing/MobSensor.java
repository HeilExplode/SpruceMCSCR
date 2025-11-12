/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.sensing;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class MobSensor<T extends LivingEntity>
extends Sensor<T> {
    private final BiPredicate<T, LivingEntity> mobTest;
    private final Predicate<T> readyTest;
    private final MemoryModuleType<Boolean> toSet;
    private final int memoryTimeToLive;

    public MobSensor(int n, BiPredicate<T, LivingEntity> biPredicate, Predicate<T> predicate, MemoryModuleType<Boolean> memoryModuleType, int n2) {
        super(n);
        this.mobTest = biPredicate;
        this.readyTest = predicate;
        this.toSet = memoryModuleType;
        this.memoryTimeToLive = n2;
    }

    @Override
    protected void doTick(ServerLevel serverLevel, T t) {
        if (!this.readyTest.test(t)) {
            this.clearMemory(t);
        } else {
            this.checkForMobsNearby(t);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(MemoryModuleType.NEAREST_LIVING_ENTITIES);
    }

    public void checkForMobsNearby(T t) {
        Optional<List<LivingEntity>> optional = ((LivingEntity)t).getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
        if (optional.isEmpty()) {
            return;
        }
        boolean bl = optional.get().stream().anyMatch(livingEntity2 -> this.mobTest.test((LivingEntity)t, (LivingEntity)livingEntity2));
        if (bl) {
            this.mobDetected(t);
        }
    }

    public void mobDetected(T t) {
        ((LivingEntity)t).getBrain().setMemoryWithExpiry(this.toSet, true, this.memoryTimeToLive);
    }

    public void clearMemory(T t) {
        ((LivingEntity)t).getBrain().eraseMemory(this.toSet);
    }
}

