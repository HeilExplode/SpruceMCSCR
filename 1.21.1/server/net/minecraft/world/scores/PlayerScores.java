/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.scores;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;

class PlayerScores {
    private final Reference2ObjectOpenHashMap<Objective, Score> scores = new Reference2ObjectOpenHashMap(16, 0.5f);

    PlayerScores() {
    }

    @Nullable
    public Score get(Objective objective) {
        return (Score)this.scores.get((Object)objective);
    }

    public Score getOrCreate(Objective objective, Consumer<Score> consumer) {
        return (Score)this.scores.computeIfAbsent((Object)objective, object -> {
            Score score = new Score();
            consumer.accept(score);
            return score;
        });
    }

    public boolean remove(Objective objective) {
        return this.scores.remove((Object)objective) != null;
    }

    public boolean hasScores() {
        return !this.scores.isEmpty();
    }

    public Object2IntMap<Objective> listScores() {
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        this.scores.forEach((arg_0, arg_1) -> PlayerScores.lambda$listScores$1((Object2IntMap)object2IntOpenHashMap, arg_0, arg_1));
        return object2IntOpenHashMap;
    }

    void setScore(Objective objective, Score score) {
        this.scores.put((Object)objective, (Object)score);
    }

    Map<Objective, Score> listRawScores() {
        return Collections.unmodifiableMap(this.scores);
    }

    private static /* synthetic */ void lambda$listScores$1(Object2IntMap object2IntMap, Objective objective, Score score) {
        object2IntMap.put((Object)objective, score.value());
    }
}

