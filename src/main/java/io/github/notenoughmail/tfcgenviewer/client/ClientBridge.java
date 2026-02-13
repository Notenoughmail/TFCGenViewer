package io.github.notenoughmail.tfcgenviewer.client;

import io.github.notenoughmail.tfcgenviewer.impl.network.packet.MultiViewResponsePacket;
import io.github.notenoughmail.tfcgenviewer.impl.network.packet.SingleViewResponsePacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientBridge {

    public static void singleViewResponse(SingleViewResponsePacket pkt, IPayloadContext ctx) {
        ClientPacketHandler.onViewResponse(pkt, ctx);
    }

    public static void multiViewResponse(MultiViewResponsePacket pkt, IPayloadContext ctx) {
        ClientPacketHandler.onMultiViewResponse(pkt, ctx);
    }
}
