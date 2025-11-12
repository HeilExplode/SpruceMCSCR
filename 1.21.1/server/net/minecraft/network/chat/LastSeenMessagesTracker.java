/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenTrackedEntry;
import net.minecraft.network.chat.MessageSignature;

public class LastSeenMessagesTracker {
    private final LastSeenTrackedEntry[] trackedMessages;
    private int tail;
    private int offset;
    @Nullable
    private MessageSignature lastTrackedMessage;

    public LastSeenMessagesTracker(int n) {
        this.trackedMessages = new LastSeenTrackedEntry[n];
    }

    public boolean addPending(MessageSignature messageSignature, boolean bl) {
        if (Objects.equals(messageSignature, this.lastTrackedMessage)) {
            return false;
        }
        this.lastTrackedMessage = messageSignature;
        this.addEntry(bl ? new LastSeenTrackedEntry(messageSignature, true) : null);
        return true;
    }

    private void addEntry(@Nullable LastSeenTrackedEntry lastSeenTrackedEntry) {
        int n = this.tail;
        this.tail = (n + 1) % this.trackedMessages.length;
        ++this.offset;
        this.trackedMessages[n] = lastSeenTrackedEntry;
    }

    public void ignorePending(MessageSignature messageSignature) {
        for (int i = 0; i < this.trackedMessages.length; ++i) {
            LastSeenTrackedEntry lastSeenTrackedEntry = this.trackedMessages[i];
            if (lastSeenTrackedEntry == null || !lastSeenTrackedEntry.pending() || !messageSignature.equals(lastSeenTrackedEntry.signature())) continue;
            this.trackedMessages[i] = null;
            break;
        }
    }

    public int getAndClearOffset() {
        int n = this.offset;
        this.offset = 0;
        return n;
    }

    public Update generateAndApplyUpdate() {
        int n = this.getAndClearOffset();
        BitSet bitSet = new BitSet(this.trackedMessages.length);
        ObjectArrayList objectArrayList = new ObjectArrayList(this.trackedMessages.length);
        for (int i = 0; i < this.trackedMessages.length; ++i) {
            int n2 = (this.tail + i) % this.trackedMessages.length;
            LastSeenTrackedEntry lastSeenTrackedEntry = this.trackedMessages[n2];
            if (lastSeenTrackedEntry == null) continue;
            bitSet.set(i, true);
            objectArrayList.add((Object)lastSeenTrackedEntry.signature());
            this.trackedMessages[n2] = lastSeenTrackedEntry.acknowledge();
        }
        LastSeenMessages lastSeenMessages = new LastSeenMessages((List<MessageSignature>)objectArrayList);
        LastSeenMessages.Update update = new LastSeenMessages.Update(n, bitSet);
        return new Update(lastSeenMessages, update);
    }

    public int offset() {
        return this.offset;
    }

    public record Update(LastSeenMessages lastSeen, LastSeenMessages.Update update) {
    }
}

