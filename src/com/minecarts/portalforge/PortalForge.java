package com.minecarts.portalforge;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;

import org.bukkit.event.*;
import org.bukkit.inventory.ItemStack;

import com.minecarts.dbconnector.DBConnector;
import com.minecarts.portalforge.command.PortalCommand;
import com.minecarts.portalforge.listener.*;
import com.minecarts.portalforge.helper.*;

import java.util.HashMap;
import java.util.ArrayList;

import org.bukkit.entity.Entity;

public class PortalForge extends org.bukkit.plugin.java.JavaPlugin{
    
    //TODO: Portal use logging
    //Nether portal ignition and logging
    //Nether portal usage inserting into DB for existing portals not yet tracked
    //Code cleaning
    
	public final Logger log = Logger.getLogger("com.minecarts.portalforge");
	
	private final PlayerListener playerListener = new PlayerListener(this);
	private final EntityListener entityListener = new EntityListener(this);
	private final BlockListener blockListener = new BlockListener(this);
	
	public DBConnector dbc;
	public HelperDB dbHelper;
	
	public HashMap<String, Integer> activePortalDesigns = new HashMap<String, Integer>();
	public ArrayList<Entity> entityPortaling = new ArrayList<Entity>();
	public ArrayList<String> debuggingPortals = new ArrayList<String>();
	public HashMap<String, ItemStack> previousInventory = new HashMap<String, ItemStack>();
	
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        PluginDescriptionFile pdf = getDescription();

        //Database Connector
        this.dbc = (DBConnector) pm.getPlugin("DBConnector");
        this.dbHelper = new HelperDB(this);

        //Register our events
        pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Monitor,this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Event.Priority.Lowest,this);
        pm.registerEvent(Event.Type.IN_PORTAL,this.entityListener,Event.Priority.Monitor,this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, this.playerListener, Event.Priority.Monitor, this);
        
        //Register commands
        getCommand("portal").setExecutor(new PortalCommand(this));
        
        log.info("[" + pdf.getName() + "] version " + pdf.getVersion() + " enabled.");
    }
    
    public void onDisable(){
        
    }
}
