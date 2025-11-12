/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestEvent;
import net.minecraft.gametest.framework.GameTestInfo;

public class GameTestSequence {
    final GameTestInfo parent;
    private final List<GameTestEvent> events = Lists.newArrayList();
    private long lastTick;

    GameTestSequence(GameTestInfo gameTestInfo) {
        this.parent = gameTestInfo;
        this.lastTick = gameTestInfo.getTick();
    }

    public GameTestSequence thenWaitUntil(Runnable runnable) {
        this.events.add(GameTestEvent.create(runnable));
        return this;
    }

    public GameTestSequence thenWaitUntil(long l, Runnable runnable) {
        this.events.add(GameTestEvent.create(l, runnable));
        return this;
    }

    public GameTestSequence thenIdle(int n) {
        return this.thenExecuteAfter(n, () -> {});
    }

    public GameTestSequence thenExecute(Runnable runnable) {
        this.events.add(GameTestEvent.create(() -> this.executeWithoutFail(runnable)));
        return this;
    }

    public GameTestSequence thenExecuteAfter(int n, Runnable runnable) {
        this.events.add(GameTestEvent.create(() -> {
            if (this.parent.getTick() < this.lastTick + (long)n) {
                throw new GameTestAssertException("Test timed out before sequence completed");
            }
            this.executeWithoutFail(runnable);
        }));
        return this;
    }

    public GameTestSequence thenExecuteFor(int n, Runnable runnable) {
        this.events.add(GameTestEvent.create(() -> {
            if (this.parent.getTick() < this.lastTick + (long)n) {
                this.executeWithoutFail(runnable);
                throw new GameTestAssertException("Test timed out before sequence completed");
            }
        }));
        return this;
    }

    public void thenSucceed() {
        this.events.add(GameTestEvent.create(this.parent::succeed));
    }

    public void thenFail(Supplier<Exception> supplier) {
        this.events.add(GameTestEvent.create(() -> this.parent.fail((Throwable)supplier.get())));
    }

    public Condition thenTrigger() {
        Condition condition = new Condition();
        this.events.add(GameTestEvent.create(() -> condition.trigger(this.parent.getTick())));
        return condition;
    }

    public void tickAndContinue(long l) {
        try {
            this.tick(l);
        }
        catch (GameTestAssertException gameTestAssertException) {
            // empty catch block
        }
    }

    public void tickAndFailIfNotComplete(long l) {
        try {
            this.tick(l);
        }
        catch (GameTestAssertException gameTestAssertException) {
            this.parent.fail(gameTestAssertException);
        }
    }

    private void executeWithoutFail(Runnable runnable) {
        try {
            runnable.run();
        }
        catch (GameTestAssertException gameTestAssertException) {
            this.parent.fail(gameTestAssertException);
        }
    }

    private void tick(long l) {
        Iterator<GameTestEvent> iterator = this.events.iterator();
        while (iterator.hasNext()) {
            GameTestEvent gameTestEvent = iterator.next();
            gameTestEvent.assertion.run();
            iterator.remove();
            long l2 = l - this.lastTick;
            long l3 = this.lastTick;
            this.lastTick = l;
            if (gameTestEvent.expectedDelay == null || gameTestEvent.expectedDelay == l2) continue;
            this.parent.fail(new GameTestAssertException("Succeeded in invalid tick: expected " + (l3 + gameTestEvent.expectedDelay) + ", but current tick is " + l));
            break;
        }
    }

    public class Condition {
        private static final long NOT_TRIGGERED = -1L;
        private long triggerTime = -1L;

        void trigger(long l) {
            if (this.triggerTime != -1L) {
                throw new IllegalStateException("Condition already triggered at " + this.triggerTime);
            }
            this.triggerTime = l;
        }

        public void assertTriggeredThisTick() {
            long l = GameTestSequence.this.parent.getTick();
            if (this.triggerTime != l) {
                if (this.triggerTime == -1L) {
                    throw new GameTestAssertException("Condition not triggered (t=" + l + ")");
                }
                throw new GameTestAssertException("Condition triggered at " + this.triggerTime + ", (t=" + l + ")");
            }
        }
    }
}

