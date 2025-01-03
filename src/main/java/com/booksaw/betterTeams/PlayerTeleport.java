package com.booksaw.betterTeams;

import com.booksaw.betterTeams.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * A class to handle a teleport with a delay
 *
 * @author booksaw
 */
public class PlayerTeleport {

    final String reference;
    private final Player player;
    private final Location location;
    private final Location playerLoc;

    /**
     * This will start the cooldown (there is no delay)
     *
     * @param player    The player to teleport
     * @param location  the location to teleport them to
     * @param reference the reference for the message that should be sent when the
     *                  player is teleported
     * @throws Exception 
     */
    public PlayerTeleport(Player player, Location location, String reference) throws Exception {
        if (location == null || location.getWorld() == null) {
            throw new Exception("Location or world is null");
        }

        this.player = player;
        this.location = location;
        this.reference = reference;

        this.playerLoc = player.getLocation();

        if (player.hasPermission("betterteams.warmup.bypass")) {
            Main.plugin.getScheduler().runTask(() -> runTp(player, location));
            return;
        }

        int wait = Main.plugin.getConfig().getInt("tpDelay");
        if (wait <= 0) {
            Main.plugin.getScheduler().runTask(() -> runTp(player, location));
            return;
        }

        // sending the wait message
        MessageManager.sendMessage(player, "teleport.wait", wait);

        Main.plugin.getScheduler().runTaskLater(() -> {
            if (canTp()) {
                try {
                    runTp(player, location);
                } catch (Exception e) {
                    throw new NullPointerException();
                }
            } else {
                cancel();
            }
        }, 20L * wait);
    }

    public void runTp(Player player, Location location) {
        try {
            player.getScheduler().run(Main.plugin, scheduledTask -> {
                World world = location.getWorld();
                if (world != null) {
                    world.getChunkAtAsync(location).thenAccept(chunk -> {
                        player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    });
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
            MessageManager.sendMessage(player, "teleport.fail");
        }
    }

    public boolean canTp() {
        if (!Main.plugin.getConfig().getBoolean("noMove")) {
            return true;
        }

        return playerLoc.getWorld() == player.getLocation().getWorld() && playerLoc.distance(player.getLocation()) <= Math.abs(Main.plugin.getConfig().getInt("maxMove"));
    }

    public void cancel() {
        MessageManager.sendMessage(player, "teleport.fail");
    }
}