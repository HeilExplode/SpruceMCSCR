/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.JukeboxSong;

public interface JukeboxSongs {
    public static final ResourceKey<JukeboxSong> THIRTEEN = JukeboxSongs.create("13");
    public static final ResourceKey<JukeboxSong> CAT = JukeboxSongs.create("cat");
    public static final ResourceKey<JukeboxSong> BLOCKS = JukeboxSongs.create("blocks");
    public static final ResourceKey<JukeboxSong> CHIRP = JukeboxSongs.create("chirp");
    public static final ResourceKey<JukeboxSong> FAR = JukeboxSongs.create("far");
    public static final ResourceKey<JukeboxSong> MALL = JukeboxSongs.create("mall");
    public static final ResourceKey<JukeboxSong> MELLOHI = JukeboxSongs.create("mellohi");
    public static final ResourceKey<JukeboxSong> STAL = JukeboxSongs.create("stal");
    public static final ResourceKey<JukeboxSong> STRAD = JukeboxSongs.create("strad");
    public static final ResourceKey<JukeboxSong> WARD = JukeboxSongs.create("ward");
    public static final ResourceKey<JukeboxSong> ELEVEN = JukeboxSongs.create("11");
    public static final ResourceKey<JukeboxSong> WAIT = JukeboxSongs.create("wait");
    public static final ResourceKey<JukeboxSong> PIGSTEP = JukeboxSongs.create("pigstep");
    public static final ResourceKey<JukeboxSong> OTHERSIDE = JukeboxSongs.create("otherside");
    public static final ResourceKey<JukeboxSong> FIVE = JukeboxSongs.create("5");
    public static final ResourceKey<JukeboxSong> RELIC = JukeboxSongs.create("relic");
    public static final ResourceKey<JukeboxSong> PRECIPICE = JukeboxSongs.create("precipice");
    public static final ResourceKey<JukeboxSong> CREATOR = JukeboxSongs.create("creator");
    public static final ResourceKey<JukeboxSong> CREATOR_MUSIC_BOX = JukeboxSongs.create("creator_music_box");

    private static ResourceKey<JukeboxSong> create(String string) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.withDefaultNamespace(string));
    }

    private static void register(BootstrapContext<JukeboxSong> bootstrapContext, ResourceKey<JukeboxSong> resourceKey, Holder.Reference<SoundEvent> reference, int n, int n2) {
        bootstrapContext.register(resourceKey, new JukeboxSong(reference, Component.translatable(Util.makeDescriptionId("jukebox_song", resourceKey.location())), n, n2));
    }

    public static void bootstrap(BootstrapContext<JukeboxSong> bootstrapContext) {
        JukeboxSongs.register(bootstrapContext, THIRTEEN, SoundEvents.MUSIC_DISC_13, 178, 1);
        JukeboxSongs.register(bootstrapContext, CAT, SoundEvents.MUSIC_DISC_CAT, 185, 2);
        JukeboxSongs.register(bootstrapContext, BLOCKS, SoundEvents.MUSIC_DISC_BLOCKS, 345, 3);
        JukeboxSongs.register(bootstrapContext, CHIRP, SoundEvents.MUSIC_DISC_CHIRP, 185, 4);
        JukeboxSongs.register(bootstrapContext, FAR, SoundEvents.MUSIC_DISC_FAR, 174, 5);
        JukeboxSongs.register(bootstrapContext, MALL, SoundEvents.MUSIC_DISC_MALL, 197, 6);
        JukeboxSongs.register(bootstrapContext, MELLOHI, SoundEvents.MUSIC_DISC_MELLOHI, 96, 7);
        JukeboxSongs.register(bootstrapContext, STAL, SoundEvents.MUSIC_DISC_STAL, 150, 8);
        JukeboxSongs.register(bootstrapContext, STRAD, SoundEvents.MUSIC_DISC_STRAD, 188, 9);
        JukeboxSongs.register(bootstrapContext, WARD, SoundEvents.MUSIC_DISC_WARD, 251, 10);
        JukeboxSongs.register(bootstrapContext, ELEVEN, SoundEvents.MUSIC_DISC_11, 71, 11);
        JukeboxSongs.register(bootstrapContext, WAIT, SoundEvents.MUSIC_DISC_WAIT, 238, 12);
        JukeboxSongs.register(bootstrapContext, PIGSTEP, SoundEvents.MUSIC_DISC_PIGSTEP, 149, 13);
        JukeboxSongs.register(bootstrapContext, OTHERSIDE, SoundEvents.MUSIC_DISC_OTHERSIDE, 195, 14);
        JukeboxSongs.register(bootstrapContext, FIVE, SoundEvents.MUSIC_DISC_5, 178, 15);
        JukeboxSongs.register(bootstrapContext, RELIC, SoundEvents.MUSIC_DISC_RELIC, 218, 14);
        JukeboxSongs.register(bootstrapContext, PRECIPICE, SoundEvents.MUSIC_DISC_PRECIPICE, 299, 13);
        JukeboxSongs.register(bootstrapContext, CREATOR, SoundEvents.MUSIC_DISC_CREATOR, 176, 12);
        JukeboxSongs.register(bootstrapContext, CREATOR_MUSIC_BOX, SoundEvents.MUSIC_DISC_CREATOR_MUSIC_BOX, 73, 11);
    }
}

