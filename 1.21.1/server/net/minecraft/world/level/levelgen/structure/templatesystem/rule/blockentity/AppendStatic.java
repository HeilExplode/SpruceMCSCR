/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType;

public class AppendStatic
implements RuleBlockEntityModifier {
    public static final MapCodec<AppendStatic> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CompoundTag.CODEC.fieldOf("data").forGetter(appendStatic -> appendStatic.tag)).apply((Applicative)instance, AppendStatic::new));
    private final CompoundTag tag;

    public AppendStatic(CompoundTag compoundTag) {
        this.tag = compoundTag;
    }

    @Override
    public CompoundTag apply(RandomSource randomSource, @Nullable CompoundTag compoundTag) {
        return compoundTag == null ? this.tag.copy() : compoundTag.merge(this.tag);
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.APPEND_STATIC;
    }
}

