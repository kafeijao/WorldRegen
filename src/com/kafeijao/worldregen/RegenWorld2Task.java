package com.kafeijao.worldregen;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.thoughtworks.xstream.XStream;

public class RegenWorld2Task extends BukkitRunnable {
	 
    private final JavaPlugin plugin;
    
    private MultiverseCore mvPlugin;
    private WorldGuardPlugin wgPlugin;	
    private WorldEditPlugin wePlugin;
    
    private World world2;
 
    private final String PLOT_PREFIX = "sh_test7";
    
    public RegenWorld2Task(JavaPlugin plugin) {
        this.plugin = plugin;
        
        world2 = plugin.getServer().getWorld("world2");
        
        mvPlugin = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        wgPlugin = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");	
        wePlugin = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
    }
 
    @Override
	public void run() {
    	
    	
    	
    	RegionManager wgRegionManager = wgPlugin.getRegionManager(world2);
    	
    	Map<String, ProtectedRegion> wgRegionMap = wgRegionManager.getRegions();
    	
    	for (Map.Entry<String, ProtectedRegion> region : wgRegionMap.entrySet()) {
    		
    		if (region.getKey().startsWith(PLOT_PREFIX)) {
    			
    			ProtectedRegion protectedRegion = region.getValue();
    			saveClipboard(protectedRegion);
    			
    			
    		}
    		
    	}
    	
    	
    	
    	
    	plugin.getServer().broadcastMessage("World2 is about to regenerate...");
    	
    	//mvPlugin.getMVWorldManager().regenWorld("world2", true, true, null);
    	
    	plugin.getServer().broadcastMessage("World2 is fully regenerated...");
    	
    }
    
    private boolean saveClipboard(ProtectedRegion protectedRegion) {
    	
    	//Criar o Clipboard
    	Vector originVector = protectedRegion.getMinimumPoint().toBlockPoint();
		Vector maximunVector = protectedRegion.getMaximumPoint().toBlockPoint();
		
		Vector sizeVector = maximunVector.subtract(originVector).add(1, 1, 1);
		
		
		List<LocalWorld> worldList = wePlugin.getServerInterface().getWorlds();
        LocalWorld localWorld = null;
        for (LocalWorld lw : worldList) {
        	if (lw.getName().equals("world2")) {
        		localWorld = lw;
        	}
        }

		
		CuboidClipboardData cc = new CuboidClipboardData(localWorld, sizeVector, originVector, new Vector(1, 1, 1));
		
    	
		String fileName = protectedRegion.getId();
		
		cc.export(fileName, cc);
		
		

		return true;
    	
    }
    
  
    private class CuboidClipboardData {

		private BaseBlock[][][] data;
				
	    private Vector offset;
	    private Vector origin;
	    private Vector size;
	    		

		public CuboidClipboardData(LocalWorld localWorld, Vector size, Vector origin, Vector offset) {
			
			this.size = size;
			this.origin = origin;
	        this.offset = offset;
	        	        
	        this.data = new BaseBlock[size.getBlockX()][size.getBlockY()][size.getBlockZ()];
	        
	       
	        
	        for (int x = 0; x < size.getBlockX(); ++x) {
	            for (int y = 0; y < size.getBlockY(); ++y) {
	                for (int z = 0; z < size.getBlockZ(); ++z) {
	                    data[x][y][z] = localWorld.getBlock(new Vector(x, y, z).add(origin));
	                    plugin.getLogger().info(localWorld.getBlock(new Vector(x, y, z).add(origin)).toString());
	                    //blocks[x][y][z] = world2.getBlockAt(x+origin.getBlockX(), y+origin.getBlockY(), z+origin.getBlockZ());
	                    plugin.getLogger().info("Loc: " + "{x: " + x + ",y: " + y + ",z: " + z + "} - " + data[x][y][z].toString());
	                }
	            }
	        }
	        
		}

		public BaseBlock[][][] getData() {
			return data;
		}

		public Vector getOffset() {
			return offset;
		}

		public Vector getOrigin() {
			return origin;
		}

		public Vector getSize() {
			return size;
		}
		

		public boolean export(String fileName, CuboidClipboardData cc) {
			
			XStream xstream = new XStream();
			
			String xml = xstream.toXML(getData());
			
			File file;
			
			try {
				 file = new File(plugin.getDataFolder().getCanonicalPath()
						+ File.separatorChar + "Plots_Temp"
						+ File.separatorChar + fileName + ".clipboard");
				 
				file.getParentFile().mkdirs();
				file.createNewFile();
				
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
			try {
				FileUtils.writeStringToFile(file, xml);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
		}

	}

}