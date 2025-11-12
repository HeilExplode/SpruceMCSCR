/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;

public abstract class SimpleCriterionTrigger<T extends SimpleInstance>
implements CriterionTrigger<T> {
    private final Map<PlayerAdvancements, Set<CriterionTrigger.Listener<T>>> players = Maps.newIdentityHashMap();

    @Override
    public final void addPlayerListener(PlayerAdvancements playerAdvancements2, CriterionTrigger.Listener<T> listener) {
        this.players.computeIfAbsent(playerAdvancements2, playerAdvancements -> Sets.newHashSet()).add(listener);
    }

    @Override
    public final void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<T> listener) {
        Set<CriterionTrigger.Listener<T>> set = this.players.get(playerAdvancements);
        if (set != null) {
            set.remove(listener);
            if (set.isEmpty()) {
                this.players.remove(playerAdvancements);
            }
        }
    }

    @Override
    public final void removePlayerListeners(PlayerAdvancements playerAdvancements) {
        this.players.remove(playerAdvancements);
    }

    protected void trigger(ServerPlayer serverPlayer, Predicate<T> predicate) {
        PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
        Set<CriterionTrigger.Listener<T>> set = this.players.get(playerAdvancements);
        if (set == null || set.isEmpty()) {
            return;
        }
        LootContext lootContext = EntityPredicate.createContext(serverPlayer, serverPlayer);
        List list = null;
        for (CriterionTrigger.Listener<Object> listener : set) {
            Optional<ContextAwarePredicate> optional;
            SimpleInstance simpleInstance = (SimpleInstance)listener.trigger();
            if (!predicate.test(simpleInstance) || !(optional = simpleInstance.player()).isEmpty() && !optional.get().matches(lootContext)) continue;
            if (list == null) {
                list = Lists.newArrayList();
            }
            list.add(listener);
        }
        if (list != null) {
            for (CriterionTrigger.Listener<Object> listener : list) {
                listener.run(playerAdvancements);
            }
        }
    }

    public static interface SimpleInstance
    extends CriterionTriggerInstance {
        @Override
        default public void validate(CriterionValidator criterionValidator) {
            criterionValidator.validateEntity(this.player(), ".player");
        }

        public Optional<ContextAwarePredicate> player();
    }
}

