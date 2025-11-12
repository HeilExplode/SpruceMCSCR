/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 */
package net.minecraft.advancements;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.network.FriendlyByteBuf;

public record AdvancementRequirements(List<List<String>> requirements) {
    public static final Codec<AdvancementRequirements> CODEC = Codec.STRING.listOf().listOf().xmap(AdvancementRequirements::new, AdvancementRequirements::requirements);
    public static final AdvancementRequirements EMPTY = new AdvancementRequirements(List.of());

    public AdvancementRequirements(FriendlyByteBuf friendlyByteBuf2) {
        this(friendlyByteBuf2.readList(friendlyByteBuf -> friendlyByteBuf.readList(FriendlyByteBuf::readUtf)));
    }

    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeCollection(this.requirements, (friendlyByteBuf, list) -> friendlyByteBuf.writeCollection(list, FriendlyByteBuf::writeUtf));
    }

    public static AdvancementRequirements allOf(Collection<String> collection) {
        return new AdvancementRequirements(collection.stream().map(List::of).toList());
    }

    public static AdvancementRequirements anyOf(Collection<String> collection) {
        return new AdvancementRequirements(List.of(List.copyOf(collection)));
    }

    public int size() {
        return this.requirements.size();
    }

    public boolean test(Predicate<String> predicate) {
        if (this.requirements.isEmpty()) {
            return false;
        }
        for (List<String> list : this.requirements) {
            if (AdvancementRequirements.anyMatch(list, predicate)) continue;
            return false;
        }
        return true;
    }

    public int count(Predicate<String> predicate) {
        int n = 0;
        for (List<String> list : this.requirements) {
            if (!AdvancementRequirements.anyMatch(list, predicate)) continue;
            ++n;
        }
        return n;
    }

    private static boolean anyMatch(List<String> list, Predicate<String> predicate) {
        for (String string : list) {
            if (!predicate.test(string)) continue;
            return true;
        }
        return false;
    }

    public DataResult<AdvancementRequirements> validate(Set<String> set) {
        ObjectOpenHashSet objectOpenHashSet = new ObjectOpenHashSet();
        for (List<String> setView : this.requirements) {
            if (setView.isEmpty() && set.isEmpty()) {
                return DataResult.error(() -> "Requirement entry cannot be empty");
            }
            objectOpenHashSet.addAll(setView);
        }
        if (!set.equals(objectOpenHashSet)) {
            Sets.SetView setView2 = Sets.difference(set, (Set)objectOpenHashSet);
            Sets.SetView setView = Sets.difference((Set)objectOpenHashSet, set);
            return DataResult.error(() -> AdvancementRequirements.lambda$validate$3((Set)setView2, (Set)setView));
        }
        return DataResult.success((Object)this);
    }

    public boolean isEmpty() {
        return this.requirements.isEmpty();
    }

    @Override
    public String toString() {
        return this.requirements.toString();
    }

    public Set<String> names() {
        ObjectOpenHashSet objectOpenHashSet = new ObjectOpenHashSet();
        for (List<String> list : this.requirements) {
            objectOpenHashSet.addAll(list);
        }
        return objectOpenHashSet;
    }

    private static /* synthetic */ String lambda$validate$3(Set set, Set set2) {
        return "Advancement completion requirements did not exactly match specified criteria. Missing: " + String.valueOf(set) + ". Unknown: " + String.valueOf(set2);
    }

    public static interface Strategy {
        public static final Strategy AND = AdvancementRequirements::allOf;
        public static final Strategy OR = AdvancementRequirements::anyOf;

        public AdvancementRequirements create(Collection<String> var1);
    }
}

