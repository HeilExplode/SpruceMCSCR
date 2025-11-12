/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.world.inventory;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.StringRepresentable;

public interface SlotRange
extends StringRepresentable {
    public IntList slots();

    default public int size() {
        return this.slots().size();
    }

    public static SlotRange of(final String string, final IntList intList) {
        return new SlotRange(){

            @Override
            public IntList slots() {
                return intList;
            }

            @Override
            public String getSerializedName() {
                return string;
            }

            public String toString() {
                return string;
            }
        };
    }
}

