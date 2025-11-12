/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Hash$Strategy
 *  javax.annotation.Nullable
 */
package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.Hash;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.TickPriority;

public record ScheduledTick<T>(T type, BlockPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
    public static final Comparator<ScheduledTick<?>> DRAIN_ORDER = (scheduledTick, scheduledTick2) -> {
        int n = Long.compare(scheduledTick.triggerTick, scheduledTick2.triggerTick);
        if (n != 0) {
            return n;
        }
        n = scheduledTick.priority.compareTo(scheduledTick2.priority);
        if (n != 0) {
            return n;
        }
        return Long.compare(scheduledTick.subTickOrder, scheduledTick2.subTickOrder);
    };
    public static final Comparator<ScheduledTick<?>> INTRA_TICK_DRAIN_ORDER = (scheduledTick, scheduledTick2) -> {
        int n = scheduledTick.priority.compareTo(scheduledTick2.priority);
        if (n != 0) {
            return n;
        }
        return Long.compare(scheduledTick.subTickOrder, scheduledTick2.subTickOrder);
    };
    public static final Hash.Strategy<ScheduledTick<?>> UNIQUE_TICK_HASH = new Hash.Strategy<ScheduledTick<?>>(){

        public int hashCode(ScheduledTick<?> scheduledTick) {
            return 31 * scheduledTick.pos().hashCode() + scheduledTick.type().hashCode();
        }

        public boolean equals(@Nullable ScheduledTick<?> scheduledTick, @Nullable ScheduledTick<?> scheduledTick2) {
            if (scheduledTick == scheduledTick2) {
                return true;
            }
            if (scheduledTick == null || scheduledTick2 == null) {
                return false;
            }
            return scheduledTick.type() == scheduledTick2.type() && scheduledTick.pos().equals(scheduledTick2.pos());
        }

        public /* synthetic */ boolean equals(@Nullable Object object, @Nullable Object object2) {
            return this.equals((ScheduledTick)object, (ScheduledTick)object2);
        }

        public /* synthetic */ int hashCode(Object object) {
            return this.hashCode((ScheduledTick)object);
        }
    };

    public ScheduledTick(T t, BlockPos blockPos, long l, long l2) {
        this(t, blockPos, l, TickPriority.NORMAL, l2);
    }

    public ScheduledTick {
        blockPos = blockPos.immutable();
    }

    public static <T> ScheduledTick<T> probe(T t, BlockPos blockPos) {
        return new ScheduledTick<T>(t, blockPos, 0L, TickPriority.NORMAL, 0L);
    }
}

