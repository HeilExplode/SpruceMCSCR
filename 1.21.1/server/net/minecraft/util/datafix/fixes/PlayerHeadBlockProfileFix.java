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
import java.util.Optional;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class PlayerHeadBlockProfileFix
extends NamedEntityFix {
    public PlayerHeadBlockProfileFix(Schema schema) {
        super(schema, false, "PlayerHeadBlockProfileFix", References.BLOCK_ENTITY, "minecraft:skull");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fix);
    }

    private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        Optional optional;
        Optional optional2 = dynamic.get("SkullOwner").result();
        Optional optional3 = optional2.or(() -> PlayerHeadBlockProfileFix.lambda$fix$0(optional = dynamic.get("ExtraType").result()));
        if (optional3.isEmpty()) {
            return dynamic;
        }
        dynamic = dynamic.remove("SkullOwner").remove("ExtraType");
        dynamic = dynamic.set("profile", ItemStackComponentizationFix.fixProfile((Dynamic)optional3.get()));
        return dynamic;
    }

    private static /* synthetic */ Optional lambda$fix$0(Optional optional) {
        return optional;
    }
}

