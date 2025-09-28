package com.notenoughmail.tfcgenviewer.network.packets;

import com.notenoughmail.tfcgenviewer.util.ClientHandoff;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record ViewerResponsePacket(
        byte fullPermissions,
        long seed,
        Settings levelSettings,
        Map<ResourceKey<PlacedFeature>, PlacedFeature> visualizableFeatures
) {

    // TODO: 1.5.1 | Maybe use RegistryCodecs#fullCodec
    // TODO: 1.21.1 | A Map<ResourceKey<? extends Registry<R>>, Map<TagKey<R>, Collection<Pair<ResourceKey<R>, R>>>> may be effective
    public static ViewerResponsePacket decode(FriendlyByteBuf data) {
        final byte permissions = data.readByte();
        final long seed = data.readLong();
        final Settings settings = data.readWithCodec(NbtOps.INSTANCE, Settings.CODEC.codec());
        final Map<ResourceKey<PlacedFeature>, PlacedFeature> features = data.readMap(
                buf -> buf.readResourceKey(Registries.PLACED_FEATURE),
                buf -> buf.readWithCodec(NbtOps.INSTANCE, PlacedFeature.DIRECT_CODEC)
        );
        return new ViewerResponsePacket(permissions, seed, settings, features);
    }

    public void encode(FriendlyByteBuf data) {
        data.writeByte(fullPermissions);
        data.writeLong(seed);
        data.writeWithCodec(NbtOps.INSTANCE, Settings.CODEC.codec(), levelSettings);
        data.writeMap(
                visualizableFeatures,
                FriendlyByteBuf::writeResourceKey,
                (buf, feature) -> buf.writeWithCodec(NbtOps.INSTANCE, PlacedFeature.DIRECT_CODEC, feature)
        );
    }

    public void handle(@Nullable ServerPlayer player) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandoff.viewWorld(this);
        }
    }
}
