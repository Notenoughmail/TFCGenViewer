package com.notenoughmail.tfcgenviewer.network;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.network.packets.ViewerRequestPacket;
import com.notenoughmail.tfcgenviewer.network.packets.ViewerResponsePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class TFCGVChannel {

    private static final String VERSION = ModList.get().getModFileById(TFCGenViewer.ID).versionString();
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(TFCGenViewer.identifier("network"), () -> VERSION, VERSION::equals, VERSION::equals); // TODO: Change this to accept any version, but only function when they are equal
    private static final MutableInt ID = new MutableInt(0);

    public static void send(PacketDistributor.PacketTarget target, Object message) {
        INSTANCE.send(target, message);
    }

    public static void init() {
        // Client -> Server
        INSTANCE.registerMessage(next(), ViewerRequestPacket.class, (pkt, buf) -> {}, buf -> ViewerRequestPacket.INSTANCE, map(ViewerRequestPacket::handle, false));

        // Server -> Client
        INSTANCE.registerMessage(next(), ViewerResponsePacket.class, ViewerResponsePacket::encode, ViewerResponsePacket::decode, map(ViewerResponsePacket::handle, true));
    }

    private static int next() {
        return ID.incrementAndGet();
    }

    private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> map(BiConsumer<T, @Nullable ServerPlayer> func, boolean onMainThread) {
        return (t, ctx) -> {
            ctx.get().setPacketHandled(true);
            if (onMainThread) {
                ctx.get().enqueueWork(() -> func.accept(t, ctx.get().getSender()));
            } else {
                func.accept(t, ctx.get().getSender());
            }
        };
    }
}
