/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.npc;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

public class VillagerData {
    public static final int MIN_VILLAGER_LEVEL = 1;
    public static final int MAX_VILLAGER_LEVEL = 5;
    private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
    public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BuiltInRegistries.VILLAGER_TYPE.byNameCodec().fieldOf("type").orElseGet(() -> VillagerType.PLAINS).forGetter(villagerData -> villagerData.type), (App)BuiltInRegistries.VILLAGER_PROFESSION.byNameCodec().fieldOf("profession").orElseGet(() -> VillagerProfession.NONE).forGetter(villagerData -> villagerData.profession), (App)Codec.INT.fieldOf("level").orElse((Object)1).forGetter(villagerData -> villagerData.level)).apply((Applicative)instance, VillagerData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, VillagerData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.registry(Registries.VILLAGER_TYPE), villagerData -> villagerData.type, ByteBufCodecs.registry(Registries.VILLAGER_PROFESSION), villagerData -> villagerData.profession, ByteBufCodecs.VAR_INT, villagerData -> villagerData.level, VillagerData::new);
    private final VillagerType type;
    private final VillagerProfession profession;
    private final int level;

    public VillagerData(VillagerType villagerType, VillagerProfession villagerProfession, int n) {
        this.type = villagerType;
        this.profession = villagerProfession;
        this.level = Math.max(1, n);
    }

    public VillagerType getType() {
        return this.type;
    }

    public VillagerProfession getProfession() {
        return this.profession;
    }

    public int getLevel() {
        return this.level;
    }

    public VillagerData setType(VillagerType villagerType) {
        return new VillagerData(villagerType, this.profession, this.level);
    }

    public VillagerData setProfession(VillagerProfession villagerProfession) {
        return new VillagerData(this.type, villagerProfession, this.level);
    }

    public VillagerData setLevel(int n) {
        return new VillagerData(this.type, this.profession, n);
    }

    public static int getMinXpPerLevel(int n) {
        return VillagerData.canLevelUp(n) ? NEXT_LEVEL_XP_THRESHOLDS[n - 1] : 0;
    }

    public static int getMaxXpPerLevel(int n) {
        return VillagerData.canLevelUp(n) ? NEXT_LEVEL_XP_THRESHOLDS[n] : 0;
    }

    public static boolean canLevelUp(int n) {
        return n >= 1 && n < 5;
    }
}

