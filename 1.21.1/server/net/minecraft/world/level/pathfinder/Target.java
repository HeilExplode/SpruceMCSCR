/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.pathfinder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.pathfinder.Node;

public class Target
extends Node {
    private float bestHeuristic = Float.MAX_VALUE;
    private Node bestNode;
    private boolean reached;

    public Target(Node node) {
        super(node.x, node.y, node.z);
    }

    public Target(int n, int n2, int n3) {
        super(n, n2, n3);
    }

    public void updateBest(float f, Node node) {
        if (f < this.bestHeuristic) {
            this.bestHeuristic = f;
            this.bestNode = node;
        }
    }

    public Node getBestNode() {
        return this.bestNode;
    }

    public void setReached() {
        this.reached = true;
    }

    public boolean isReached() {
        return this.reached;
    }

    public static Target createFromStream(FriendlyByteBuf friendlyByteBuf) {
        Target target = new Target(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
        Target.readContents(friendlyByteBuf, target);
        return target;
    }
}

