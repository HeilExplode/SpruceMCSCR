/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands;

@FunctionalInterface
public interface CommandResultCallback {
    public static final CommandResultCallback EMPTY = new CommandResultCallback(){

        @Override
        public void onResult(boolean bl, int n) {
        }

        public String toString() {
            return "<empty>";
        }
    };

    public void onResult(boolean var1, int var2);

    default public void onSuccess(int n) {
        this.onResult(true, n);
    }

    default public void onFailure() {
        this.onResult(false, 0);
    }

    public static CommandResultCallback chain(CommandResultCallback commandResultCallback, CommandResultCallback commandResultCallback2) {
        if (commandResultCallback == EMPTY) {
            return commandResultCallback2;
        }
        if (commandResultCallback2 == EMPTY) {
            return commandResultCallback;
        }
        return (bl, n) -> {
            commandResultCallback.onResult(bl, n);
            commandResultCallback2.onResult(bl, n);
        };
    }
}

