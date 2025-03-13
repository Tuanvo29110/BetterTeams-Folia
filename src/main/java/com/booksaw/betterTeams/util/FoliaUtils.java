package com.booksaw.betterTeams.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static com.booksaw.betterTeams.Main.plugin;

/**
 * Utility class for handling teleportation in both Folia and Paper/Spigot/Bukkit servers.
 *
 * <p>Folia is a fork of Paper that introduces region-based multithreading, allowing different
 * regions in the world to run on separate threads for better performance. However, this
 * comes with limitations on accessing world data asynchronously.</p>
 *
 * <p>Relevant resources:</p>
 * <ul>
 *     <li><a href="https://github.com/PaperMC/Folia?tab=readme-ov-file#current-broken-api">Folia Broken API List</a></li>
 *     <li><a href="https://docs.papermc.io/folia/faq">Folia FAQ</a></li>
 * </ul>
 *
 * <p>This class automatically detects the server type and handles teleportation safely,
 * using region-safe methods for Folia and standard methods for Paper/Spigot/Bukkit.</p>
 *
 */
public class FoliaUtils {

	private static final boolean isFolia;

	// Static block to detect if the server is Folia
	static {
		boolean foliaCheck = false;
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			foliaCheck = true;
			plugin.getLogger().info("Folia detected! Using region-based threading system.");
		} catch (ClassNotFoundException ignored) {
			plugin.getLogger().info("Running on standard Paper/Spigot/Bukkit server.");
		}
		isFolia = foliaCheck;
	}

	/**
	 * Check if the server is running Folia.
	 */
	public static boolean isFolia() {
		return isFolia;
	}

	/**
	 * Teleport a player to a location asynchronously by default.
	 *
	 * <p>If the server is Folia, this method uses {@code teleportAsync}. Otherwise,
	 * it falls back to the standard {@code teleport} method for Paper/Spigot/Bukkit.</p>
	 *
	 */
	public static void teleport(final Player player, final Location location) {
		teleport(player, location, false);
	}

	/**
	 * Teleport a player to a location, either synchronously or asynchronously.
	 *
	 */
	public static void teleport(final Player player, final Location location, final boolean sync) {
		final Runnable teleportTask = () -> {
			if (isFolia) {
				player.teleportAsync(location); // Folia async teleport
			} else {
				player.teleport(location); // Paper/Spigot synchronous teleport
			}
		};

		if (isFolia) {
			if (sync) {
				// Folia: Run synchronous task within the player's region
				Bukkit.getRegionScheduler().execute(plugin, location, teleportTask);
			} else {
				// Folia: Run asynchronous teleport
				teleportTask.run();
			}
		} else {
			if (sync) {
				// Paper/Spigot: Run synchronous teleport on the main thread
				Bukkit.getScheduler().runTask(plugin, teleportTask);
			} else {
				// Paper/Spigot: Run asynchronous teleport
				teleportTask.run();
			}
		}
	}
}
