package com.kafeijao.worldregen;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.thoughtworks.xstream.XStream;

public class CuboidClipboardData {

	private JavaPlugin plugin;
	
	private LocalWorld world;
	
	private BaseBlock[][][] data;
			
    private Vector offset;
    private Vector origin;
    private Vector size;
    
    private boolean isValid;
    

	public CuboidClipboardData(JavaPlugin plugin, LocalWorld world, Vector size, BlockVector origin, Vector offset) {
		
		this.plugin = plugin;
		
		this.world = world;

		this.size = size;
		this.origin = origin;
		this.offset = offset;

		this.data = new BaseBlock[size.getBlockX()][size.getBlockY()][size.getBlockZ()];

		for (int x = 0; x < size.getBlockX(); ++x) {
			for (int y = 0; y < size.getBlockY(); ++y) {
				for (int z = 0; z < size.getBlockZ(); ++z) {
					data[x][y][z] = world.getBlock(new BlockVector(x, y, z).add(origin));
				}
			}
		}
		
		this.isValid = true;
		
	}
	
	public CuboidClipboardData(JavaPlugin plugin, LocalWorld world, File file) {
		
		this.plugin = plugin;
		this.world = world;
		this.isValid = importXML(file);
		
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
	
	public boolean isValid() {
		return isValid;
	}

	public Vector[] getVectors() {
		return new Vector[] {getSize(), getOrigin(), getOffset()};
	}
	
	public void setVectors(Vector[] vectors) {
		this.size = vectors[0];
		this.origin = vectors[1];
		this.offset = vectors[2];
	}
	
	
	public boolean exportXML(String fileName) {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = Calendar.getInstance().getTime();
		
		
		XStream xstream = new XStream();
		
		String xmlBlocks = xstream.toXML(getData());
		String xmlVectors = xstream.toXML(getVectors());
		
		String xml = xmlBlocks.concat(" &_& ").concat(xmlVectors);
		
		
		File file;
		
		try {
			file = new File(plugin.getDataFolder().getCanonicalPath()
					+ File.separatorChar + "Plots_Backup"
					+ File.separatorChar + dateFormat.format(date)
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
	
	public boolean importXML (File file) {
		
		String xml;
		
		try {
			xml = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		plugin.getLogger().info(xml);
		
		String[] strings = xml.split(" &_& ");
		
		plugin.getLogger().info("LENGHT ARRAY: " + strings.length);
		
		String blocksXML = strings[0];
		String vectorsXML = strings[1];
		
		
		XStream xStream = new XStream();
		
		this.data = (BaseBlock[][][]) xStream.fromXML(blocksXML);
		setVectors((Vector[]) xStream.fromXML(vectorsXML)); 
		
		toWorld();
		
		return true;
		
	}
	
	
	public boolean toWorld() {
		
		
		for (int x = 0; x < size.getBlockX(); ++x) {
			for (int y = 0; y < size.getBlockY(); ++y) {
				for (int z = 0; z < size.getBlockZ(); ++z) {
					world.setBlock(new BlockVector(x, y, z).add(origin), this.data[x][y][z], false);
				}
			}
		}
		
		return true;
	}


}
