package com.kafeijao.worldregen;


import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class WorldRegen extends JavaPlugin{

	
	@Override
	public void onEnable() {
		
		//Consola
		getLogger().info("O ARTUR NAO E SEXY!!!");
		
		//Add external jars
		JarUtils.addExternalClassPaths(this);
		
		//Registar Eventos
		getServer().getPluginManager().registerEvents(new LoginListener(), this);
		getServer().getPluginManager().registerEvents(new JoinListener(), this);
		
		//Commandos
		getCommand("rain").setExecutor(new WorldRegenCommandExecutor(this));
		getCommand("norain").setExecutor(new WorldRegenCommandExecutor(this));
		getCommand("l").setExecutor(new WorldRegenCommandExecutor(this));
		
		//Configs
		saveDefaultConfig();
		
		@SuppressWarnings("unused")
		List<String> adminsList = getConfig().getStringList("admins");
		
		//Scheldue task
		@SuppressWarnings("unused")
		BukkitTask task = new MessageBrodcastTask(this).runTaskTimer(this, 1200, 1200);
		
		
		
		
		
	}
	
	@Override
	public void onDisable() {


	}
	

}
