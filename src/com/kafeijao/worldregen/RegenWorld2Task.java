package com.kafeijao.worldregen;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegenWorld2Task extends BukkitRunnable {
	 
    private final JavaPlugin plugin;
    
    private final File fileImport;
    
    private final String PLOT_PREFIX = "sh_";
    private final String WORLD_NAME = "world2";

    
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
	    	
	    	//Get all regions from worldguard and atribute to the map wgRegionMap
	    	Map<String, ProtectedRegion> wgRegionMap = wgRegionManager.getRegions();
	    	
	    	//Cicle the regions map being able to get each region name(key) and protectionRegion(value)
	    	for (Map.Entry<String, ProtectedRegion> region : wgRegionMap.entrySet()) {
	    		//If the region name starts with PLOT_PREFIX
	    		if (region.getKey().startsWith(PLOT_PREFIX)) {
	    			ProtectedRegion protectedRegion = region.getValue();
	    			//Save the region as a schematic AND get the CuboidClipboard of that region
	    			CuboidClipboard cc = saveRegionToSchematic(protectedRegion);
	    			//Add the region name and the clipboard we created from the region to a HashMap
	    			clipBoardsMap.put(protectedRegion.getId(), cc);
	    		}
	    	}
	    	
	    	
	    	plugin.getLogger().info("World2 is about to Regenerate...");
	    	
	    	//Delete the previous World, and regen a new world with a random seed
	    	mvPlugin.getMVWorldManager().regenWorld(world.getName(), true, true, null);
	    	
	    	plugin.getLogger().info("World2 has Regenerated...");
	    	
	    	//Load the world
	    	world = plugin.getServer().createWorld(new WorldCreator(world.getName()));
	    	List<LocalWorld> worldList = wePlugin.getServerInterface().getWorlds();
	        for (LocalWorld lw : worldList) {
	        	if (lw.getName().equals(WORLD_NAME)) {
	        		localWorld = lw;
	        	}
	        }
	        
	    	plugin.getLogger().info("World Loaded...");
	    	
	    	RestoreRegionsTask task = new RestoreRegionsTask(plugin, clipBoardsMap, localWorld, wgRegionManager, world);
	    	
	    	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 300);
	    	
	    	
	    	
	    	
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