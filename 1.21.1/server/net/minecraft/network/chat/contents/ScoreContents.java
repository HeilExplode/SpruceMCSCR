/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;

public class ScoreContents
implements ComponentContents {
    public static final MapCodec<ScoreContents> INNER_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("name").forGetter(ScoreContents::getName), (App)Codec.STRING.fieldOf("objective").forGetter(ScoreContents::getObjective)).apply((Applicative)instance, ScoreContents::new));
    public static final MapCodec<ScoreContents> CODEC = INNER_CODEC.fieldOf("score");
    public static final ComponentContents.Type<ScoreContents> TYPE = new ComponentContents.Type<ScoreContents>(CODEC, "score");
    private final String name;
    @Nullable
    private final EntitySelector selector;
    private final String objective;

    @Nullable
    private static EntitySelector parseSelector(String string) {
        try {
            return new EntitySelectorParser(new StringReader(string), true).parse();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    public ScoreContents(String string, String string2) {
        this.name = string;
        this.selector = ScoreContents.parseSelector(string);
        this.objective = string2;
    }

    @Override
    public ComponentContents.Type<?> type() {
        return TYPE;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public EntitySelector getSelector() {
        return this.selector;
    }

    public String getObjective() {
        return this.objective;
    }

    private ScoreHolder findTargetName(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        List<? extends Entity> list;
        if (this.selector != null && !(list = this.selector.findEntities(commandSourceStack)).isEmpty()) {
            if (list.size() != 1) {
                throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
            }
            return list.get(0);
        }
        return ScoreHolder.forNameOnly(this.name);
    }

    private MutableComponent getScore(ScoreHolder scoreHolder, CommandSourceStack commandSourceStack) {
        ReadOnlyScoreInfo readOnlyScoreInfo;
        ServerScoreboard serverScoreboard;
        Objective objective;
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (minecraftServer != null && (objective = (serverScoreboard = minecraftServer.getScoreboard()).getObjective(this.objective)) != null && (readOnlyScoreInfo = serverScoreboard.getPlayerScoreInfo(scoreHolder, objective)) != null) {
            return readOnlyScoreInfo.formatValue(objective.numberFormatOrDefault(StyledFormat.NO_STYLE));
        }
        return Component.empty();
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int n) throws CommandSyntaxException {
        if (commandSourceStack == null) {
            return Component.empty();
        }
        ScoreHolder scoreHolder = this.findTargetName(commandSourceStack);
        ScoreHolder scoreHolder2 = entity != null && scoreHolder.equals(ScoreHolder.WILDCARD) ? entity : scoreHolder;
        return this.getScore(scoreHolder2, commandSourceStack);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ScoreContents)) return false;
        ScoreContents scoreContents = (ScoreContents)object;
        if (!this.name.equals(scoreContents.name)) return false;
        if (!this.objective.equals(scoreContents.objective)) return false;
        return true;
    }

    public int hashCode() {
        int n = this.name.hashCode();
        n = 31 * n + this.objective.hashCode();
        return n;
    }

    public String toString() {
        return "score{name='" + this.name + "', objective='" + this.objective + "'}";
    }
}

