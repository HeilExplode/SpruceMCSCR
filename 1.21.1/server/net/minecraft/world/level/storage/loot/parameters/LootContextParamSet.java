/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 */
package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class LootContextParamSet {
    private final Set<LootContextParam<?>> required;
    private final Set<LootContextParam<?>> all;

    LootContextParamSet(Set<LootContextParam<?>> set, Set<LootContextParam<?>> set2) {
        this.required = ImmutableSet.copyOf(set);
        this.all = ImmutableSet.copyOf((Collection)Sets.union(set, set2));
    }

    public boolean isAllowed(LootContextParam<?> lootContextParam) {
        return this.all.contains(lootContextParam);
    }

    public Set<LootContextParam<?>> getRequired() {
        return this.required;
    }

    public Set<LootContextParam<?>> getAllowed() {
        return this.all;
    }

    public String toString() {
        return "[" + Joiner.on((String)", ").join(this.all.stream().map(lootContextParam -> (this.required.contains(lootContextParam) ? "!" : "") + String.valueOf(lootContextParam.getName())).iterator()) + "]";
    }

    public void validateUser(ValidationContext validationContext, LootContextUser lootContextUser) {
        this.validateUser(validationContext.reporter(), lootContextUser);
    }

    public void validateUser(ProblemReporter problemReporter, LootContextUser lootContextUser) {
        Set<LootContextParam<?>> set = lootContextUser.getReferencedContextParams();
        Sets.SetView setView = Sets.difference(set, this.all);
        if (!setView.isEmpty()) {
            problemReporter.report("Parameters " + String.valueOf(setView) + " are not provided in this context");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<LootContextParam<?>> required = Sets.newIdentityHashSet();
        private final Set<LootContextParam<?>> optional = Sets.newIdentityHashSet();

        public Builder required(LootContextParam<?> lootContextParam) {
            if (this.optional.contains(lootContextParam)) {
                throw new IllegalArgumentException("Parameter " + String.valueOf(lootContextParam.getName()) + " is already optional");
            }
            this.required.add(lootContextParam);
            return this;
        }

        public Builder optional(LootContextParam<?> lootContextParam) {
            if (this.required.contains(lootContextParam)) {
                throw new IllegalArgumentException("Parameter " + String.valueOf(lootContextParam.getName()) + " is already required");
            }
            this.optional.add(lootContextParam);
            return this;
        }

        public LootContextParamSet build() {
            return new LootContextParamSet(this.required, this.optional);
        }
    }
}

