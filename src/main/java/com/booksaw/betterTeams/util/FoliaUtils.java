package com.booksaw.betterTeams.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static com.booksaw.betterTeams.Main.plugin;

public class FoliaUtils {

	public static boolean isFolia() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static void teleport(final Player player, final Location location) {
		teleport(player, location, false);
	}

	/**
	 * Teleport player if sync or not sync.
	 */
	public static void teleport(final Player player, final Location location, final boolean sync) {
		final Runnable runnable = () -> {
			if (isFolia()) {
				// Using teleportAsync for Folia
				player.teleportAsync(location);
			} else {
				// Using teleport for Paper/Spigot/Bukkit forks
				player.teleport(location);
			}
		};

		if (isFolia()) {
			if (sync) {
				// Run synchronous task
				Bukkit.getRegionScheduler().execute(plugin, location, runnable);
			} else {
				// Run asynchronously
				runnable.run();
			}
		} else {
			if (sync) {
				// Run synchronous task
				Bukkit.getScheduler().runTask(plugin, runnable);
			} else {
				// Run asynchronously
				runnable.run();
			}
		}
	}
}
