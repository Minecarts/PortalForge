package com.minecarts.portalforge;

import java.util.logging.Logger;

import com.minecarts.portalforge.portal.PortalFlag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;

import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import org.bukkit.event.*;
import org.bukkit.inventory.ItemStack;

import com.minecarts.dbconnector.DBConnector;
import com.minecarts.portalforge.command.PortalCommand;
import com.minecarts.portalforge.event.PortalSuccessEvent;
import com.minecarts.portalforge.listener.*;
import com.minecarts.portalforge.helper.*;
import com.minecarts.portalforge.portal.NetherPortal;
import com.minecarts.portalforge.portal.Portal;
import com.minecarts.portalforge.portal.PortalType;

import java.util.HashMap;
import java.util.ArrayList;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PortalForge extends org.bukkit.plugin.java.JavaPlugin{
    public final Logger log = Logger.getLogger("com.minecarts.portalforge");
    
    private final PlayerListener playerListener = new PlayerListener(this);
    private final EntityListener entityListener = new EntityListener(this);
    private final BlockListener blockListener = new BlockListener(this);
    private final PortalListener portalListener = new PortalListener(this);
    
    public DBConnector dbc;
    public HelperDB dbHelper;
    public HelperCache cache;
    public NetherPortal netherPortal;
    
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
        this.netherPortal = new NetherPortal(this);
        this.cache = new HelperCache(this);

        //Register our events
        pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Event.Priority.Lowest,this);
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, this.blockListener, Event.Priority.Normal,this);
        pm.registerEvent(Event.Type.BLOCK_FROMTO, this.blockListener, Event.Priority.Normal,this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Monitor,this);
        pm.registerEvent(Event.Type.ENTITY_PORTAL_ENTER,this.entityListener,Event.Priority.Monitor,this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, this.playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_PORTAL, this.playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.CUSTOM_EVENT, this.portalListener, Event.Priority.Monitor, this);

        //Register commands
        getCommand("portal").setExecutor(new PortalCommand(this));
        
        log.info("[" + pdf.getName() + "] version " + pdf.getVersion() + " enabled.");
    }
    
    public void onDisable(){
        
    }
    
    public void log(String msg){
        System.out.println("PortalForge> " + msg);
    }
    public void logSendMessage(org.bukkit.entity.Player player, String msg){
        log(msg);
        player.sendMessage(msg);
    }
    
    public void logDebug(Player p, String msg){
        if(this.debuggingPortals.contains(p.getName())){
            p.sendMessage(ChatColor.GRAY + msg);
        }
    }

    //Ideally we would store a list of worlds in the database
    //  would be nice to have some metadata about which is the default spawn
    //  which nether goes to which world.. or if nether disabled entirely, etc..
    //
    //  But until then, this enum is all we need
    private enum MinecartWorlds{
        world,
        world_19
    }

    public void finalizeAndFireEvent(Player player, Portal p){
        Portal portal = p.clone(); //Clone so we don't modify a cached portal

        //Check any portal use requirements
        if(portal.flags.contains(PortalFlag.REQUIRE_EMPTY_INVENTORY)){
            for(ItemStack stack : player.getInventory().getContents()){
                if(stack != null && stack.getTypeId() != 0){
                    player.sendMessage("Your inventory must be empty to use this portal.");
                    return;
                }
            }
        }
        if(portal.flags.contains(PortalFlag.SUBSCRIBER)){
            if(!player.hasPermission("subscriber")){
                player.sendMessage("Only subscribers can use this portal. Please visit " + ChatColor.GOLD + "minecarts.com" + ChatColor.WHITE + " for more info.");
            }
        }

        if(portal.type == PortalType.GENERIC){
            if(portal.endPoint == null){ //Verify there is an endpoint for this generic
                player.sendMessage("Portal is not linked. Portaling aborted.");
                log("Portal " + portal.id + " is not linked. Aborted portal for " + player.getName() + " at " + portal.endPoint);
                return;
            }
        } else if (portal.type == PortalType.HOME){
            player.sendMessage("Home portals are not currently implemented. Sorry!");
            return;
        } else if (portal.type == PortalType.NETHER){
            //Teleport the player to the correct nether based upon the world they're using a nether portal in
            switch(MinecartWorlds.valueOf(player.getLocation().getWorld().getName().toLowerCase())){
                case world:
                    //They're in the old world, so send them to the old nether
                    //Keep track of where they entered from for return portal usage
                    portalListener.entryPortalTracker.put(player.getName(),player.getLocation());
                    if(portal.endPoint == null){
                        portal.endPoint = Bukkit.getServer().getWorld("world_nether").getSpawnLocation();
                    }
                    break;
                case world_19:
                    //They're in the new world, so send them to the new nether
                    portalListener.entryPortalTracker.put(player.getName(),player.getLocation());
                    if(portal.endPoint == null){
                        portal.endPoint = Bukkit.getServer().getWorld("nether_new").getSpawnLocation();
                    }
                    break;
                default:
                    //They must be in a nether or non standard world, so send them back
                    //  to the portal they entered from (this may cause issues for non standard worlds)
                    //  If they don't have an entry portal (happens on /reload), send them back to world
                    if(portal.endPoint == null){
                        if(portalListener.entryPortalTracker.containsKey(player.getName())){
                            portal.endPoint = portalListener.entryPortalTracker.remove(player.getName());
                            Location newLoc = findSafeExit(portal.endPoint); //Try to find a safe location near this portal
                            if(newLoc != null){
                                newLoc.setX(newLoc.getX() + 0.5);
                                newLoc.setY(newLoc.getY() + 1);
                                newLoc.setZ(newLoc.getZ() + 0.5);
                                portal.endPoint = newLoc;
                            }
                        } else {
                            //Send them to the spawn since we don't know where they entered
                            portal.endPoint = Bukkit.getServer().getWorld("world").getSpawnLocation();
                        }
                    } else {
                        //Send them to the the portal.endPoint... or do we want to log that this nether portal has an endpoint and shouldn't?
                        //  Probably this should just override the location
                    }
                    break;
            } //switch whatWorldPlayerIsIn
        } else if (portal.type == PortalType.SKYLAND){
            player.sendMessage("The skyland has been removed by Notch. This portal is no longer functional.");
            return;
        } else if (portal.type == PortalType.END){
            player.sendMessage("For now... The End is only accessable via a natural portal.");
            return;
        } else {
                log("ERROR: " + portal.id +" has unknown portal type: " + portal.type);
                player.sendMessage("Unknown portal type: " + portal.type);
                return;
        }
        Bukkit.getServer().getPluginManager().callEvent(new PortalSuccessEvent(player,portal));
    }
    
    
    public Location findSafeExit(Location location){
        org.bukkit.block.Block portalBlock = location.getWorld().getBlockAt(location);
        //First of all, find out which way this portal is facing
        
        BlockFace facePositive = null, faceNegative = null;
        
        if(portalBlock.getRelative(BlockFace.EAST).getType() == Material.PORTAL || portalBlock.getRelative(BlockFace.WEST).getType() == Material.PORTAL){
            //Facing north / south
            facePositive = BlockFace.NORTH;
            faceNegative = BlockFace.SOUTH;
        } else if(portalBlock.getRelative(BlockFace.NORTH).getType() == Material.PORTAL || portalBlock.getRelative(BlockFace.SOUTH).getType() == Material.PORTAL){
            //facing east/west
            facePositive = BlockFace.EAST;
            faceNegative = BlockFace.WEST;
        }

        if(facePositive == null || faceNegative == null){
            System.out.println("Unable to find a safe exit location near " + location);
            return null;
        }

        boolean blockFound = false;
        int searchCount = 0, successCount = 0;
        BlockFace face = facePositive;

        while(true){
            Block searchBlock = portalBlock.getRelative(face);
            if(searchBlock.getType() == Material.AIR && searchBlock.getRelative(BlockFace.UP).getType() == Material.AIR){
                portalBlock = searchBlock;
                ++successCount;
            } else { 
                //Search the other way!
                face = faceNegative;
                portalBlock = portalBlock.getFace(face);
            }

            if(successCount == 2){
                while(searchBlock.getType() == Material.AIR){
                    searchBlock = searchBlock.getRelative(BlockFace.DOWN);
                } //And find the ground nearby, so they dont fall to their death.. but it could be lava.
                return searchBlock.getLocation();
            } else if (++searchCount == 10){
                return null;
            }
        }
    }
}
