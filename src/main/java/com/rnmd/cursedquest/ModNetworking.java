package com.rnmd.cursedquest;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "2";
    private static int packetId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CursedQuest.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ModNetworking() {
    }

    public static void register() {
        CHANNEL.registerMessage(packetId++, ScarePacket.class,
                ScarePacket::encode, ScarePacket::decode, ScarePacket::handle);
    }

    public static void sendScare(ServerPlayer player, int type) {
        if (player != null && player.connection != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ScarePacket(type));
        }
    }
}
