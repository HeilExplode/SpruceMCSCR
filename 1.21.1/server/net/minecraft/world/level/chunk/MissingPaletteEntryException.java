/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.chunk;

public class MissingPaletteEntryException
extends RuntimeException {
    public MissingPaletteEntryException(int n) {
        super("Missing Palette entry for index " + n + ".");
    }
}

