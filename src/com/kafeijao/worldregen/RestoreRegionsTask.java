package com.kafeijao.worldregen;

import java.util.Map;

import org.bukkit.World;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.HeightMap;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.filtering.GaussianKernel;
import com.sk89q.worldedit.filtering.HeightMapFilter;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.CylinderRegionSelector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RestoreRegionsTask extends BukkitRunnable{
	
    private final int INNER_CIRCLE_RADIUS = 13;
    private final int OUTTER_CIRCLE_RADIUS = 17;
    private final int SMOOTH_REGION_OFFSET = 30;
    private final int SMOOTH_ITERATIONS = 5;
	
	private JavaPlugin plugin;
	
	private Map<String, CuboidClipboard> clipBoardsMap;
	private LocalWorld localWorld;
	private RegionManager wgRegionManager;
	private World world;
	private WorldGuardPlugin wgPlugin;
	
	
	public RestoreRegionsTask(JavaPlugin plugin, Map<String, CuboidClipboard> clipBoardsMap, LocalWorld localWorld, RegionManager wgRegionManager, World world, WorldGuardPlugin wgPlugin) {
		
		this.plugin = plugin;
		this.clipBoardsMap = clipBoardsMap;
		this.localWorld = localWorld;
		this.wgRegionManager = wgRegionManager;
		this.world = world;
		this.wgPlugin = wgPlugin;
		
	}
	
	
	
	@Override
	public void run() {
		
		
		plugin.getLogger().info("Starting to place regions!");
    	
    	//Cicle the map we creted that has the region name and the region Clipboards
    	for (Map.Entry<String, CuboidClipboard> cc : clipBoardsMap.entrySet()) {
    		
    		//Create a temporary edit session to point to the localworld
    		EditSession session = new EditSession(localWorld, Integer.MAX_VALUE);
    		
    		//Get the origin of the region so we can apply later the terrain modifications with a certain radius
    		BlockVector origin = getRegionOrigin(cc.getKey());
    		
    		//Create a temporary clipboard point to the hashmap cliboard value
    		CuboidClipboard clipBoard = cc.getValue();
    		
    		//Prepare the terrain to receive the region restore (smoothig, foundations, etc...)
    		preparePlotTerrain(wgRegionManager.getRegion(cc.getKey()));
    		
    		try {
    			//Attempt to paste the clibpoard to place it was originally
				clipBoard.paste(session, origin, false);
				plugin.getLogger().info("The region {" + cc.getKey() + "} was successfully Restored!");
			} catch (MaxChangedBlocksException e) {
				e.printStackTrace();
			}
    		
    	}
    	
    	plugin.getLogger().info("All regions were placed!");
    	
    	//Unregister the listener that disallow the players to login
    	PlayerLoginEvent.getHandlerList().unregister(plugin);

		
	}
	
	
	private void preparePlotTerrain(ProtectedRegion protectedRegion) {
   	 
   	 Vector pt1 = protectedRegion.getMinimumPoint();
   	 Vector pt2 = protectedRegion.getMaximumPoint();
   	 
   	 int regionMaxSideSize = Math.max(pt2.getBlockX() - pt1.getBlockX(), pt2.getBlockZ() - pt1.getBlockZ());
   	     	 
   	 RegionSelector selector = new CuboidRegionSelector(localWorld);
   	 
   	 EditSession session = new EditSession(localWorld, Integer.MAX_VALUE);
   	 
   	 selector.selectPrimary(pt1);
   	 selector.selectSecondary(pt2);
   	 
   	 Region region = null;

		try {
			region = selector.getRegion();
		} catch (IncompleteRegionException e) {
			e.printStackTrace();
		}
   	
		
		Vector center = region.getCenter();
		
		Vector2D radiusInner = new Vector2D(regionMaxSideSize + INNER_CIRCLE_RADIUS, regionMaxSideSize + INNER_CIRCLE_RADIUS);
		Vector2D radiusOutter = new Vector2D(regionMaxSideSize + OUTTER_CIRCLE_RADIUS, regionMaxSideSize + OUTTER_CIRCLE_RADIUS);
		
		
		//Stone
		RegionSelector outterCircleBase = new CylinderRegionSelector(localWorld, center.toVector2D(), radiusOutter, 2, 60);
				
		//Air
		RegionSelector outterCircleTop = new CylinderRegionSelector(localWorld, center.toVector2D(), radiusOutter, 63, world.getMaxHeight() - 1);
		
		//Sand if +20% water else Dirt
		RegionSelector outterCircle = new CylinderRegionSelector(localWorld, center.toVector2D(), radiusOutter, 60, 62);

		//Dirt if 20% water else do nothing
		RegionSelector innerCircle = new CylinderRegionSelector(localWorld, center.toVector2D(), radiusInner, 60, 62);
		
		

		
		
		//Generate chuncks that are in the outter region
		int radiusChuncks = (radiusOutter.getBlockX() / 16) + 2;

		
		for (int x = -radiusChuncks; x <= radiusChuncks; x++) {
			for (int z = -radiusChuncks; z <= radiusChuncks; z++) {
				world.loadChunk(region.getCenter().getBlockX() + x, region.getCenter().getBlockZ() + z, true);
			}
		}
				
		
		try {
			
			//Set the OutterCircleBase region to stone
			Region r1 = outterCircleBase.getRegion();
			session.setBlocks(r1, new BaseBlock(1));
			
			//Set the OutterCircleTop region to air
			Region r2 = outterCircleTop.getRegion();
			session.setBlocks(r2, new BaseBlock(0));
			
			//if 20% of the region blocks are water
			int waterBlocks = 0;
			for (int y = 60; y < 63; y++) {
				int blockRadius = radiusOutter.getBlockX();
				for (int x = -blockRadius; x <= blockRadius; x++) {
					for (int z = -blockRadius; z <= blockRadius; z++) {
						if (localWorld.getBlockType(new Vector(region.getCenter().getBlockX() + x, y, region.getCenter().getBlockZ() + z)) == 9) {
							waterBlocks++;
						}
					}
				}
			}
			
			//if 40% of the region blocks are sand
			int sandBlocks = 0;
			for (int y = 60; y < 63; y++) {
				int blockRadius = radiusOutter.getBlockX();
				for (int x = -blockRadius; x <= blockRadius; x++) {
					for (int z = -blockRadius; z <= blockRadius; z++) {
						if (localWorld.getBlockType(new Vector(region.getCenter().getBlockX() + x, y, region.getCenter().getBlockZ() + z)) == 12) {
							sandBlocks++;
						}
					}
				}
			}
			
			
			plugin.getLogger().info("Water blocks: " + waterBlocks + " AREA: :" + outterCircle.getArea() + " Ratio: " + 100*waterBlocks/outterCircle.getArea() + "%");
			plugin.getLogger().info("Sand blocks: " + sandBlocks + " AREA: :" + outterCircle.getArea() + " Ratio: " + 100*sandBlocks/outterCircle.getArea() + "%");
			//if 20% of the top 3 block layer is water
			if (waterBlocks != 0 && outterCircle.getArea() * 0.2 < waterBlocks) {
				
				Region r3 = outterCircle.getRegion();
				session.setBlocks(r3, new BaseBlock(12));
				
				Region r4 = innerCircle.getRegion();
				session.setBlocks(r4, new BaseBlock(2));
				
			//if 40% of the top 3 block layer is sand
			} else if (waterBlocks != 0 && outterCircle.getArea() * 0.4 < sandBlocks) {
				
				Region r3 = outterCircle.getRegion();
				session.setBlocks(r3, new BaseBlock(12));
				
				Region r4 = innerCircle.getRegion();
				session.setBlocks(r4, new BaseBlock(2));
				
			//Nothing above
			} else {
				Region r3 = innerCircle.getRegion();
				session.setBlocks(r3, new BaseBlock(2));
			}
			
			
		} catch (IncompleteRegionException e) {
			e.printStackTrace();
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}
		
		
		
		Vector2D radiusSmooth = new Vector2D(regionMaxSideSize + SMOOTH_REGION_OFFSET, regionMaxSideSize + SMOOTH_REGION_OFFSET);
		RegionSelector smoothCircle = new CylinderRegionSelector(localWorld, center.toVector2D(), radiusSmooth, 55, world.getMaxHeight() - 1);
		
		Region smoothRegion = null;
		
		try {
			 smoothRegion= smoothCircle.getRegion();
		} catch (IncompleteRegionException e1) {
			e1.printStackTrace();
		}
		
		int iterations = SMOOTH_ITERATIONS;

       HeightMap heightMap = new HeightMap(session, smoothRegion);
       HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
       
       try {
			heightMap.applyFilter(filter, iterations);
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}
		
   	 
    }
	
	
	private BlockVector getRegionOrigin(String regionName) {

		RegionManager wgRegionManager = wgPlugin.getRegionManager(world);
		ProtectedRegion region = wgRegionManager.getRegion(regionName);

		return region.getMinimumPoint();
	}

}
