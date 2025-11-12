/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;

public abstract class NodeEvaluator {
    protected PathfindingContext currentContext;
    protected Mob mob;
    protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap();
    protected int entityWidth;
    protected int entityHeight;
    protected int entityDepth;
    protected boolean canPassDoors;
    protected boolean canOpenDoors;
    protected boolean canFloat;
    protected boolean canWalkOverFences;

    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        this.currentContext = new PathfindingContext(pathNavigationRegion, mob);
        this.mob = mob;
        this.nodes.clear();
        this.entityWidth = Mth.floor(mob.getBbWidth() + 1.0f);
        this.entityHeight = Mth.floor(mob.getBbHeight() + 1.0f);
        this.entityDepth = Mth.floor(mob.getBbWidth() + 1.0f);
    }

    public void done() {
        this.currentContext = null;
        this.mob = null;
    }

    protected Node getNode(BlockPos blockPos) {
        return this.getNode(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    protected Node getNode(int n, int n2, int n3) {
        return (Node)this.nodes.computeIfAbsent(Node.createHash(n, n2, n3), n4 -> new Node(n, n2, n3));
    }

    public abstract Node getStart();

    public abstract Target getTarget(double var1, double var3, double var5);

    protected Target getTargetNodeAt(double d, double d2, double d3) {
        return new Target(this.getNode(Mth.floor(d), Mth.floor(d2), Mth.floor(d3)));
    }

    public abstract int getNeighbors(Node[] var1, Node var2);

    public abstract PathType getPathTypeOfMob(PathfindingContext var1, int var2, int var3, int var4, Mob var5);

    public abstract PathType getPathType(PathfindingContext var1, int var2, int var3, int var4);

    public PathType getPathType(Mob mob, BlockPos blockPos) {
        return this.getPathType(new PathfindingContext(mob.level(), mob), blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public void setCanPassDoors(boolean bl) {
        this.canPassDoors = bl;
    }

    public void setCanOpenDoors(boolean bl) {
        this.canOpenDoors = bl;
    }

    public void setCanFloat(boolean bl) {
        this.canFloat = bl;
    }

    public void setCanWalkOverFences(boolean bl) {
        this.canWalkOverFences = bl;
    }

    public boolean canPassDoors() {
        return this.canPassDoors;
    }

    public boolean canOpenDoors() {
        return this.canOpenDoors;
    }

    public boolean canFloat() {
        return this.canFloat;
    }

    public boolean canWalkOverFences() {
        return this.canWalkOverFences;
    }

    public static boolean isBurningBlock(BlockState blockState) {
        return blockState.is(BlockTags.FIRE) || blockState.is(Blocks.LAVA) || blockState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState) || blockState.is(Blocks.LAVA_CAULDRON);
    }
}

