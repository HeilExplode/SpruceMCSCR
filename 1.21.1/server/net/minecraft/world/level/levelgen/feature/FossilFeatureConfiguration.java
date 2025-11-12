/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class FossilFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<FossilFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ResourceLocation.CODEC.listOf().fieldOf("fossil_structures").forGetter(fossilFeatureConfiguration -> fossilFeatureConfiguration.fossilStructures), (App)ResourceLocation.CODEC.listOf().fieldOf("overlay_structures").forGetter(fossilFeatureConfiguration -> fossilFeatureConfiguration.overlayStructures), (App)StructureProcessorType.LIST_CODEC.fieldOf("fossil_processors").forGetter(fossilFeatureConfiguration -> fossilFeatureConfiguration.fossilProcessors), (App)StructureProcessorType.LIST_CODEC.fieldOf("overlay_processors").forGetter(fossilFeatureConfiguration -> fossilFeatureConfiguration.overlayProcessors), (App)Codec.intRange((int)0, (int)7).fieldOf("max_empty_corners_allowed").forGetter(fossilFeatureConfiguration -> fossilFeatureConfiguration.maxEmptyCornersAllowed)).apply((Applicative)instance, FossilFeatureConfiguration::new));
    public final List<ResourceLocation> fossilStructures;
    public final List<ResourceLocation> overlayStructures;
    public final Holder<StructureProcessorList> fossilProcessors;
    public final Holder<StructureProcessorList> overlayProcessors;
    public final int maxEmptyCornersAllowed;

    public FossilFeatureConfiguration(List<ResourceLocation> list, List<ResourceLocation> list2, Holder<StructureProcessorList> holder, Holder<StructureProcessorList> holder2, int n) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Fossil structure lists need at least one entry");
        }
        if (list.size() != list2.size()) {
            throw new IllegalArgumentException("Fossil structure lists must be equal lengths");
        }
        this.fossilStructures = list;
        this.overlayStructures = list2;
        this.fossilProcessors = holder;
        this.overlayProcessors = holder2;
        this.maxEmptyCornersAllowed = n;
    }
}

