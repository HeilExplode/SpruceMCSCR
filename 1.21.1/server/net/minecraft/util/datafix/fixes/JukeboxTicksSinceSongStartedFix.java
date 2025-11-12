/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class JukeboxTicksSinceSongStartedFix
extends NamedEntityFix {
    public JukeboxTicksSinceSongStartedFix(Schema schema) {
        super(schema, false, "JukeboxTicksSinceSongStartedFix", References.BLOCK_ENTITY, "minecraft:jukebox");
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        long l = dynamic.get("TickCount").asLong(0L) - dynamic.get("RecordStartTick").asLong(0L);
        Dynamic dynamic2 = dynamic.remove("IsPlaying").remove("TickCount").remove("RecordStartTick");
        if (l > 0L) {
            return dynamic2.set("ticks_since_song_started", dynamic.createLong(l));
        }
        return dynamic2;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}

