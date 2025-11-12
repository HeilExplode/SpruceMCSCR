/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import net.minecraft.world.level.ChunkPos;

public interface ChunkTrackingView {
    public static final ChunkTrackingView EMPTY = new ChunkTrackingView(){

        @Override
        public boolean contains(int n, int n2, boolean bl) {
            return false;
        }

        @Override
        public void forEach(Consumer<ChunkPos> consumer) {
        }
    };

    public static ChunkTrackingView of(ChunkPos chunkPos, int n) {
        return new Positioned(chunkPos, n);
    }

    /*
     * Enabled aggressive block sorting
     */
    public static void difference(ChunkTrackingView chunkTrackingView, ChunkTrackingView chunkTrackingView2, Consumer<ChunkPos> consumer, Consumer<ChunkPos> consumer2) {
        Positioned positioned;
        Positioned positioned2;
        block8: {
            block7: {
                if (chunkTrackingView.equals(chunkTrackingView2)) {
                    return;
                }
                if (!(chunkTrackingView instanceof Positioned)) break block7;
                positioned2 = (Positioned)chunkTrackingView;
                if (chunkTrackingView2 instanceof Positioned && positioned2.squareIntersects(positioned = (Positioned)chunkTrackingView2)) break block8;
            }
            chunkTrackingView.forEach(consumer2);
            chunkTrackingView2.forEach(consumer);
            return;
        }
        int n = Math.min(positioned2.minX(), positioned.minX());
        int n2 = Math.min(positioned2.minZ(), positioned.minZ());
        int n3 = Math.max(positioned2.maxX(), positioned.maxX());
        int n4 = Math.max(positioned2.maxZ(), positioned.maxZ());
        int n5 = n;
        while (n5 <= n3) {
            for (int i = n2; i <= n4; ++i) {
                boolean bl;
                boolean bl2 = positioned2.contains(n5, i);
                if (bl2 == (bl = positioned.contains(n5, i))) continue;
                if (bl) {
                    consumer.accept(new ChunkPos(n5, i));
                    continue;
                }
                consumer2.accept(new ChunkPos(n5, i));
            }
            ++n5;
        }
        return;
    }

    default public boolean contains(ChunkPos chunkPos) {
        return this.contains(chunkPos.x, chunkPos.z);
    }

    default public boolean contains(int n, int n2) {
        return this.contains(n, n2, true);
    }

    public boolean contains(int var1, int var2, boolean var3);

    public void forEach(Consumer<ChunkPos> var1);

    default public boolean isInViewDistance(int n, int n2) {
        return this.contains(n, n2, false);
    }

    public static boolean isInViewDistance(int n, int n2, int n3, int n4, int n5) {
        return ChunkTrackingView.isWithinDistance(n, n2, n3, n4, n5, false);
    }

    public static boolean isWithinDistance(int n, int n2, int n3, int n4, int n5, boolean bl) {
        int n6 = Math.max(0, Math.abs(n4 - n) - 1);
        int n7 = Math.max(0, Math.abs(n5 - n2) - 1);
        long l = Math.max(0, Math.max(n6, n7) - (bl ? 1 : 0));
        long l2 = Math.min(n6, n7);
        long l3 = l2 * l2 + l * l;
        int n8 = n3 * n3;
        return l3 < (long)n8;
    }

    public record Positioned(ChunkPos center, int viewDistance) implements ChunkTrackingView
    {
        int minX() {
            return this.center.x - this.viewDistance - 1;
        }

        int minZ() {
            return this.center.z - this.viewDistance - 1;
        }

        int maxX() {
            return this.center.x + this.viewDistance + 1;
        }

        int maxZ() {
            return this.center.z + this.viewDistance + 1;
        }

        @VisibleForTesting
        protected boolean squareIntersects(Positioned positioned) {
            return this.minX() <= positioned.maxX() && this.maxX() >= positioned.minX() && this.minZ() <= positioned.maxZ() && this.maxZ() >= positioned.minZ();
        }

        @Override
        public boolean contains(int n, int n2, boolean bl) {
            return ChunkTrackingView.isWithinDistance(this.center.x, this.center.z, this.viewDistance, n, n2, bl);
        }

        @Override
        public void forEach(Consumer<ChunkPos> consumer) {
            for (int i = this.minX(); i <= this.maxX(); ++i) {
                for (int j = this.minZ(); j <= this.maxZ(); ++j) {
                    if (!this.contains(i, j)) continue;
                    consumer.accept(new ChunkPos(i, j));
                }
            }
        }
    }
}

