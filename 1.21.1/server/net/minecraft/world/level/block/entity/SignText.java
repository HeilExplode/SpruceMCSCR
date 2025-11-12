/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

public class SignText {
    private static final Codec<Component[]> LINES_CODEC = ComponentSerialization.FLAT_CODEC.listOf().comapFlatMap(var0 -> {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * java.lang.UnsupportedOperationException
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.NewAnonymousArray.getDimSize(NewAnonymousArray.java:142)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.isNewArrayLambda(LambdaRewriter.java:455)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:409)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:167)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterHelper.applyForwards(ExpressionRewriterHelper.java:12)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriterToArgs(AbstractMemberFunctionInvokation.java:101)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriter(AbstractMemberFunctionInvokation.java:88)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:103)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn.rewriteExpressions(StructuredReturn.java:99)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:88)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1050)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }, componentArray -> List.of(componentArray[0], componentArray[1], componentArray[2], componentArray[3]));
    public static final Codec<SignText> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)LINES_CODEC.fieldOf("messages").forGetter(signText -> signText.messages), (App)LINES_CODEC.lenientOptionalFieldOf("filtered_messages").forGetter(SignText::filteredMessages), (App)DyeColor.CODEC.fieldOf("color").orElse((Object)DyeColor.BLACK).forGetter(signText -> signText.color), (App)Codec.BOOL.fieldOf("has_glowing_text").orElse((Object)false).forGetter(signText -> signText.hasGlowingText)).apply((Applicative)instance, SignText::load));
    public static final int LINES = 4;
    private final Component[] messages;
    private final Component[] filteredMessages;
    private final DyeColor color;
    private final boolean hasGlowingText;
    @Nullable
    private FormattedCharSequence[] renderMessages;
    private boolean renderMessagedFiltered;

    public SignText() {
        this(SignText.emptyMessages(), SignText.emptyMessages(), DyeColor.BLACK, false);
    }

    public SignText(Component[] componentArray, Component[] componentArray2, DyeColor dyeColor, boolean bl) {
        this.messages = componentArray;
        this.filteredMessages = componentArray2;
        this.color = dyeColor;
        this.hasGlowingText = bl;
    }

    private static Component[] emptyMessages() {
        return new Component[]{CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY};
    }

    private static SignText load(Component[] componentArray, Optional<Component[]> optional, DyeColor dyeColor, boolean bl) {
        return new SignText(componentArray, optional.orElse(Arrays.copyOf(componentArray, componentArray.length)), dyeColor, bl);
    }

    public boolean hasGlowingText() {
        return this.hasGlowingText;
    }

    public SignText setHasGlowingText(boolean bl) {
        if (bl == this.hasGlowingText) {
            return this;
        }
        return new SignText(this.messages, this.filteredMessages, this.color, bl);
    }

    public DyeColor getColor() {
        return this.color;
    }

    public SignText setColor(DyeColor dyeColor) {
        if (dyeColor == this.getColor()) {
            return this;
        }
        return new SignText(this.messages, this.filteredMessages, dyeColor, this.hasGlowingText);
    }

    public Component getMessage(int n, boolean bl) {
        return this.getMessages(bl)[n];
    }

    public SignText setMessage(int n, Component component) {
        return this.setMessage(n, component, component);
    }

    public SignText setMessage(int n, Component component, Component component2) {
        Component[] componentArray = Arrays.copyOf(this.messages, this.messages.length);
        Component[] componentArray2 = Arrays.copyOf(this.filteredMessages, this.filteredMessages.length);
        componentArray[n] = component;
        componentArray2[n] = component2;
        return new SignText(componentArray, componentArray2, this.color, this.hasGlowingText);
    }

    public boolean hasMessage(Player player) {
        return Arrays.stream(this.getMessages(player.isTextFilteringEnabled())).anyMatch(component -> !component.getString().isEmpty());
    }

    public Component[] getMessages(boolean bl) {
        return bl ? this.filteredMessages : this.messages;
    }

    public FormattedCharSequence[] getRenderMessages(boolean bl, Function<Component, FormattedCharSequence> function) {
        if (this.renderMessages == null || this.renderMessagedFiltered != bl) {
            this.renderMessagedFiltered = bl;
            this.renderMessages = new FormattedCharSequence[4];
            for (int i = 0; i < 4; ++i) {
                this.renderMessages[i] = function.apply(this.getMessage(i, bl));
            }
        }
        return this.renderMessages;
    }

    private Optional<Component[]> filteredMessages() {
        for (int i = 0; i < 4; ++i) {
            if (this.filteredMessages[i].equals(this.messages[i])) continue;
            return Optional.of(this.filteredMessages);
        }
        return Optional.empty();
    }

    public boolean hasAnyClickCommands(Player player) {
        for (Component component : this.getMessages(player.isTextFilteringEnabled())) {
            Style style = component.getStyle();
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent == null || clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) continue;
            return true;
        }
        return false;
    }
}

