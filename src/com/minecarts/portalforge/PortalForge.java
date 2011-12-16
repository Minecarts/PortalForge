package com.minecarts.portalforge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.minecarts.dbquery.DBQuery;
import com.minecarts.portalforge.command.PortalCommand;
import com.minecarts.portalforge.listener.BlockListener;
import com.minecarts.portalforge.listener.EntityListener;
import com.minecarts.portalforge.listener.PlayerListener;
import com.minecarts.portalforge.listener.PortalListener;
import com.minecarts.portalforge.portal.*;
import com.minecarts.portalforge.portal.internal.PortalActivation;
import com.minecarts.portalforge.portal.internal.PortalType;
import org.bukkit.*;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.event.*;

import org.bukkit.entity.Player;

public class PortalForge extends org.bukkit.plugin.java.JavaPlugin{
    public final Logger log = Logger.getLogger("com.minecarts.portalforge");

    private DBQuery dbq;

    private final PlayerListener playerListener = new PlayerListener(this);
    private final EntityListener entityListener = new EntityListener(this);
    private final BlockListener blockListener = new BlockListener(this);
    private final PortalListener portalListener = new PortalListener(this);
    
    private ArrayList<Entity> portalingEntities = new ArrayList<Entity>();
    
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        PluginDescriptionFile pdf = getDescription();

        dbq = (DBQuery) getServer().getPluginManager().getPlugin("DBQuery");

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
        if(p.hasPermission("portalforge.debug")){
            p.sendMessage(ChatColor.GRAY + msg);
        }
    }

    class Query extends com.minecarts.dbquery.Query {
        public Query(String sql) {
            super(PortalForge.this, dbq.getProvider("minecarts"), sql);
        }
    }

    public void entityPortalingAdd(Entity e){
        if(!portalingEntities.contains(e)){
            portalingEntities.add(e);
        }
    }
    public void entityPortalingRemove(Entity e){
        portalingEntities.remove(e);
    }
    public boolean isEntityPortaling(Entity e){
        return portalingEntities.contains(e);
    }
    
    public void entityTouchedPortal(final Entity entity, final Location loc){
        //Add this entity to the portal touched list to prevent spaming of events
        if(isEntityPortaling(entity)) return;
        entityPortalingAdd(entity);

        //What to do here?
        Query query = getQuery(entity,PortalActivation.INSTANT);
        query.fetchOne(
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );

        Bukkit.getScheduler().scheduleSyncDelayedTask(this,new Runnable() {
            public void run() {
                PortalForge.this.entityPortalingRemove(entity);
            }
        },20 * getConfig().getInt("portal.touch_timeout")); //Reset portaling state X seconds later
    }
    
    public void entityUsedPortal(final Entity entity){

        Location loc = getNearestPortalBlock(entity);
        Query query = getQuery(entity,PortalActivation.DELAYED);
        query.fetchOne(
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );
    }

    public Location getNearestPortalBlock(Entity e){
        //TODO: Search for the nearest portal block and return it's location, not sure if it's necessary
        return e.getLocation();
    }
    
    public Location getSafeExitPoint(Location teleportLocation){
        //TODO: Find a safe exit point.. maybe this is part of BasePortal or inside of the PortalListener?
        return teleportLocation;
    }

    public Query getQuery(final Entity entity, final PortalActivation activation){
        return new Query("SELECT `portals`.* FROM `portals`,`portal_blocks` WHERE `portal_blocks`.`portal_id` = `portals`.`id` AND `portal_blocks`.`world` = ? AND `portal_blocks`.`x` = ? AND `portal_blocks`.`y` = ? AND `portal_blocks`.`z` = ? LIMIT 1") {
            @Override
            public void onFetchOne(HashMap row) {
                BasePortal portal;
                //Handle unknown portals here, since row == null if it is not in the DB
                if(row == null){
                    portal = new UnknownPortal();
                    portal.setPlugin(PortalForge.this);
                    portal.setPortalingEntity(entity); //Only set the entity, we don't know anything else
                    portal.setType(PortalType.UNKNOWN);
                    if(activation == PortalActivation.INSTANT){ //How this portal was activated
                        portal.onTouch();
                    } else if (activation == PortalActivation.DELAYED){
                        portal.onPortal();
                    }
                    return;
                }

                //Find out what type of portal it is, and set this portal type correctly
                switch(PortalType.valueOf((String)row.get("type"))){
                    case END:
                        portal = new EndPortal();
                        break;
                    case GENERIC:
                        portal = new GenericPortal();
                        break;
                    case HOME:
                        portal = new HomePortal();
                        break;
                    case NETHER:
                        portal = new NetherPortal();
                        break;
                    case SKYLAND:
                        portal = new SkylandPortal();
                        break;
                    default:
                        portal = new UnknownPortal();
                        break;
                }
                portal.setPlugin(PortalForge.this);
                portal.setPortalingEntity(entity); //Only set the entity, we don't know anything else
                portal.setType(PortalType.UNKNOWN);
                portal.setType(PortalType.valueOf((String) row.get("type")));
                portal.setId((Long) row.get("id")); //java.lang.Integer, if UNSIGNED java.lang.Long -- http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-type-conversions.html
                portal.setActivation(PortalActivation.valueOf((String)row.get("activation")));
                portal.setMessage((String)row.get("message"));
                portal.setFlags((String)row.get("flags"));
                portal.setExitLocation(
                        (World)row.get("dest_world"),
                        (Double)row.get("dest_x"),
                        (Double)row.get("dest_y"),
                        (Double)row.get("dest_z")
                );
                portal.setExitVector(
                        (Float)row.get("dest_vel_x"),
                        (Float)row.get("dest_vel_y"),
                        (Float)row.get("dest_vel_z")
                );

                if(activation == PortalActivation.INSTANT){
                    portal.onTouch();
                } else if(activation == PortalActivation.DELAYED){
                    portal.onPortal(); //Call it!
                }
            }
            @Override
            public void onException(Exception x, FinalQuery query) {
                // rethrow
                try {
                    throw x;
                }
                catch(Exception e) {
                    e.printStackTrace();

                    // retry query
                    //query.run();
                }
            }
        };
    }
}
