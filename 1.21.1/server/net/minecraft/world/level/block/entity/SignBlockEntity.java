/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.FilteredText;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SignBlockEntity
extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TEXT_LINE_WIDTH = 90;
    private static final int TEXT_LINE_HEIGHT = 10;
    @Nullable
    private UUID playerWhoMayEdit;
    private SignText frontText = this.createDefaultSignText();
    private SignText backText = this.createDefaultSignText();
    private boolean isWaxed;

    public SignBlockEntity(BlockPos blockPos, BlockState blockState) {
        this((BlockEntityType)BlockEntityType.SIGN, blockPos, blockState);
    }

    public SignBlockEntity(BlockEntityType blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    protected SignText createDefaultSignText() {
        return new SignText();
    }

    public boolean isFacingFrontText(Player player) {
        Object object = this.getBlockState().getBlock();
        if (object instanceof SignBlock) {
            float f;
            SignBlock signBlock = (SignBlock)object;
            object = signBlock.getSignHitboxCenterPosition(this.getBlockState());
            double d = player.getX() - ((double)this.getBlockPos().getX() + ((Vec3)object).x);
            double d2 = player.getZ() - ((double)this.getBlockPos().getZ() + ((Vec3)object).z);
            float f2 = signBlock.getYRotationDegrees(this.getBlockState());
            return Mth.degreesDifferenceAbs(f2, f = (float)(Mth.atan2(d2, d) * 57.2957763671875) - 90.0f) <= 90.0f;
        }
        return false;
    }

    public SignText getText(boolean bl) {
        return bl ? this.frontText : this.backText;
    }

    public SignText getFrontText() {
        return this.frontText;
    }

    public SignText getBackText() {
        return this.backText;
    }

    public int getTextLineHeight() {
        return 10;
    }

    public int getMaxTextLineWidth() {
        return 90;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
        SignText.DIRECT_CODEC.encodeStart(registryOps, (Object)this.frontText).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("front_text", (Tag)tag));
        SignText.DIRECT_CODEC.encodeStart(registryOps, (Object)this.backText).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("back_text", (Tag)tag));
        compoundTag.putBoolean("is_waxed", this.isWaxed);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
        if (compoundTag.contains("front_text")) {
            SignText.DIRECT_CODEC.parse(registryOps, (Object)compoundTag.getCompound("front_text")).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(signText -> {
                this.frontText = this.loadLines((SignText)signText);
            });
        }
        if (compoundTag.contains("back_text")) {
            SignText.DIRECT_CODEC.parse(registryOps, (Object)compoundTag.getCompound("back_text")).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(signText -> {
                this.backText = this.loadLines((SignText)signText);
            });
        }
        this.isWaxed = compoundTag.getBoolean("is_waxed");
    }

    private SignText loadLines(SignText signText) {
        for (int i = 0; i < 4; ++i) {
            Component component = this.loadLine(signText.getMessage(i, false));
            Component component2 = this.loadLine(signText.getMessage(i, true));
            signText = signText.setMessage(i, component, component2);
        }
        return signText;
    }

    private Component loadLine(Component component) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            try {
                return ComponentUtils.updateForEntity(SignBlockEntity.createCommandSourceStack(null, serverLevel, this.worldPosition), component, null, 0);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }
        return component;
    }

    public void updateSignText(Player player, boolean bl, List<FilteredText> list) {
        if (this.isWaxed() || !player.getUUID().equals(this.getPlayerWhoMayEdit()) || this.level == null) {
            LOGGER.warn("Player {} just tried to change non-editable sign", (Object)player.getName().getString());
            return;
        }
        this.updateText(signText -> this.setMessages(player, list, (SignText)signText), bl);
        this.setAllowedPlayerEditor(null);
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean updateText(UnaryOperator<SignText> unaryOperator, boolean bl) {
        SignText signText = this.getText(bl);
        return this.setText((SignText)unaryOperator.apply(signText), bl);
    }

    private SignText setMessages(Player player, List<FilteredText> list, SignText signText) {
        for (int i = 0; i < list.size(); ++i) {
            FilteredText filteredText = list.get(i);
            Style style = signText.getMessage(i, player.isTextFilteringEnabled()).getStyle();
            signText = player.isTextFilteringEnabled() ? signText.setMessage(i, Component.literal(filteredText.filteredOrEmpty()).setStyle(style)) : signText.setMessage(i, Component.literal(filteredText.raw()).setStyle(style), Component.literal(filteredText.filteredOrEmpty()).setStyle(style));
        }
        return signText;
    }

    public boolean setText(SignText signText, boolean bl) {
        return bl ? this.setFrontText(signText) : this.setBackText(signText);
    }

    private boolean setBackText(SignText signText) {
        if (signText != this.backText) {
            this.backText = signText;
            this.markUpdated();
            return true;
        }
        return false;
    }

    private boolean setFrontText(SignText signText) {
        if (signText != this.frontText) {
            this.frontText = signText;
            this.markUpdated();
            return true;
        }
        return false;
    }

    public boolean canExecuteClickCommands(boolean bl, Player player) {
        return this.isWaxed() && this.getText(bl).hasAnyClickCommands(player);
    }

    public boolean executeClickCommandsIfPresent(Player player, Level level, BlockPos blockPos, boolean bl) {
        boolean bl2 = false;
        for (Component component : this.getText(bl).getMessages(player.isTextFilteringEnabled())) {
            Style style = component.getStyle();
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent == null || clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) continue;
            player.getServer().getCommands().performPrefixedCommand(SignBlockEntity.createCommandSourceStack(player, level, blockPos), clickEvent.getValue());
            bl2 = true;
        }
        return bl2;
    }

    private static CommandSourceStack createCommandSourceStack(@Nullable Player player, Level level, BlockPos blockPos) {
        String string = player == null ? "Sign" : player.getName().getString();
        Component component = player == null ? Component.literal("Sign") : player.getDisplayName();
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(blockPos), Vec2.ZERO, (ServerLevel)level, 2, string, component, level.getServer(), player);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public void setAllowedPlayerEditor(@Nullable UUID uUID) {
        this.playerWhoMayEdit = uUID;
    }

    @Nullable
    public UUID getPlayerWhoMayEdit() {
        return this.playerWhoMayEdit;
    }

    private void markUpdated() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean isWaxed() {
        return this.isWaxed;
    }

    public boolean setWaxed(boolean bl) {
        if (this.isWaxed != bl) {
            this.isWaxed = bl;
            this.markUpdated();
            return true;
        }
        return false;
    }

    public boolean playerIsTooFarAwayToEdit(UUID uUID) {
        Player player = this.level.getPlayerByUUID(uUID);
        return player == null || !player.canInteractWithBlock(this.getBlockPos(), 4.0);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SignBlockEntity signBlockEntity) {
        UUID uUID = signBlockEntity.getPlayerWhoMayEdit();
        if (uUID != null) {
            signBlockEntity.clearInvalidPlayerWhoMayEdit(signBlockEntity, level, uUID);
        }
    }

    private void clearInvalidPlayerWhoMayEdit(SignBlockEntity signBlockEntity, Level level, UUID uUID) {
        if (signBlockEntity.playerIsTooFarAwayToEdit(uUID)) {
            signBlockEntity.setAllowedPlayerEditor(null);
        }
    }

    public SoundEvent getSignInteractionFailedSoundEvent() {
        return SoundEvents.WAXED_SIGN_INTERACT_FAIL;
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }
}

