/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BoatItem
extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private final Boat.Type type;
    private final boolean hasChest;

    public BoatItem(boolean bl, Boat.Type type, Item.Properties properties) {
        super(properties);
        this.hasChest = bl;
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        Object object;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult blockHitResult = BoatItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (((HitResult)blockHitResult).getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }
        Vec3 vec3 = player.getViewVector(1.0f);
        double d = 5.0;
        List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(vec3.scale(5.0)).inflate(1.0), ENTITY_PREDICATE);
        if (!list.isEmpty()) {
            object = player.getEyePosition();
            for (Entity entity : list) {
                AABB aABB = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (!aABB.contains((Vec3)object)) continue;
                return InteractionResultHolder.pass(itemStack);
            }
        }
        if (((HitResult)blockHitResult).getType() == HitResult.Type.BLOCK) {
            object = this.getBoat(level, blockHitResult, itemStack, player);
            ((Boat)object).setVariant(this.type);
            ((Entity)object).setYRot(player.getYRot());
            if (!level.noCollision((Entity)object, ((Entity)object).getBoundingBox())) {
                return InteractionResultHolder.fail(itemStack);
            }
            if (!level.isClientSide) {
                level.addFreshEntity((Entity)object);
                level.gameEvent((Entity)player, GameEvent.ENTITY_PLACE, blockHitResult.getLocation());
                itemStack.consume(1, player);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
        }
        return InteractionResultHolder.pass(itemStack);
    }

    private Boat getBoat(Level level, HitResult hitResult, ItemStack itemStack, Player player) {
        Boat boat;
        Vec3 vec3 = hitResult.getLocation();
        Boat boat2 = boat = this.hasChest ? new ChestBoat(level, vec3.x, vec3.y, vec3.z) : new Boat(level, vec3.x, vec3.y, vec3.z);
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            EntityType.createDefaultStackConfig(serverLevel, itemStack, player).accept((ChestBoat)boat);
        }
        return boat;
    }
}

