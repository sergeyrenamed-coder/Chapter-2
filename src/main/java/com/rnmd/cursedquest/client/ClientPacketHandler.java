package com.rnmd.cursedquest.client;

import net.minecraft.client.Minecraft;

public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void handleScare(int type) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        minecraft.setScreen(new HorrorScreen(type));
    }
}
