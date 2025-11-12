/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

public class JigsawReplacementProcessor
extends StructureProcessor {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<JigsawReplacementProcessor> CODEC = MapCodec.unit(() -> INSTANCE);
    public static final JigsawReplacementProcessor INSTANCE = new JigsawReplacementProcessor();

    private JigsawReplacementProcessor() {
    }

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        BlockState blockState;
        BlockState blockState2 = structureBlockInfo2.state();
        if (!blockState2.is(Blocks.JIGSAW)) {
            return structureBlockInfo2;
        }
        if (structureBlockInfo2.nbt() == null) {
            LOGGER.warn("Jigsaw block at {} is missing nbt, will not replace", (Object)blockPos);
            return structureBlockInfo2;
        }
        String string = structureBlockInfo2.nbt().getString("final_state");
        try {
            BlockStateParser.BlockResult blockResult = BlockStateParser.parseForBlock(levelReader.holderLookup(Registries.BLOCK), string, true);
            blockState = blockResult.blockState();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            LOGGER.error("Failed to parse jigsaw replacement state '{}' at {}: {}", new Object[]{string, blockPos, commandSyntaxException.getMessage()});
            return null;
        }
        if (blockState.is(Blocks.STRUCTURE_VOID)) {
            return null;
        }
        return new StructureTemplate.StructureBlockInfo(structureBlockInfo2.pos(), blockState, null);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.JIGSAW_REPLACEMENT;
    }
}

