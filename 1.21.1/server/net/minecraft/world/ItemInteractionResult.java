/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

import net.minecraft.world.InteractionResult;

public enum ItemInteractionResult {
    SUCCESS,
    CONSUME,
    CONSUME_PARTIAL,
    PASS_TO_DEFAULT_BLOCK_INTERACTION,
    SKIP_DEFAULT_BLOCK_INTERACTION,
    FAIL;


    public boolean consumesAction() {
        return this.result().consumesAction();
    }

    public static ItemInteractionResult sidedSuccess(boolean bl) {
        return bl ? SUCCESS : CONSUME;
    }

    public InteractionResult result() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> InteractionResult.SUCCESS;
            case 1 -> InteractionResult.CONSUME;
            case 2 -> InteractionResult.CONSUME_PARTIAL;
            case 3, 4 -> InteractionResult.PASS;
            case 5 -> InteractionResult.FAIL;
        };
    }
}

