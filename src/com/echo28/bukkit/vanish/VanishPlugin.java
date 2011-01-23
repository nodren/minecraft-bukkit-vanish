package com.echo28.bukkit.vanish;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet29DestroyEntity;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.echo28.bukkit.findme.FindMe;
import com.nijikokun.bukkit.Permissions.Permissions;


/**
 * Vanish for Bukkit
 * 
 * @author Nodren
 */
public class VanishPlugin extends JavaPlugin
{
	public static Permissions Permissions = null;
	public int RANGE;
	public String AUTO_ON_GROUP;
	public int TOTAL_REFRESHES;
	private Configuration config;

	public HashMap<String, Player> invisible = new HashMap<String, Player>();

	private final VanishPlayerListener playerListener = new VanishPlayerListener(this);
	private final Logger log = Logger.getLogger("Minecraft");

	public VanishPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		config = new Configuration(new File("plugins/vanish.yml"));
		config.load();
		RANGE = config.getInt("range", 512);
		TOTAL_REFRESHES = config.getInt("total_refreshes", 10);
		AUTO_ON_GROUP = config.getString("auto_on_group", "");
	}

	public void onDisable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
	}

	public void onEnable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Monitor, this);
	}

	public void setupPermissions()
	{
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

		if (this.Permissions == null)
		{
			if (test != null)
			{
				this.Permissions = (Permissions) test;
			}
		}
	}

	@Override
	public boolean onCommand(Player player, Command command, String commandLabel, String[] args)
	{
		if (command.getName().equalsIgnoreCase("vanish"))
		{
			if ((args.length == 1) && (args[0].equalsIgnoreCase("list")))
			{
				if ((Permissions != null) && (!Permissions.Security.permission(player, "vanish.vanish.list"))) { return true; }
				String message = "List of Invisible Players: ";
				for (Player InvisiblePlayer : invisible.values())
				{
					message += InvisiblePlayer.getName() + ", ";
				}
				player.sendMessage(ChatColor.RED + message.substring(0, message.length() - 2));
				return true;
			}
			if ((Permissions != null) && (!Permissions.Security.permission(player, "vanish.vanish"))) { return true; }
			vanish(player);
			return true;
		}
		return false;
	}

	private void invisible(Player p1, Player p2)
	{
		CraftPlayer hide = (CraftPlayer) p1;
		CraftPlayer hideFrom = (CraftPlayer) p2;
		hideFrom.getHandle().a.b(new Packet29DestroyEntity(hide.getHandle().g));
	}

	private void uninvisible(Player p1, Player p2)
	{
		CraftPlayer unHide = (CraftPlayer) p1;
		CraftPlayer unHideFrom = (CraftPlayer) p2;
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
		Player[] playerList = getServer().getOnlinePlayers();
		for (Player p : playerList)
		{
			if (getDistance(player, p) <= RANGE && !p.equals(player))
			{
				invisible(player, p);
			}
		}
		log.info(player.getName() + " disappeared.");
		player.sendMessage(ChatColor.RED + "Poof!");

		Plugin plugin = getServer().getPluginManager().getPlugin("FindMe");
		if (plugin != null)
		{
			FindMe findMe = (FindMe) plugin;
			findMe.hidePlayer(player);
		}
	}

	public void reappear(Player player)
	{
		if (invisible.get(player.getName()) != null)
		{
			invisible.remove(player.getName());
			// make someone really disappear if there's any doubt, should remove
			// cloning
			updateInvisibleForAll();
			Player[] playerList = getServer().getOnlinePlayers();
			for (Player p : playerList)
			{
				if (getDistance(player, p) < RANGE && !p.equals(player))
				{
					uninvisible(player, p);
				}
			}
			log.info(player.getName() + " reappeared.");
			player.sendMessage(ChatColor.RED + "You have reappeared!");

			Plugin plugin = getServer().getPluginManager().getPlugin("FindMe");
			if (plugin != null)
			{
				FindMe findMe = (FindMe) plugin;
				findMe.unHidePlayer(player);
			}
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
		Player[] playerList = getServer().getOnlinePlayers();
		for (Player invisiblePlayer : invisible.values())
		{
			for (Player p : playerList)
			{
				if (getDistance(invisiblePlayer, p) <= RANGE && !p.equals(invisiblePlayer))
				{
					invisible(invisiblePlayer, p);
				}
			}
		}
	}

	public void updateInvisible(Player player)
	{
		for (Player invisiblePlayer : invisible.values())
		{
			if (getDistance(invisiblePlayer, player) <= RANGE && !player.equals(invisiblePlayer))
			{
				invisible(invisiblePlayer, player);
			}
		}
	}

	public double getDistance(Player player1, Player player2)
	{
		Location loc1 = player1.getLocation();
		Location loc2 = player1.getLocation();
		return Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2) + Math.pow(loc1.getY() - loc2.getY(), 2) + Math.pow(loc1.getZ() - loc2.getZ(), 2));
	}

	public void updateInvisibleOnTimer()
	{
		updateInvisibleForAll();
		Timer timer = new Timer();
		int i = 0;
		while (i < TOTAL_REFRESHES)
		{
			i++;
			timer.schedule(new UpdateInvisibleTimerTask(), i * 1000);
		}
	}

	public class UpdateInvisibleTimerTask extends TimerTask
	{
		public void run()
		{
			updateInvisibleForAll();
		}
	}

}
