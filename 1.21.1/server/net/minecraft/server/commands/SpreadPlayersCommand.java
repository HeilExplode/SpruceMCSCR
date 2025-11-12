/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType
 */
package net.minecraft.server.commands;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.PlayerTeam;

public class SpreadPlayersCommand {
    private static final int MAX_ITERATION_COUNT = 10000;
    private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_TEAMS = new Dynamic4CommandExceptionType((object, object2, object3, object4) -> Component.translatableEscape("commands.spreadplayers.failed.teams", object, object2, object3, object4));
    private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_ENTITIES = new Dynamic4CommandExceptionType((object, object2, object3, object4) -> Component.translatableEscape("commands.spreadplayers.failed.entities", object, object2, object3, object4));
    private static final Dynamic2CommandExceptionType ERROR_INVALID_MAX_HEIGHT = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.spreadplayers.failed.invalid.height", object, object2));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spreadplayers").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("center", Vec2Argument.vec2()).then(Commands.argument("spreadDistance", FloatArgumentType.floatArg((float)0.0f)).then(((RequiredArgumentBuilder)Commands.argument("maxRange", FloatArgumentType.floatArg((float)1.0f)).then(Commands.argument("respectTeams", BoolArgumentType.bool()).then(Commands.argument("targets", EntityArgument.entities()).executes(commandContext -> SpreadPlayersCommand.spreadPlayers((CommandSourceStack)commandContext.getSource(), Vec2Argument.getVec2((CommandContext<CommandSourceStack>)commandContext, "center"), FloatArgumentType.getFloat((CommandContext)commandContext, (String)"spreadDistance"), FloatArgumentType.getFloat((CommandContext)commandContext, (String)"maxRange"), ((CommandSourceStack)commandContext.getSource()).getLevel().getMaxBuildHeight(), BoolArgumentType.getBool((CommandContext)commandContext, (String)"respectTeams"), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets")))))).then(Commands.literal("under").then(Commands.argument("maxHeight", IntegerArgumentType.integer()).then(Commands.argument("respectTeams", BoolArgumentType.bool()).then(Commands.argument("targets", EntityArgument.entities()).executes(commandContext -> SpreadPlayersCommand.spreadPlayers((CommandSourceStack)commandContext.getSource(), Vec2Argument.getVec2((CommandContext<CommandSourceStack>)commandContext, "center"), FloatArgumentType.getFloat((CommandContext)commandContext, (String)"spreadDistance"), FloatArgumentType.getFloat((CommandContext)commandContext, (String)"maxRange"), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"maxHeight"), BoolArgumentType.getBool((CommandContext)commandContext, (String)"respectTeams"), EntityArgument.getEntities((CommandContext<CommandSourceStack>)commandContext, "targets")))))))))));
    }

    private static int spreadPlayers(CommandSourceStack commandSourceStack, Vec2 vec2, float f, float f2, int n, boolean bl, Collection<? extends Entity> collection) throws CommandSyntaxException {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        int n2 = serverLevel.getMinBuildHeight();
        if (n < n2) {
            throw ERROR_INVALID_MAX_HEIGHT.create((Object)n, (Object)n2);
        }
        RandomSource randomSource = RandomSource.create();
        double d = vec2.x - f2;
        double d2 = vec2.y - f2;
        double d3 = vec2.x + f2;
        double d4 = vec2.y + f2;
        Position[] positionArray = SpreadPlayersCommand.createInitialPositions(randomSource, bl ? SpreadPlayersCommand.getNumberOfTeams(collection) : collection.size(), d, d2, d3, d4);
        SpreadPlayersCommand.spreadPositions(vec2, f, serverLevel, randomSource, d, d2, d3, d4, n, positionArray, bl);
        double d5 = SpreadPlayersCommand.setPlayerPositions(collection, serverLevel, positionArray, n, bl);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.spreadplayers.success." + (bl ? "teams" : "entities"), positionArray.length, Float.valueOf(vec2.x), Float.valueOf(vec2.y), String.format(Locale.ROOT, "%.2f", d5)), true);
        return positionArray.length;
    }

    private static int getNumberOfTeams(Collection<? extends Entity> collection) {
        HashSet hashSet = Sets.newHashSet();
        for (Entity entity : collection) {
            if (entity instanceof Player) {
                hashSet.add(entity.getTeam());
                continue;
            }
            hashSet.add(null);
        }
        return hashSet.size();
    }

    private static void spreadPositions(Vec2 vec2, double d, ServerLevel serverLevel, RandomSource randomSource, double d2, double d3, double d4, double d5, int n, Position[] positionArray, boolean bl) throws CommandSyntaxException {
        int n2;
        boolean bl2 = true;
        double d6 = 3.4028234663852886E38;
        for (n2 = 0; n2 < 10000 && bl2; ++n2) {
            bl2 = false;
            d6 = 3.4028234663852886E38;
            for (int i = 0; i < positionArray.length; ++i) {
                Position position = positionArray[i];
                int n3 = 0;
                Position position2 = new Position();
                for (int j = 0; j < positionArray.length; ++j) {
                    if (i == j) continue;
                    Position position3 = positionArray[j];
                    double d7 = position.dist(position3);
                    d6 = Math.min(d7, d6);
                    if (!(d7 < d)) continue;
                    ++n3;
                    position2.x += position3.x - position.x;
                    position2.z += position3.z - position.z;
                }
                if (n3 > 0) {
                    position2.x /= (double)n3;
                    position2.z /= (double)n3;
                    double d8 = position2.getLength();
                    if (d8 > 0.0) {
                        position2.normalize();
                        position.moveAway(position2);
                    } else {
                        position.randomize(randomSource, d2, d3, d4, d5);
                    }
                    bl2 = true;
                }
                if (!position.clamp(d2, d3, d4, d5)) continue;
                bl2 = true;
            }
            if (bl2) continue;
            for (Position position2 : positionArray) {
                if (position2.isSafe(serverLevel, n)) continue;
                position2.randomize(randomSource, d2, d3, d4, d5);
                bl2 = true;
            }
        }
        if (d6 == 3.4028234663852886E38) {
            d6 = 0.0;
        }
        if (n2 >= 10000) {
            if (bl) {
                throw ERROR_FAILED_TO_SPREAD_TEAMS.create((Object)positionArray.length, (Object)Float.valueOf(vec2.x), (Object)Float.valueOf(vec2.y), (Object)String.format(Locale.ROOT, "%.2f", d6));
            }
            throw ERROR_FAILED_TO_SPREAD_ENTITIES.create((Object)positionArray.length, (Object)Float.valueOf(vec2.x), (Object)Float.valueOf(vec2.y), (Object)String.format(Locale.ROOT, "%.2f", d6));
        }
    }

    private static double setPlayerPositions(Collection<? extends Entity> collection, ServerLevel serverLevel, Position[] positionArray, int n, boolean bl) {
        double d = 0.0;
        int n2 = 0;
        HashMap hashMap = Maps.newHashMap();
        for (Entity entity : collection) {
            Position position;
            if (bl) {
                PlayerTeam playerTeam;
                PlayerTeam playerTeam2 = playerTeam = entity instanceof Player ? entity.getTeam() : null;
                if (!hashMap.containsKey(playerTeam)) {
                    hashMap.put(playerTeam, positionArray[n2++]);
                }
                position = (Position)hashMap.get(playerTeam);
            } else {
                position = positionArray[n2++];
            }
            entity.teleportTo(serverLevel, (double)Mth.floor(position.x) + 0.5, position.getSpawnY(serverLevel, n), (double)Mth.floor(position.z) + 0.5, Set.of(), entity.getYRot(), entity.getXRot());
            double d2 = Double.MAX_VALUE;
            for (Position position2 : positionArray) {
                if (position == position2) continue;
                double d3 = position.dist(position2);
                d2 = Math.min(d3, d2);
            }
            d += d2;
        }
        if (collection.size() < 2) {
            return 0.0;
        }
        return d /= (double)collection.size();
    }

    private static Position[] createInitialPositions(RandomSource randomSource, int n, double d, double d2, double d3, double d4) {
        Position[] positionArray = new Position[n];
        for (int i = 0; i < positionArray.length; ++i) {
            Position position = new Position();
            position.randomize(randomSource, d, d2, d3, d4);
            positionArray[i] = position;
        }
        return positionArray;
    }

    static class Position {
        double x;
        double z;

        Position() {
        }

        double dist(Position position) {
            double d = this.x - position.x;
            double d2 = this.z - position.z;
            return Math.sqrt(d * d + d2 * d2);
        }

        void normalize() {
            double d = this.getLength();
            this.x /= d;
            this.z /= d;
        }

        double getLength() {
            return Math.sqrt(this.x * this.x + this.z * this.z);
        }

        public void moveAway(Position position) {
            this.x -= position.x;
            this.z -= position.z;
        }

        public boolean clamp(double d, double d2, double d3, double d4) {
            boolean bl = false;
            if (this.x < d) {
                this.x = d;
                bl = true;
            } else if (this.x > d3) {
                this.x = d3;
                bl = true;
            }
            if (this.z < d2) {
                this.z = d2;
                bl = true;
            } else if (this.z > d4) {
                this.z = d4;
                bl = true;
            }
            return bl;
        }

        public int getSpawnY(BlockGetter blockGetter, int n) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.x, (double)(n + 1), this.z);
            boolean bl = blockGetter.getBlockState(mutableBlockPos).isAir();
            mutableBlockPos.move(Direction.DOWN);
            boolean bl2 = blockGetter.getBlockState(mutableBlockPos).isAir();
            while (mutableBlockPos.getY() > blockGetter.getMinBuildHeight()) {
                mutableBlockPos.move(Direction.DOWN);
                boolean bl3 = blockGetter.getBlockState(mutableBlockPos).isAir();
                if (!bl3 && bl2 && bl) {
                    return mutableBlockPos.getY() + 1;
                }
                bl = bl2;
                bl2 = bl3;
            }
            return n + 1;
        }

        public boolean isSafe(BlockGetter blockGetter, int n) {
            BlockPos blockPos = BlockPos.containing(this.x, this.getSpawnY(blockGetter, n) - 1, this.z);
            BlockState blockState = blockGetter.getBlockState(blockPos);
            return blockPos.getY() < n && !blockState.liquid() && !blockState.is(BlockTags.FIRE);
        }

        public void randomize(RandomSource randomSource, double d, double d2, double d3, double d4) {
            this.x = Mth.nextDouble(randomSource, d, d3);
            this.z = Mth.nextDouble(randomSource, d2, d4);
        }
    }
}

