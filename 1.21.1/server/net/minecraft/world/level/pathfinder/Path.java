/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.pathfinder;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.Vec3;

public class Path {
    private final List<Node> nodes;
    @Nullable
    private DebugData debugData;
    private int nextNodeIndex;
    private final BlockPos target;
    private final float distToTarget;
    private final boolean reached;

    public Path(List<Node> list, BlockPos blockPos, boolean bl) {
        this.nodes = list;
        this.target = blockPos;
        this.distToTarget = list.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).distanceManhattan(this.target);
        this.reached = bl;
    }

    public void advance() {
        ++this.nextNodeIndex;
    }

    public boolean notStarted() {
        return this.nextNodeIndex <= 0;
    }

    public boolean isDone() {
        return this.nextNodeIndex >= this.nodes.size();
    }

    @Nullable
    public Node getEndNode() {
        if (!this.nodes.isEmpty()) {
            return this.nodes.get(this.nodes.size() - 1);
        }
        return null;
    }

    public Node getNode(int n) {
        return this.nodes.get(n);
    }

    public void truncateNodes(int n) {
        if (this.nodes.size() > n) {
            this.nodes.subList(n, this.nodes.size()).clear();
        }
    }

    public void replaceNode(int n, Node node) {
        this.nodes.set(n, node);
    }

    public int getNodeCount() {
        return this.nodes.size();
    }

    public int getNextNodeIndex() {
        return this.nextNodeIndex;
    }

    public void setNextNodeIndex(int n) {
        this.nextNodeIndex = n;
    }

    public Vec3 getEntityPosAtNode(Entity entity, int n) {
        Node node = this.nodes.get(n);
        double d = (double)node.x + (double)((int)(entity.getBbWidth() + 1.0f)) * 0.5;
        double d2 = node.y;
        double d3 = (double)node.z + (double)((int)(entity.getBbWidth() + 1.0f)) * 0.5;
        return new Vec3(d, d2, d3);
    }

    public BlockPos getNodePos(int n) {
        return this.nodes.get(n).asBlockPos();
    }

    public Vec3 getNextEntityPos(Entity entity) {
        return this.getEntityPosAtNode(entity, this.nextNodeIndex);
    }

    public BlockPos getNextNodePos() {
        return this.nodes.get(this.nextNodeIndex).asBlockPos();
    }

    public Node getNextNode() {
        return this.nodes.get(this.nextNodeIndex);
    }

    @Nullable
    public Node getPreviousNode() {
        return this.nextNodeIndex > 0 ? this.nodes.get(this.nextNodeIndex - 1) : null;
    }

    public boolean sameAs(@Nullable Path path) {
        if (path == null) {
            return false;
        }
        if (path.nodes.size() != this.nodes.size()) {
            return false;
        }
        for (int i = 0; i < this.nodes.size(); ++i) {
            Node node = this.nodes.get(i);
            Node node2 = path.nodes.get(i);
            if (node.x == node2.x && node.y == node2.y && node.z == node2.z) continue;
            return false;
        }
        return true;
    }

    public boolean canReach() {
        return this.reached;
    }

    @VisibleForDebug
    void setDebug(Node[] nodeArray, Node[] nodeArray2, Set<Target> set) {
        this.debugData = new DebugData(nodeArray, nodeArray2, set);
    }

    @Nullable
    public DebugData debugData() {
        return this.debugData;
    }

    public void writeToStream(FriendlyByteBuf friendlyByteBuf2) {
        if (this.debugData == null || this.debugData.targetNodes.isEmpty()) {
            return;
        }
        friendlyByteBuf2.writeBoolean(this.reached);
        friendlyByteBuf2.writeInt(this.nextNodeIndex);
        friendlyByteBuf2.writeBlockPos(this.target);
        friendlyByteBuf2.writeCollection(this.nodes, (friendlyByteBuf, node) -> node.writeToStream((FriendlyByteBuf)((Object)friendlyByteBuf)));
        this.debugData.write(friendlyByteBuf2);
    }

    public static Path createFromStream(FriendlyByteBuf friendlyByteBuf) {
        boolean bl = friendlyByteBuf.readBoolean();
        int n = friendlyByteBuf.readInt();
        BlockPos blockPos = friendlyByteBuf.readBlockPos();
        List<Node> list = friendlyByteBuf.readList(Node::createFromStream);
        DebugData debugData = DebugData.read(friendlyByteBuf);
        Path path = new Path(list, blockPos, bl);
        path.debugData = debugData;
        path.nextNodeIndex = n;
        return path;
    }

    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public BlockPos getTarget() {
        return this.target;
    }

    public float getDistToTarget() {
        return this.distToTarget;
    }

    static Node[] readNodeArray(FriendlyByteBuf friendlyByteBuf) {
        Node[] nodeArray = new Node[friendlyByteBuf.readVarInt()];
        for (int i = 0; i < nodeArray.length; ++i) {
            nodeArray[i] = Node.createFromStream(friendlyByteBuf);
        }
        return nodeArray;
    }

    static void writeNodeArray(FriendlyByteBuf friendlyByteBuf, Node[] nodeArray) {
        friendlyByteBuf.writeVarInt(nodeArray.length);
        for (Node node : nodeArray) {
            node.writeToStream(friendlyByteBuf);
        }
    }

    public Path copy() {
        Path path = new Path(this.nodes, this.target, this.reached);
        path.debugData = this.debugData;
        path.nextNodeIndex = this.nextNodeIndex;
        return path;
    }

    public static final class DebugData
    extends Record {
        private final Node[] openSet;
        private final Node[] closedSet;
        final Set<Target> targetNodes;

        public DebugData(Node[] nodeArray, Node[] nodeArray2, Set<Target> set) {
            this.openSet = nodeArray;
            this.closedSet = nodeArray2;
            this.targetNodes = set;
        }

        public void write(FriendlyByteBuf friendlyByteBuf2) {
            friendlyByteBuf2.writeCollection(this.targetNodes, (friendlyByteBuf, target) -> target.writeToStream((FriendlyByteBuf)((Object)friendlyByteBuf)));
            Path.writeNodeArray(friendlyByteBuf2, this.openSet);
            Path.writeNodeArray(friendlyByteBuf2, this.closedSet);
        }

        public static DebugData read(FriendlyByteBuf friendlyByteBuf) {
            HashSet hashSet = friendlyByteBuf.readCollection(HashSet::new, Target::createFromStream);
            Node[] nodeArray = Path.readNodeArray(friendlyByteBuf);
            Node[] nodeArray2 = Path.readNodeArray(friendlyByteBuf);
            return new DebugData(nodeArray, nodeArray2, hashSet);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DebugData.class, "openSet;closedSet;targetNodes", "openSet", "closedSet", "targetNodes"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DebugData.class, "openSet;closedSet;targetNodes", "openSet", "closedSet", "targetNodes"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DebugData.class, "openSet;closedSet;targetNodes", "openSet", "closedSet", "targetNodes"}, this, object);
        }

        public Node[] openSet() {
            return this.openSet;
        }

        public Node[] closedSet() {
            return this.closedSet;
        }

        public Set<Target> targetNodes() {
            return this.targetNodes;
        }
    }
}

