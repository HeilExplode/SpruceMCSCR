/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.parsing.packrat;

public interface Control {
    public static final Control UNBOUND = () -> {};

    public void cut();
}

