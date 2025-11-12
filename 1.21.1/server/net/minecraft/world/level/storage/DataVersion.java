/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage;

public class DataVersion {
    private final int version;
    private final String series;
    public static String MAIN_SERIES = "main";

    public DataVersion(int n) {
        this(n, MAIN_SERIES);
    }

    public DataVersion(int n, String string) {
        this.version = n;
        this.series = string;
    }

    public boolean isSideSeries() {
        return !this.series.equals(MAIN_SERIES);
    }

    public String getSeries() {
        return this.series;
    }

    public int getVersion() {
        return this.version;
    }

    public boolean isCompatible(DataVersion dataVersion) {
        return this.getSeries().equals(dataVersion.getSeries());
    }
}

