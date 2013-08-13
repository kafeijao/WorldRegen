package com.kafeijao.worldregen;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
		event.setJoinMessage(ChatColor.RED + event.getPlayer().getName() + ChatColor.AQUA + " afirma que o Artur é" + ChatColor.BOLD + ChatColor.LIGHT_PURPLE + " SEXY " + ChatColor.DARK_RED + "D:");
	}
	
}
