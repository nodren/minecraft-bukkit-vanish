package com.echo28.bukkit.vanish;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;


/**
 * Handle events for all Player related events
 * 
 * @author Nodren
 */
public class VanishPlayerListener extends PlayerListener
{
	private final VanishPlugin plugin;
	private final Logger log = Logger.getLogger("Minecraft");

	public VanishPlayerListener(VanishPlugin instance)
	{
		plugin = instance;
	}

	@Override
	public void onPlayerJoin(PlayerEvent event)
	{
		Player player = event.getPlayer();
		plugin.updateInvisible(player);
		if ((plugin.perm != null) && (plugin.perm.Security.getGroup(player.getName()).equalsIgnoreCase(plugin.AUTO_ON_GROUP)))
		{
			log.info(plugin.getDescription().getName() + "Auto hiding " + player.getName() + "because he is in group " + plugin.AUTO_ON_GROUP);
			plugin.vanish(player);
		}
	}

	@Override
	public void onPlayerQuit(PlayerEvent event)
	{
		Player player = event.getPlayer();
		plugin.invisible.remove(player.getName());
	}

	@Override
	public void onPlayerTeleport(PlayerMoveEvent event)
	{
		if (event.isCancelled()) { return; }
		plugin.updateInvisibleOnTimer();
	}
}
