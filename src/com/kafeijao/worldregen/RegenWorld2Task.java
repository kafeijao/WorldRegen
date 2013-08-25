package com.kafeijao.worldregen;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Countable;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.HeightMap;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.filtering.GaussianKernel;
import com.sk89q.worldedit.filtering.HeightMapFilter;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.CylinderRegionSelector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegenWorld2Task extends BukkitRunnable {
	 
    private final JavaPlugin plugin;
    
    private final File fileImport;
    
    private final String PLOT_PREFIX = "sh_";
    private final String WORLD_NAME = "world2";
    private final int INNER_CIRCLE_RADIUS = 15;
    private final int OUTTER_CIRCLE_RADIUS = 17;
    private final int SMOOTH_REGION_OFFSET = 30;
    private final int SMOOTH_ITERATIONS = 5;
    
    private MultiverseCore mvPlugin;
    private WorldGuardPlugin wgPlugin;	
    private WorldEditPlugin wePlugin;
    
    private World world;
    
    private LocalWorld localWorld = null;
 
    
    
public RegenWorld2Task(JavaPlugin plugin) {
    	
    	
        this.plugin = plugin;
        
        this.fileImport = null;
        
        world = plugin.getServer().getWorld(WORLD_NAME);
        
        mvPlugin = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        wgPlugin = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");	
        wePlugin = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        
        List<LocalWorld> worldList = wePlugin.getServerInterface().getWorlds();
        for (LocalWorld lw : worldList) {
        	if (lw.getName().equals(WORLD_NAME)) {
        		localWorld = lw;
        	}
        }
        
    }


	public RegenWorld2Task(JavaPlugin plugin, File file) {

		this.plugin = plugin;
		
		this.fileImport = file;

		world = plugin.getServer().getWorld(WORLD_NAME);

		mvPlugin = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
		wgPlugin = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		wePlugin = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
		
		List<LocalWorld> worldList = wePlugin.getServerInterface().getWorlds();
        for (LocalWorld lw : worldList) {
        	if (lw.getName().equals(WORLD_NAME)) {
        		localWorld = lw;
        	}
        }
        
	}
 
    @Override
	public void run() {
    	
    	if (this.fileImport == null) {
    	
    		Map<String, CuboidClipboard> clipBoardsMap = new HashMap<String, CuboidClipboard>();
    		
	    	RegionManager wgRegionManager = wgPlugin.getRegionManager(world);
	    	
	    	Map<String, ProtectedRegion> wgRegionMap = wgRegionManager.getRegions();
	    	
	    	for (Map.Entry<String, ProtectedRegion> region : wgRegionMap.entrySet()) {
	    		if (region.getKey().startsWith(PLOT_PREFIX)) {
	    			ProtectedRegion protectedRegion = region.getValue();
	    			CuboidClipboard cc = saveRegionToSchematic(protectedRegion);
	    			clipBoardsMap.put(protectedRegion.getId(), cc);
	    		}
	    	}
	    	
	    	
	    	plugin.getLogger().info("World2 is about to Regenerate...");
	    	
	    	mvPlugin.getMVWorldManager().regenWorld(world.getName(), true, true, null);
	    	
	    	plugin.getLogger().info("World2 has Regenerated...");
	    	
	    	plugin.getLogger().info("Starting to place regions!");
	    	
	    	for (Map.Entry<String, CuboidClipboard> cc : clipBoardsMap.entrySet()) {
	    		
	    		EditSession session = new EditSession(localWorld, Integer.MAX_VALUE);
	    		
	    		BlockVector origin = getRegionOrigin(cc.getKey());
	    		
	    		CuboidClipboard clipBoard = cc.getValue();
	    		
	    		
	    		preparePlotTerrain(wgRegionManager.getRegion(cc.getKey()));
	    		
	    		try {
					clipBoard.paste(session, origin, false);
					plugin.getLogger().info("The region {" + cc.getKey() + "} was successfully Restored!");
				} catch (MaxChangedBlocksException e) {
					e.printStackTrace();
				}
	    		
	    	}
	    	
	    	plugin.getLogger().info("All regions were placed!");
	    	
	    	
    	//if a file is specified do this, meaning: if import schematic command is used
    	} else {
    		
    		SchematicFormat schematic = SchematicFormat.MCEDIT;
    		
    		CuboidClipboard clipboard = null;
    		
    		try {
				clipboard = schematic.load(fileImport);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DataException e) {
				e.printStackTrace();
			}
    		
    		EditSession session = new EditSession(localWorld, Integer.MAX_VALUE);
    		
    		
    		
    		try {
				clipboard.paste(session, getRegionOrigin(fileImport.getName()), false);
				plugin.getLogger().info("A importação do schematic " + fileImport.getPath() + " foi feita com sucesso!");
			} catch (MaxChangedBlocksException e) {
				plugin.getLogger().info("ERRO: A importação do schematic " + fileImport.getPath() + " nao foi feita com sucesso!");
				e.printStackTrace();
			}
    		
    	}
    	
    }
    
    private CuboidClipboard saveRegionToSchematic(ProtectedRegion protectedRegion) {
    	
    	BlockVector originVector = protectedRegion.getMinimumPoint().toBlockPoint();
		Vector maximunVector = protectedRegion.getMaximumPoint().toBlockPoint();
		
		Vector sizeVector = maximunVector.subtract(originVector).add(1, 1, 1);
		
		
		
		
        CuboidClipboard clipBoard = new CuboidClipboard(sizeVector, originVector, Vector.ZERO);
        
        EditSession session = new EditSession(localWorld, Integer.MAX_VALUE);
        
        clipBoard.copy(session);
        
        SchematicFormat schematic = SchematicFormat.MCEDIT;
        
       
        String fileName = protectedRegion.getId();
		
        File file = getSchematicFile(fileName);
		
		try {
			schematic.save(clipBoard, file);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DataException e) {
			e.printStackTrace();
		}

		return clipBoard;
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
		
		

		
		
		//Generate chuncks that are in the region
		int radiusChuncks = (radiusOutter.getBlockX() / 16) + 2;

		plugin.getLogger().info("RAIO DE BLOCOS: " + radiusOutter.getBlockX() + "RAIO DE CHUNCKS: " + radiusChuncks + ", x start: " + (-radiusChuncks) + ", acaba quando for igual a: " + (radiusChuncks*2));
		
		for (int x = -radiusChuncks; x <= radiusChuncks; x++) {
			for (int z = -radiusChuncks; z <= radiusChuncks; z++) {
				world.loadChunk(region.getCenter().getBlockX() + x, region.getCenter().getBlockZ() + z, true);
			}
			plugin.getLogger().info("Chunck: [" + x + "] origin: [" + region.getCenter().getBlockX() + ", " + region.getCenter().getBlockZ() + "]");
		}
		
		world.save();
		
		
		try {
			
			//Set the OutterCircleBase region to stone
			Region r1 = outterCircleBase.getRegion();
			session.setBlocks(r1, new BaseBlock(1));
			
			//Set the OutterCircleTop region to air
			Region r2 = outterCircleTop.getRegion();
			session.setBlocks(r2, new BaseBlock(0));
			
			//if 20% of the region blocks are water
			List<Countable<Integer>> blocks = session.getBlockDistribution(outterCircle.getRegion());
			int waterBlocks = 0;
			for (Countable<Integer> block : blocks) {
				if (block.getID() == 9) {
					waterBlocks = block.getAmount();
				}
			}
			
			plugin.getLogger().info("Water blocks: " + waterBlocks + " AREA: :" + outterCircle.getArea());
			
			if (waterBlocks != 0 && outterCircle.getArea() * 0.2 < waterBlocks) {
				
				Region r3 = outterCircle.getRegion();
				session.setBlocks(r3, new BaseBlock(12));
				
				Region r4 = innerCircle.getRegion();
				session.setBlocks(r4, new BaseBlock(2));
				
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

	private File getSchematicFile(String fileName) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = Calendar.getInstance().getTime();

		File file = null;

		try {
			file = new File(plugin.getDataFolder().getCanonicalPath()
					+ File.separatorChar + "Plots_Backup" + File.separatorChar
					+ dateFormat.format(date) + File.separatorChar + fileName
					+ ".schematic");

			file.getParentFile().mkdirs();
			file.createNewFile();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;

	}
    
    private BlockVector getRegionOrigin(String regionName) {
    	
    	if (regionName.endsWith(".schematic")) {
    		String regionNames[] = fileImport.getName().split(".schematic");
    		regionName = regionNames[0];
    	}
    	
    	RegionManager wgRegionManager = wgPlugin.getRegionManager(world);
		ProtectedRegion region = wgRegionManager.getRegion(regionName);
		
		return region.getMinimumPoint();
    }
  
    
}