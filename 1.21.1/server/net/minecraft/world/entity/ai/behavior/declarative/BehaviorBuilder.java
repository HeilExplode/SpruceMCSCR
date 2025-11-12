/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.kinds.Applicative$Mu
 *  com.mojang.datafixers.kinds.Const$Mu
 *  com.mojang.datafixers.kinds.IdF
 *  com.mojang.datafixers.kinds.IdF$Mu
 *  com.mojang.datafixers.kinds.K1
 *  com.mojang.datafixers.kinds.OptionalBox
 *  com.mojang.datafixers.kinds.OptionalBox$Mu
 *  com.mojang.datafixers.util.Function3
 *  com.mojang.datafixers.util.Function4
 *  com.mojang.datafixers.util.Unit
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.OptionalBox;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Unit;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryCondition;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BehaviorBuilder<E extends LivingEntity, M>
implements App<Mu<E>, M> {
    private final TriggerWithResult<E, M> trigger;

    public static <E extends LivingEntity, M> BehaviorBuilder<E, M> unbox(App<Mu<E>, M> app) {
        return (BehaviorBuilder)app;
    }

    public static <E extends LivingEntity> Instance<E> instance() {
        return new Instance();
    }

    public static <E extends LivingEntity> OneShot<E> create(Function<Instance<E>, ? extends App<Mu<E>, Trigger<E>>> function) {
        final TriggerWithResult<E, Trigger<E>> triggerWithResult = BehaviorBuilder.get(function.apply(BehaviorBuilder.instance()));
        return new OneShot<E>(){

            @Override
            public boolean trigger(ServerLevel serverLevel, E e, long l) {
                Trigger trigger = (Trigger)triggerWithResult.tryTrigger(serverLevel, e, l);
                if (trigger == null) {
                    return false;
                }
                return trigger.trigger(serverLevel, e, l);
            }

            @Override
            public String debugString() {
                return "OneShot[" + triggerWithResult.debugString() + "]";
            }

            public String toString() {
                return this.debugString();
            }
        };
    }

    public static <E extends LivingEntity> OneShot<E> sequence(Trigger<? super E> trigger, Trigger<? super E> trigger2) {
        return BehaviorBuilder.create((Instance<E> instance) -> instance.group(instance.ifTriggered(trigger)).apply((Applicative)instance, unit -> trigger2::trigger));
    }

    public static <E extends LivingEntity> OneShot<E> triggerIf(Predicate<E> predicate, OneShot<? super E> oneShot) {
        return BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(predicate), oneShot);
    }

    public static <E extends LivingEntity> OneShot<E> triggerIf(Predicate<E> predicate) {
        return BehaviorBuilder.create((Instance<E> instance) -> instance.point((serverLevel, livingEntity, l) -> predicate.test(livingEntity)));
    }

    public static <E extends LivingEntity> OneShot<E> triggerIf(BiPredicate<ServerLevel, E> biPredicate) {
        return BehaviorBuilder.create((Instance<E> instance) -> instance.point((serverLevel, livingEntity, l) -> biPredicate.test(serverLevel, livingEntity)));
    }

    static <E extends LivingEntity, M> TriggerWithResult<E, M> get(App<Mu<E>, M> app) {
        return BehaviorBuilder.unbox(app).trigger;
    }

    BehaviorBuilder(TriggerWithResult<E, M> triggerWithResult) {
        this.trigger = triggerWithResult;
    }

    static <E extends LivingEntity, M> BehaviorBuilder<E, M> create(TriggerWithResult<E, M> triggerWithResult) {
        return new BehaviorBuilder<E, M>(triggerWithResult);
    }

    public static final class Instance<E extends LivingEntity>
    implements Applicative<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, Mu<E>> {
        public <Value> Optional<Value> tryGet(MemoryAccessor<OptionalBox.Mu, Value> memoryAccessor) {
            return OptionalBox.unbox(memoryAccessor.value());
        }

        public <Value> Value get(MemoryAccessor<IdF.Mu, Value> memoryAccessor) {
            return (Value)IdF.get(memoryAccessor.value());
        }

        public <Value> BehaviorBuilder<E, MemoryAccessor<OptionalBox.Mu, Value>> registered(MemoryModuleType<Value> memoryModuleType) {
            return new PureMemory(new MemoryCondition.Registered<Value>(memoryModuleType));
        }

        public <Value> BehaviorBuilder<E, MemoryAccessor<IdF.Mu, Value>> present(MemoryModuleType<Value> memoryModuleType) {
            return new PureMemory(new MemoryCondition.Present<Value>(memoryModuleType));
        }

        public <Value> BehaviorBuilder<E, MemoryAccessor<Const.Mu<Unit>, Value>> absent(MemoryModuleType<Value> memoryModuleType) {
            return new PureMemory(new MemoryCondition.Absent<Value>(memoryModuleType));
        }

        public BehaviorBuilder<E, Unit> ifTriggered(Trigger<? super E> trigger) {
            return new TriggerWrapper<E>(trigger);
        }

        public <A> BehaviorBuilder<E, A> point(A a) {
            return new Constant(a);
        }

        public <A> BehaviorBuilder<E, A> point(Supplier<String> supplier, A a) {
            return new Constant(a, supplier);
        }

        public <A, R> Function<App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, A>, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, R>> lift1(App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, Function<A, R>> app) {
            return app2 -> {
                final TriggerWithResult triggerWithResult = BehaviorBuilder.get(app2);
                final TriggerWithResult triggerWithResult2 = BehaviorBuilder.get(app);
                return BehaviorBuilder.create(new TriggerWithResult<E, R>(this){

                    @Override
                    public R tryTrigger(ServerLevel serverLevel, E e, long l) {
                        Object r = triggerWithResult.tryTrigger(serverLevel, e, l);
                        if (r == null) {
                            return null;
                        }
                        Function function = (Function)triggerWithResult2.tryTrigger(serverLevel, e, l);
                        if (function == null) {
                            return null;
                        }
                        return function.apply(r);
                    }

                    @Override
                    public String debugString() {
                        return triggerWithResult2.debugString() + " * " + triggerWithResult.debugString();
                    }

                    public String toString() {
                        return this.debugString();
                    }
                });
            };
        }

        public <T, R> BehaviorBuilder<E, R> map(final Function<? super T, ? extends R> function, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T> app) {
            final TriggerWithResult<E, T> triggerWithResult = BehaviorBuilder.get(app);
            return BehaviorBuilder.create(new TriggerWithResult<E, R>(this){

                @Override
                public R tryTrigger(ServerLevel serverLevel, E e, long l) {
                    Object r = triggerWithResult.tryTrigger(serverLevel, e, l);
                    if (r == null) {
                        return null;
                    }
                    return function.apply(r);
                }

                @Override
                public String debugString() {
                    return triggerWithResult.debugString() + ".map[" + String.valueOf(function) + "]";
                }

                public String toString() {
                    return this.debugString();
                }
            });
        }

        public <A, B, R> BehaviorBuilder<E, R> ap2(App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, BiFunction<A, B, R>> app, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, A> app2, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, B> app3) {
            final TriggerWithResult<E, A> triggerWithResult = BehaviorBuilder.get(app2);
            final TriggerWithResult<E, B> triggerWithResult2 = BehaviorBuilder.get(app3);
            final TriggerWithResult<E, BiFunction<A, B, R>> triggerWithResult3 = BehaviorBuilder.get(app);
            return BehaviorBuilder.create(new TriggerWithResult<E, R>(this){

                @Override
                public R tryTrigger(ServerLevel serverLevel, E e, long l) {
                    Object r = triggerWithResult.tryTrigger(serverLevel, e, l);
                    if (r == null) {
                        return null;
                    }
                    Object r2 = triggerWithResult2.tryTrigger(serverLevel, e, l);
                    if (r2 == null) {
                        return null;
                    }
                    BiFunction biFunction = (BiFunction)triggerWithResult3.tryTrigger(serverLevel, e, l);
                    if (biFunction == null) {
                        return null;
                    }
                    return biFunction.apply(r, r2);
                }

                @Override
                public String debugString() {
                    return triggerWithResult3.debugString() + " * " + triggerWithResult.debugString() + " * " + triggerWithResult2.debugString();
                }

                public String toString() {
                    return this.debugString();
                }
            });
        }

        public <T1, T2, T3, R> BehaviorBuilder<E, R> ap3(App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, Function3<T1, T2, T3, R>> app, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T1> app2, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T2> app3, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T3> app4) {
            final TriggerWithResult<E, T1> triggerWithResult = BehaviorBuilder.get(app2);
            final TriggerWithResult<E, T2> triggerWithResult2 = BehaviorBuilder.get(app3);
            final TriggerWithResult<E, T3> triggerWithResult3 = BehaviorBuilder.get(app4);
            final TriggerWithResult<E, Function3<T1, T2, T3, R>> triggerWithResult4 = BehaviorBuilder.get(app);
            return BehaviorBuilder.create(new TriggerWithResult<E, R>(this){

                @Override
                public R tryTrigger(ServerLevel serverLevel, E e, long l) {
                    Object r = triggerWithResult.tryTrigger(serverLevel, e, l);
                    if (r == null) {
                        return null;
                    }
                    Object r2 = triggerWithResult2.tryTrigger(serverLevel, e, l);
                    if (r2 == null) {
                        return null;
                    }
                    Object r3 = triggerWithResult3.tryTrigger(serverLevel, e, l);
                    if (r3 == null) {
                        return null;
                    }
                    Function3 function3 = (Function3)triggerWithResult4.tryTrigger(serverLevel, e, l);
                    if (function3 == null) {
                        return null;
                    }
                    return function3.apply(r, r2, r3);
                }

                @Override
                public String debugString() {
                    return triggerWithResult4.debugString() + " * " + triggerWithResult.debugString() + " * " + triggerWithResult2.debugString() + " * " + triggerWithResult3.debugString();
                }

                public String toString() {
                    return this.debugString();
                }
            });
        }

        public <T1, T2, T3, T4, R> BehaviorBuilder<E, R> ap4(App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, Function4<T1, T2, T3, T4, R>> app, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T1> app2, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T2> app3, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T3> app4, App<net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder$Mu<E>, T4> app5) {
            final TriggerWithResult<E, T1> triggerWithResult = BehaviorBuilder.get(app2);
            final TriggerWithResult<E, T2> triggerWithResult2 = BehaviorBuilder.get(app3);
            final TriggerWithResult<E, T3> triggerWithResult3 = BehaviorBuilder.get(app4);
            final TriggerWithResult<E, T4> triggerWithResult4 = BehaviorBuilder.get(app5);
            final TriggerWithResult<E, Function4<T1, T2, T3, T4, R>> triggerWithResult5 = BehaviorBuilder.get(app);
            return BehaviorBuilder.create(new TriggerWithResult<E, R>(this){

                @Override
                public R tryTrigger(ServerLevel serverLevel, E e, long l) {
                    Object r = triggerWithResult.tryTrigger(serverLevel, e, l);
                    if (r == null) {
                        return null;
                    }
                    Object r2 = triggerWithResult2.tryTrigger(serverLevel, e, l);
                    if (r2 == null) {
                        return null;
                    }
                    Object r3 = triggerWithResult3.tryTrigger(serverLevel, e, l);
                    if (r3 == null) {
                        return null;
                    }
                    Object r4 = triggerWithResult4.tryTrigger(serverLevel, e, l);
                    if (r4 == null) {
                        return null;
                    }
                    Function4 function4 = (Function4)triggerWithResult5.tryTrigger(serverLevel, e, l);
                    if (function4 == null) {
                        return null;
                    }
                    return function4.apply(r, r2, r3, r4);
                }

                @Override
                public String debugString() {
                    return triggerWithResult5.debugString() + " * " + triggerWithResult.debugString() + " * " + triggerWithResult2.debugString() + " * " + triggerWithResult3.debugString() + " * " + triggerWithResult4.debugString();
                }

                public String toString() {
                    return this.debugString();
                }
            });
        }

        public /* synthetic */ App ap4(App app, App app2, App app3, App app4, App app5) {
            return this.ap4(app, app2, app3, app4, app5);
        }

        public /* synthetic */ App ap3(App app, App app2, App app3, App app4) {
            return this.ap3(app, app2, app3, app4);
        }

        public /* synthetic */ App ap2(App app, App app2, App app3) {
            return this.ap2(app, app2, app3);
        }

        public /* synthetic */ App point(Object object) {
            return this.point(object);
        }

        public /* synthetic */ App map(Function function, App app) {
            return this.map(function, app);
        }

        static final class Mu<E extends LivingEntity>
        implements Applicative.Mu {
            private Mu() {
            }
        }
    }

    static interface TriggerWithResult<E extends LivingEntity, R> {
        @Nullable
        public R tryTrigger(ServerLevel var1, E var2, long var3);

        public String debugString();
    }

    static final class TriggerWrapper<E extends LivingEntity>
    extends BehaviorBuilder<E, Unit> {
        TriggerWrapper(final Trigger<? super E> trigger) {
            super(new TriggerWithResult<E, Unit>(){

                @Override
                @Nullable
                public Unit tryTrigger(ServerLevel serverLevel, E e, long l) {
                    return trigger.trigger(serverLevel, e, l) ? Unit.INSTANCE : null;
                }

                @Override
                public String debugString() {
                    return "T[" + String.valueOf(trigger) + "]";
                }

                @Override
                @Nullable
                public /* synthetic */ Object tryTrigger(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
                    return this.tryTrigger(serverLevel, (Object)livingEntity, l);
                }
            });
        }
    }

    static final class Constant<E extends LivingEntity, A>
    extends BehaviorBuilder<E, A> {
        Constant(A a) {
            this(a, () -> "C[" + String.valueOf(a) + "]");
        }

        Constant(final A a, final Supplier<String> supplier) {
            super(new TriggerWithResult<E, A>(){

                @Override
                public A tryTrigger(ServerLevel serverLevel, E e, long l) {
                    return a;
                }

                @Override
                public String debugString() {
                    return (String)supplier.get();
                }

                public String toString() {
                    return this.debugString();
                }
            });
        }
    }

    static final class PureMemory<E extends LivingEntity, F extends K1, Value>
    extends BehaviorBuilder<E, MemoryAccessor<F, Value>> {
        PureMemory(final MemoryCondition<F, Value> memoryCondition) {
            super(new TriggerWithResult<E, MemoryAccessor<F, Value>>(){

                @Override
                public MemoryAccessor<F, Value> tryTrigger(ServerLevel serverLevel, E e, long l) {
                    Brain<?> brain = ((LivingEntity)e).getBrain();
                    Optional optional = brain.getMemoryInternal(memoryCondition.memory());
                    if (optional == null) {
                        return null;
                    }
                    return memoryCondition.createAccessor(brain, optional);
                }

                @Override
                public String debugString() {
                    return "M[" + String.valueOf(memoryCondition) + "]";
                }

                public String toString() {
                    return this.debugString();
                }

                @Override
                public /* synthetic */ Object tryTrigger(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
                    return this.tryTrigger(serverLevel, livingEntity, l);
                }
            });
        }
    }

    public static final class Mu<E extends LivingEntity>
    implements K1 {
    }
}

