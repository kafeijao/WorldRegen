package com.kafeijao.worldregen;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.thoughtworks.xstream.XStream;

public class RegenWorld2Task extends BukkitRunnable {
	 
    private final JavaPlugin plugin;
    
    private final String PLOT_PREFIX = "sh_";
    private final String WORLD_NAME = "world2";
    
    private MultiverseCore mvPlugin;
    private WorldGuardPlugin wgPlugin;	
    private WorldEditPlugin wePlugin;
    
    private World world;
 
    
    
    public RegenWorld2Task(JavaPlugin plugin) {
    	
    	
        this.plugin = plugin;
        
        world = plugin.getServer().getWorld(WORLD_NAME);
        
        mvPlugin = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        wgPlugin = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");	
        wePlugin = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
    }
 
    @Override
	public void run() {
    	
    	
    	
    	RegionManager wgRegionManager = wgPlugin.getRegionManager(world);
    	
    	Map<String, ProtectedRegion> wgRegionMap = wgRegionManager.getRegions();
    	
    	for (Map.Entry<String, ProtectedRegion> region : wgRegionMap.entrySet()) {
    		
    		if (region.getKey().startsWith(PLOT_PREFIX)) {
    			
    			ProtectedRegion protectedRegion = region.getValue();
    			saveRegionToSchematic(protectedRegion);
    			
    			
    		}
    		
    	}
    	
    	
    	
    	
    	plugin.getServer().broadcastMessage("World2 is about to regenerate...");
    	
    	//mvPlugin.getMVWorldManager().regenWorld("world", true, true, null);
    	
    	plugin.getServer().broadcastMessage("World2 is fully regenerated...");
    	
    }
    
    private boolean saveRegionToSchematic(ProtectedRegion protectedRegion) {
    	
    	BlockVector originVector = protectedRegion.getMinimumPoint().toBlockPoint();
		Vector maximunVector = protectedRegion.getMaximumPoint().toBlockPoint();
		
		Vector sizeVector = maximunVector.subtract(originVector).add(1, 1, 1);
		
		
		List<LocalWorld> worldList = wePlugin.getServerInterface().getWorlds();
        LocalWorld localWorld = null;
        for (LocalWorld lw : worldList) {
        	if (lw.getName().equals(WORLD_NAME)) {
        		localWorld = lw;
        	}
        }

        
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
	
		return true;
    }
    
    
    
    
    private File getSchematicFile(String fileName) {
    	
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = Calendar.getInstance().getTime();
		
		File file = null;
		
		try {
			file = new File(plugin.getDataFolder().getCanonicalPath()
					+ File.separatorChar + "Plots_Backup"
					+ File.separatorChar + dateFormat.format(date)
					+ File.separatorChar + fileName + ".schematic");
			 
			file.getParentFile().mkdirs();
			file.createNewFile();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return file;
		
		
    }
  
    
}