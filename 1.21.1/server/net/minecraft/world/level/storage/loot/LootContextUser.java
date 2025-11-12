/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot;

import java.util.Set;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public interface LootContextUser {
    default public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of();
    }

    default public void validate(ValidationContext validationContext) {
        validationContext.validateUser(this);
    }
}

