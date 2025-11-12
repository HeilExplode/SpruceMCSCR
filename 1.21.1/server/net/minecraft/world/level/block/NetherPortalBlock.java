/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class NetherPortalBlock
extends Block
implements Portal {
    public static final MapCodec<NetherPortalBlock> CODEC = NetherPortalBlock.simpleCodec(NetherPortalBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static final int AABB_OFFSET = 2;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public MapCodec<NetherPortalBlock> codec() {
        return CODEC;
    }

    public NetherPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch (blockState.getValue(AXIS)) {
            case Z: {
                return Z_AXIS_AABB;
            }
        }
        return X_AXIS_AABB;
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (serverLevel.dimensionType().natural() && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && randomSource.nextInt(2000) < serverLevel.getDifficulty().getId()) {
            ZombifiedPiglin zombifiedPiglin;
            while (serverLevel.getBlockState(blockPos).is(this)) {
                blockPos = blockPos.below();
            }
            if (serverLevel.getBlockState(blockPos).isValidSpawn(serverLevel, blockPos, EntityType.ZOMBIFIED_PIGLIN) && (zombifiedPiglin = EntityType.ZOMBIFIED_PIGLIN.spawn(serverLevel, blockPos.above(), MobSpawnType.STRUCTURE)) != null) {
                zombifiedPiglin.setPortalCooldown();
            }
        }
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        boolean bl;
        Direction.Axis axis = direction.getAxis();
        Direction.Axis axis2 = blockState.getValue(AXIS);
        boolean bl2 = bl = axis2 != axis && axis.isHorizontal();
        if (bl || blockState2.is(this) || new PortalShape(levelAccessor, blockPos, axis2).isComplete()) {
            return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, blockPos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel serverLevel, Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            return Math.max(1, serverLevel.getGameRules().getInt(player.getAbilities().invulnerable ? GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY : GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY));
        }
        return 0;
    }

    @Override
    @Nullable
    public DimensionTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos blockPos) {
        ResourceKey<Level> resourceKey = serverLevel.dimension() == Level.NETHER ? Level.OVERWORLD : Level.NETHER;
        ServerLevel serverLevel2 = serverLevel.getServer().getLevel(resourceKey);
        if (serverLevel2 == null) {
            return null;
        }
        boolean bl = serverLevel2.dimension() == Level.NETHER;
        WorldBorder worldBorder = serverLevel2.getWorldBorder();
        double d = DimensionType.getTeleportationScale(serverLevel.dimensionType(), serverLevel2.dimensionType());
        BlockPos blockPos2 = worldBorder.clampToBounds(entity.getX() * d, entity.getY(), entity.getZ() * d);
        return this.getExitPortal(serverLevel2, entity, blockPos, blockPos2, bl, worldBorder);
    }

    @Nullable
    private DimensionTransition getExitPortal(ServerLevel serverLevel, Entity entity2, BlockPos blockPos2, BlockPos blockPos3, boolean bl, WorldBorder worldBorder) {
        DimensionTransition.PostDimensionTransition postDimensionTransition;
        BlockUtil.FoundRectangle foundRectangle;
        Optional<BlockPos> optional = serverLevel.getPortalForcer().findClosestPortalPosition(blockPos3, bl, worldBorder);
        if (optional.isPresent()) {
            BlockPos blockPos4 = optional.get();
            BlockState blockState = serverLevel.getBlockState(blockPos4);
            foundRectangle = BlockUtil.getLargestRectangleAround(blockPos4, blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, blockPos -> serverLevel.getBlockState((BlockPos)blockPos) == blockState);
            postDimensionTransition = DimensionTransition.PLAY_PORTAL_SOUND.then(entity -> entity.placePortalTicket(blockPos4));
        } else {
            Direction.Axis axis = entity2.level().getBlockState(blockPos2).getOptionalValue(AXIS).orElse(Direction.Axis.X);
            Optional<BlockUtil.FoundRectangle> optional2 = serverLevel.getPortalForcer().createPortal(blockPos3, axis);
            if (optional2.isEmpty()) {
                LOGGER.error("Unable to create a portal, likely target out of worldborder");
                return null;
            }
            foundRectangle = optional2.get();
            postDimensionTransition = DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET);
        }
        return NetherPortalBlock.getDimensionTransitionFromExit(entity2, blockPos2, foundRectangle, serverLevel, postDimensionTransition);
    }

    private static DimensionTransition getDimensionTransitionFromExit(Entity entity, BlockPos blockPos2, BlockUtil.FoundRectangle foundRectangle, ServerLevel serverLevel, DimensionTransition.PostDimensionTransition postDimensionTransition) {
        Vec3 vec3;
        Direction.Axis axis;
        BlockState blockState = entity.level().getBlockState(blockPos2);
        if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            BlockUtil.FoundRectangle foundRectangle2 = BlockUtil.getLargestRectangleAround(blockPos2, axis, 21, Direction.Axis.Y, 21, blockPos -> entity.level().getBlockState((BlockPos)blockPos) == blockState);
            vec3 = entity.getRelativePortalPosition(axis, foundRectangle2);
        } else {
            axis = Direction.Axis.X;
            vec3 = new Vec3(0.5, 0.0, 0.0);
        }
        return NetherPortalBlock.createDimensionTransition(serverLevel, foundRectangle, axis, vec3, entity, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), postDimensionTransition);
    }

    private static DimensionTransition createDimensionTransition(ServerLevel serverLevel, BlockUtil.FoundRectangle foundRectangle, Direction.Axis axis, Vec3 vec3, Entity entity, Vec3 vec32, float f, float f2, DimensionTransition.PostDimensionTransition postDimensionTransition) {
        BlockPos blockPos = foundRectangle.minCorner;
        BlockState blockState = serverLevel.getBlockState(blockPos);
        Direction.Axis axis2 = blockState.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
        double d = foundRectangle.axis1Size;
        double d2 = foundRectangle.axis2Size;
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        int n = axis == axis2 ? 0 : 90;
        Vec3 vec33 = axis == axis2 ? vec32 : new Vec3(vec32.z, vec32.y, -vec32.x);
        double d3 = (double)entityDimensions.width() / 2.0 + (d - (double)entityDimensions.width()) * vec3.x();
        double d4 = (d2 - (double)entityDimensions.height()) * vec3.y();
        double d5 = 0.5 + vec3.z();
        boolean bl = axis2 == Direction.Axis.X;
        Vec3 vec34 = new Vec3((double)blockPos.getX() + (bl ? d3 : d5), (double)blockPos.getY() + d4, (double)blockPos.getZ() + (bl ? d5 : d3));
        Vec3 vec35 = PortalShape.findCollisionFreePosition(vec34, serverLevel, entity, entityDimensions);
        return new DimensionTransition(serverLevel, vec35, vec33, f + (float)n, f2, postDimensionTransition);
    }

    @Override
    public Portal.Transition getLocalTransition() {
        return Portal.Transition.CONFUSION;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextInt(100) == 0) {
            level.playLocalSound((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5f, randomSource.nextFloat() * 0.4f + 0.8f, false);
        }
        for (int i = 0; i < 4; ++i) {
            double d = (double)blockPos.getX() + randomSource.nextDouble();
            double d2 = (double)blockPos.getY() + randomSource.nextDouble();
            double d3 = (double)blockPos.getZ() + randomSource.nextDouble();
            double d4 = ((double)randomSource.nextFloat() - 0.5) * 0.5;
            double d5 = ((double)randomSource.nextFloat() - 0.5) * 0.5;
            double d6 = ((double)randomSource.nextFloat() - 0.5) * 0.5;
            int n = randomSource.nextInt(2) * 2 - 1;
            if (level.getBlockState(blockPos.west()).is(this) || level.getBlockState(blockPos.east()).is(this)) {
                d3 = (double)blockPos.getZ() + 0.5 + 0.25 * (double)n;
                d6 = randomSource.nextFloat() * 2.0f * (float)n;
            } else {
                d = (double)blockPos.getX() + 0.5 + 0.25 * (double)n;
                d4 = randomSource.nextFloat() * 2.0f * (float)n;
            }
            level.addParticle(ParticleTypes.PORTAL, d, d2, d3, d4, d5, d6);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return ItemStack.EMPTY;
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                switch (blockState.getValue(AXIS)) {
                    case X: {
                        return (BlockState)blockState.setValue(AXIS, Direction.Axis.Z);
                    }
                    case Z: {
                        return (BlockState)blockState.setValue(AXIS, Direction.Axis.X);
                    }
                }
                return blockState;
            }
        }
        return blockState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
}

