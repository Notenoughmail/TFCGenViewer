package com.notenoughmail.tfcgenviewer.network.packets;

import net.dries007.tfc.world.settings.Settings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record ViewerResponsePacket(
        byte fullPermissions,
        long seed,
        Settings levelSettings
) {

    public static ViewerResponsePacket decode(FriendlyByteBuf data) {
        final byte permissions = data.readByte();
        final long seed = data.readLong();
        final Settings settings = Settings.CODEC.compressedDecode(NbtOps.INSTANCE, data.readNbt()).result().orElse(null);
        return new ViewerResponsePacket(permissions, seed, settings);
    }

    public void encode(FriendlyByteBuf data) {
        data.writeByte(fullPermissions);
        data.writeLong(seed);
        data.writeNbt((CompoundTag) Settings.CODEC.encoder().encodeStart(NbtOps.INSTANCE, levelSettings).result().get());
    }

    public void handle(@Nullable ServerPlayer player) {
        // TODO: Implement
    }
}
