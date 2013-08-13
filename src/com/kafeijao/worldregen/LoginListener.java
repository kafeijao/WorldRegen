package com.kafeijao.worldregen;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class LoginListener implements Listener{

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		Bukkit.broadcastMessage("O Player " + event.getPlayer().getDisplayName() + " entrou no server, " + ChatColor.DARK_RED + "FUJAM!!!");		
	}
	
}
