/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  javax.annotation.Nullable
 *  org.jetbrains.annotations.VisibleForTesting
 */
package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import org.jetbrains.annotations.VisibleForTesting;

public class MessageSignatureCache {
    public static final int NOT_FOUND = -1;
    private static final int DEFAULT_CAPACITY = 128;
    private final MessageSignature[] entries;

    public MessageSignatureCache(int n) {
        this.entries = new MessageSignature[n];
    }

    public static MessageSignatureCache createDefault() {
        return new MessageSignatureCache(128);
    }

    public int pack(MessageSignature messageSignature) {
        for (int i = 0; i < this.entries.length; ++i) {
            if (!messageSignature.equals(this.entries[i])) continue;
            return i;
        }
        return -1;
    }

    @Nullable
    public MessageSignature unpack(int n) {
        return this.entries[n];
    }

    public void push(SignedMessageBody signedMessageBody, @Nullable MessageSignature messageSignature) {
        List<MessageSignature> list = signedMessageBody.lastSeen().entries();
        ArrayDeque<MessageSignature> arrayDeque = new ArrayDeque<MessageSignature>(list.size() + 1);
        arrayDeque.addAll(list);
        if (messageSignature != null) {
            arrayDeque.add(messageSignature);
        }
        this.push(arrayDeque);
    }

    @VisibleForTesting
    void push(List<MessageSignature> list) {
        this.push(new ArrayDeque<MessageSignature>(list));
    }

    private void push(ArrayDeque<MessageSignature> arrayDeque) {
        ObjectOpenHashSet objectOpenHashSet = new ObjectOpenHashSet(arrayDeque);
        for (int i = 0; !arrayDeque.isEmpty() && i < this.entries.length; ++i) {
            MessageSignature messageSignature = this.entries[i];
            this.entries[i] = arrayDeque.removeLast();
            if (messageSignature == null || objectOpenHashSet.contains(messageSignature)) continue;
            arrayDeque.addFirst(messageSignature);
        }
    }
}

