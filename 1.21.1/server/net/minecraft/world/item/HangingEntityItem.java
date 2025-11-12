/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.world.item;

import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class HangingEntityItem
extends Item {
    private static final Component TOOLTIP_RANDOM_VARIANT = Component.translatable("painting.random").withStyle(ChatFormatting.GRAY);
    private final EntityType<? extends HangingEntity> type;

    public HangingEntityItem(EntityType<? extends HangingEntity> entityType, Item.Properties properties) {
        super(properties);
        this.type = entityType;
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        HangingEntity hangingEntity;
        Optional<Painting> optional;
        BlockPos blockPos = useOnContext.getClickedPos();
        Direction direction = useOnContext.getClickedFace();
        BlockPos blockPos2 = blockPos.relative(direction);
        Player player = useOnContext.getPlayer();
        ItemStack itemStack = useOnContext.getItemInHand();
        if (player != null && !this.mayPlace(player, direction, itemStack, blockPos2)) {
            return InteractionResult.FAIL;
        }
        Level level = useOnContext.getLevel();
        if (this.type == EntityType.PAINTING) {
            optional = Painting.create(level, blockPos2, direction);
            if (optional.isEmpty()) {
                return InteractionResult.CONSUME;
            }
            hangingEntity = optional.get();
        } else if (this.type == EntityType.ITEM_FRAME) {
            hangingEntity = new ItemFrame(level, blockPos2, direction);
        } else if (this.type == EntityType.GLOW_ITEM_FRAME) {
            hangingEntity = new GlowItemFrame(level, blockPos2, direction);
        } else {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        optional = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        if (!((CustomData)((Object)optional)).isEmpty()) {
            EntityType.updateCustomEntityTag(level, player, hangingEntity, (CustomData)((Object)optional));
        }
        if (hangingEntity.survives()) {
            if (!level.isClientSide) {
                hangingEntity.playPlacementSound();
                level.gameEvent((Entity)player, GameEvent.ENTITY_PLACE, hangingEntity.position());
                level.addFreshEntity(hangingEntity);
            }
            itemStack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.CONSUME;
    }

    protected boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos) {
        return !direction.getAxis().isVertical() && player.mayUseItemAt(blockPos, direction, itemStack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        HolderLookup.Provider provider = tooltipContext.registries();
        if (provider != null && this.type == EntityType.PAINTING) {
            CustomData customData = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
            if (!customData.isEmpty()) {
                customData.read((DynamicOps<Tag>)provider.createSerializationContext(NbtOps.INSTANCE), Painting.VARIANT_MAP_CODEC).result().ifPresentOrElse(holder -> {
                    holder.unwrapKey().ifPresent(resourceKey -> {
                        list.add(Component.translatable(resourceKey.location().toLanguageKey("painting", "title")).withStyle(ChatFormatting.YELLOW));
                        list.add(Component.translatable(resourceKey.location().toLanguageKey("painting", "author")).withStyle(ChatFormatting.GRAY));
                    });
                    list.add(Component.translatable("painting.dimensions", ((PaintingVariant)holder.value()).width(), ((PaintingVariant)holder.value()).height()));
                }, () -> list.add(TOOLTIP_RANDOM_VARIANT));
            } else if (tooltipFlag.isCreative()) {
                list.add(TOOLTIP_RANDOM_VARIANT);
            }
        }
    }
}

