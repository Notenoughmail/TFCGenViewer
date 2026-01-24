package io.github.notenoughmail.tfcgenviewer.impl.network.packet;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public record MultiViewResponsePacket(Set<ResourceLocation> generatorVisualizers) implements CustomPacketPayload {

    public static final Type<MultiViewResponsePacket> TYPE = new Type<>(TFCGenViewer.id("multi_view_response"));

    public static final StreamCodec<ByteBuf, MultiViewResponsePacket> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.<ByteBuf, ResourceLocation, Set<ResourceLocation>>collection(HashSet::new))
                    .map(MultiViewResponsePacket::new, MultiViewResponsePacket::generatorVisualizers);

    @Override
    public Type<MultiViewResponsePacket> type() {
        return TYPE;
    }
}
