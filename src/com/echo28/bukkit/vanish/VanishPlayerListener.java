package com.echo28.bukkit.vanish;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet29DestroyEntity;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Player;
import org.bukkit.craftbukkit.CraftPlayer;
import org.bukkit.event.player.PlayerChatEvent;
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
	private HashMap<String, Player> invisible = new HashMap<String, Player>();
	private final Logger log = Logger.getLogger("Minecraft");

	public VanishPlayerListener(VanishPlugin instance)
	{
		plugin = instance;
	}

	@Override
	public void onPlayerJoin(PlayerEvent event)
	{
		Player player = event.getPlayer();
		updateInvisible(player);
		/*
		 * if (player.isInGroup(AUTO_ON_GROUP)) { if (plugin.isDebugging(player)) { player.sendMessage("Auto On group !"); } vanish(player); }
		 */
	}

	@Override
	public void onPlayerQuit(PlayerEvent event)
	{
		Player player = event.getPlayer();
		invisible.remove(player);
	}

	@Override
	public void onPlayerCommand(PlayerChatEvent event)
	{
		String[] split = event.getMessage().split(" ");
		Player player = event.getPlayer();

		if (split[0].equalsIgnoreCase("/vanish"))
		{
			if (split[1].equalsIgnoreCase("list"))
			{
				String message = "List of Invisible Players: ";
				for (Player InvisiblePlayer : invisible.values())
				{
					message += InvisiblePlayer.getName() + ", ";
				}
				player.sendMessage(Color.RED
						+ message.substring(0,
								message.length() - 2));
				event.setCancelled(true);
				return;
			}
			vanish(player);
			event.setCancelled(true);
		}
		else if (split[0].equalsIgnoreCase("/tp")
				|| split[0].equalsIgnoreCase("/home")
				|| split[0].equalsIgnoreCase("/spawn")
				|| split[0].equalsIgnoreCase("/warp")
				|| split[0].equalsIgnoreCase("/t"))
		{
			if (split[0].equalsIgnoreCase("/tp")
					&& split.length == 2 && plugin.DISABLE_TP)
			{
				// let's drop an error if they try and tp to someone invisible
				// giving away their position.
				Player tpPlayer = plugin.getServer().getPlayer(
						split[1]);
				if (tpPlayer != null)
				{
					if (player.getName().equalsIgnoreCase(
							tpPlayer.getName()))
					{
						player.sendMessage(Color.RED
								+ "You're already here!");
						event.setCancelled(true);
					}
					if (invisible.get(tpPlayer.getName()) != null)
					{
						// same message that /tp uses
						String message = "Can't find user "
								+ split[1] + ".";
						player.sendMessage(Color.RED + message);
						log.info(player.getName()
								+ " tried to /tp to " + split[1]
								+ " but they are invisible.");
						event.setCancelled(true);
					}
				}
			}
			updateInvisibleForAll();
			Timer timer = new Timer();
			Player[] playerList = plugin.getServer()
					.getOnlinePlayers();
			int i = 0;
			while (i < plugin.TOTAL_REFRESHES)
			{
				i++;
				timer.schedule(new UpdateInvisibleTimerTask(
						playerList), i * 1000);
			}
		}
	}

	private void invisible(Player p1, Player p2)
	{
		CraftPlayer hide = (CraftPlayer)p1;
		CraftPlayer hideFrom = (CraftPlayer)p2;
		hideFrom.getHandle().a.b(new Packet29DestroyEntity(hide.getHandle().g));
	}

	private void uninvisible(Player p1, Player p2)
	{
		CraftPlayer unHide = (CraftPlayer)p1;
		CraftPlayer unHideFrom = (CraftPlayer)p2;
		unHideFrom.getHandle().a.b(new Packet20NamedEntitySpawn(unHide.getHandle()));
	}

	public void vanish(Player player)
	{
		if (invisible.get(player.getName()) != null)
		{
			reappear(player);
			return;
		}
		invisible.put(player.getName(), player);
		Player[] playerList = plugin.getServer()
				.getOnlinePlayers();
		for (Player p : playerList)
		{
			if (getDistance(player, p) <= plugin.RANGE
					&& !p.equals(player))
			{
				invisible(player,p);
			}
		}
		log.info(player.getName() + " disappeared.");
		player.sendMessage(Color.RED + "Poof!");
	}

	public void reappear(Player player)
	{
		if (invisible.get(player.getName()) != null)
		{
			invisible.remove(player.getName());
			// make someone really disappear if there's any doubt, should remove
			// cloning
			updateInvisibleForAll();
			Player[] playerList = plugin.getServer()
					.getOnlinePlayers();
			for (Player p : playerList)
			{
				if (getDistance(player, p) < plugin.RANGE
						&& !p.equals(player))
				{
					uninvisible(player,p);
				}
			}
			log.info(player.getName() + " reappeared.");
			player.sendMessage(Color.RED + "You have reappeared!");
		}
	}

	public void reappearAll()
	{
		log.info("Everyone is going reappear.");
		for (Player InvisiblePlayer : invisible.values())
		{
			reappear(InvisiblePlayer);
		}
		invisible.clear();
	}

	public void updateInvisibleForAll()
	{
		Player[] playerList = plugin.getServer()
				.getOnlinePlayers();
		for (Player invisiblePlayer : invisible.values())
		{
			for (Player p : playerList)
			{
				if (getDistance(invisiblePlayer, p) <= plugin.RANGE
						&& !p.equals(invisiblePlayer))
				{
					invisible(invisiblePlayer,p);
				}
			}
		}
	}

	public void updateInvisible(Player player)
	{
		for (Player invisiblePlayer : invisible.values())
		{
			if (getDistance(invisiblePlayer, player) <= plugin.RANGE
					&& !player.equals(invisiblePlayer))
			{
				invisible(invisiblePlayer,player);
			}
		}
	}

	public double getDistance(Player player1, Player player2)
	{
		Location loc1 = player1.getLocation();
		Location loc2 = player1.getLocation();
		return Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2)
				+ Math.pow(loc1.getY() - loc2.getY(), 2)
				+ Math.pow(loc1.getZ() - loc2.getZ(), 2));
	}

	private class UpdateInvisibleTimerTask extends TimerTask
	{
		Player[] playerList;

		public UpdateInvisibleTimerTask(Player[] playerList)
		{
			// this.invisible = invisible;
			this.playerList = playerList;
		}

		public void run()
		{
			for (Player invisiblePlayer : invisible.values())
			{
				for (Player p : playerList)
				{
					if (getDistance(invisiblePlayer, p) <= plugin.RANGE
							&& !p.equals(invisiblePlayer))
					{
						invisible(invisiblePlayer,p);
					}
				}
			}
		}
	}
}
