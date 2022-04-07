package com.redlimerl.speedrunigt.timer.packet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;

public class TimerPacketUtils {

    public static void sendClient2ServerPacket(MinecraftClient client, TimerPacket packet) {
        if (client.getNetworkHandler() != null) client.getNetworkHandler().sendPacket(packet.createClient2ServerPacket(client));
    }

    public static void sendServer2ClientPacket(ServerPlayerEntity player, TimerPacket packet) {
        player.networkHandler.sendPacket(packet.createServer2ClientPacket(player.server));
    }

    public static void sendServer2ClientPacket(Collection<ServerPlayerEntity> players, TimerPacket packet) {
        for (ServerPlayerEntity player : players) sendServer2ClientPacket(player, packet);
    }

    public static void sendServer2ClientPacket(MinecraftServer server, TimerPacket packet) {
        sendServer2ClientPacket(server.getPlayerManager().players, packet);
    }

    public static void sendServer2ClientPacket(MinecraftServer server, TimerPacket packet, TimerPacketBuf buf) {
        for (Object obj : server.getPlayerManager().players) {
            ((ServerPlayerEntity) obj).networkHandler.sendPacket(packet.createServer2ClientPacket(server, buf));
        }
    }
}
