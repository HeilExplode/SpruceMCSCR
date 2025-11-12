/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public final class JigsawStructure
extends Structure {
    public static final DimensionPadding DEFAULT_DIMENSION_PADDING = DimensionPadding.ZERO;
    public static final LiquidSettings DEFAULT_LIQUID_SETTINGS = LiquidSettings.APPLY_WATERLOGGING;
    public static final int MAX_TOTAL_STRUCTURE_RANGE = 128;
    public static final int MIN_DEPTH = 0;
    public static final int MAX_DEPTH = 20;
    public static final MapCodec<JigsawStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(JigsawStructure.settingsCodec(instance), (App)StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(jigsawStructure -> jigsawStructure.startPool), (App)ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(jigsawStructure -> jigsawStructure.startJigsawName), (App)Codec.intRange((int)0, (int)20).fieldOf("size").forGetter(jigsawStructure -> jigsawStructure.maxDepth), (App)HeightProvider.CODEC.fieldOf("start_height").forGetter(jigsawStructure -> jigsawStructure.startHeight), (App)Codec.BOOL.fieldOf("use_expansion_hack").forGetter(jigsawStructure -> jigsawStructure.useExpansionHack), (App)Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(jigsawStructure -> jigsawStructure.projectStartToHeightmap), (App)Codec.intRange((int)1, (int)128).fieldOf("max_distance_from_center").forGetter(jigsawStructure -> jigsawStructure.maxDistanceFromCenter), (App)Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(jigsawStructure -> jigsawStructure.poolAliases), (App)DimensionPadding.CODEC.optionalFieldOf("dimension_padding", (Object)DEFAULT_DIMENSION_PADDING).forGetter(jigsawStructure -> jigsawStructure.dimensionPadding), (App)LiquidSettings.CODEC.optionalFieldOf("liquid_settings", (Object)DEFAULT_LIQUID_SETTINGS).forGetter(jigsawStructure -> jigsawStructure.liquidSettings)).apply((Applicative)instance, JigsawStructure::new)).validate(JigsawStructure::verifyRange);
    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final List<PoolAliasBinding> poolAliases;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    private static DataResult<JigsawStructure> verifyRange(JigsawStructure jigsawStructure) {
        int n;
        switch (jigsawStructure.terrainAdaptation()) {
            default: {
                throw new MatchException(null, null);
            }
            case NONE: {
                int n2 = 0;
                break;
            }
            case BURY: 
            case BEARD_THIN: 
            case BEARD_BOX: 
            case ENCAPSULATE: {
                int n2 = n = 12;
            }
        }
        if (jigsawStructure.maxDistanceFromCenter + n > 128) {
            return DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128");
        }
        return DataResult.success((Object)jigsawStructure);
    }

    public JigsawStructure(Structure.StructureSettings structureSettings, Holder<StructureTemplatePool> holder, Optional<ResourceLocation> optional, int n, HeightProvider heightProvider, boolean bl, Optional<Heightmap.Types> optional2, int n2, List<PoolAliasBinding> list, DimensionPadding dimensionPadding, LiquidSettings liquidSettings) {
        super(structureSettings);
        this.startPool = holder;
        this.startJigsawName = optional;
        this.maxDepth = n;
        this.startHeight = heightProvider;
        this.useExpansionHack = bl;
        this.projectStartToHeightmap = optional2;
        this.maxDistanceFromCenter = n2;
        this.poolAliases = list;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    public JigsawStructure(Structure.StructureSettings structureSettings, Holder<StructureTemplatePool> holder, int n, HeightProvider heightProvider, boolean bl, Heightmap.Types types) {
        this(structureSettings, holder, Optional.empty(), n, heightProvider, bl, Optional.of(types), 80, List.of(), DEFAULT_DIMENSION_PADDING, DEFAULT_LIQUID_SETTINGS);
    }

    public JigsawStructure(Structure.StructureSettings structureSettings, Holder<StructureTemplatePool> holder, int n, HeightProvider heightProvider, boolean bl) {
        this(structureSettings, holder, Optional.empty(), n, heightProvider, bl, Optional.empty(), 80, List.of(), DEFAULT_DIMENSION_PADDING, DEFAULT_LIQUID_SETTINGS);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        ChunkPos chunkPos = generationContext.chunkPos();
        int n = this.startHeight.sample(generationContext.random(), new WorldGenerationContext(generationContext.chunkGenerator(), generationContext.heightAccessor()));
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), n, chunkPos.getMinBlockZ());
        return JigsawPlacement.addPieces(generationContext, this.startPool, this.startJigsawName, this.maxDepth, blockPos, this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter, PoolAliasLookup.create(this.poolAliases, blockPos, generationContext.seed()), this.dimensionPadding, this.liquidSettings);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.JIGSAW;
    }
}

