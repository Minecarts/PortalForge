package com.minecarts.portalforge;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.minecarts.dbquery.DBQuery;
import com.minecarts.portalforge.command.CommandRouter;
import com.minecarts.portalforge.listener.BlockListener;
import com.minecarts.portalforge.listener.EntityListener;
import com.minecarts.portalforge.listener.PlayerListener;
import com.minecarts.portalforge.listener.PortalListener;
import com.minecarts.portalforge.portal.*;
import com.minecarts.portalforge.portal.internal.PortalActivation;
import com.minecarts.portalforge.portal.internal.PortalFlag;
import com.minecarts.portalforge.portal.internal.PortalType;
import org.bukkit.*;

import org.bukkit.block.Block;
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
    
    private ArrayList<Entity> portalingEntitiesTouch = new ArrayList<Entity>();
    private ArrayList<Entity> portalingEntitiesPortal = new ArrayList<Entity>();

    private HashMap<Player, GenericPortal> portalEditingMap = new HashMap<Player, GenericPortal>();
    private HashMap<Long, GenericPortal> portalCache = new HashMap<Long, GenericPortal>();
    
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
        getCommand("portal").setExecutor(new CommandRouter(this));

        log.info("[" + pdf.getName() + "] version " + pdf.getVersion() + " enabled.");

        //Save the default config
        getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    public void onDisable(){

    }

    public static void log(String msg){
        System.out.println("PortalForge> " + msg);
    }
    public static void logAndMessagePlayer(Player player, String msg){
        log(player.getName() + " " + msg);
        player.sendMessage(msg);
    }

    class Query extends com.minecarts.dbquery.Query {
        public Query(String sql) {
            super(PortalForge.this, dbq.getProvider(getConfig().getString("db.provider")), sql);
        }
        @Override
        public void onComplete(FinalQuery query) {
            if(query.elapsed() > 500) {
                log(MessageFormat.format("Slow query took {0,number,#} ms", query.elapsed()));
            }
        }
        @Override
        public void onException(Exception x, FinalQuery query) {
            try { throw x; }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void entityPortalingAdd(Entity e, PortalActivation type){
        if(type == PortalActivation.INSTANT){
            if(!portalingEntitiesTouch.contains(e)){
                portalingEntitiesTouch.add(e);
            }
        } else if(type == PortalActivation.DELAYED){
            if(!portalingEntitiesPortal.contains(e)){
                portalingEntitiesPortal.add(e);
            }
        }
    }
    public void entityPortalingRemove(Entity e, PortalActivation type){
        if(type == PortalActivation.INSTANT){
            portalingEntitiesTouch.remove(e);
        } else if(type == PortalActivation.DELAYED){
            portalingEntitiesPortal.remove(e);
        }
    }
    public boolean isEntityPortaling(Entity e, PortalActivation type){
        if(type == PortalActivation.INSTANT){
            return portalingEntitiesTouch.contains(e);
        } else if(type == PortalActivation.DELAYED){
            return portalingEntitiesPortal.contains(e);
        }
        log("Unknown isEntityPortaling type" + type.name());
        return false;
    }
    
    public void entityTouchedPortal(final Entity entity, final Location loc){
        //Add this entity to the portal touched list to prevent spaming of events
        if(isEntityPortaling(entity, PortalActivation.INSTANT)) return;
        entityPortalingAdd(entity, PortalActivation.INSTANT);

        //What to do here?
        getQuery(entity,PortalActivation.INSTANT).fetchOne(
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );

        Bukkit.getScheduler().scheduleSyncDelayedTask(this,new Runnable() {
            public void run() {
                PortalForge.this.entityPortalingRemove(entity, PortalActivation.INSTANT);
            }
        },20 * getConfig().getInt("portal.touch_timeout",3)); //Reset portaling state X seconds later
    }
    
    public void entityUsedPortal(final Entity entity){
        if(isEntityPortaling(entity, PortalActivation.DELAYED)) return;
        entityPortalingAdd(entity, PortalActivation.DELAYED);

        Location loc = getNearestPortalBlock(entity);
        getQuery(entity,PortalActivation.DELAYED).fetchOne(
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );

        Bukkit.getScheduler().scheduleSyncDelayedTask(this,new Runnable() {
            public void run() {
                PortalForge.this.entityPortalingRemove(entity, PortalActivation.DELAYED);
            }
        },20 * getConfig().getInt("portal.portal_timeout",3)); //Reset portaling state X seconds later
    }

    public Location getNearestPortalBlock(Entity e){
        //TODO: Search for the nearest portal block and return it's location, not sure if it's necessary
        return e.getLocation();
    }



    public void setEditingPortal(final Player player, GenericPortal portal){
        portalEditingMap.put(player,portal);
    }
    public GenericPortal getEditingPortal(final Player player){
        return portalEditingMap.get(player);
    }
    public void clearEditingPortal(final Player player){
        portalEditingMap.remove(player);
    }

    public void createPortal(final Player player, final GenericPortal portal){
        createPortal(player,portal,null);
    }
    public void createPortal(final Player player, final GenericPortal portal, final Block block){
        if(portal.getPlugin() == null) portal.setPlugin(this); //Always set the plugin when creating a portal
        new Query("INSERT INTO portals(`type`,`activation`) VALUES (?,?)") {
            @Override
            public void onInsertId(Integer id) {
                setEditingPortal(player, portal);
                portal.setId((long)id); //Update the portal reference's ID
                //Trigger the portalCreated() to the portal, and pass a block to the callback, used for detecting
                //  pre-existing portals such as nether and end portals
                portal.portalCreated(player,block);
            }
        }.insertId(
                portal.getType().name(),
                portal.getActivation().name()
        );
    }

    public void updatePortal(final Player player, final GenericPortal portal){
        new Query("UPDATE `portals` SET `dest_world` =?,`dest_x`=?,`dest_y`=?,`dest_z`=?,`dest_pitch`=?,`dest_yaw`=?, `dest_vel_x`=?, `dest_vel_y`=?, `dest_vel_z`=?,`type`=?,`activation`=?,`flags`=?,`message`=? WHERE `id`=? LIMIT 1") {
            @Override
            public void onAffected(Integer affected) {
                logAndMessagePlayer(player, "Updated " + affected + " portal with ID: " + portal.getId());
            }
        }.affected(
                portal.getExitLocation().getWorld().getName(),
                portal.getExitLocation().getX(),
                portal.getExitLocation().getY(),
                portal.getExitLocation().getZ(),
                portal.getExitLocation().getPitch(),
                portal.getExitLocation().getYaw(),
                portal.getExitVector().getX(),
                portal.getExitVector().getY(),
                portal.getExitVector().getZ(),
                portal.getType().name(),
                portal.getActivation().name(),
                portal.getFlags(),
                portal.getMessage(),
                portal.getId()
        );
    }

    public void addBlockToPortal(final Player player, final Location blockLocation){
        addBlockToPortal(player,blockLocation,false);
    }
    public void addBlockToPortal(final Player player, final Location blockLocation, final boolean silent){
        if(getEditingPortal(player) != null){
            new Query("INSERT IGNORE INTO portal_blocks(portal_id,world,x,y,z) VALUES (?,?,?,?,?)") {
                @Override
                public void onInsertId(Integer id) {
                    if(!silent){
                        logAndMessagePlayer(player, MessageFormat.format("Added portal block at ({4}: {0},{1},{2}) to field {3}",
                                blockLocation.getBlockX(),
                                blockLocation.getBlockY(),
                                blockLocation.getBlockZ(),
                                getEditingPortal(player).getId(),
                                blockLocation.getWorld().getName()
                        ));
                    }
                }
            }.insertId(
                    getEditingPortal(player).getId(),
                    blockLocation.getWorld().getName(),
                    blockLocation.getBlockX(),
                    blockLocation.getBlockY(),
                    blockLocation.getBlockZ()
            );
        } else {
            log(player.getName() + " is not editing a portal yet tried to add a block to a field");
        }
    }
    public void removeBlockFromPortal(final Player player, final GenericPortal portal, final Block block){
        new Query("DELETE FROM `portal_blocks` WHERE `portal_id` = ? AND world = ? AND x = ? AND y = ? AND z = ? LIMIT 1") {
            @Override
            public void onAffected(Integer affected) {
                if(affected > 0){
                    logAndMessagePlayer(player,"Removed block from field " + portal.getId());
                } else {
                    logAndMessagePlayer(player,"Block does not belong to portal " + portal.getId());
                    block.setType(Material.PORTAL); //Restore it back
                }
            }
        }.affected(
                portal.getId(),
                block.getLocation().getWorld().getName(),
                block.getLocation().getBlockX(),
                block.getLocation().getBlockY(),
                block.getLocation().getBlockZ()
        );
    }


    public void setEditingPortalFromId(final Player player, final Integer id){
        new Query("SELECT `portals`.* FROM `portals` WHERE `id` = ? LIMIT 1") {
            @Override
            public void onFetchOne(HashMap row) {
                GenericPortal portal;
                //Handle unknown portals here, since row == null if it is not in the DB
                if(row == null){
                    player.sendMessage("There is no portal with the id: " + id);
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
                portal.setId((Long)row.get("id")); //java.lang.Integer, if UNSIGNED java.lang.Long -- http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-type-conversions.html
                portal.setActivation(PortalActivation.valueOf((String)row.get("activation")));
                portal.setMessage((String)row.get("message"));
                portal.setFlags((String)row.get("flags"));
                if(row.get("dest_world") != null && row.get("dest_x")!= null){
                    portal.setExitLocation(
                            Bukkit.getWorld((String)row.get("dest_world")),
                            (Double)row.get("dest_x"),
                            (Double)row.get("dest_y"),
                            (Double)row.get("dest_z"),
                            (Float)row.get("dest_pitch"),
                            (Float)row.get("dest_yaw")
                    );
                }
                if(row.get("del_vel_x") != null){
                    portal.setExitVector(
                            (Float)row.get("dest_vel_x"),
                            (Float)row.get("dest_vel_y"),
                            (Float)row.get("dest_vel_z")
                    );
                }
                setEditingPortal(player,portal);
                logAndMessagePlayer(player, "Started editing portal " + portal.getId());
            }
        }.fetchOne(id);
    }
    
        

    public Query getQuery(final Entity entity, final PortalActivation activation){
        return new Query("SELECT `portals`.* FROM `portals`,`portal_blocks` WHERE `portal_blocks`.`portal_id` = `portals`.`id` AND `portal_blocks`.`world` = ? AND `portal_blocks`.`x` = ? AND `portal_blocks`.`y` = ? AND `portal_blocks`.`z` = ? LIMIT 1") {
            @Override
            public void onFetchOne(HashMap row) {
                GenericPortal portal;
                //Handle unknown portals here, since row == null if it is not in the DB
                if(row == null){
                    //Chedk to see if the block is a nether or end portal block
                    Block block = entity.getWorld().getBlockAt(entity.getLocation());
                    if(block.getType() == Material.ENDER_PORTAL){
                        portal = new EndPortal();
                        portal.setPlugin(PortalForge.this);
                        portal.setActivation(PortalActivation.DELAYED); //Technically it's instant, but.. nether portals fire OnPlayerPortal which is DELAYED in our case
                        portal.setPortalingEntity(entity);
                        ((EndPortal)portal).createInDB(block);
                    } else if (block.getType() == Material.PORTAL){
                        portal = new NetherPortal();
                    } else {
                        portal = new UnknownPortal();
                        portal.setPlugin(PortalForge.this);
                        portal.setPortalingEntity(entity); //Only set the entity, we don't know anything else
                        if(activation == PortalActivation.INSTANT){ //How this portal was activated
                            portal.showDebug(); //Show debug on touch for all portals
                            portal.onTouch();
                        } else if (activation == PortalActivation.DELAYED){
                            portal.onPortal();
                        }
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
                portal.setId((Long) row.get("id")); //java.lang.Integer, if UNSIGNED java.lang.Long -- http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-type-conversions.html
                portal.setActivation(PortalActivation.valueOf((String)row.get("activation")));
                portal.setMessage((String)row.get("message"));
                portal.setFlags((String)row.get("flags"));
                if(row.get("dest_world") != null && row.get("dest_x")!= null){
                    portal.setExitLocation(
                            Bukkit.getWorld((String)row.get("dest_world")),
                            (Double)row.get("dest_x"),
                            (Double)row.get("dest_y"),
                            (Double)row.get("dest_z"),
                            (Float)row.get("dest_pitch"),
                            (Float)row.get("dest_yaw")
                    );
                }
                if(row.get("del_vel_x") != null){
                    portal.setExitVector(
                            (Float)row.get("dest_vel_x"),
                            (Float)row.get("dest_vel_y"),
                            (Float)row.get("dest_vel_z")
                    );
                }

                if(activation == PortalActivation.INSTANT){
                    portal.showDebug();
                    portal.onTouch();
                } else if(activation == PortalActivation.DELAYED){
                    portal.onPortal(); //Call it!
                }
            }
        };
    }

    
    public void setPortalHistory(final Player player, final NetherPortal portal){
         new Query("INSERT INTO portal_history(player, world, x, y, z, portal_id, timestamp, dest_world, dest_x, dest_y, dest_z) VALUES (?,?,?,?,?,?,NOW(),?,?,?,?) ON DUPLICATE KEY UPDATE world = ?, x = ?, y = ?, z = ?, portal_id = ?, timestamp = NOW(), dest_world = ?, dest_x = ?, dest_y = ?, dest_z = ?"){
            @Override
            public void onAffected(Integer affected) {

            }
        }.affected(
                player.getName(),
                player.getLocation().getWorld().getName(),
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                portal.getId(),
                portal.getExitLocation().getWorld().getName(),
                portal.getExitLocation().getX(),
                portal.getExitLocation().getY(),
                portal.getExitLocation().getZ(),
                player.getLocation().getWorld().getName(),
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                portal.getId(),
                portal.getExitLocation().getWorld().getName(),
                portal.getExitLocation().getX(),
                portal.getExitLocation().getY(),
                portal.getExitLocation().getZ()
        );
    }

    
    public void setPortalEnterLocation(final GenericPortal portal){
        new Query("SELECT * FROM `portal_blocks` WHERE `portal_id` = ? LIMIT 1") {
            @Override
            public void onFetchOne(HashMap row) {
                if(row != null){
                    //Note, block Locations in this case are integers, not doubles
                    Location loc = new Location(
                            Bukkit.getWorld((String)row.get("world")),
                            (Integer)row.get("x"),
                            (Integer)row.get("y"),
                            (Integer)row.get("z")
                    );
                    portal.setEnterLocation(loc);
                }
            }
        }.sync().fetchOne(portal.getId()).async();
    }

    //Does a shared historical teleport
    public void doHistoricalTeleport(final Player player, final String worldName, final NetherPortal portal){
        String queryString = "SELECT * FROM `portal_history` WHERE (`portal_id` = ? AND `timestamp` >= DATE_SUB(NOW(), INTERVAL "+getConfig().getInt("portal.shared_cooldown",30)+" SECOND)) OR (`player` = ? AND `world` = ?) ORDER BY `timestamp` DESC LIMIT 1";

        if(portal.containsFlag(PortalFlag.NO_SHARED_PORTALING)){
            queryString = "SELECT * FROM `portal_history` WHERE (`player` = ? AND `world` = ?) LIMIT 1";
        }
        Query query = new Query(queryString) {
            @Override
            public void onFetchOne(HashMap row) {
                if(row == null){
                    portal.historicalTeleport(Bukkit.getWorld(worldName).getSpawnLocation());
                    //No history, do a spawn teleport
                    return;
                }
                if(portal.getPortalingPlayer().getName().equals((String)row.get("player"))){ //If a history location for this player
                    Location loc = new Location(
                            Bukkit.getWorld((String)row.get("world")),
                            (Double)row.get("x"),
                            (Double)row.get("y"),
                            (Double)row.get("z")
                    );
                    portal.historicalTeleport(loc);
                } else { //Someone else destination is being used, so fire the shared teleport for the portal
                    Location loc = new Location(
                            Bukkit.getWorld((String)row.get("dest_world")),
                            (Double)row.get("dest_x"),
                            (Double)row.get("dest_y"),
                            (Double)row.get("dest_z")
                    );
                    portal.sharedTeleport((String)row.get("player"),loc);
                }
            }
        };
        if(portal.containsFlag(PortalFlag.NO_SHARED_PORTALING)){
            query.fetchOne(player.getName(),worldName);
        } else {
            query.fetchOne(portal.getId(),player.getName(),worldName);
        }
    }
    
    private GenericPortal constructPortalFromRow(HashMap row){
        //TODO, abstract the duplicate querying under diffetent conditions to this, returns a portal from a row of portal data
        return null;
    }
}