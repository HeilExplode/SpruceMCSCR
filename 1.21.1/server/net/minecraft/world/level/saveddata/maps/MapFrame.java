/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.saveddata.maps;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class MapFrame {
    private final BlockPos pos;
    private final int rotation;
    private final int entityId;

    public MapFrame(BlockPos blockPos, int n, int n2) {
        this.pos = blockPos;
        this.rotation = n;
        this.entityId = n2;
    }

    @Nullable
    public static MapFrame load(CompoundTag compoundTag) {
        Optional<BlockPos> optional = NbtUtils.readBlockPos(compoundTag, "pos");
        if (optional.isEmpty()) {
            return null;
        }
        int n = compoundTag.getInt("rotation");
        int n2 = compoundTag.getInt("entity_id");
        return new MapFrame(optional.get(), n, n2);
    }

    public CompoundTag save() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("pos", NbtUtils.writeBlockPos(this.pos));
        compoundTag.putInt("rotation", this.rotation);
        compoundTag.putInt("entity_id", this.entityId);
        return compoundTag;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getRotation() {
        return this.rotation;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getId() {
        return MapFrame.frameId(this.pos);
    }

    public static String frameId(BlockPos blockPos) {
        return "frame-" + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
    }
}

