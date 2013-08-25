package com.kafeijao.worldregen;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

public class WorldRegenCommandExecutor implements CommandExecutor{

	private WorldRegen plugin;
	 
	public WorldRegenCommandExecutor(WorldRegen plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		World world = Bukkit.getWorld("world");

		//RAIN COMMAND
		if (cmd.getName().equalsIgnoreCase("rain")){
			
			if (!world.hasStorm()) {
				world.setStorm(true);
				world.setThundering(true);
				return true;
			} else {
				sender.sendMessage(ChatColor.DARK_RED + "Já está a chover burro de merda!");
				return true;
			}
		}
		
		//NORAIN COMMAND
		if (cmd.getName().equalsIgnoreCase("norain")){
			if (world.hasStorm()) {
				world.setStorm(false);
				world.setThundering(false);
				return true;
			} else {
				sender.sendMessage(ChatColor.DARK_RED + "Não está a chover burro de merda!");
				return true;
			}
		}
		
		//LIGHTNING COMMAND (L)
		if (cmd.getName().equalsIgnoreCase("lightning")) {
			
			@SuppressWarnings("unused")
			BukkitTask task2 = new RegenWorld2Task(plugin).runTask(plugin);
			
			if (args.length > 0 && plugin.getServer().getPlayer(args[0]) != null) {
				world = Bukkit.getPlayer(args[0]).getWorld();
				world.strikeLightning(plugin.getServer().getPlayer(args[0]).getLocation());
				return true;
			}
			sender.sendMessage("Nome do Player Inválido ou está offline defeciente!");
			return false;
		}
		
		//ImportXML Command
		if (cmd.getName().equalsIgnoreCase("importXML")) {
			sender.sendMessage("Command ISSUED. Args: " + args.length);
			if (args.length > 1 && args.length < 3) {
							
				String fileName = args[0];
				
				String date = args[1];
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				
				try {
					dateFormat.parse(date);
				} catch (ParseException e1) {
					sender.sendMessage("Date must be in the format 'yyyy-mm-dd', eg: 2013-08-14");
					return false;
				}
				
				File file;
				
				try {
					file = new File(plugin.getDataFolder().getCanonicalPath()
							+ File.separatorChar + "Plots_Backup"
							+ File.separatorChar + date
							+ File.separatorChar + fileName + ".schematic");
					
					sender.sendMessage(file.getAbsolutePath());
					
					if (!file.exists()) {
						sender.sendMessage("The file specified doesn't exist!");
						return false;
					}

				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
				
				
				@SuppressWarnings("unused")
				BukkitTask task2 = new RegenWorld2Task(plugin, file).runTask(plugin);
				
		        
		
				
			} else {
				sender.sendMessage("Invalid Arguments Number...");
				return false;
			}
			
			
		
			
		
		}
		
		return false;
	}

}
