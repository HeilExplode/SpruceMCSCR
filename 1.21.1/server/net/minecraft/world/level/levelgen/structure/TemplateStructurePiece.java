/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public abstract class TemplateStructurePiece
extends StructurePiece {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final String templateName;
    protected StructureTemplate template;
    protected StructurePlaceSettings placeSettings;
    protected BlockPos templatePosition;

    public TemplateStructurePiece(StructurePieceType structurePieceType, int n, StructureTemplateManager structureTemplateManager, ResourceLocation resourceLocation, String string, StructurePlaceSettings structurePlaceSettings, BlockPos blockPos) {
        super(structurePieceType, n, structureTemplateManager.getOrCreate(resourceLocation).getBoundingBox(structurePlaceSettings, blockPos));
        this.setOrientation(Direction.NORTH);
        this.templateName = string;
        this.templatePosition = blockPos;
        this.template = structureTemplateManager.getOrCreate(resourceLocation);
        this.placeSettings = structurePlaceSettings;
    }

    public TemplateStructurePiece(StructurePieceType structurePieceType, CompoundTag compoundTag, StructureTemplateManager structureTemplateManager, Function<ResourceLocation, StructurePlaceSettings> function) {
        super(structurePieceType, compoundTag);
        this.setOrientation(Direction.NORTH);
        this.templateName = compoundTag.getString("Template");
        this.templatePosition = new BlockPos(compoundTag.getInt("TPX"), compoundTag.getInt("TPY"), compoundTag.getInt("TPZ"));
        ResourceLocation resourceLocation = this.makeTemplateLocation();
        this.template = structureTemplateManager.getOrCreate(resourceLocation);
        this.placeSettings = function.apply(resourceLocation);
        this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
    }

    protected ResourceLocation makeTemplateLocation() {
        return ResourceLocation.parse(this.templateName);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
        compoundTag.putInt("TPX", this.templatePosition.getX());
        compoundTag.putInt("TPY", this.templatePosition.getY());
        compoundTag.putInt("TPZ", this.templatePosition.getZ());
        compoundTag.putString("Template", this.templateName);
    }

    @Override
    public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        this.placeSettings.setBoundingBox(boundingBox);
        this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
        if (this.template.placeInWorld(worldGenLevel, this.templatePosition, blockPos, this.placeSettings, randomSource, 2)) {
            Object object;
            List<StructureTemplate.StructureBlockInfo> list = this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK);
            for (StructureTemplate.StructureBlockInfo object22 : list) {
                if (object22.nbt() == null || (object = StructureMode.valueOf(object22.nbt().getString("mode"))) != StructureMode.DATA) continue;
                this.handleDataMarker(object22.nbt().getString("metadata"), object22.pos(), worldGenLevel, randomSource, boundingBox);
            }
            List<StructureTemplate.StructureBlockInfo> list2 = this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW);
            Iterator iterator = list2.iterator();
            while (iterator.hasNext()) {
                object = (StructureTemplate.StructureBlockInfo)iterator.next();
                if (((StructureTemplate.StructureBlockInfo)object).nbt() == null) continue;
                String string = ((StructureTemplate.StructureBlockInfo)object).nbt().getString("final_state");
                BlockState blockState = Blocks.AIR.defaultBlockState();
                try {
                    blockState = BlockStateParser.parseForBlock(worldGenLevel.holderLookup(Registries.BLOCK), string, true).blockState();
                }
                catch (CommandSyntaxException commandSyntaxException) {
                    LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", (Object)string, (Object)((StructureTemplate.StructureBlockInfo)object).pos());
                }
                worldGenLevel.setBlock(((StructureTemplate.StructureBlockInfo)object).pos(), blockState, 3);
            }
        }
    }

    protected abstract void handleDataMarker(String var1, BlockPos var2, ServerLevelAccessor var3, RandomSource var4, BoundingBox var5);

    @Override
    @Deprecated
    public void move(int n, int n2, int n3) {
        super.move(n, n2, n3);
        this.templatePosition = this.templatePosition.offset(n, n2, n3);
    }

    @Override
    public Rotation getRotation() {
        return this.placeSettings.getRotation();
    }

    public StructureTemplate template() {
        return this.template;
    }

    public BlockPos templatePosition() {
        return this.templatePosition;
    }

    public StructurePlaceSettings placeSettings() {
        return this.placeSettings;
    }
}

