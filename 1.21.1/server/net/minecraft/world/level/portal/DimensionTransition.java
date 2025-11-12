/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record DimensionTransition(ServerLevel newLevel, Vec3 pos, Vec3 speed, float yRot, float xRot, boolean missingRespawnBlock, PostDimensionTransition postDimensionTransition) {
    public static final PostDimensionTransition DO_NOTHING = entity -> {};
    public static final PostDimensionTransition PLAY_PORTAL_SOUND = DimensionTransition::playPortalSound;
    public static final PostDimensionTransition PLACE_PORTAL_TICKET = DimensionTransition::placePortalTicket;

    public DimensionTransition(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32, float f, float f2, PostDimensionTransition postDimensionTransition) {
        this(serverLevel, vec3, vec32, f, f2, false, postDimensionTransition);
    }

    public DimensionTransition(ServerLevel serverLevel, Entity entity, PostDimensionTransition postDimensionTransition) {
        this(serverLevel, DimensionTransition.findAdjustedSharedSpawnPos(serverLevel, entity), Vec3.ZERO, 0.0f, 0.0f, false, postDimensionTransition);
    }

    private static void playPortalSound(Entity entity) {
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
        }
    }

    private static void placePortalTicket(Entity entity) {
        entity.placePortalTicket(BlockPos.containing(entity.position()));
    }

    public static DimensionTransition missingRespawnBlock(ServerLevel serverLevel, Entity entity, PostDimensionTransition postDimensionTransition) {
        return new DimensionTransition(serverLevel, DimensionTransition.findAdjustedSharedSpawnPos(serverLevel, entity), Vec3.ZERO, 0.0f, 0.0f, true, postDimensionTransition);
    }

    private static Vec3 findAdjustedSharedSpawnPos(ServerLevel serverLevel, Entity entity) {
        return entity.adjustSpawnLocation(serverLevel, serverLevel.getSharedSpawnPos()).getBottomCenter();
    }

    @FunctionalInterface
    public static interface PostDimensionTransition {
        public void onTransition(Entity var1);

        default public PostDimensionTransition then(PostDimensionTransition postDimensionTransition) {
            return entity -> {
                this.onTransition(entity);
                postDimensionTransition.onTransition(entity);
            };
        }
    }
}

