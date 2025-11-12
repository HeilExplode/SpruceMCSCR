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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;

public record ItemJukeboxPlayablePredicate(Optional<HolderSet<JukeboxSong>> song) implements SingleComponentItemPredicate<JukeboxPlayable>
{
    public static final Codec<ItemJukeboxPlayablePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.JUKEBOX_SONG).optionalFieldOf("song").forGetter(ItemJukeboxPlayablePredicate::song)).apply((Applicative)instance, ItemJukeboxPlayablePredicate::new));

    @Override
    public DataComponentType<JukeboxPlayable> componentType() {
        return DataComponents.JUKEBOX_PLAYABLE;
    }

    @Override
    public boolean matches(ItemStack itemStack, JukeboxPlayable jukeboxPlayable) {
        if (this.song.isPresent()) {
            boolean bl = false;
            for (Holder holder : this.song.get()) {
                Optional optional = holder.unwrapKey();
                if (optional.isEmpty() || optional.get() != jukeboxPlayable.song().key()) continue;
                bl = true;
                break;
            }
            return bl;
        }
        return true;
    }

    public static ItemJukeboxPlayablePredicate any() {
        return new ItemJukeboxPlayablePredicate(Optional.empty());
    }
}

