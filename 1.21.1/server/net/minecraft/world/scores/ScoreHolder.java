/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  javax.annotation.Nullable
 */
package net.minecraft.world.scores;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public interface ScoreHolder {
    public static final String WILDCARD_NAME = "*";
    public static final ScoreHolder WILDCARD = new ScoreHolder(){

        @Override
        public String getScoreboardName() {
            return ScoreHolder.WILDCARD_NAME;
        }
    };

    public String getScoreboardName();

    @Nullable
    default public Component getDisplayName() {
        return null;
    }

    default public Component getFeedbackDisplayName() {
        Component component = this.getDisplayName();
        if (component != null) {
            return component.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(this.getScoreboardName()))));
        }
        return Component.literal(this.getScoreboardName());
    }

    public static ScoreHolder forNameOnly(final String string) {
        if (string.equals(WILDCARD_NAME)) {
            return WILDCARD;
        }
        final MutableComponent mutableComponent = Component.literal(string);
        return new ScoreHolder(){

            @Override
            public String getScoreboardName() {
                return string;
            }

            @Override
            public Component getFeedbackDisplayName() {
                return mutableComponent;
            }
        };
    }

    public static ScoreHolder fromGameProfile(GameProfile gameProfile) {
        final String string = gameProfile.getName();
        return new ScoreHolder(){

            @Override
            public String getScoreboardName() {
                return string;
            }
        };
    }
}

