package com.kafeijao.worldregen;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MessageBrodcastTask extends BukkitRunnable {
	 
    private final JavaPlugin plugin;
 
    public MessageBrodcastTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }
 
    @Override
	public void run() {
        plugin.getServer().broadcastMessage(ChatColor.DARK_RED + "NOS AMAMOS O ARTURZINHO SEXY!!!");
    }
 
}