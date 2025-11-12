/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.state;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockBehaviour
implements FeatureElement {
    protected static final Direction[] UPDATE_SHAPE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
    protected final boolean hasCollision;
    protected final float explosionResistance;
    protected final boolean isRandomlyTicking;
    protected final SoundType soundType;
    protected final float friction;
    protected final float speedFactor;
    protected final float jumpFactor;
    protected final boolean dynamicShape;
    protected final FeatureFlagSet requiredFeatures;
    protected final Properties properties;
    @Nullable
    protected ResourceKey<LootTable> drops;

    public BlockBehaviour(Properties properties) {
        this.hasCollision = properties.hasCollision;
        this.drops = properties.drops;
        this.explosionResistance = properties.explosionResistance;
        this.isRandomlyTicking = properties.isRandomlyTicking;
        this.soundType = properties.soundType;
        this.friction = properties.friction;
        this.speedFactor = properties.speedFactor;
        this.jumpFactor = properties.jumpFactor;
        this.dynamicShape = properties.dynamicShape;
        this.requiredFeatures = properties.requiredFeatures;
        this.properties = properties;
    }

    public Properties properties() {
        return this.properties;
    }

    protected abstract MapCodec<? extends Block> codec();

    protected static <B extends Block> RecordCodecBuilder<B, Properties> propertiesCodec() {
        return Properties.CODEC.fieldOf("properties").forGetter(BlockBehaviour::properties);
    }

    public static <B extends Block> MapCodec<B> simpleCodec(Function<Properties, B> function) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(BlockBehaviour.propertiesCodec()).apply((Applicative)instance, function));
    }

    protected void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int n, int n2) {
    }

    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        switch (pathComputationType) {
            case LAND: {
                return !blockState.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            }
            case WATER: {
                return blockState.getFluidState().is(FluidTags.WATER);
            }
            case AIR: {
                return !blockState.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            }
        }
        return false;
    }

    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return blockState;
    }

    protected boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        return false;
    }

    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        DebugPackets.sendNeighborsUpdatePacket(level, blockPos);
    }

    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
    }

    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.hasBlockEntity() && !blockState.is(blockState2.getBlock())) {
            level.removeBlockEntity(blockPos);
        }
    }

    protected void onExplosionHit(BlockState blockState, Level level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
        if (blockState.isAir() || explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK) {
            return;
        }
        Block block = blockState.getBlock();
        boolean bl = explosion.getIndirectSourceEntity() instanceof Player;
        if (block.dropFromExplosion(explosion) && level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
            LootParams.Builder builder = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, explosion.getDirectSourceEntity());
            if (explosion.getBlockInteraction() == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
                builder.withParameter(LootContextParams.EXPLOSION_RADIUS, Float.valueOf(explosion.radius()));
            }
            blockState.spawnAfterBreak(serverLevel, blockPos, ItemStack.EMPTY, bl);
            blockState.getDrops(builder).forEach(itemStack -> biConsumer.accept((ItemStack)itemStack, blockPos));
        }
        level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
        block.wasExploded(level, blockPos, explosion);
    }

    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        return InteractionResult.PASS;
    }

    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int n, int n2) {
        return false;
    }

    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return false;
    }

    protected boolean isSignalSource(BlockState blockState) {
        return false;
    }

    protected FluidState getFluidState(BlockState blockState) {
        return Fluids.EMPTY.defaultFluidState();
    }

    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return false;
    }

    protected float getMaxHorizontalOffset() {
        return 0.25f;
    }

    protected float getMaxVerticalOffset() {
        return 0.2f;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState;
    }

    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState;
    }

    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return blockState.canBeReplaced() && (blockPlaceContext.getItemInHand().isEmpty() || !blockPlaceContext.getItemInHand().is(this.asItem()));
    }

    protected boolean canBeReplaced(BlockState blockState, Fluid fluid) {
        return blockState.canBeReplaced() || !blockState.isSolid();
    }

    protected List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
        ResourceKey<LootTable> resourceKey = this.getLootTable();
        if (resourceKey == BuiltInLootTables.EMPTY) {
            return Collections.emptyList();
        }
        LootParams lootParams = builder.withParameter(LootContextParams.BLOCK_STATE, blockState).create(LootContextParamSets.BLOCK);
        ServerLevel serverLevel = lootParams.getLevel();
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(resourceKey);
        return lootTable.getRandomItems(lootParams);
    }

    protected long getSeed(BlockState blockState, BlockPos blockPos) {
        return Mth.getSeed(blockPos);
    }

    protected VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.getShape(blockGetter, blockPos);
    }

    protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return this.getCollisionShape(blockState, blockGetter, blockPos, CollisionContext.empty());
    }

    protected VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.empty();
    }

    protected int getLightBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        if (blockState.isSolidRender(blockGetter, blockPos)) {
            return blockGetter.getMaxLightLevel();
        }
        return blockState.propagatesSkylightDown(blockGetter, blockPos) ? 0 : 1;
    }

    @Nullable
    protected MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return null;
    }

    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return true;
    }

    protected float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.isCollisionShapeFullBlock(blockGetter, blockPos) ? 0.2f : 1.0f;
    }

    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return 0;
    }

    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.block();
    }

    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.hasCollision ? blockState.getShape(blockGetter, blockPos) : Shapes.empty();
    }

    protected boolean isCollisionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Block.isShapeFullBlock(blockState.getCollisionShape(blockGetter, blockPos));
    }

    protected boolean isOcclusionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Block.isShapeFullBlock(blockState.getOcclusionShape(blockGetter, blockPos));
    }

    protected VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
    }

    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
    }

    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
    }

    protected float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
        float f = blockState.getDestroySpeed(blockGetter, blockPos);
        if (f == -1.0f) {
            return 0.0f;
        }
        int n = player.hasCorrectToolForDrops(blockState) ? 30 : 100;
        return player.getDestroySpeed(blockState) / f / (float)n;
    }

    protected void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
    }

    protected void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
    }

    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
    }

    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return 0;
    }

    public final ResourceKey<LootTable> getLootTable() {
        if (this.drops == null) {
            ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(this.asBlock());
            this.drops = ResourceKey.create(Registries.LOOT_TABLE, resourceLocation.withPrefix("blocks/"));
        }
        return this.drops;
    }

    protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
    }

    protected boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return !Block.isShapeFullBlock(blockState.getShape(blockGetter, blockPos)) && blockState.getFluidState().isEmpty();
    }

    protected boolean isRandomlyTicking(BlockState blockState) {
        return this.isRandomlyTicking;
    }

    protected SoundType getSoundType(BlockState blockState) {
        return this.soundType;
    }

    public abstract Item asItem();

    protected abstract Block asBlock();

    public MapColor defaultMapColor() {
        return this.properties.mapColor.apply(this.asBlock().defaultBlockState());
    }

    public float defaultDestroyTime() {
        return this.properties.destroyTime;
    }

    public static class Properties {
        public static final Codec<Properties> CODEC = Codec.unit(() -> Properties.of());
        Function<BlockState, MapColor> mapColor = blockState -> MapColor.NONE;
        boolean hasCollision = true;
        SoundType soundType = SoundType.STONE;
        ToIntFunction<BlockState> lightEmission = blockState -> 0;
        float explosionResistance;
        float destroyTime;
        boolean requiresCorrectToolForDrops;
        boolean isRandomlyTicking;
        float friction = 0.6f;
        float speedFactor = 1.0f;
        float jumpFactor = 1.0f;
        ResourceKey<LootTable> drops;
        boolean canOcclude = true;
        boolean isAir;
        boolean ignitedByLava;
        @Deprecated
        boolean liquid;
        @Deprecated
        boolean forceSolidOff;
        boolean forceSolidOn;
        PushReaction pushReaction = PushReaction.NORMAL;
        boolean spawnTerrainParticles = true;
        NoteBlockInstrument instrument = NoteBlockInstrument.HARP;
        boolean replaceable;
        StateArgumentPredicate<EntityType<?>> isValidSpawn = (blockState, blockGetter, blockPos, entityType) -> blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP) && blockState.getLightEmission() < 14;
        StatePredicate isRedstoneConductor = (blockState, blockGetter, blockPos) -> blockState.isCollisionShapeFullBlock(blockGetter, blockPos);
        StatePredicate isSuffocating;
        StatePredicate isViewBlocking = this.isSuffocating = (blockState, blockGetter, blockPos) -> blockState.blocksMotion() && blockState.isCollisionShapeFullBlock(blockGetter, blockPos);
        StatePredicate hasPostProcess = (blockState, blockGetter, blockPos) -> false;
        StatePredicate emissiveRendering = (blockState, blockGetter, blockPos) -> false;
        boolean dynamicShape;
        FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
        @Nullable
        OffsetFunction offsetFunction;

        private Properties() {
        }

        public static Properties of() {
            return new Properties();
        }

        public static Properties ofFullCopy(BlockBehaviour blockBehaviour) {
            Properties properties = Properties.ofLegacyCopy(blockBehaviour);
            Properties properties2 = blockBehaviour.properties;
            properties.jumpFactor = properties2.jumpFactor;
            properties.isRedstoneConductor = properties2.isRedstoneConductor;
            properties.isValidSpawn = properties2.isValidSpawn;
            properties.hasPostProcess = properties2.hasPostProcess;
            properties.isSuffocating = properties2.isSuffocating;
            properties.isViewBlocking = properties2.isViewBlocking;
            properties.drops = properties2.drops;
            return properties;
        }

        @Deprecated
        public static Properties ofLegacyCopy(BlockBehaviour blockBehaviour) {
            Properties properties = new Properties();
            Properties properties2 = blockBehaviour.properties;
            properties.destroyTime = properties2.destroyTime;
            properties.explosionResistance = properties2.explosionResistance;
            properties.hasCollision = properties2.hasCollision;
            properties.isRandomlyTicking = properties2.isRandomlyTicking;
            properties.lightEmission = properties2.lightEmission;
            properties.mapColor = properties2.mapColor;
            properties.soundType = properties2.soundType;
            properties.friction = properties2.friction;
            properties.speedFactor = properties2.speedFactor;
            properties.dynamicShape = properties2.dynamicShape;
            properties.canOcclude = properties2.canOcclude;
            properties.isAir = properties2.isAir;
            properties.ignitedByLava = properties2.ignitedByLava;
            properties.liquid = properties2.liquid;
            properties.forceSolidOff = properties2.forceSolidOff;
            properties.forceSolidOn = properties2.forceSolidOn;
            properties.pushReaction = properties2.pushReaction;
            properties.requiresCorrectToolForDrops = properties2.requiresCorrectToolForDrops;
            properties.offsetFunction = properties2.offsetFunction;
            properties.spawnTerrainParticles = properties2.spawnTerrainParticles;
            properties.requiredFeatures = properties2.requiredFeatures;
            properties.emissiveRendering = properties2.emissiveRendering;
            properties.instrument = properties2.instrument;
            properties.replaceable = properties2.replaceable;
            return properties;
        }

        public Properties mapColor(DyeColor dyeColor) {
            this.mapColor = blockState -> dyeColor.getMapColor();
            return this;
        }

        public Properties mapColor(MapColor mapColor) {
            this.mapColor = blockState -> mapColor;
            return this;
        }

        public Properties mapColor(Function<BlockState, MapColor> function) {
            this.mapColor = function;
            return this;
        }

        public Properties noCollission() {
            this.hasCollision = false;
            this.canOcclude = false;
            return this;
        }

        public Properties noOcclusion() {
            this.canOcclude = false;
            return this;
        }

        public Properties friction(float f) {
            this.friction = f;
            return this;
        }

        public Properties speedFactor(float f) {
            this.speedFactor = f;
            return this;
        }

        public Properties jumpFactor(float f) {
            this.jumpFactor = f;
            return this;
        }

        public Properties sound(SoundType soundType) {
            this.soundType = soundType;
            return this;
        }

        public Properties lightLevel(ToIntFunction<BlockState> toIntFunction) {
            this.lightEmission = toIntFunction;
            return this;
        }

        public Properties strength(float f, float f2) {
            return this.destroyTime(f).explosionResistance(f2);
        }

        public Properties instabreak() {
            return this.strength(0.0f);
        }

        public Properties strength(float f) {
            this.strength(f, f);
            return this;
        }

        public Properties randomTicks() {
            this.isRandomlyTicking = true;
            return this;
        }

        public Properties dynamicShape() {
            this.dynamicShape = true;
            return this;
        }

        public Properties noLootTable() {
            this.drops = BuiltInLootTables.EMPTY;
            return this;
        }

        public Properties dropsLike(Block block) {
            this.drops = block.getLootTable();
            return this;
        }

        public Properties ignitedByLava() {
            this.ignitedByLava = true;
            return this;
        }

        public Properties liquid() {
            this.liquid = true;
            return this;
        }

        public Properties forceSolidOn() {
            this.forceSolidOn = true;
            return this;
        }

        @Deprecated
        public Properties forceSolidOff() {
            this.forceSolidOff = true;
            return this;
        }

        public Properties pushReaction(PushReaction pushReaction) {
            this.pushReaction = pushReaction;
            return this;
        }

        public Properties air() {
            this.isAir = true;
            return this;
        }

        public Properties isValidSpawn(StateArgumentPredicate<EntityType<?>> stateArgumentPredicate) {
            this.isValidSpawn = stateArgumentPredicate;
            return this;
        }

        public Properties isRedstoneConductor(StatePredicate statePredicate) {
            this.isRedstoneConductor = statePredicate;
            return this;
        }

        public Properties isSuffocating(StatePredicate statePredicate) {
            this.isSuffocating = statePredicate;
            return this;
        }

        public Properties isViewBlocking(StatePredicate statePredicate) {
            this.isViewBlocking = statePredicate;
            return this;
        }

        public Properties hasPostProcess(StatePredicate statePredicate) {
            this.hasPostProcess = statePredicate;
            return this;
        }

        public Properties emissiveRendering(StatePredicate statePredicate) {
            this.emissiveRendering = statePredicate;
            return this;
        }

        public Properties requiresCorrectToolForDrops() {
            this.requiresCorrectToolForDrops = true;
            return this;
        }

        public Properties destroyTime(float f) {
            this.destroyTime = f;
            return this;
        }

        public Properties explosionResistance(float f) {
            this.explosionResistance = Math.max(0.0f, f);
            return this;
        }

        public Properties offsetType(OffsetType offsetType) {
            this.offsetFunction = switch (offsetType.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> null;
                case 2 -> (blockState, blockGetter, blockPos) -> {
                    Block block = blockState.getBlock();
                    long l = Mth.getSeed(blockPos.getX(), 0, blockPos.getZ());
                    double d = ((double)((float)(l >> 4 & 0xFL) / 15.0f) - 1.0) * (double)block.getMaxVerticalOffset();
                    float f = block.getMaxHorizontalOffset();
                    double d2 = Mth.clamp(((double)((float)(l & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                    double d3 = Mth.clamp(((double)((float)(l >> 8 & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                    return new Vec3(d2, d, d3);
                };
                case 1 -> (blockState, blockGetter, blockPos) -> {
                    Block block = blockState.getBlock();
                    long l = Mth.getSeed(blockPos.getX(), 0, blockPos.getZ());
                    float f = block.getMaxHorizontalOffset();
                    double d = Mth.clamp(((double)((float)(l & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                    double d2 = Mth.clamp(((double)((float)(l >> 8 & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                    return new Vec3(d, 0.0, d2);
                };
            };
            return this;
        }

        public Properties noTerrainParticles() {
            this.spawnTerrainParticles = false;
            return this;
        }

        public Properties requiredFeatures(FeatureFlag ... featureFlagArray) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlagArray);
            return this;
        }

        public Properties instrument(NoteBlockInstrument noteBlockInstrument) {
            this.instrument = noteBlockInstrument;
            return this;
        }

        public Properties replaceable() {
            this.replaceable = true;
            return this;
        }
    }

    public static interface StateArgumentPredicate<A> {
        public boolean test(BlockState var1, BlockGetter var2, BlockPos var3, A var4);
    }

    public static interface OffsetFunction {
        public Vec3 evaluate(BlockState var1, BlockGetter var2, BlockPos var3);
    }

    public static interface StatePredicate {
        public boolean test(BlockState var1, BlockGetter var2, BlockPos var3);
    }

    public static abstract class BlockStateBase
    extends StateHolder<Block, BlockState> {
        private final int lightEmission;
        private final boolean useShapeForLightOcclusion;
        private final boolean isAir;
        private final boolean ignitedByLava;
        @Deprecated
        private final boolean liquid;
        @Deprecated
        private boolean legacySolid;
        private final PushReaction pushReaction;
        private final MapColor mapColor;
        private final float destroySpeed;
        private final boolean requiresCorrectToolForDrops;
        private final boolean canOcclude;
        private final StatePredicate isRedstoneConductor;
        private final StatePredicate isSuffocating;
        private final StatePredicate isViewBlocking;
        private final StatePredicate hasPostProcess;
        private final StatePredicate emissiveRendering;
        @Nullable
        private final OffsetFunction offsetFunction;
        private final boolean spawnTerrainParticles;
        private final NoteBlockInstrument instrument;
        private final boolean replaceable;
        @Nullable
        protected Cache cache;
        private FluidState fluidState = Fluids.EMPTY.defaultFluidState();
        private boolean isRandomlyTicking;

        protected BlockStateBase(Block block, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<BlockState> mapCodec) {
            super(block, reference2ObjectArrayMap, mapCodec);
            Properties properties = block.properties;
            this.lightEmission = properties.lightEmission.applyAsInt(this.asState());
            this.useShapeForLightOcclusion = block.useShapeForLightOcclusion(this.asState());
            this.isAir = properties.isAir;
            this.ignitedByLava = properties.ignitedByLava;
            this.liquid = properties.liquid;
            this.pushReaction = properties.pushReaction;
            this.mapColor = properties.mapColor.apply(this.asState());
            this.destroySpeed = properties.destroyTime;
            this.requiresCorrectToolForDrops = properties.requiresCorrectToolForDrops;
            this.canOcclude = properties.canOcclude;
            this.isRedstoneConductor = properties.isRedstoneConductor;
            this.isSuffocating = properties.isSuffocating;
            this.isViewBlocking = properties.isViewBlocking;
            this.hasPostProcess = properties.hasPostProcess;
            this.emissiveRendering = properties.emissiveRendering;
            this.offsetFunction = properties.offsetFunction;
            this.spawnTerrainParticles = properties.spawnTerrainParticles;
            this.instrument = properties.instrument;
            this.replaceable = properties.replaceable;
        }

        private boolean calculateSolid() {
            if (((Block)this.owner).properties.forceSolidOn) {
                return true;
            }
            if (((Block)this.owner).properties.forceSolidOff) {
                return false;
            }
            if (this.cache == null) {
                return false;
            }
            VoxelShape voxelShape = this.cache.collisionShape;
            if (voxelShape.isEmpty()) {
                return false;
            }
            AABB aABB = voxelShape.bounds();
            if (aABB.getSize() >= 0.7291666666666666) {
                return true;
            }
            return aABB.getYsize() >= 1.0;
        }

        public void initCache() {
            this.fluidState = ((Block)this.owner).getFluidState(this.asState());
            this.isRandomlyTicking = ((Block)this.owner).isRandomlyTicking(this.asState());
            if (!this.getBlock().hasDynamicShape()) {
                this.cache = new Cache(this.asState());
            }
            this.legacySolid = this.calculateSolid();
        }

        public Block getBlock() {
            return (Block)this.owner;
        }

        public Holder<Block> getBlockHolder() {
            return ((Block)this.owner).builtInRegistryHolder();
        }

        @Deprecated
        public boolean blocksMotion() {
            Block block = this.getBlock();
            return block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING && this.isSolid();
        }

        @Deprecated
        public boolean isSolid() {
            return this.legacySolid;
        }

        public boolean isValidSpawn(BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
            return this.getBlock().properties.isValidSpawn.test(this.asState(), blockGetter, blockPos, entityType);
        }

        public boolean propagatesSkylightDown(BlockGetter blockGetter, BlockPos blockPos) {
            if (this.cache != null) {
                return this.cache.propagatesSkylightDown;
            }
            return this.getBlock().propagatesSkylightDown(this.asState(), blockGetter, blockPos);
        }

        public int getLightBlock(BlockGetter blockGetter, BlockPos blockPos) {
            if (this.cache != null) {
                return this.cache.lightBlock;
            }
            return this.getBlock().getLightBlock(this.asState(), blockGetter, blockPos);
        }

        public VoxelShape getFaceOcclusionShape(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            if (this.cache != null && this.cache.occlusionShapes != null) {
                return this.cache.occlusionShapes[direction.ordinal()];
            }
            return Shapes.getFaceShape(this.getOcclusionShape(blockGetter, blockPos), direction);
        }

        public VoxelShape getOcclusionShape(BlockGetter blockGetter, BlockPos blockPos) {
            return this.getBlock().getOcclusionShape(this.asState(), blockGetter, blockPos);
        }

        public boolean hasLargeCollisionShape() {
            return this.cache == null || this.cache.largeCollisionShape;
        }

        public boolean useShapeForLightOcclusion() {
            return this.useShapeForLightOcclusion;
        }

        public int getLightEmission() {
            return this.lightEmission;
        }

        public boolean isAir() {
            return this.isAir;
        }

        public boolean ignitedByLava() {
            return this.ignitedByLava;
        }

        @Deprecated
        public boolean liquid() {
            return this.liquid;
        }

        public MapColor getMapColor(BlockGetter blockGetter, BlockPos blockPos) {
            return this.mapColor;
        }

        public BlockState rotate(Rotation rotation) {
            return this.getBlock().rotate(this.asState(), rotation);
        }

        public BlockState mirror(Mirror mirror) {
            return this.getBlock().mirror(this.asState(), mirror);
        }

        public RenderShape getRenderShape() {
            return this.getBlock().getRenderShape(this.asState());
        }

        public boolean emissiveRendering(BlockGetter blockGetter, BlockPos blockPos) {
            return this.emissiveRendering.test(this.asState(), blockGetter, blockPos);
        }

        public float getShadeBrightness(BlockGetter blockGetter, BlockPos blockPos) {
            return this.getBlock().getShadeBrightness(this.asState(), blockGetter, blockPos);
        }

        public boolean isRedstoneConductor(BlockGetter blockGetter, BlockPos blockPos) {
            return this.isRedstoneConductor.test(this.asState(), blockGetter, blockPos);
        }

        public boolean isSignalSource() {
            return this.getBlock().isSignalSource(this.asState());
        }

        public int getSignal(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            return this.getBlock().getSignal(this.asState(), blockGetter, blockPos, direction);
        }

        public boolean hasAnalogOutputSignal() {
            return this.getBlock().hasAnalogOutputSignal(this.asState());
        }

        public int getAnalogOutputSignal(Level level, BlockPos blockPos) {
            return this.getBlock().getAnalogOutputSignal(this.asState(), level, blockPos);
        }

        public float getDestroySpeed(BlockGetter blockGetter, BlockPos blockPos) {
            return this.destroySpeed;
        }

        public float getDestroyProgress(Player player, BlockGetter blockGetter, BlockPos blockPos) {
            return this.getBlock().getDestroyProgress(this.asState(), player, blockGetter, blockPos);
        }

        public int getDirectSignal(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            return this.getBlock().getDirectSignal(this.asState(), blockGetter, blockPos, direction);
        }

        public PushReaction getPistonPushReaction() {
            return this.pushReaction;
        }

        public boolean isSolidRender(BlockGetter blockGetter, BlockPos blockPos) {
            if (this.cache != null) {
                return this.cache.solidRender;
            }
            BlockState blockState = this.asState();
            if (blockState.canOcclude()) {
                return Block.isShapeFullBlock(blockState.getOcclusionShape(blockGetter, blockPos));
            }
            return false;
        }

        public boolean canOcclude() {
            return this.canOcclude;
        }

        public boolean skipRendering(BlockState blockState, Direction direction) {
            return this.getBlock().skipRendering(this.asState(), blockState, direction);
        }

        public VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos) {
            return this.getShape(blockGetter, blockPos, CollisionContext.empty());
        }

        public VoxelShape getShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
            return this.getBlock().getShape(this.asState(), blockGetter, blockPos, collisionContext);
        }

        public VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos) {
            if (this.cache != null) {
                return this.cache.collisionShape;
            }
            return this.getCollisionShape(blockGetter, blockPos, CollisionContext.empty());
        }

        public VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
            return this.getBlock().getCollisionShape(this.asState(), blockGetter, blockPos, collisionContext);
        }

        public VoxelShape getBlockSupportShape(BlockGetter blockGetter, BlockPos blockPos) {
            return this.getBlock().getBlockSupportShape(this.asState(), blockGetter, blockPos);
        }

        public VoxelShape getVisualShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
            return this.getBlock().getVisualShape(this.asState(), blockGetter, blockPos, collisionContext);
        }

        public VoxelShape getInteractionShape(BlockGetter blockGetter, BlockPos blockPos) {
            return this.getBlock().getInteractionShape(this.asState(), blockGetter, blockPos);
        }

        public final boolean entityCanStandOn(BlockGetter blockGetter, BlockPos blockPos, Entity entity) {
            return this.entityCanStandOnFace(blockGetter, blockPos, entity, Direction.UP);
        }

        public final boolean entityCanStandOnFace(BlockGetter blockGetter, BlockPos blockPos, Entity entity, Direction direction) {
            return Block.isFaceFull(this.getCollisionShape(blockGetter, blockPos, CollisionContext.of(entity)), direction);
        }

        public Vec3 getOffset(BlockGetter blockGetter, BlockPos blockPos) {
            OffsetFunction offsetFunction = this.offsetFunction;
            if (offsetFunction != null) {
                return offsetFunction.evaluate(this.asState(), blockGetter, blockPos);
            }
            return Vec3.ZERO;
        }

        public boolean hasOffsetFunction() {
            return this.offsetFunction != null;
        }

        public boolean triggerEvent(Level level, BlockPos blockPos, int n, int n2) {
            return this.getBlock().triggerEvent(this.asState(), level, blockPos, n, n2);
        }

        public void handleNeighborChanged(Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
            this.getBlock().neighborChanged(this.asState(), level, blockPos, block, blockPos2, bl);
        }

        public final void updateNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int n) {
            this.updateNeighbourShapes(levelAccessor, blockPos, n, 512);
        }

        public final void updateNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int n, int n2) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (Direction direction : UPDATE_SHAPE_ORDER) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
                levelAccessor.neighborShapeChanged(direction.getOpposite(), this.asState(), mutableBlockPos, blockPos, n, n2);
            }
        }

        public final void updateIndirectNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int n) {
            this.updateIndirectNeighbourShapes(levelAccessor, blockPos, n, 512);
        }

        public void updateIndirectNeighbourShapes(LevelAccessor levelAccessor, BlockPos blockPos, int n, int n2) {
            this.getBlock().updateIndirectNeighbourShapes(this.asState(), levelAccessor, blockPos, n, n2);
        }

        public void onPlace(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
            this.getBlock().onPlace(this.asState(), level, blockPos, blockState, bl);
        }

        public void onRemove(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
            this.getBlock().onRemove(this.asState(), level, blockPos, blockState, bl);
        }

        public void onExplosionHit(Level level, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer) {
            this.getBlock().onExplosionHit(this.asState(), level, blockPos, explosion, biConsumer);
        }

        public void tick(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
            this.getBlock().tick(this.asState(), serverLevel, blockPos, randomSource);
        }

        public void randomTick(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
            this.getBlock().randomTick(this.asState(), serverLevel, blockPos, randomSource);
        }

        public void entityInside(Level level, BlockPos blockPos, Entity entity) {
            this.getBlock().entityInside(this.asState(), level, blockPos, entity);
        }

        public void spawnAfterBreak(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
            this.getBlock().spawnAfterBreak(this.asState(), serverLevel, blockPos, itemStack, bl);
        }

        public List<ItemStack> getDrops(LootParams.Builder builder) {
            return this.getBlock().getDrops(this.asState(), builder);
        }

        public ItemInteractionResult useItemOn(ItemStack itemStack, Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
            return this.getBlock().useItemOn(itemStack, this.asState(), level, blockHitResult.getBlockPos(), player, interactionHand, blockHitResult);
        }

        public InteractionResult useWithoutItem(Level level, Player player, BlockHitResult blockHitResult) {
            return this.getBlock().useWithoutItem(this.asState(), level, blockHitResult.getBlockPos(), player, blockHitResult);
        }

        public void attack(Level level, BlockPos blockPos, Player player) {
            this.getBlock().attack(this.asState(), level, blockPos, player);
        }

        public boolean isSuffocating(BlockGetter blockGetter, BlockPos blockPos) {
            return this.isSuffocating.test(this.asState(), blockGetter, blockPos);
        }

        public boolean isViewBlocking(BlockGetter blockGetter, BlockPos blockPos) {
            return this.isViewBlocking.test(this.asState(), blockGetter, blockPos);
        }

        public BlockState updateShape(Direction direction, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
            return this.getBlock().updateShape(this.asState(), direction, blockState, levelAccessor, blockPos, blockPos2);
        }

        public boolean isPathfindable(PathComputationType pathComputationType) {
            return this.getBlock().isPathfindable(this.asState(), pathComputationType);
        }

        public boolean canBeReplaced(BlockPlaceContext blockPlaceContext) {
            return this.getBlock().canBeReplaced(this.asState(), blockPlaceContext);
        }

        public boolean canBeReplaced(Fluid fluid) {
            return this.getBlock().canBeReplaced(this.asState(), fluid);
        }

        public boolean canBeReplaced() {
            return this.replaceable;
        }

        public boolean canSurvive(LevelReader levelReader, BlockPos blockPos) {
            return this.getBlock().canSurvive(this.asState(), levelReader, blockPos);
        }

        public boolean hasPostProcess(BlockGetter blockGetter, BlockPos blockPos) {
            return this.hasPostProcess.test(this.asState(), blockGetter, blockPos);
        }

        @Nullable
        public MenuProvider getMenuProvider(Level level, BlockPos blockPos) {
            return this.getBlock().getMenuProvider(this.asState(), level, blockPos);
        }

        public boolean is(TagKey<Block> tagKey) {
            return this.getBlock().builtInRegistryHolder().is(tagKey);
        }

        public boolean is(TagKey<Block> tagKey, Predicate<BlockStateBase> predicate) {
            return this.is(tagKey) && predicate.test(this);
        }

        public boolean is(HolderSet<Block> holderSet) {
            return holderSet.contains(this.getBlock().builtInRegistryHolder());
        }

        public boolean is(Holder<Block> holder) {
            return this.is(holder.value());
        }

        public Stream<TagKey<Block>> getTags() {
            return this.getBlock().builtInRegistryHolder().tags();
        }

        public boolean hasBlockEntity() {
            return this.getBlock() instanceof EntityBlock;
        }

        @Nullable
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockEntityType<T> blockEntityType) {
            if (this.getBlock() instanceof EntityBlock) {
                return ((EntityBlock)((Object)this.getBlock())).getTicker(level, this.asState(), blockEntityType);
            }
            return null;
        }

        public boolean is(Block block) {
            return this.getBlock() == block;
        }

        public boolean is(ResourceKey<Block> resourceKey) {
            return this.getBlock().builtInRegistryHolder().is(resourceKey);
        }

        public FluidState getFluidState() {
            return this.fluidState;
        }

        public boolean isRandomlyTicking() {
            return this.isRandomlyTicking;
        }

        public long getSeed(BlockPos blockPos) {
            return this.getBlock().getSeed(this.asState(), blockPos);
        }

        public SoundType getSoundType() {
            return this.getBlock().getSoundType(this.asState());
        }

        public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
            this.getBlock().onProjectileHit(level, blockState, blockHitResult, projectile);
        }

        public boolean isFaceSturdy(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
            return this.isFaceSturdy(blockGetter, blockPos, direction, SupportType.FULL);
        }

        public boolean isFaceSturdy(BlockGetter blockGetter, BlockPos blockPos, Direction direction, SupportType supportType) {
            if (this.cache != null) {
                return this.cache.isFaceSturdy(direction, supportType);
            }
            return supportType.isSupporting(this.asState(), blockGetter, blockPos, direction);
        }

        public boolean isCollisionShapeFullBlock(BlockGetter blockGetter, BlockPos blockPos) {
            if (this.cache != null) {
                return this.cache.isCollisionShapeFullBlock;
            }
            return this.getBlock().isCollisionShapeFullBlock(this.asState(), blockGetter, blockPos);
        }

        protected abstract BlockState asState();

        public boolean requiresCorrectToolForDrops() {
            return this.requiresCorrectToolForDrops;
        }

        public boolean shouldSpawnTerrainParticles() {
            return this.spawnTerrainParticles;
        }

        public NoteBlockInstrument instrument() {
            return this.instrument;
        }

        static final class Cache {
            private static final Direction[] DIRECTIONS = Direction.values();
            private static final int SUPPORT_TYPE_COUNT = SupportType.values().length;
            protected final boolean solidRender;
            final boolean propagatesSkylightDown;
            final int lightBlock;
            @Nullable
            final VoxelShape[] occlusionShapes;
            protected final VoxelShape collisionShape;
            protected final boolean largeCollisionShape;
            private final boolean[] faceSturdy;
            protected final boolean isCollisionShapeFullBlock;

            Cache(BlockState blockState) {
                Block block = blockState.getBlock();
                this.solidRender = blockState.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                this.propagatesSkylightDown = block.propagatesSkylightDown(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                this.lightBlock = block.getLightBlock(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                if (!blockState.canOcclude()) {
                    this.occlusionShapes = null;
                } else {
                    this.occlusionShapes = new VoxelShape[DIRECTIONS.length];
                    Direction[] directionArray = block.getOcclusionShape(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                    Direction[] directionArray2 = DIRECTIONS;
                    int n = directionArray2.length;
                    for (int i = 0; i < n; ++i) {
                        SupportType[] supportTypeArray = directionArray2[i];
                        this.occlusionShapes[supportTypeArray.ordinal()] = Shapes.getFaceShape((VoxelShape)directionArray, (Direction)supportTypeArray);
                    }
                }
                this.collisionShape = block.getCollisionShape(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
                if (!this.collisionShape.isEmpty() && blockState.hasOffsetFunction()) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.", BuiltInRegistries.BLOCK.getKey(block)));
                }
                this.largeCollisionShape = Arrays.stream(Direction.Axis.values()).anyMatch(axis -> this.collisionShape.min((Direction.Axis)axis) < 0.0 || this.collisionShape.max((Direction.Axis)axis) > 1.0);
                this.faceSturdy = new boolean[DIRECTIONS.length * SUPPORT_TYPE_COUNT];
                for (Direction direction : DIRECTIONS) {
                    for (SupportType supportType : SupportType.values()) {
                        this.faceSturdy[Cache.getFaceSupportIndex((Direction)direction, (SupportType)supportType)] = supportType.isSupporting(blockState, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, direction);
                    }
                }
                this.isCollisionShapeFullBlock = Block.isShapeFullBlock(blockState.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
            }

            public boolean isFaceSturdy(Direction direction, SupportType supportType) {
                return this.faceSturdy[Cache.getFaceSupportIndex(direction, supportType)];
            }

            private static int getFaceSupportIndex(Direction direction, SupportType supportType) {
                return direction.ordinal() * SUPPORT_TYPE_COUNT + supportType.ordinal();
            }
        }
    }

    public static enum OffsetType {
        NONE,
        XZ,
        XYZ;

    }
}

