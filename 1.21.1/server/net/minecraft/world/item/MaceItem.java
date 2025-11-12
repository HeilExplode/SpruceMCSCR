/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MaceItem
extends Item {
    private static final int DEFAULT_ATTACK_DAMAGE = 5;
    private static final float DEFAULT_ATTACK_SPEED = -3.4f;
    public static final float SMASH_ATTACK_FALL_THRESHOLD = 1.5f;
    private static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0f;
    public static final float SMASH_ATTACK_KNOCKBACK_RADIUS = 3.5f;
    private static final float SMASH_ATTACK_KNOCKBACK_POWER = 0.7f;

    public MaceItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.4f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(), 1.0f, 2);
    }

    @Override
    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        return !player.isCreative();
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        ServerPlayer serverPlayer;
        if (livingEntity2 instanceof ServerPlayer && MaceItem.canSmashAttack(serverPlayer = (ServerPlayer)livingEntity2)) {
            ServerLevel serverLevel = (ServerLevel)livingEntity2.level();
            if (serverPlayer.isIgnoringFallDamageFromCurrentImpulse() && serverPlayer.currentImpulseImpactPos != null) {
                if (serverPlayer.currentImpulseImpactPos.y > serverPlayer.position().y) {
                    serverPlayer.currentImpulseImpactPos = serverPlayer.position();
                }
            } else {
                serverPlayer.currentImpulseImpactPos = serverPlayer.position();
            }
            serverPlayer.setIgnoreFallDamageFromCurrentImpulse(true);
            serverPlayer.setDeltaMovement(serverPlayer.getDeltaMovement().with(Direction.Axis.Y, 0.01f));
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            if (livingEntity.onGround()) {
                serverPlayer.setSpawnExtraParticlesOnFall(true);
                SoundEvent soundEvent = serverPlayer.fallDistance > 5.0f ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
                serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), soundEvent, serverPlayer.getSoundSource(), 1.0f, 1.0f);
            } else {
                serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.MACE_SMASH_AIR, serverPlayer.getSoundSource(), 1.0f, 1.0f);
            }
            MaceItem.knockback(serverLevel, serverPlayer, livingEntity);
        }
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        itemStack.hurtAndBreak(1, livingEntity2, EquipmentSlot.MAINHAND);
        if (MaceItem.canSmashAttack(livingEntity2)) {
            livingEntity2.resetFallDistance();
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack2.is(Items.BREEZE_ROD);
    }

    @Override
    public float getAttackDamageBonus(Entity entity, float f, DamageSource damageSource) {
        Entity entity2 = damageSource.getDirectEntity();
        if (!(entity2 instanceof LivingEntity)) {
            return 0.0f;
        }
        LivingEntity livingEntity = (LivingEntity)entity2;
        if (!MaceItem.canSmashAttack(livingEntity)) {
            return 0.0f;
        }
        float f2 = 3.0f;
        float f3 = 8.0f;
        float f4 = livingEntity.fallDistance;
        float f5 = f4 <= 3.0f ? 4.0f * f4 : (f4 <= 8.0f ? 12.0f + 2.0f * (f4 - 3.0f) : 22.0f + f4 - 8.0f);
        Level level = livingEntity.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return f5 + EnchantmentHelper.modifyFallBasedDamage(serverLevel, livingEntity.getWeaponItem(), entity, damageSource, 0.0f) * f4;
        }
        return f5;
    }

    private static void knockback(Level level, Player player, Entity entity) {
        level.levelEvent(2013, entity.getOnPos(), 750);
        level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(3.5), MaceItem.knockbackPredicate(player, entity)).forEach(livingEntity -> {
            Vec3 vec3 = livingEntity.position().subtract(entity.position());
            double d = MaceItem.getKnockbackPower(player, livingEntity, vec3);
            Vec3 vec32 = vec3.normalize().scale(d);
            if (d > 0.0) {
                livingEntity.push(vec32.x, 0.7f, vec32.z);
                if (livingEntity instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                }
            }
        });
    }

    private static Predicate<LivingEntity> knockbackPredicate(Player player, Entity entity) {
        return livingEntity -> {
            ArmorStand armorStand;
            TamableAnimal tamableAnimal;
            boolean bl = !livingEntity.isSpectator();
            boolean bl2 = livingEntity != player && livingEntity != entity;
            boolean bl3 = !player.isAlliedTo((Entity)livingEntity);
            boolean bl4 = !(livingEntity instanceof TamableAnimal && (tamableAnimal = (TamableAnimal)livingEntity).isTame() && player.getUUID().equals(tamableAnimal.getOwnerUUID()));
            boolean bl5 = !(livingEntity instanceof ArmorStand) || !(armorStand = (ArmorStand)livingEntity).isMarker();
            boolean bl6 = entity.distanceToSqr((Entity)livingEntity) <= Math.pow(3.5, 2.0);
            return bl && bl2 && bl3 && bl4 && bl5 && bl6;
        };
    }

    private static double getKnockbackPower(Player player, LivingEntity livingEntity, Vec3 vec3) {
        return (3.5 - vec3.length()) * (double)0.7f * (double)(player.fallDistance > 5.0f ? 2 : 1) * (1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    }

    public static boolean canSmashAttack(LivingEntity livingEntity) {
        return livingEntity.fallDistance > 1.5f && !livingEntity.isFallFlying();
    }
}

