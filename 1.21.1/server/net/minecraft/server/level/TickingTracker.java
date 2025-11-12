/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 */
package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;

public class TickingTracker
extends ChunkTracker {
    public static final int MAX_LEVEL = 33;
    private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
    protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
    private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap();

    public TickingTracker() {
        super(34, 16, 256);
        this.chunks.defaultReturnValue((byte)33);
    }

    private SortedArraySet<Ticket<?>> getTickets(long l2) {
        return (SortedArraySet)this.tickets.computeIfAbsent(l2, l -> SortedArraySet.create(4));
    }

    private int getTicketLevelAt(SortedArraySet<Ticket<?>> sortedArraySet) {
        return sortedArraySet.isEmpty() ? 34 : sortedArraySet.first().getTicketLevel();
    }

    public void addTicket(long l, Ticket<?> ticket) {
        SortedArraySet<Ticket<?>> sortedArraySet = this.getTickets(l);
        int n = this.getTicketLevelAt(sortedArraySet);
        sortedArraySet.add(ticket);
        if (ticket.getTicketLevel() < n) {
            this.update(l, ticket.getTicketLevel(), true);
        }
    }

    public void removeTicket(long l, Ticket<?> ticket) {
        SortedArraySet<Ticket<?>> sortedArraySet = this.getTickets(l);
        sortedArraySet.remove(ticket);
        if (sortedArraySet.isEmpty()) {
            this.tickets.remove(l);
        }
        this.update(l, this.getTicketLevelAt(sortedArraySet), false);
    }

    public <T> void addTicket(TicketType<T> ticketType, ChunkPos chunkPos, int n, T t) {
        this.addTicket(chunkPos.toLong(), new Ticket<T>(ticketType, n, t));
    }

    public <T> void removeTicket(TicketType<T> ticketType, ChunkPos chunkPos, int n, T t) {
        Ticket<T> ticket = new Ticket<T>(ticketType, n, t);
        this.removeTicket(chunkPos.toLong(), ticket);
    }

    public void replacePlayerTicketsLevel(int n) {
        ArrayList<Pair> arrayList = new ArrayList<Pair>();
        for (Long2ObjectMap.Entry entry : this.tickets.long2ObjectEntrySet()) {
            for (Ticket ticket : (SortedArraySet)entry.getValue()) {
                if (ticket.getType() != TicketType.PLAYER) continue;
                arrayList.add(Pair.of((Object)ticket, (Object)entry.getLongKey()));
            }
        }
        for (Pair pair : arrayList) {
            Ticket ticket;
            Long l = (Long)pair.getSecond();
            ticket = (Ticket)pair.getFirst();
            this.removeTicket(l, ticket);
            ChunkPos chunkPos = new ChunkPos(l);
            TicketType ticketType = ticket.getType();
            this.addTicket(ticketType, chunkPos, n, chunkPos);
        }
    }

    @Override
    protected int getLevelFromSource(long l) {
        SortedArraySet sortedArraySet = (SortedArraySet)this.tickets.get(l);
        if (sortedArraySet == null || sortedArraySet.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return ((Ticket)sortedArraySet.first()).getTicketLevel();
    }

    public int getLevel(ChunkPos chunkPos) {
        return this.getLevel(chunkPos.toLong());
    }

    @Override
    protected int getLevel(long l) {
        return this.chunks.get(l);
    }

    @Override
    protected void setLevel(long l, int n) {
        if (n >= 33) {
            this.chunks.remove(l);
        } else {
            this.chunks.put(l, (byte)n);
        }
    }

    public void runAllUpdates() {
        this.runUpdates(Integer.MAX_VALUE);
    }

    public String getTicketDebugString(long l) {
        SortedArraySet sortedArraySet = (SortedArraySet)this.tickets.get(l);
        if (sortedArraySet == null || sortedArraySet.isEmpty()) {
            return "no_ticket";
        }
        return ((Ticket)sortedArraySet.first()).toString();
    }
}

