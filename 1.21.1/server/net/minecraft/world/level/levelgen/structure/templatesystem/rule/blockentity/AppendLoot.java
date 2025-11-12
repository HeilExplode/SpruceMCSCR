/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

public class AppendLoot
implements RuleBlockEntityModifier {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<AppendLoot> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(appendLoot -> appendLoot.lootTable)).apply((Applicative)instance, AppendLoot::new));
    private final ResourceKey<LootTable> lootTable;

    public AppendLoot(ResourceKey<LootTable> resourceKey) {
        this.lootTable = resourceKey;
    }

    @Override
    public CompoundTag apply(RandomSource randomSource, @Nullable CompoundTag compoundTag) {
        CompoundTag compoundTag2 = compoundTag == null ? new CompoundTag() : compoundTag.copy();
        ResourceKey.codec(Registries.LOOT_TABLE).encodeStart((DynamicOps)NbtOps.INSTANCE, this.lootTable).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag2.put("LootTable", (Tag)tag));
        compoundTag2.putLong("LootTableSeed", randomSource.nextLong());
        return compoundTag2;
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.APPEND_LOOT;
    }
}

