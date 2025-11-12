/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

public class ClickEvent {
    public static final Codec<ClickEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Action.CODEC.forGetter(clickEvent -> clickEvent.action), (App)Codec.STRING.fieldOf("value").forGetter(clickEvent -> clickEvent.value)).apply((Applicative)instance, ClickEvent::new));
    private final Action action;
    private final String value;

    public ClickEvent(Action action, String string) {
        this.action = action;
        this.value = string;
    }

    public Action getAction() {
        return this.action;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        ClickEvent clickEvent = (ClickEvent)object;
        return this.action == clickEvent.action && this.value.equals(clickEvent.value);
    }

    public String toString() {
        return "ClickEvent{action=" + String.valueOf(this.action) + ", value='" + this.value + "'}";
    }

    public int hashCode() {
        int n = this.action.hashCode();
        n = 31 * n + this.value.hashCode();
        return n;
    }

    public static enum Action implements StringRepresentable
    {
        OPEN_URL("open_url", true),
        OPEN_FILE("open_file", false),
        RUN_COMMAND("run_command", true),
        SUGGEST_COMMAND("suggest_command", true),
        CHANGE_PAGE("change_page", true),
        COPY_TO_CLIPBOARD("copy_to_clipboard", true);

        public static final MapCodec<Action> UNSAFE_CODEC;
        public static final MapCodec<Action> CODEC;
        private final boolean allowFromServer;
        private final String name;

        private Action(String string2, boolean bl) {
            this.name = string2;
            this.allowFromServer = bl;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static DataResult<Action> filterForSerialization(Action action) {
            if (!action.isAllowedFromServer()) {
                return DataResult.error(() -> "Action not allowed: " + String.valueOf(action));
            }
            return DataResult.success((Object)action, (Lifecycle)Lifecycle.stable());
        }

        static {
            UNSAFE_CODEC = StringRepresentable.fromEnum(Action::values).fieldOf("action");
            CODEC = UNSAFE_CODEC.validate(Action::filterForSerialization);
        }
    }
}

