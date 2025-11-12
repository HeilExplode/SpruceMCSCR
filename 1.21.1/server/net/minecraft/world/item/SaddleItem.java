/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

public class SaddleItem
extends Item {
    public SaddleItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        if (livingEntity instanceof Saddleable) {
            Saddleable saddleable = (Saddleable)((Object)livingEntity);
            if (livingEntity.isAlive() && !saddleable.isSaddled() && saddleable.isSaddleable()) {
                if (!player.level().isClientSide) {
                    saddleable.equipSaddle(itemStack.split(1), SoundSource.NEUTRAL);
                    livingEntity.level().gameEvent((Entity)livingEntity, GameEvent.EQUIP, livingEntity.position());
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}

