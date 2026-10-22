package com.rnmd.cursedquest;

import com.rnmd.cursedquest.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ScarePacket {
    private final int type;

    public ScarePacket(int type) {
        this.type = Math.max(0, Math.min(11, type));
    }

    public static void encode(ScarePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.type);
    }

    public static ScarePacket decode(FriendlyByteBuf buffer) {
        return new ScarePacket(buffer.readVarInt());
    }

    public static void handle(ScarePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> ClientPacketHandler.handleScare(packet.type)));
        context.setPacketHandled(true);
    }
}
