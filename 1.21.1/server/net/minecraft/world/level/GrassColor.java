/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

public class GrassColor {
    private static int[] pixels = new int[65536];

    public static void init(int[] nArray) {
        pixels = nArray;
    }

    public static int get(double d, double d2) {
        int n = (int)((1.0 - (d2 *= d)) * 255.0);
        int n2 = (int)((1.0 - d) * 255.0);
        int n3 = n << 8 | n2;
        if (n3 >= pixels.length) {
            return -65281;
        }
        return pixels[n3];
    }

    public static int getDefaultColor() {
        return GrassColor.get(0.5, 1.0);
    }
}

