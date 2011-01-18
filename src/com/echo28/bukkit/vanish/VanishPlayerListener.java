package com.echo28.bukkit.vanish;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;


/**
 * Handle events for all Player related events
 * 
 * @author Nodren
 */
public class VanishPlayerListener extends PlayerListener
{
	private final VanishPlugin plugin;

	public VanishPlayerListener(VanishPlugin instance)
	{
		plugin = instance;
	}

	@Override
	public void onPlayerJoin(PlayerEvent event)
	{
		Player player = event.getPlayer();
		plugin.updateInvisible(player);
		/*
		 * if (player.isInGroup(AUTO_ON_GROUP)) { if (plugin.isDebugging(player)) { player.sendMessage("Auto On group !"); } vanish(player); }
		 */
	}

	@Override
	public void onPlayerQuit(PlayerEvent event)
	{
		Player player = event.getPlayer();
		plugin.invisible.remove(player);
	}
}
