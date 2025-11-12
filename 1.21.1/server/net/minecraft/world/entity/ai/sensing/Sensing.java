/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 */
package net.minecraft.world.entity.ai.sensing;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class Sensing {
    private final Mob mob;
    private final IntSet seen = new IntOpenHashSet();
    private final IntSet unseen = new IntOpenHashSet();

    public Sensing(Mob mob) {
        this.mob = mob;
    }

    public void tick() {
        this.seen.clear();
        this.unseen.clear();
    }

    public boolean hasLineOfSight(Entity entity) {
        int n = entity.getId();
        if (this.seen.contains(n)) {
            return true;
        }
        if (this.unseen.contains(n)) {
            return false;
        }
        this.mob.level().getProfiler().push("hasLineOfSight");
        boolean bl = this.mob.hasLineOfSight(entity);
        this.mob.level().getProfiler().pop();
        if (bl) {
            this.seen.add(n);
        } else {
            this.unseen.add(n);
        }
        return bl;
    }
}

