/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity.trialspawner;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public interface PlayerDetector {
    public static final PlayerDetector NO_CREATIVE_PLAYERS = (serverLevel, entitySelector, blockPos, d, bl) -> entitySelector.getPlayers(serverLevel, player -> player.blockPosition().closerThan(blockPos, d) && !player.isCreative() && !player.isSpectator()).stream().filter(player -> !bl || PlayerDetector.inLineOfSight(serverLevel, blockPos.getCenter(), player.getEyePosition())).map(Entity::getUUID).toList();
    public static final PlayerDetector INCLUDING_CREATIVE_PLAYERS = (serverLevel, entitySelector, blockPos, d, bl) -> entitySelector.getPlayers(serverLevel, player -> player.blockPosition().closerThan(blockPos, d) && !player.isSpectator()).stream().filter(player -> !bl || PlayerDetector.inLineOfSight(serverLevel, blockPos.getCenter(), player.getEyePosition())).map(Entity::getUUID).toList();
    public static final PlayerDetector SHEEP = (serverLevel, entitySelector, blockPos, d, bl) -> {
        AABB aABB = new AABB(blockPos).inflate(d);
        return entitySelector.getEntities(serverLevel, EntityType.SHEEP, aABB, LivingEntity::isAlive).stream().filter(sheep -> !bl || PlayerDetector.inLineOfSight(serverLevel, blockPos.getCenter(), sheep.getEyePosition())).map(Entity::getUUID).toList();
    };

    public List<UUID> detect(ServerLevel var1, EntitySelector var2, BlockPos var3, double var4, boolean var6);

    private static boolean inLineOfSight(Level level, Vec3 vec3, Vec3 vec32) {
        BlockHitResult blockHitResult = level.clip(new ClipContext(vec32, vec3, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return blockHitResult.getBlockPos().equals(BlockPos.containing(vec3)) || blockHitResult.getType() == HitResult.Type.MISS;
    }

    public static interface EntitySelector {
        public static final EntitySelector SELECT_FROM_LEVEL = new EntitySelector(){

            public List<ServerPlayer> getPlayers(ServerLevel serverLevel, Predicate<? super Player> predicate) {
                return serverLevel.getPlayers(predicate);
            }

            @Override
            public <T extends Entity> List<T> getEntities(ServerLevel serverLevel, EntityTypeTest<Entity, T> entityTypeTest, AABB aABB, Predicate<? super T> predicate) {
                return serverLevel.getEntities(entityTypeTest, aABB, predicate);
            }
        };

        public List<? extends Player> getPlayers(ServerLevel var1, Predicate<? super Player> var2);

        public <T extends Entity> List<T> getEntities(ServerLevel var1, EntityTypeTest<Entity, T> var2, AABB var3, Predicate<? super T> var4);

        public static EntitySelector onlySelectPlayer(Player player) {
            return EntitySelector.onlySelectPlayers(List.of(player));
        }

        public static EntitySelector onlySelectPlayers(final List<Player> list) {
            return new EntitySelector(){

                public List<Player> getPlayers(ServerLevel serverLevel, Predicate<? super Player> predicate) {
                    return list.stream().filter(predicate).toList();
                }

                @Override
                public <T extends Entity> List<T> getEntities(ServerLevel serverLevel, EntityTypeTest<Entity, T> entityTypeTest, AABB aABB, Predicate<? super T> predicate) {
                    return list.stream().map(entityTypeTest::tryCast).filter(Objects::nonNull).filter(predicate).toList();
                }
            };
        }
    }
}

