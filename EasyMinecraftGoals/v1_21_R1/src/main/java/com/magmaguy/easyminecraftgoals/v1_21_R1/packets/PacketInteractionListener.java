package com.magmaguy.easyminecraftgoals.v1_21_R1.packets;

import com.magmaguy.easyminecraftgoals.internal.PacketEntityInteractionManager;
import io.netty.channel.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for ServerboundInteractPacket to detect when players interact with packet-only entities.
 * Injects a Netty channel handler into each player's connection pipeline.
 */
public class PacketInteractionListener implements Listener {

    private static final String HANDLER_NAME = "emg_packet_interaction";
    private final Plugin plugin;
    private final Map<UUID, Channel> playerChannels = new ConcurrentHashMap<>();

    // Reflection fields for accessing packet data
    private static Field entityIdField;
    private static Field actionField;
    private static Field connectionField;
    private static Field channelField;

    static {
        try {
            // Get entityId field from ServerboundInteractPacket
            entityIdField = ServerboundInteractPacket.class.getDeclaredField("entityId");
            entityIdField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            try {
                entityIdField = ServerboundInteractPacket.class.getDeclaredField("a");
                entityIdField.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                ex.printStackTrace();
            }
        }

        try {
            // Get action field from ServerboundInteractPacket
            actionField = ServerboundInteractPacket.class.getDeclaredField("action");
            actionField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            try {
                actionField = ServerboundInteractPacket.class.getDeclaredField("b");
                actionField.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                ex.printStackTrace();
            }
        }

        // Find Connection field by type to handle obfuscated names
        for (Field f : ServerCommonPacketListenerImpl.class.getDeclaredFields()) {
            if (Connection.class.isAssignableFrom(f.getType())) {
                connectionField = f;
                connectionField.setAccessible(true);
                break;
            }
        }

        // Find channel field by type to handle obfuscated names
        for (Field f : Connection.class.getDeclaredFields()) {
            if (Channel.class.isAssignableFrom(f.getType())) {
                channelField = f;
                channelField.setAccessible(true);
                break;
            }
        }
    }

    public PacketInteractionListener(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the listener and injects into all online players.
     */
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Inject into all currently online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            injectPlayer(player);
        }
    }

    /**
     * Shuts down the listener and removes all injected handlers.
     */
    public void shutdown() {
        // Remove handlers from all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            uninjectPlayer(player);
        }
        playerChannels.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Delay injection by 1 tick to ensure connection is fully established
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                injectPlayer(event.getPlayer());
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        uninjectPlayer(event.getPlayer());
    }

    private void injectPlayer(Player player) {
        try {
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            ServerGamePacketListenerImpl packetListener = serverPlayer.connection;

            // Access the channel through reflection
            Channel channel = getChannel(packetListener);

            if (channel == null) {
                return;
            }

            // Remove existing handler if present
            if (channel.pipeline().get(HANDLER_NAME) != null) {
                channel.pipeline().remove(HANDLER_NAME);
            }

            // Add our handler before the packet_handler
            channel.pipeline().addBefore("packet_handler", HANDLER_NAME, new PacketHandler(player));
            playerChannels.put(player.getUniqueId(), channel);

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to inject packet handler for player " + player.getName() + ": " + e.getMessage());
        }
    }

    private Channel getChannel(ServerGamePacketListenerImpl packetListener) {
        try {
            if (connectionField != null && channelField != null) {
                Connection connection = (Connection) connectionField.get(packetListener);
                return (Channel) channelField.get(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void uninjectPlayer(Player player) {
        Channel channel = playerChannels.remove(player.getUniqueId());
        if (channel != null && channel.pipeline().get(HANDLER_NAME) != null) {
            try {
                channel.pipeline().remove(HANDLER_NAME);
            } catch (Exception ignored) {
                // Channel might already be closed
            }
        }
    }

    /**
     * Netty handler that intercepts incoming packets.
     */
    private class PacketHandler extends ChannelDuplexHandler {
        private final Player player;

        public PacketHandler(Player player) {
            this.player = player;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof ServerboundInteractPacket packet) {
                try {
                    int entityId = getEntityId(packet);
                    boolean isAttack = isAttackAction(packet);

                    // Check if this entity ID belongs to a packet entity
                    if (PacketEntityInteractionManager.getInstance().getByEntityId(entityId) != null) {
                        // This is a packet entity - handle on main thread and don't pass to server
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            PacketEntityInteractionManager.getInstance().handleInteraction(player, entityId, isAttack);
                        });
                        return; // Don't pass the packet to the server
                    }
                } catch (Exception e) {
                    // If anything goes wrong, let the packet through normally
                    e.printStackTrace();
                }
            }

            // Pass packet to next handler
            super.channelRead(ctx, msg);
        }
    }

    private static int getEntityId(ServerboundInteractPacket packet) {
        try {
            if (entityIdField != null) {
                return entityIdField.getInt(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static boolean isAttackAction(ServerboundInteractPacket packet) {
        try {
            if (actionField != null) {
                Object action = actionField.get(packet);
                // The action's class name contains "Attack" for attack actions
                return action.getClass().getSimpleName().contains("Attack");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
