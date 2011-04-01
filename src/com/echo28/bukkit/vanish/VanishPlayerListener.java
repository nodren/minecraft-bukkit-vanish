package com.echo28.bukkit.vanish;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;


/**
 * Handle events for all Player related events
 * 
 * @author Nodren
 */
public class VanishPlayerListener extends PlayerListener
{
	private final Vanish plugin;

	public VanishPlayerListener(Vanish instance)
	{
		plugin = instance;
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		plugin.updateInvisible(player);
	}

	@Override
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		plugin.invisible.remove(player);
	}

	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		if (event.isCancelled()) { return; }
		plugin.updateInvisibleOnTimer();
	}
}
