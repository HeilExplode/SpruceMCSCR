/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.util.Either
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.embedded.EmbeddedChannel
 *  javax.annotation.Nullable
 */
package net.minecraft.gametest.framework;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GameTestHelper {
    private final GameTestInfo testInfo;
    private boolean finalCheckAdded;

    public GameTestHelper(GameTestInfo gameTestInfo) {
        this.testInfo = gameTestInfo;
    }

    public ServerLevel getLevel() {
        return this.testInfo.getLevel();
    }

    public BlockState getBlockState(BlockPos blockPos) {
        return this.getLevel().getBlockState(this.absolutePos(blockPos));
    }

    public <T extends BlockEntity> T getBlockEntity(BlockPos blockPos) {
        BlockEntity blockEntity = this.getLevel().getBlockEntity(this.absolutePos(blockPos));
        if (blockEntity == null) {
            throw new GameTestAssertPosException("Missing block entity", this.absolutePos(blockPos), blockPos, this.testInfo.getTick());
        }
        return (T)blockEntity;
    }

    public void killAllEntities() {
        this.killAllEntitiesOfClass(Entity.class);
    }

    public void killAllEntitiesOfClass(Class clazz) {
        AABB aABB = this.getBounds();
        List<Entity> list = this.getLevel().getEntitiesOfClass(clazz, aABB.inflate(1.0), entity -> !(entity instanceof Player));
        list.forEach(Entity::kill);
    }

    public ItemEntity spawnItem(Item item, Vec3 vec3) {
        ServerLevel serverLevel = this.getLevel();
        Vec3 vec32 = this.absoluteVec(vec3);
        ItemEntity itemEntity = new ItemEntity(serverLevel, vec32.x, vec32.y, vec32.z, new ItemStack(item, 1));
        itemEntity.setDeltaMovement(0.0, 0.0, 0.0);
        serverLevel.addFreshEntity(itemEntity);
        return itemEntity;
    }

    public ItemEntity spawnItem(Item item, float f, float f2, float f3) {
        return this.spawnItem(item, new Vec3(f, f2, f3));
    }

    public ItemEntity spawnItem(Item item, BlockPos blockPos) {
        return this.spawnItem(item, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, BlockPos blockPos) {
        return this.spawn(entityType, Vec3.atBottomCenterOf(blockPos));
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, Vec3 vec3) {
        Object object;
        ServerLevel serverLevel = this.getLevel();
        E e = entityType.create(serverLevel);
        if (e == null) {
            throw new NullPointerException("Failed to create entity " + String.valueOf(entityType.builtInRegistryHolder().key().location()));
        }
        if (e instanceof Mob) {
            object = (Mob)e;
            ((Mob)object).setPersistenceRequired();
        }
        object = this.absoluteVec(vec3);
        ((Entity)e).moveTo(((Vec3)object).x, ((Vec3)object).y, ((Vec3)object).z, ((Entity)e).getYRot(), ((Entity)e).getXRot());
        serverLevel.addFreshEntity((Entity)e);
        return e;
    }

    public <E extends Entity> E findOneEntity(EntityType<E> entityType) {
        return this.findClosestEntity(entityType, 0, 0, 0, 2.147483647E9);
    }

    public <E extends Entity> E findClosestEntity(EntityType<E> entityType, int n, int n2, int n3, double d) {
        List<E> list = this.findEntities(entityType, n, n2, n3, d);
        if (list.isEmpty()) {
            throw new GameTestAssertException("Expected " + entityType.toShortString() + " to exist around " + n + "," + n2 + "," + n3);
        }
        if (list.size() > 1) {
            throw new GameTestAssertException("Expected only one " + entityType.toShortString() + " to exist around " + n + "," + n2 + "," + n3 + ", but found " + list.size());
        }
        Vec3 vec3 = this.absoluteVec(new Vec3(n, n2, n3));
        list.sort((entity, entity2) -> {
            double d = entity.position().distanceTo(vec3);
            double d2 = entity2.position().distanceTo(vec3);
            return Double.compare(d, d2);
        });
        return (E)((Entity)list.get(0));
    }

    public <E extends Entity> List<E> findEntities(EntityType<E> entityType, int n, int n2, int n3, double d) {
        return this.findEntities(entityType, Vec3.atBottomCenterOf(new BlockPos(n, n2, n3)), d);
    }

    public <E extends Entity> List<E> findEntities(EntityType<E> entityType, Vec3 vec3, double d) {
        ServerLevel serverLevel = this.getLevel();
        Vec3 vec32 = this.absoluteVec(vec3);
        AABB aABB = this.testInfo.getStructureBounds();
        AABB aABB2 = new AABB(vec32.add(-d, -d, -d), vec32.add(d, d, d));
        return serverLevel.getEntities(entityType, aABB, entity -> entity.getBoundingBox().intersects(aABB2) && entity.isAlive());
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, int n, int n2, int n3) {
        return this.spawn(entityType, new BlockPos(n, n2, n3));
    }

    public <E extends Entity> E spawn(EntityType<E> entityType, float f, float f2, float f3) {
        return this.spawn(entityType, new Vec3(f, f2, f3));
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, BlockPos blockPos) {
        Mob mob = (Mob)this.spawn(entityType, blockPos);
        mob.removeFreeWill();
        return (E)mob;
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, int n, int n2, int n3) {
        return this.spawnWithNoFreeWill(entityType, new BlockPos(n, n2, n3));
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, Vec3 vec3) {
        Mob mob = (Mob)this.spawn(entityType, vec3);
        mob.removeFreeWill();
        return (E)mob;
    }

    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> entityType, float f, float f2, float f3) {
        return this.spawnWithNoFreeWill(entityType, new Vec3(f, f2, f3));
    }

    public void moveTo(Mob mob, float f, float f2, float f3) {
        Vec3 vec3 = this.absoluteVec(new Vec3(f, f2, f3));
        mob.moveTo(vec3.x, vec3.y, vec3.z, mob.getYRot(), mob.getXRot());
    }

    public GameTestSequence walkTo(Mob mob, BlockPos blockPos, float f) {
        return this.startSequence().thenExecuteAfter(2, () -> {
            Path path = mob.getNavigation().createPath(this.absolutePos(blockPos), 0);
            mob.getNavigation().moveTo(path, (double)f);
        });
    }

    public void pressButton(int n, int n2, int n3) {
        this.pressButton(new BlockPos(n, n2, n3));
    }

    public void pressButton(BlockPos blockPos) {
        this.assertBlockState(blockPos, blockState -> blockState.is(BlockTags.BUTTONS), () -> "Expected button");
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockState blockState2 = this.getLevel().getBlockState(blockPos2);
        ButtonBlock buttonBlock = (ButtonBlock)blockState2.getBlock();
        buttonBlock.press(blockState2, this.getLevel(), blockPos2, null);
    }

    public void useBlock(BlockPos blockPos) {
        this.useBlock(blockPos, this.makeMockPlayer(GameType.CREATIVE));
    }

    public void useBlock(BlockPos blockPos, Player player) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        this.useBlock(blockPos, player, new BlockHitResult(Vec3.atCenterOf(blockPos2), Direction.NORTH, blockPos2, true));
    }

    public void useBlock(BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        InteractionHand interactionHand;
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockState blockState = this.getLevel().getBlockState(blockPos2);
        ItemInteractionResult itemInteractionResult = blockState.useItemOn(player.getItemInHand(interactionHand = InteractionHand.MAIN_HAND), this.getLevel(), player, interactionHand, blockHitResult);
        if (itemInteractionResult.consumesAction()) {
            return;
        }
        if (itemInteractionResult == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && blockState.useWithoutItem(this.getLevel(), player, blockHitResult).consumesAction()) {
            return;
        }
        UseOnContext useOnContext = new UseOnContext(player, interactionHand, blockHitResult);
        player.getItemInHand(interactionHand).useOn(useOnContext);
    }

    public LivingEntity makeAboutToDrown(LivingEntity livingEntity) {
        livingEntity.setAirSupply(0);
        livingEntity.setHealth(0.25f);
        return livingEntity;
    }

    public LivingEntity withLowHealth(LivingEntity livingEntity) {
        livingEntity.setHealth(0.25f);
        return livingEntity;
    }

    public Player makeMockPlayer(final GameType gameType) {
        return new Player(this, this.getLevel(), BlockPos.ZERO, 0.0f, new GameProfile(UUID.randomUUID(), "test-mock-player")){

            @Override
            public boolean isSpectator() {
                return gameType == GameType.SPECTATOR;
            }

            @Override
            public boolean isCreative() {
                return gameType.isCreative();
            }

            @Override
            public boolean isLocalPlayer() {
                return true;
            }
        };
    }

    @Deprecated(forRemoval=true)
    public ServerPlayer makeMockServerPlayerInLevel() {
        CommonListenerCookie commonListenerCookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayer serverPlayer = new ServerPlayer(this, this.getLevel().getServer(), this.getLevel(), commonListenerCookie.gameProfile(), commonListenerCookie.clientInformation()){

            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ChannelHandler[]{connection});
        this.getLevel().getServer().getPlayerList().placeNewPlayer(connection, serverPlayer, commonListenerCookie);
        return serverPlayer;
    }

    public void pullLever(int n, int n2, int n3) {
        this.pullLever(new BlockPos(n, n2, n3));
    }

    public void pullLever(BlockPos blockPos) {
        this.assertBlockPresent(Blocks.LEVER, blockPos);
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockState blockState = this.getLevel().getBlockState(blockPos2);
        LeverBlock leverBlock = (LeverBlock)blockState.getBlock();
        leverBlock.pull(blockState, this.getLevel(), blockPos2, null);
    }

    public void pulseRedstone(BlockPos blockPos, long l) {
        this.setBlock(blockPos, Blocks.REDSTONE_BLOCK);
        this.runAfterDelay(l, () -> this.setBlock(blockPos, Blocks.AIR));
    }

    public void destroyBlock(BlockPos blockPos) {
        this.getLevel().destroyBlock(this.absolutePos(blockPos), false, null);
    }

    public void setBlock(int n, int n2, int n3, Block block) {
        this.setBlock(new BlockPos(n, n2, n3), block);
    }

    public void setBlock(int n, int n2, int n3, BlockState blockState) {
        this.setBlock(new BlockPos(n, n2, n3), blockState);
    }

    public void setBlock(BlockPos blockPos, Block block) {
        this.setBlock(blockPos, block.defaultBlockState());
    }

    public void setBlock(BlockPos blockPos, BlockState blockState) {
        this.getLevel().setBlock(this.absolutePos(blockPos), blockState, 3);
    }

    public void setNight() {
        this.setDayTime(13000);
    }

    public void setDayTime(int n) {
        this.getLevel().setDayTime(n);
    }

    public void assertBlockPresent(Block block, int n, int n2, int n3) {
        this.assertBlockPresent(block, new BlockPos(n, n2, n3));
    }

    public void assertBlockPresent(Block block, BlockPos blockPos) {
        BlockState blockState = this.getBlockState(blockPos);
        this.assertBlock(blockPos, (Block block2) -> blockState.is(block), "Expected " + block.getName().getString() + ", got " + blockState.getBlock().getName().getString());
    }

    public void assertBlockNotPresent(Block block, int n, int n2, int n3) {
        this.assertBlockNotPresent(block, new BlockPos(n, n2, n3));
    }

    public void assertBlockNotPresent(Block block, BlockPos blockPos) {
        this.assertBlock(blockPos, (Block block2) -> !this.getBlockState(blockPos).is(block), "Did not expect " + block.getName().getString());
    }

    public void succeedWhenBlockPresent(Block block, int n, int n2, int n3) {
        this.succeedWhenBlockPresent(block, new BlockPos(n, n2, n3));
    }

    public void succeedWhenBlockPresent(Block block, BlockPos blockPos) {
        this.succeedWhen(() -> this.assertBlockPresent(block, blockPos));
    }

    public void assertBlock(BlockPos blockPos, Predicate<Block> predicate, String string) {
        this.assertBlock(blockPos, predicate, () -> string);
    }

    public void assertBlock(BlockPos blockPos, Predicate<Block> predicate, Supplier<String> supplier) {
        this.assertBlockState(blockPos, blockState -> predicate.test(blockState.getBlock()), supplier);
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPos blockPos, Property<T> property, T t) {
        BlockState blockState = this.getBlockState(blockPos);
        boolean bl = blockState.hasProperty(property);
        if (!bl || !blockState.getValue(property).equals(t)) {
            String string = bl ? "was " + String.valueOf(blockState.getValue(property)) : "property " + property.getName() + " is missing";
            String string2 = String.format(Locale.ROOT, "Expected property %s to be %s, %s", property.getName(), t, string);
            throw new GameTestAssertPosException(string2, this.absolutePos(blockPos), blockPos, this.testInfo.getTick());
        }
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPos blockPos, Property<T> property, Predicate<T> predicate, String string) {
        this.assertBlockState(blockPos, blockState -> {
            if (!blockState.hasProperty(property)) {
                return false;
            }
            Object t = blockState.getValue(property);
            return predicate.test(t);
        }, () -> string);
    }

    public void assertBlockState(BlockPos blockPos, Predicate<BlockState> predicate, Supplier<String> supplier) {
        BlockState blockState = this.getBlockState(blockPos);
        if (!predicate.test(blockState)) {
            throw new GameTestAssertPosException(supplier.get(), this.absolutePos(blockPos), blockPos, this.testInfo.getTick());
        }
    }

    public <T extends BlockEntity> void assertBlockEntityData(BlockPos blockPos, Predicate<T> predicate, Supplier<String> supplier) {
        T t = this.getBlockEntity(blockPos);
        if (!predicate.test(t)) {
            throw new GameTestAssertPosException(supplier.get(), this.absolutePos(blockPos), blockPos, this.testInfo.getTick());
        }
    }

    public void assertRedstoneSignal(BlockPos blockPos, Direction direction, IntPredicate intPredicate, Supplier<String> supplier) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        ServerLevel serverLevel = this.getLevel();
        BlockState blockState = serverLevel.getBlockState(blockPos2);
        int n = blockState.getSignal(serverLevel, blockPos2, direction);
        if (!intPredicate.test(n)) {
            throw new GameTestAssertPosException(supplier.get(), blockPos2, blockPos, this.testInfo.getTick());
        }
    }

    public void assertEntityPresent(EntityType<?> entityType) {
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
        if (list.isEmpty()) {
            throw new GameTestAssertException("Expected " + entityType.toShortString() + " to exist");
        }
    }

    public void assertEntityPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.assertEntityPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void assertEntityPresent(EntityType<?> entityType, BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (list.isEmpty()) {
            throw new GameTestAssertPosException("Expected " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
        }
    }

    public void assertEntityPresent(EntityType<?> entityType, Vec3 vec3, Vec3 vec32) {
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(vec3, vec32), Entity::isAlive);
        if (list.isEmpty()) {
            throw new GameTestAssertPosException("Expected " + entityType.toShortString() + " between ", BlockPos.containing(vec3), BlockPos.containing(vec32), this.testInfo.getTick());
        }
    }

    public void assertEntitiesPresent(EntityType<?> entityType, int n) {
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
        if (list.size() != n) {
            throw new GameTestAssertException("Expected " + n + " of type " + entityType.toShortString() + " to exist, found " + list.size());
        }
    }

    public void assertEntitiesPresent(EntityType<?> entityType, BlockPos blockPos, int n, double d) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<?> list = this.getEntities(entityType, blockPos, d);
        if (list.size() != n) {
            throw new GameTestAssertPosException("Expected " + n + " entities of type " + entityType.toShortString() + ", actual number of entities found=" + list.size(), blockPos2, blockPos, this.testInfo.getTick());
        }
    }

    public void assertEntityPresent(EntityType<?> entityType, BlockPos blockPos, double d) {
        List<?> list = this.getEntities(entityType, blockPos, d);
        if (list.isEmpty()) {
            BlockPos blockPos2 = this.absolutePos(blockPos);
            throw new GameTestAssertPosException("Expected " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
        }
    }

    public <T extends Entity> List<T> getEntities(EntityType<T> entityType, BlockPos blockPos, double d) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        return this.getLevel().getEntities(entityType, new AABB(blockPos2).inflate(d), Entity::isAlive);
    }

    public <T extends Entity> List<T> getEntities(EntityType<T> entityType) {
        return this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
    }

    public void assertEntityInstancePresent(Entity entity, int n, int n2, int n3) {
        this.assertEntityInstancePresent(entity, new BlockPos(n, n2, n3));
    }

    public void assertEntityInstancePresent(Entity entity, BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entity.getType(), new AABB(blockPos2), Entity::isAlive);
        list.stream().filter(entity2 -> entity2 == entity).findFirst().orElseThrow(() -> new GameTestAssertPosException("Expected " + entity.getType().toShortString(), blockPos2, blockPos, this.testInfo.getTick()));
    }

    public void assertItemEntityCountIs(Item item, BlockPos blockPos, double d, int n) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, new AABB(blockPos2).inflate(d), Entity::isAlive);
        int n2 = 0;
        for (ItemEntity itemEntity : list) {
            ItemStack itemStack = itemEntity.getItem();
            if (!itemStack.is(item)) continue;
            n2 += itemStack.getCount();
        }
        if (n2 != n) {
            throw new GameTestAssertPosException("Expected " + n + " " + item.getDescription().getString() + " items to exist (found " + n2 + ")", blockPos2, blockPos, this.testInfo.getTick());
        }
    }

    public void assertItemEntityPresent(Item item, BlockPos blockPos, double d) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, new AABB(blockPos2).inflate(d), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (!itemEntity.getItem().getItem().equals(item)) continue;
            return;
        }
        throw new GameTestAssertPosException("Expected " + item.getDescription().getString() + " item", blockPos2, blockPos, this.testInfo.getTick());
    }

    public void assertItemEntityNotPresent(Item item, BlockPos blockPos, double d) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, new AABB(blockPos2).inflate(d), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (!itemEntity.getItem().getItem().equals(item)) continue;
            throw new GameTestAssertPosException("Did not expect " + item.getDescription().getString() + " item", blockPos2, blockPos, this.testInfo.getTick());
        }
    }

    public void assertItemEntityPresent(Item item) {
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, this.getBounds(), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (!itemEntity.getItem().getItem().equals(item)) continue;
            return;
        }
        throw new GameTestAssertException("Expected " + item.getDescription().getString() + " item");
    }

    public void assertItemEntityNotPresent(Item item) {
        List<ItemEntity> list = this.getLevel().getEntities(EntityType.ITEM, this.getBounds(), Entity::isAlive);
        for (Entity entity : list) {
            ItemEntity itemEntity = (ItemEntity)entity;
            if (!itemEntity.getItem().getItem().equals(item)) continue;
            throw new GameTestAssertException("Did not expect " + item.getDescription().getString() + " item");
        }
    }

    public void assertEntityNotPresent(EntityType<?> entityType) {
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), Entity::isAlive);
        if (!list.isEmpty()) {
            throw new GameTestAssertException("Did not expect " + entityType.toShortString() + " to exist");
        }
    }

    public void assertEntityNotPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.assertEntityNotPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void assertEntityNotPresent(EntityType<?> entityType, BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (!list.isEmpty()) {
            throw new GameTestAssertPosException("Did not expect " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
        }
    }

    public void assertEntityNotPresent(EntityType<?> entityType, Vec3 vec3, Vec3 vec32) {
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(vec3, vec32), Entity::isAlive);
        if (!list.isEmpty()) {
            throw new GameTestAssertPosException("Did not expect " + entityType.toShortString() + " between ", BlockPos.containing(vec3), BlockPos.containing(vec32), this.testInfo.getTick());
        }
    }

    public void assertEntityTouching(EntityType<?> entityType, double d, double d2, double d3) {
        Vec3 vec3 = new Vec3(d, d2, d3);
        Vec3 vec32 = this.absoluteVec(vec3);
        Predicate<Entity> predicate = entity -> entity.getBoundingBox().intersects(vec32, vec32);
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), predicate);
        if (list.isEmpty()) {
            throw new GameTestAssertException("Expected " + entityType.toShortString() + " to touch " + String.valueOf(vec32) + " (relative " + String.valueOf(vec3) + ")");
        }
    }

    public void assertEntityNotTouching(EntityType<?> entityType, double d, double d2, double d3) {
        Vec3 vec3 = new Vec3(d, d2, d3);
        Vec3 vec32 = this.absoluteVec(vec3);
        Predicate<Entity> predicate = entity -> !entity.getBoundingBox().intersects(vec32, vec32);
        List<Entity> list = this.getLevel().getEntities(entityType, this.getBounds(), predicate);
        if (list.isEmpty()) {
            throw new GameTestAssertException("Did not expect " + entityType.toShortString() + " to touch " + String.valueOf(vec32) + " (relative " + String.valueOf(vec3) + ")");
        }
    }

    public <E extends Entity, T> void assertEntityData(BlockPos blockPos, EntityType<E> entityType, Function<? super E, T> function, @Nullable T t) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (list.isEmpty()) {
            throw new GameTestAssertPosException("Expected " + entityType.toShortString(), blockPos2, blockPos, this.testInfo.getTick());
        }
        for (Entity entity : list) {
            T t2 = function.apply(entity);
            if (!(t2 == null ? t != null : !t2.equals(t))) continue;
            throw new GameTestAssertException("Expected entity data to be: " + String.valueOf(t) + ", but was: " + String.valueOf(t2));
        }
    }

    public <E extends LivingEntity> void assertEntityIsHolding(BlockPos blockPos, EntityType<E> entityType, Item item) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<LivingEntity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), Entity::isAlive);
        if (list.isEmpty()) {
            throw new GameTestAssertPosException("Expected entity of type: " + String.valueOf(entityType), blockPos2, blockPos, this.getTick());
        }
        for (LivingEntity livingEntity : list) {
            if (!livingEntity.isHolding(item)) continue;
            return;
        }
        throw new GameTestAssertPosException("Entity should be holding: " + String.valueOf(item), blockPos2, blockPos, this.getTick());
    }

    public <E extends Entity> void assertEntityInventoryContains(BlockPos blockPos, EntityType<E> entityType, Item item) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        List<Entity> list = this.getLevel().getEntities(entityType, new AABB(blockPos2), object -> ((Entity)object).isAlive());
        if (list.isEmpty()) {
            throw new GameTestAssertPosException("Expected " + entityType.toShortString() + " to exist", blockPos2, blockPos, this.getTick());
        }
        for (Entity entity : list) {
            if (!((InventoryCarrier)((Object)entity)).getInventory().hasAnyMatching(itemStack -> itemStack.is(item))) continue;
            return;
        }
        throw new GameTestAssertPosException("Entity inventory should contain: " + String.valueOf(item), blockPos2, blockPos, this.getTick());
    }

    public void assertContainerEmpty(BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockEntity blockEntity = this.getLevel().getBlockEntity(blockPos2);
        if (blockEntity instanceof BaseContainerBlockEntity && !((BaseContainerBlockEntity)blockEntity).isEmpty()) {
            throw new GameTestAssertException("Container should be empty");
        }
    }

    public void assertContainerContains(BlockPos blockPos, Item item) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        BlockEntity blockEntity = this.getLevel().getBlockEntity(blockPos2);
        if (!(blockEntity instanceof BaseContainerBlockEntity)) {
            throw new GameTestAssertException("Expected a container at " + String.valueOf(blockPos) + ", found " + String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType())));
        }
        if (((BaseContainerBlockEntity)blockEntity).countItem(item) != 1) {
            throw new GameTestAssertException("Container should contain: " + String.valueOf(item));
        }
    }

    public void assertSameBlockStates(BoundingBox boundingBox, BlockPos blockPos) {
        BlockPos.betweenClosedStream(boundingBox).forEach(blockPos2 -> {
            BlockPos blockPos3 = blockPos.offset(blockPos2.getX() - boundingBox.minX(), blockPos2.getY() - boundingBox.minY(), blockPos2.getZ() - boundingBox.minZ());
            this.assertSameBlockState((BlockPos)blockPos2, blockPos3);
        });
    }

    public void assertSameBlockState(BlockPos blockPos, BlockPos blockPos2) {
        BlockState blockState;
        BlockState blockState2 = this.getBlockState(blockPos);
        if (blockState2 != (blockState = this.getBlockState(blockPos2))) {
            this.fail("Incorrect state. Expected " + String.valueOf(blockState) + ", got " + String.valueOf(blockState2), blockPos);
        }
    }

    public void assertAtTickTimeContainerContains(long l, BlockPos blockPos, Item item) {
        this.runAtTickTime(l, () -> this.assertContainerContains(blockPos, item));
    }

    public void assertAtTickTimeContainerEmpty(long l, BlockPos blockPos) {
        this.runAtTickTime(l, () -> this.assertContainerEmpty(blockPos));
    }

    public <E extends Entity, T> void succeedWhenEntityData(BlockPos blockPos, EntityType<E> entityType, Function<E, T> function, T t) {
        this.succeedWhen(() -> this.assertEntityData(blockPos, entityType, function, t));
    }

    public void assertEntityPosition(Entity entity, AABB aABB, String string) {
        if (!aABB.contains(this.relativeVec(entity.position()))) {
            this.fail(string);
        }
    }

    public <E extends Entity> void assertEntityProperty(E e, Predicate<E> predicate, String string) {
        if (!predicate.test(e)) {
            throw new GameTestAssertException("Entity " + String.valueOf(e) + " failed " + string + " test");
        }
    }

    public <E extends Entity, T> void assertEntityProperty(E e, Function<E, T> function, String string, T t) {
        T t2 = function.apply(e);
        if (!t2.equals(t)) {
            throw new GameTestAssertException("Entity " + String.valueOf(e) + " value " + string + "=" + String.valueOf(t2) + " is not equal to expected " + String.valueOf(t));
        }
    }

    public void assertLivingEntityHasMobEffect(LivingEntity livingEntity, Holder<MobEffect> holder, int n) {
        MobEffectInstance mobEffectInstance = livingEntity.getEffect(holder);
        if (mobEffectInstance == null || mobEffectInstance.getAmplifier() != n) {
            int n2 = n + 1;
            throw new GameTestAssertException("Entity " + String.valueOf(livingEntity) + " failed has " + holder.value().getDescriptionId() + " x " + n2 + " test");
        }
    }

    public void succeedWhenEntityPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.succeedWhenEntityPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void succeedWhenEntityPresent(EntityType<?> entityType, BlockPos blockPos) {
        this.succeedWhen(() -> this.assertEntityPresent(entityType, blockPos));
    }

    public void succeedWhenEntityNotPresent(EntityType<?> entityType, int n, int n2, int n3) {
        this.succeedWhenEntityNotPresent(entityType, new BlockPos(n, n2, n3));
    }

    public void succeedWhenEntityNotPresent(EntityType<?> entityType, BlockPos blockPos) {
        this.succeedWhen(() -> this.assertEntityNotPresent(entityType, blockPos));
    }

    public void succeed() {
        this.testInfo.succeed();
    }

    private void ensureSingleFinalCheck() {
        if (this.finalCheckAdded) {
            throw new IllegalStateException("This test already has final clause");
        }
        this.finalCheckAdded = true;
    }

    public void succeedIf(Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(0L, runnable).thenSucceed();
    }

    public void succeedWhen(Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(runnable).thenSucceed();
    }

    public void succeedOnTickWhen(int n, Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(n, runnable).thenSucceed();
    }

    public void runAtTickTime(long l, Runnable runnable) {
        this.testInfo.setRunAtTickTime(l, runnable);
    }

    public void runAfterDelay(long l, Runnable runnable) {
        this.runAtTickTime(this.testInfo.getTick() + l, runnable);
    }

    public void randomTick(BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        ServerLevel serverLevel = this.getLevel();
        serverLevel.getBlockState(blockPos2).randomTick(serverLevel, blockPos2, serverLevel.random);
    }

    public void tickPrecipitation(BlockPos blockPos) {
        BlockPos blockPos2 = this.absolutePos(blockPos);
        ServerLevel serverLevel = this.getLevel();
        serverLevel.tickPrecipitation(blockPos2);
    }

    public void tickPrecipitation() {
        AABB aABB = this.getRelativeBounds();
        int n = (int)Math.floor(aABB.maxX);
        int n2 = (int)Math.floor(aABB.maxZ);
        int n3 = (int)Math.floor(aABB.maxY);
        for (int i = (int)Math.floor(aABB.minX); i < n; ++i) {
            for (int j = (int)Math.floor(aABB.minZ); j < n2; ++j) {
                this.tickPrecipitation(new BlockPos(i, n3, j));
            }
        }
    }

    public int getHeight(Heightmap.Types types, int n, int n2) {
        BlockPos blockPos = this.absolutePos(new BlockPos(n, 0, n2));
        return this.relativePos(this.getLevel().getHeightmapPos(types, blockPos)).getY();
    }

    public void fail(String string, BlockPos blockPos) {
        throw new GameTestAssertPosException(string, this.absolutePos(blockPos), blockPos, this.getTick());
    }

    public void fail(String string, Entity entity) {
        throw new GameTestAssertPosException(string, entity.blockPosition(), this.relativePos(entity.blockPosition()), this.getTick());
    }

    public void fail(String string) {
        throw new GameTestAssertException(string);
    }

    public void failIf(Runnable runnable) {
        this.testInfo.createSequence().thenWaitUntil(runnable).thenFail(() -> new GameTestAssertException("Fail conditions met"));
    }

    public void failIfEver(Runnable runnable) {
        LongStream.range(this.testInfo.getTick(), this.testInfo.getTimeoutTicks()).forEach(l -> this.testInfo.setRunAtTickTime(l, runnable::run));
    }

    public GameTestSequence startSequence() {
        return this.testInfo.createSequence();
    }

    public BlockPos absolutePos(BlockPos blockPos) {
        BlockPos blockPos2 = this.testInfo.getStructureBlockPos();
        BlockPos blockPos3 = blockPos2.offset(blockPos);
        return StructureTemplate.transform(blockPos3, Mirror.NONE, this.testInfo.getRotation(), blockPos2);
    }

    public BlockPos relativePos(BlockPos blockPos) {
        BlockPos blockPos2 = this.testInfo.getStructureBlockPos();
        Rotation rotation = this.testInfo.getRotation().getRotated(Rotation.CLOCKWISE_180);
        BlockPos blockPos3 = StructureTemplate.transform(blockPos, Mirror.NONE, rotation, blockPos2);
        return blockPos3.subtract(blockPos2);
    }

    public Vec3 absoluteVec(Vec3 vec3) {
        Vec3 vec32 = Vec3.atLowerCornerOf(this.testInfo.getStructureBlockPos());
        return StructureTemplate.transform(vec32.add(vec3), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getStructureBlockPos());
    }

    public Vec3 relativeVec(Vec3 vec3) {
        Vec3 vec32 = Vec3.atLowerCornerOf(this.testInfo.getStructureBlockPos());
        return StructureTemplate.transform(vec3.subtract(vec32), Mirror.NONE, this.testInfo.getRotation(), this.testInfo.getStructureBlockPos());
    }

    public Rotation getTestRotation() {
        return this.testInfo.getRotation();
    }

    public void assertTrue(boolean bl, String string) {
        if (!bl) {
            throw new GameTestAssertException(string);
        }
    }

    public <N> void assertValueEqual(N n, N n2, String string) {
        if (!n.equals(n2)) {
            throw new GameTestAssertException("Expected " + string + " to be " + String.valueOf(n2) + ", but was " + String.valueOf(n));
        }
    }

    public void assertFalse(boolean bl, String string) {
        if (bl) {
            throw new GameTestAssertException(string);
        }
    }

    public long getTick() {
        return this.testInfo.getTick();
    }

    public AABB getBounds() {
        return this.testInfo.getStructureBounds();
    }

    private AABB getRelativeBounds() {
        AABB aABB = this.testInfo.getStructureBounds();
        return aABB.move(BlockPos.ZERO.subtract(this.absolutePos(BlockPos.ZERO)));
    }

    public void forEveryBlockInStructure(Consumer<BlockPos> consumer) {
        AABB aABB = this.getRelativeBounds().contract(1.0, 1.0, 1.0);
        BlockPos.MutableBlockPos.betweenClosedStream(aABB).forEach(consumer);
    }

    public void onEachTick(Runnable runnable) {
        LongStream.range(this.testInfo.getTick(), this.testInfo.getTimeoutTicks()).forEach(l -> this.testInfo.setRunAtTickTime(l, runnable::run));
    }

    public void placeAt(Player player, ItemStack itemStack, BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = this.absolutePos(blockPos.relative(direction));
        BlockHitResult blockHitResult = new BlockHitResult(Vec3.atCenterOf(blockPos2), direction, blockPos2, false);
        UseOnContext useOnContext = new UseOnContext(player, InteractionHand.MAIN_HAND, blockHitResult);
        itemStack.useOn(useOnContext);
    }

    public void setBiome(ResourceKey<Biome> resourceKey) {
        AABB aABB = this.getBounds();
        BlockPos blockPos = BlockPos.containing(aABB.minX, aABB.minY, aABB.minZ);
        BlockPos blockPos2 = BlockPos.containing(aABB.maxX, aABB.maxY, aABB.maxZ);
        Either<Integer, CommandSyntaxException> either = FillBiomeCommand.fill(this.getLevel(), blockPos, blockPos2, this.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(resourceKey));
        if (either.right().isPresent()) {
            this.fail("Failed to set biome for test");
        }
    }
}

