/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;

public record ScoreboardValue(ScoreboardNameProvider target, String score, float scale) implements NumberProvider
{
    public static final MapCodec<ScoreboardValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ScoreboardNameProviders.CODEC.fieldOf("target").forGetter(ScoreboardValue::target), (App)Codec.STRING.fieldOf("score").forGetter(ScoreboardValue::score), (App)Codec.FLOAT.fieldOf("scale").orElse((Object)Float.valueOf(1.0f)).forGetter(ScoreboardValue::scale)).apply((Applicative)instance, ScoreboardValue::new));

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.SCORE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.target.getReferencedContextParams();
    }

    public static ScoreboardValue fromScoreboard(LootContext.EntityTarget entityTarget, String string) {
        return ScoreboardValue.fromScoreboard(entityTarget, string, 1.0f);
    }

    public static ScoreboardValue fromScoreboard(LootContext.EntityTarget entityTarget, String string, float f) {
        return new ScoreboardValue(ContextScoreboardNameProvider.forTarget(entityTarget), string, f);
    }

    @Override
    public float getFloat(LootContext lootContext) {
        ScoreHolder scoreHolder = this.target.getScoreHolder(lootContext);
        if (scoreHolder == null) {
            return 0.0f;
        }
        ServerScoreboard serverScoreboard = lootContext.getLevel().getScoreboard();
        Objective objective = serverScoreboard.getObjective(this.score);
        if (objective == null) {
            return 0.0f;
        }
        ReadOnlyScoreInfo readOnlyScoreInfo = serverScoreboard.getPlayerScoreInfo(scoreHolder, objective);
        if (readOnlyScoreInfo == null) {
            return 0.0f;
        }
        return (float)readOnlyScoreInfo.value() * this.scale;
    }
}

