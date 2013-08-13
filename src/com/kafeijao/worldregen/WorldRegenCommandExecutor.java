package com.kafeijao.worldregen;

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
			
		
		
		
		return false;
	}

}
