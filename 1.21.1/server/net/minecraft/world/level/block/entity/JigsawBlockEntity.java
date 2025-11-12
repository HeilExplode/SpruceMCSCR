/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class JigsawBlockEntity
extends BlockEntity {
    public static final String TARGET = "target";
    public static final String POOL = "pool";
    public static final String JOINT = "joint";
    public static final String PLACEMENT_PRIORITY = "placement_priority";
    public static final String SELECTION_PRIORITY = "selection_priority";
    public static final String NAME = "name";
    public static final String FINAL_STATE = "final_state";
    private ResourceLocation name = ResourceLocation.withDefaultNamespace("empty");
    private ResourceLocation target = ResourceLocation.withDefaultNamespace("empty");
    private ResourceKey<StructureTemplatePool> pool = ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.withDefaultNamespace("empty"));
    private JointType joint = JointType.ROLLABLE;
    private String finalState = "minecraft:air";
    private int placementPriority;
    private int selectionPriority;

    public JigsawBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.JIGSAW, blockPos, blockState);
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public ResourceLocation getTarget() {
        return this.target;
    }

    public ResourceKey<StructureTemplatePool> getPool() {
        return this.pool;
    }

    public String getFinalState() {
        return this.finalState;
    }

    public JointType getJoint() {
        return this.joint;
    }

    public int getPlacementPriority() {
        return this.placementPriority;
    }

    public int getSelectionPriority() {
        return this.selectionPriority;
    }

    public void setName(ResourceLocation resourceLocation) {
        this.name = resourceLocation;
    }

    public void setTarget(ResourceLocation resourceLocation) {
        this.target = resourceLocation;
    }

    public void setPool(ResourceKey<StructureTemplatePool> resourceKey) {
        this.pool = resourceKey;
    }

    public void setFinalState(String string) {
        this.finalState = string;
    }

    public void setJoint(JointType jointType) {
        this.joint = jointType;
    }

    public void setPlacementPriority(int n) {
        this.placementPriority = n;
    }

    public void setSelectionPriority(int n) {
        this.selectionPriority = n;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        compoundTag.putString(NAME, this.name.toString());
        compoundTag.putString(TARGET, this.target.toString());
        compoundTag.putString(POOL, this.pool.location().toString());
        compoundTag.putString(FINAL_STATE, this.finalState);
        compoundTag.putString(JOINT, this.joint.getSerializedName());
        compoundTag.putInt(PLACEMENT_PRIORITY, this.placementPriority);
        compoundTag.putInt(SELECTION_PRIORITY, this.selectionPriority);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.name = ResourceLocation.parse(compoundTag.getString(NAME));
        this.target = ResourceLocation.parse(compoundTag.getString(TARGET));
        this.pool = ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.parse(compoundTag.getString(POOL)));
        this.finalState = compoundTag.getString(FINAL_STATE);
        this.joint = JointType.byName(compoundTag.getString(JOINT)).orElseGet(() -> JigsawBlock.getFrontFacing(this.getBlockState()).getAxis().isHorizontal() ? JointType.ALIGNED : JointType.ROLLABLE);
        this.placementPriority = compoundTag.getInt(PLACEMENT_PRIORITY);
        this.selectionPriority = compoundTag.getInt(SELECTION_PRIORITY);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    public void generate(ServerLevel serverLevel, int n, boolean bl) {
        BlockPos blockPos = this.getBlockPos().relative(this.getBlockState().getValue(JigsawBlock.ORIENTATION).front());
        Registry<StructureTemplatePool> registry = serverLevel.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        Holder.Reference<StructureTemplatePool> reference = registry.getHolderOrThrow(this.pool);
        JigsawPlacement.generateJigsaw(serverLevel, reference, this.target, n, blockPos, bl);
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }

    public static enum JointType implements StringRepresentable
    {
        ROLLABLE("rollable"),
        ALIGNED("aligned");

        private final String name;

        private JointType(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static Optional<JointType> byName(String string) {
            return Arrays.stream(JointType.values()).filter(jointType -> jointType.getSerializedName().equals(string)).findFirst();
        }

        public Component getTranslatedName() {
            return Component.translatable("jigsaw_block.joint." + this.name);
        }
    }
}

