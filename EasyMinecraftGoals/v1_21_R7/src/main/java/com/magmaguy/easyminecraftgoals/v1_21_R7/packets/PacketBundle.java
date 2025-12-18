package com.magmaguy.easyminecraftgoals.v1_21_R7.packets;

import com.magmaguy.easyminecraftgoals.internal.AbstractPacketBundle;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class PacketBundle implements AbstractPacketBundle {
    private final List<PacketBundleEntry> entries = new ArrayList<>();
    private static final int MAX_PACKETS_PER_BUNDLE = 3000;

    public PacketBundle() {
    }

    @Override
    public void addPacket(Object packet, List<Player> viewers) {
        entries.add(new PacketBundleEntry((Packet<?>) packet, viewers));
    }

    @Override
    public void send() {
        // Group packets by player for efficiency
        Map<Player, List<Packet<ClientGamePacketListener>>> playerPackets = new HashMap<>();

        for (PacketBundleEntry entry : entries) {
            // Skip if no viewers
            if (entry.viewers().isEmpty()) continue;

            // Type check and cast once per packet
            if (!isClientGamePacket(entry.packet())) continue;

            @SuppressWarnings("unchecked")
            Packet<ClientGamePacketListener> clientPacket = (Packet<ClientGamePacketListener>) entry.packet();

            // Add to each viewer's packet list
            for (Player viewer : entry.viewers()) {
                playerPackets.computeIfAbsent(viewer, k -> new ArrayList<>()).add(clientPacket);
            }
        }

        // Send bundles to each player, splitting into chunks of 3000 if needed
        playerPackets.forEach((player, packets) -> {
            if (packets.isEmpty() || player == null || !player.isOnline()) return;

            // Split into chunks of MAX_PACKETS_PER_BUNDLE
            for (int i = 0; i < packets.size(); i += MAX_PACKETS_PER_BUNDLE) {
                int end = Math.min(i + MAX_PACKETS_PER_BUNDLE, packets.size());
                List<Packet<ClientGamePacketListener>> chunk = packets.subList(i, end);

                ClientboundBundlePacket bundle = new ClientboundBundlePacket(new HashSet<>(chunk));
                sendPacketBundle(player, bundle);
            }
        });

        int bundleCount = playerPackets.values().stream()
                .mapToInt(packets -> (packets.size() + MAX_PACKETS_PER_BUNDLE - 1) / MAX_PACKETS_PER_BUNDLE)
                .sum();

    }

    private boolean isClientGamePacket(Packet<?> packet) {
        // This is a bit hacky but works for most NMS versions
        // Alternatively, you could check specific packet types you know are valid
        return packet != null;
    }

    private void sendPacketBundle(Player player, Packet<?> nmsPacket) {
        if (nmsPacket == null) return;

        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.connection.send(nmsPacket);
    }

    private record PacketBundleEntry(Packet<?> packet, List<Player> viewers) {
    }

}
