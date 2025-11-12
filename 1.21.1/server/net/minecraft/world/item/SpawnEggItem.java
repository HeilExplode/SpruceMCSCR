/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SpawnEggItem
extends Item {
    private static final Map<EntityType<? extends Mob>, SpawnEggItem> BY_ID = Maps.newIdentityHashMap();
    private static final MapCodec<EntityType<?>> ENTITY_TYPE_FIELD_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id");
    private final int backgroundColor;
    private final int highlightColor;
    private final EntityType<?> defaultType;

    public SpawnEggItem(EntityType<? extends Mob> entityType, int n, int n2, Item.Properties properties) {
        super(properties);
        this.defaultType = entityType;
        this.backgroundColor = n;
        this.highlightColor = n2;
        BY_ID.put(entityType, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack itemStack = useOnContext.getItemInHand();
        BlockPos blockPos = useOnContext.getClickedPos();
        Direction direction = useOnContext.getClickedFace();
        BlockState blockState = level.getBlockState(blockPos);
        EntityType<?> entityType = level.getBlockEntity(blockPos);
        if (entityType instanceof Spawner) {
            Spawner spawner = (Spawner)((Object)entityType);
            entityType = this.getType(itemStack);
            spawner.setEntityId(entityType, level.getRandom());
            level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            level.gameEvent((Entity)useOnContext.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
            itemStack.shrink(1);
            return InteractionResult.CONSUME;
        }
        BlockPos blockPos2 = blockState.getCollisionShape(level, blockPos).isEmpty() ? blockPos : blockPos.relative(direction);
        entityType = this.getType(itemStack);
        if (entityType.spawn((ServerLevel)level, itemStack, useOnContext.getPlayer(), blockPos2, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockPos, blockPos2) && direction == Direction.UP) != null) {
            itemStack.shrink(1);
            level.gameEvent((Entity)useOnContext.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult blockHitResult = SpawnEggItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (!(level instanceof ServerLevel)) {
            return InteractionResultHolder.success(itemStack);
        }
        BlockHitResult blockHitResult2 = blockHitResult;
        BlockPos blockPos = blockHitResult2.getBlockPos();
        if (!(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos, blockHitResult2.getDirection(), itemStack)) {
            return InteractionResultHolder.fail(itemStack);
        }
        EntityType<?> entityType = this.getType(itemStack);
        Object obj = entityType.spawn((ServerLevel)level, itemStack, player, blockPos, MobSpawnType.SPAWN_EGG, false, false);
        if (obj == null) {
            return InteractionResultHolder.pass(itemStack);
        }
        itemStack.consume(1, player);
        player.awardStat(Stats.ITEM_USED.get(this));
        level.gameEvent((Entity)player, GameEvent.ENTITY_PLACE, ((Entity)obj).position());
        return InteractionResultHolder.consume(itemStack);
    }

    public boolean spawnsEntity(ItemStack itemStack, EntityType<?> entityType) {
        return Objects.equals(this.getType(itemStack), entityType);
    }

    public int getColor(int n) {
        return n == 0 ? this.backgroundColor : this.highlightColor;
    }

    @Nullable
    public static SpawnEggItem byId(@Nullable EntityType<?> entityType) {
        return BY_ID.get(entityType);
    }

    public static Iterable<SpawnEggItem> eggs() {
        return Iterables.unmodifiableIterable(BY_ID.values());
    }

    public EntityType<?> getType(ItemStack itemStack) {
        CustomData customData = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        if (!customData.isEmpty()) {
            return customData.read(ENTITY_TYPE_FIELD_CODEC).result().orElse(this.defaultType);
        }
        return this.defaultType;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.defaultType.requiredFeatures();
    }

    public Optional<Mob> spawnOffspringFromSpawnEgg(Player player, Mob mob, EntityType<? extends Mob> entityType, ServerLevel serverLevel, Vec3 vec3, ItemStack itemStack) {
        if (!this.spawnsEntity(itemStack, entityType)) {
            return Optional.empty();
        }
        Mob mob2 = mob instanceof AgeableMob ? ((AgeableMob)mob).getBreedOffspring(serverLevel, (AgeableMob)mob) : entityType.create(serverLevel);
        if (mob2 == null) {
            return Optional.empty();
        }
        mob2.setBaby(true);
        if (!mob2.isBaby()) {
            return Optional.empty();
        }
        mob2.moveTo(vec3.x(), vec3.y(), vec3.z(), 0.0f, 0.0f);
        serverLevel.addFreshEntityWithPassengers(mob2);
        mob2.setCustomName(itemStack.get(DataComponents.CUSTOM_NAME));
        itemStack.consume(1, player);
        return Optional.of(mob2);
    }
}

