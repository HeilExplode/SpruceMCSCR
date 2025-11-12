/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;

public class AdvancementNode {
    private final AdvancementHolder holder;
    @Nullable
    private final AdvancementNode parent;
    private final Set<AdvancementNode> children = new ReferenceOpenHashSet();

    @VisibleForTesting
    public AdvancementNode(AdvancementHolder advancementHolder, @Nullable AdvancementNode advancementNode) {
        this.holder = advancementHolder;
        this.parent = advancementNode;
    }

    public Advancement advancement() {
        return this.holder.value();
    }

    public AdvancementHolder holder() {
        return this.holder;
    }

    @Nullable
    public AdvancementNode parent() {
        return this.parent;
    }

    public AdvancementNode root() {
        return AdvancementNode.getRoot(this);
    }

    public static AdvancementNode getRoot(AdvancementNode advancementNode) {
        AdvancementNode advancementNode2 = advancementNode;
        AdvancementNode advancementNode3;
        while ((advancementNode3 = advancementNode2.parent()) != null) {
            advancementNode2 = advancementNode3;
        }
        return advancementNode2;
    }

    public Iterable<AdvancementNode> children() {
        return this.children;
    }

    @VisibleForTesting
    public void addChild(AdvancementNode advancementNode) {
        this.children.add(advancementNode);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof AdvancementNode)) return false;
        AdvancementNode advancementNode = (AdvancementNode)object;
        if (!this.holder.equals(advancementNode.holder)) return false;
        return true;
    }

    public int hashCode() {
        return this.holder.hashCode();
    }

    public String toString() {
        return this.holder.id().toString();
    }
}

