/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.pathfinder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Target;

public class PathFinder {
    private static final float FUDGING = 1.5f;
    private final Node[] neighbors = new Node[32];
    private final int maxVisitedNodes;
    private final NodeEvaluator nodeEvaluator;
    private static final boolean DEBUG = false;
    private final BinaryHeap openSet = new BinaryHeap();

    public PathFinder(NodeEvaluator nodeEvaluator, int n) {
        this.nodeEvaluator = nodeEvaluator;
        this.maxVisitedNodes = n;
    }

    @Nullable
    public Path findPath(PathNavigationRegion pathNavigationRegion, Mob mob, Set<BlockPos> set, float f, int n, float f2) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(pathNavigationRegion, mob);
        Node node = this.nodeEvaluator.getStart();
        if (node == null) {
            return null;
        }
        Map<Target, BlockPos> map = set.stream().collect(Collectors.toMap(blockPos -> this.nodeEvaluator.getTarget(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Function.identity()));
        Path path = this.findPath(pathNavigationRegion.getProfiler(), node, map, f, n, f2);
        this.nodeEvaluator.done();
        return path;
    }

    @Nullable
    private Path findPath(ProfilerFiller profilerFiller, Node node, Map<Target, BlockPos> map, float f, int n, float f2) {
        Object object;
        profilerFiller.push("find_path");
        profilerFiller.markForCharting(MetricCategory.PATH_FINDING);
        Set<Target> set = map.keySet();
        node.g = 0.0f;
        node.f = node.h = this.getBestH(node, set);
        this.openSet.clear();
        this.openSet.insert(node);
        ImmutableSet immutableSet = ImmutableSet.of();
        int n2 = 0;
        HashSet hashSet = Sets.newHashSetWithExpectedSize((int)set.size());
        int n3 = (int)((float)this.maxVisitedNodes * f2);
        while (!this.openSet.isEmpty() && ++n2 < n3) {
            object = this.openSet.pop();
            ((Node)object).closed = true;
            for (Target target2 : set) {
                if (!(((Node)object).distanceManhattan(target2) <= (float)n)) continue;
                target2.setReached();
                hashSet.add(target2);
            }
            if (!hashSet.isEmpty()) break;
            if (((Node)object).distanceTo(node) >= f) continue;
            int n4 = this.nodeEvaluator.getNeighbors(this.neighbors, (Node)object);
            for (int i = 0; i < n4; ++i) {
                Node node2 = this.neighbors[i];
                float f3 = this.distance((Node)object, node2);
                node2.walkedDistance = ((Node)object).walkedDistance + f3;
                float f4 = ((Node)object).g + f3 + node2.costMalus;
                if (!(node2.walkedDistance < f) || node2.inOpenSet() && !(f4 < node2.g)) continue;
                node2.cameFrom = object;
                node2.g = f4;
                node2.h = this.getBestH(node2, set) * 1.5f;
                if (node2.inOpenSet()) {
                    this.openSet.changeCost(node2, node2.g + node2.h);
                    continue;
                }
                node2.f = node2.g + node2.h;
                this.openSet.insert(node2);
            }
        }
        object = !hashSet.isEmpty() ? hashSet.stream().map(target -> this.reconstructPath(target.getBestNode(), (BlockPos)map.get(target), true)).min(Comparator.comparingInt(Path::getNodeCount)) : set.stream().map(target -> this.reconstructPath(target.getBestNode(), (BlockPos)map.get(target), false)).min(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
        profilerFiller.pop();
        if (((Optional)object).isEmpty()) {
            return null;
        }
        Path path = (Path)((Optional)object).get();
        return path;
    }

    protected float distance(Node node, Node node2) {
        return node.distanceTo(node2);
    }

    private float getBestH(Node node, Set<Target> set) {
        float f = Float.MAX_VALUE;
        for (Target target : set) {
            float f2 = node.distanceTo(target);
            target.updateBest(f2, node);
            f = Math.min(f2, f);
        }
        return f;
    }

    private Path reconstructPath(Node node, BlockPos blockPos, boolean bl) {
        ArrayList arrayList = Lists.newArrayList();
        Node node2 = node;
        arrayList.add(0, node2);
        while (node2.cameFrom != null) {
            node2 = node2.cameFrom;
            arrayList.add(0, node2);
        }
        return new Path(arrayList, blockPos, bl);
    }

    private static /* synthetic */ Node[] lambda$findPath$3(int n) {
        return new Node[n];
    }
}

