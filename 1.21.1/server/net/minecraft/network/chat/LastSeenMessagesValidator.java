/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenTrackedEntry;
import net.minecraft.network.chat.MessageSignature;

public class LastSeenMessagesValidator {
    private final int lastSeenCount;
    private final ObjectList<LastSeenTrackedEntry> trackedMessages = new ObjectArrayList();
    @Nullable
    private MessageSignature lastPendingMessage;

    public LastSeenMessagesValidator(int n) {
        this.lastSeenCount = n;
        for (int i = 0; i < n; ++i) {
            this.trackedMessages.add(null);
        }
    }

    public void addPending(MessageSignature messageSignature) {
        if (!messageSignature.equals(this.lastPendingMessage)) {
            this.trackedMessages.add((Object)new LastSeenTrackedEntry(messageSignature, true));
            this.lastPendingMessage = messageSignature;
        }
    }

    public int trackedMessagesCount() {
        return this.trackedMessages.size();
    }

    public boolean applyOffset(int n) {
        int n2 = this.trackedMessages.size() - this.lastSeenCount;
        if (n >= 0 && n <= n2) {
            this.trackedMessages.removeElements(0, n);
            return true;
        }
        return false;
    }

    public Optional<LastSeenMessages> applyUpdate(LastSeenMessages.Update update) {
        if (!this.applyOffset(update.offset())) {
            return Optional.empty();
        }
        ObjectArrayList objectArrayList = new ObjectArrayList(update.acknowledged().cardinality());
        if (update.acknowledged().length() > this.lastSeenCount) {
            return Optional.empty();
        }
        for (int i = 0; i < this.lastSeenCount; ++i) {
            boolean bl = update.acknowledged().get(i);
            LastSeenTrackedEntry lastSeenTrackedEntry = (LastSeenTrackedEntry)this.trackedMessages.get(i);
            if (bl) {
                if (lastSeenTrackedEntry == null) {
                    return Optional.empty();
                }
                this.trackedMessages.set(i, (Object)lastSeenTrackedEntry.acknowledge());
                objectArrayList.add((Object)lastSeenTrackedEntry.signature());
                continue;
            }
            if (lastSeenTrackedEntry != null && !lastSeenTrackedEntry.pending()) {
                return Optional.empty();
            }
            this.trackedMessages.set(i, null);
        }
        return Optional.of(new LastSeenMessages((List<MessageSignature>)objectArrayList));
    }
}

