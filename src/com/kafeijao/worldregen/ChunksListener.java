package com.kafeijao.worldregen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ChunksListener implements Listener {
	
	private JavaPlugin plugin;
	private World world;
	private WorldGuardPlugin wgPlugin;
	private int regionsHashCode = 0;
	private List<Chunk> chunks = new ArrayList<Chunk>();

	public ChunksListener(JavaPlugin plugin, String worldName) {

		this.plugin = plugin;
		this.world = plugin.getServer().getWorld(worldName);
				
		reloadRegions();

	}

	//Reload Chunks list
	private void reloadRegions() {
		
		this.wgPlugin = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");

		final int OUTTER_CIRCLE_RADIUS = 17;

		RegionManager wgRegionManager = wgPlugin.getRegionManager(world);
		Map<String, ProtectedRegion> wgRegionMap = wgRegionManager.getRegions();
		
		this.regionsHashCode = wgRegionMap.hashCode();

		for (Map.Entry<String, ProtectedRegion> regionInfo : wgRegionMap.entrySet()) {

			ProtectedRegion protectedRegion = regionInfo.getValue();

			Vector pt1 = protectedRegion.getMinimumPoint();
			Vector pt2 = protectedRegion.getMaximumPoint();

			int regionMaxSideSize = Math.max(pt2.getBlockX() - pt1.getBlockX(), pt2.getBlockZ() - pt1.getBlockZ());

			Vector2D radiusOutter = new Vector2D(regionMaxSideSize + OUTTER_CIRCLE_RADIUS, regionMaxSideSize + OUTTER_CIRCLE_RADIUS);

			int radiusChuncks = (radiusOutter.getBlockX() / 16) + 2;

			for (int x = -radiusChuncks; x <= radiusChuncks; x++) {
				for (int z = -radiusChuncks; z <= radiusChuncks; z++) {
					chunks.add(world.getChunkAt(x, z));
				}
			}

		}
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkUnload(ChunkUnloadEvent event) {
		
		RegionManager wgRegionManager = wgPlugin.getRegionManager(world);
		Map<String, ProtectedRegion> wgRegionMap = wgRegionManager.getRegions();
		
		if (wgRegionMap.hashCode() != this.regionsHashCode) {
			reloadRegions();
		}
		
		Chunk chunk = event.getChunk();
		
		if (chunks.contains(chunk)) {
			event.setCancelled(true);
		}
	}

	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWorldUnload(WorldUnloadEvent event) {
		if (event.getWorld().getName().equals(world.getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWorldLoad(WorldLoadEvent event) {

		if (event.getWorld().getName().equals(world.getName())) {

			for (Chunk chunk : chunks) {

				plugin.getLogger().info("Loaded Chunk: { " + chunk.getX() + " - " + chunk.getZ() + " }");
				world.loadChunk(chunk);

			}
		}
	}

}
