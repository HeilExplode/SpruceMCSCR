/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

public class OminousBottleItem
extends Item {
    private static final int DRINK_DURATION = 32;
    public static final int EFFECT_DURATION = 120000;
    public static final int MIN_AMPLIFIER = 0;
    public static final int MAX_AMPLIFIER = 4;

    public OminousBottleItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        Object object;
        if (livingEntity instanceof ServerPlayer) {
            object = (ServerPlayer)livingEntity;
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)object, itemStack);
            ((Player)object).awardStat(Stats.ITEM_USED.get(this));
        }
        if (!level.isClientSide) {
            level.playSound(null, livingEntity.blockPosition(), SoundEvents.OMINOUS_BOTTLE_DISPOSE, livingEntity.getSoundSource(), 1.0f, 1.0f);
            object = itemStack.getOrDefault(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, 0);
            livingEntity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 120000, (Integer)object, false, false, true));
        }
        itemStack.consume(1, livingEntity);
        return itemStack;
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        return ItemUtils.startUsingInstantly(level, player, interactionHand);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        Integer n = itemStack.getOrDefault(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, 0);
        List<MobEffectInstance> list2 = List.of(new MobEffectInstance(MobEffects.BAD_OMEN, 120000, n, false, false, true));
        PotionContents.addPotionTooltip(list2, list::add, 1.0f, tooltipContext.tickRate());
    }
}

