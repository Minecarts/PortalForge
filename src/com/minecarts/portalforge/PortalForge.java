package com.minecarts.portalforge;

import java.util.logging.Logger;

import com.minecarts.portalforge.portal.PortalFlag;
import org.bukkit.*;
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

    public void finalizeAndFireEvent(Player player, final Portal p){
        final Portal portal = p.clone(); //Clone so we don't modify a cached portal

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
                log("Portal " + portal.id + " is not linked. Aborted portal for " + player.getName() + " at " + player.getLocation());
                return;
            }
        } else if (portal.type == PortalType.HOME){
            player.sendMessage("Home portals are not currently implemented. Sorry!");
            return;
        } else if (portal.type == PortalType.NETHER){
            //Only log nether portal usage for return portals
            String playerWorld = player.getLocation().getWorld().getName();

            //We always query the portal endPoint so that we can correctly notify the player
            //  that they're using a new endpoint and not their normal one
            if(playerWorld.equals("world_nether")){
                portal.endPoint = dbHelper.getPortalEntryLocation(player,Bukkit.getWorld("world"));
            } else if(playerWorld.equals("world")){
                portal.endPoint = dbHelper.getPortalEntryLocation(player,Bukkit.getWorld("world_nether"));
            } else if(playerWorld.equals("new_highridge")){
                portal.endPoint = dbHelper.getPortalEntryLocation(player,Bukkit.getWorld("new_nether"));
            } else if(playerWorld.equals("new_nether")){
                portal.endPoint = dbHelper.getPortalEntryLocation(player,Bukkit.getWorld("new_highridge"));
            }

            System.out.println("PortalForge> DEBUG: Found the enpoint for the portal to be: " + portal.endPoint);
            
            if(p.shareDestination != null && portal.endPoint.distance(p.shareDestination) > 5){
                portal.endPoint = p.shareDestination;
                player.sendMessage(ChatColor.DARK_GRAY + "You follow the last person to use this nether portal.");
                System.out.println("PortalForge> DEBUG: " + player.getName() + " used a shared destination portal");
            }

            //Check to see if we need to temporarily set this portal destination
            if(p.shareDestination == null && !portal.flags.contains(PortalFlag.NO_SHARED_PORTALING)){
                //Set this portal shared destination
                p.shareDestination = portal.endPoint;
                System.out.println("PortalForge> DEBUG: Set shared destination to " + p.shareDestination);
                //And clear this destination after X minutes!
                Bukkit.getScheduler().scheduleSyncDelayedTask(this,new Runnable() {
                    public void run() {
                        System.out.println("PortalForge> DEBUG: Cleared shared destination for portal " + p.id);
                        p.shareDestination = null;
                    }
                },20 * 30);
            }

            //Only set their portal usage if they didn't share a portal
            if(p.shareDestination != null){
                System.out.println("PortalForge> DEBUG: Set the last used portal location for " + player + " to " + player.getLocation());
                dbHelper.setPortalEntryLocation(player);
            }
            
            Location newLoc = findSafeExit(portal.endPoint); //Try to find a safe location near this portal
            if(newLoc != null){
                newLoc.setX(newLoc.getX() + 0.5);
                newLoc.setY(newLoc.getY() + 1);
                newLoc.setZ(newLoc.getZ() + 0.5);
                portal.endPoint = newLoc;
            }
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
