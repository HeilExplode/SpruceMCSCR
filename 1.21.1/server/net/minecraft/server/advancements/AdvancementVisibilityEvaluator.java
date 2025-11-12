/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Stack
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.DisplayInfo;

public class AdvancementVisibilityEvaluator {
    private static final int VISIBILITY_DEPTH = 2;

    private static VisibilityRule evaluateVisibilityRule(Advancement advancement, boolean bl) {
        Optional<DisplayInfo> optional = advancement.display();
        if (optional.isEmpty()) {
            return VisibilityRule.HIDE;
        }
        if (bl) {
            return VisibilityRule.SHOW;
        }
        if (optional.get().isHidden()) {
            return VisibilityRule.HIDE;
        }
        return VisibilityRule.NO_CHANGE;
    }

    private static boolean evaluateVisiblityForUnfinishedNode(Stack<VisibilityRule> stack) {
        for (int i = 0; i <= 2; ++i) {
            VisibilityRule visibilityRule = (VisibilityRule)((Object)stack.peek(i));
            if (visibilityRule == VisibilityRule.SHOW) {
                return true;
            }
            if (visibilityRule != VisibilityRule.HIDE) continue;
            return false;
        }
        return false;
    }

    private static boolean evaluateVisibility(AdvancementNode advancementNode, Stack<VisibilityRule> stack, Predicate<AdvancementNode> predicate, Output output) {
        boolean bl = predicate.test(advancementNode);
        VisibilityRule visibilityRule = AdvancementVisibilityEvaluator.evaluateVisibilityRule(advancementNode.advancement(), bl);
        boolean bl2 = bl;
        stack.push((Object)visibilityRule);
        for (AdvancementNode advancementNode2 : advancementNode.children()) {
            bl2 |= AdvancementVisibilityEvaluator.evaluateVisibility(advancementNode2, stack, predicate, output);
        }
        boolean bl3 = bl2 || AdvancementVisibilityEvaluator.evaluateVisiblityForUnfinishedNode(stack);
        stack.pop();
        output.accept(advancementNode, bl3);
        return bl2;
    }

    public static void evaluateVisibility(AdvancementNode advancementNode, Predicate<AdvancementNode> predicate, Output output) {
        AdvancementNode advancementNode2 = advancementNode.root();
        ObjectArrayList objectArrayList = new ObjectArrayList();
        for (int i = 0; i <= 2; ++i) {
            objectArrayList.push((Object)VisibilityRule.NO_CHANGE);
        }
        AdvancementVisibilityEvaluator.evaluateVisibility(advancementNode2, (Stack<VisibilityRule>)objectArrayList, predicate, output);
    }

    static enum VisibilityRule {
        SHOW,
        HIDE,
        NO_CHANGE;

    }

    @FunctionalInterface
    public static interface Output {
        public void accept(AdvancementNode var1, boolean var2);
    }
}

