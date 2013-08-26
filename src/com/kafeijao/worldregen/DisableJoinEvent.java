package com.kafeijao.worldregen;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class DisableJoinEvent implements Listener{

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		event.disallow(Result.KICK_OTHER, "World2 is Regenerating, please wait... 1min~");
	}
	
}
