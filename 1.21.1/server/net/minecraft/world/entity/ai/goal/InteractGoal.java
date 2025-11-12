/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class InteractGoal
extends LookAtPlayerGoal {
    public InteractGoal(Mob mob, Class<? extends LivingEntity> clazz, float f) {
        super(mob, clazz, f);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }

    public InteractGoal(Mob mob, Class<? extends LivingEntity> clazz, float f, float f2) {
        super(mob, clazz, f, f2);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }
}

