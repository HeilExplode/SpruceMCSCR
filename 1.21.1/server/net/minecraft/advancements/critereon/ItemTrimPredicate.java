/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;

public record ItemTrimPredicate(Optional<HolderSet<TrimMaterial>> material, Optional<HolderSet<TrimPattern>> pattern) implements SingleComponentItemPredicate<ArmorTrim>
{
    public static final Codec<ItemTrimPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.TRIM_MATERIAL).optionalFieldOf("material").forGetter(ItemTrimPredicate::material), (App)RegistryCodecs.homogeneousList(Registries.TRIM_PATTERN).optionalFieldOf("pattern").forGetter(ItemTrimPredicate::pattern)).apply((Applicative)instance, ItemTrimPredicate::new));

    @Override
    public DataComponentType<ArmorTrim> componentType() {
        return DataComponents.TRIM;
    }

    @Override
    public boolean matches(ItemStack itemStack, ArmorTrim armorTrim) {
        if (this.material.isPresent() && !this.material.get().contains(armorTrim.material())) {
            return false;
        }
        return !this.pattern.isPresent() || this.pattern.get().contains(armorTrim.pattern());
    }
}

