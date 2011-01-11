package com.echo28.bukkit.vanish;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Vanish for Bukkit
 * 
 * @author Nodren
 */
public class VanishPlugin extends JavaPlugin
{
	public int RANGE = 512;
	public String AUTO_ON_GROUP = "";
	public int TOTAL_REFRESHES = 10;
	public boolean DISABLE_TP = true;
	public boolean HIDE_USERS = false;
	
	private final VanishPlayerListener playerListener = new VanishPlayerListener(this);
	private final Logger log = Logger.getLogger("Minecraft");

	public VanishPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File plugin, ClassLoader cLoader)
	{
		super(pluginLoader, instance, desc, plugin, cLoader);

		registerEvents();
	}

	public void onDisable()
	{
		log.info(getDescription().getName() + " "+getDescription().getVersion() + " unloaded.");
	}

	public void onEnable()
	{
		log.info(getDescription().getName() + " "+getDescription().getVersion() + " unloaded.");
	}

	private void registerEvents()
	{
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
	}

}
