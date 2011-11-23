package com.minecarts.portalforge.helper;

import java.text.MessageFormat;

import com.minecarts.portalforge.portal.PortalFlag;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.minecarts.portalforge.portal.Portal;
import com.minecarts.portalforge.PortalForge;
import org.bukkit.util.Vector;

public class teleportLater implements Runnable{
    private Entity e;
    private Portal portal;
    private PortalForge plugin;
    public teleportLater(PortalForge plugin, Entity e, Portal portal){
        this.plugin = plugin;
        this.e = e;
        this.portal = portal;
    }
    public void run() {
        if(e instanceof Player){
            Player p = (Player) e;
            plugin.logDebug(p,MessageFormat.format("USED: Portal #{0} ({1}): Source: ({2,number,#.##}, {3,number,#.##}, {4,number,#.##}) to Dest: ({5,number,#.##}, {6,number,#.##}, {7,number,#.##}) [P: {8}, Y: {9}]",
                        portal.id,
                        portal.type,
                        e.getLocation().getX(),
                        e.getLocation().getY(),
                        e.getLocation().getZ(),
                        portal.endPoint.getX(),
                        portal.endPoint.getY(),
                        portal.endPoint.getZ(),
                        portal.endPoint.getPitch(),
                        portal.endPoint.getYaw()
                        ));
        }
        //Check to see if the end point is blocked (@TODO: this check should be improved)
        Block relativeBlock = portal.endPoint.getWorld().getBlockAt(portal.endPoint).getRelative(BlockFace.UP);
        if(relativeBlock.getType() == Material.AIR || relativeBlock.getType() == Material.PORTAL){
            String entityName = (e instanceof Player) ? ((Player)e).getName() : e.toString() +"["+e.getEntityId()+"]";
            //Display to console log
            //[P:{8,number,#.##},Y:{9,number,#.##},V:{10,number,#.##}] 
            plugin.log(MessageFormat.format("{10} used Portal #{0} ({1}): [{2,number,#.##}, {3,number,#.##}, {4,number,#.##}]->[{5,number,#.##}, {6,number,#.##}, {7,number,#.##}]",
                    portal.id,
                    portal.type,
                    e.getLocation().getX(),
                    e.getLocation().getY(),
                    e.getLocation().getZ(),
                    portal.endPoint.getX(),
                    portal.endPoint.getY(),
                    portal.endPoint.getZ(),
                    portal.endPoint.getPitch(),
                    portal.endPoint.getYaw(),
                    entityName
                    ));

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new clearPortalingState(plugin,e),40);
            
            //Check if the chunk is loaded
            if (!portal.endPoint.getWorld().isChunkLoaded(portal.endPoint.getBlockX() >> 4, portal.endPoint.getBlockZ() >> 4)) {
                if(e instanceof Player){
                    portal.endPoint.getWorld().loadChunk(portal.endPoint.getBlockX() >> 4, portal.endPoint.getBlockZ() >> 4);
                } else { //Don't teleport non player entities to unloaded chunks
                    e.remove();
                }
            }

        //Handle the flags
            if(portal.flags.contains(PortalFlag.CLEAR_INVENTORY)){
                if(e instanceof Player){
                    ((Player) e).getInventory().clear();
                }
            }
            e.teleport(portal.endPoint);
            e.setVelocity(portal.velocityVector);

            if(portal.flags.contains(PortalFlag.MESSAGE) && portal.message != null && portal.message.length() > 0){
                if(e instanceof Player){
                    ((Player) e).sendMessage(portal.message);
                }
            }

        } else {
            //It was blocked, display a message?
            if(e instanceof Player){
                ((Player)e).sendMessage("This portal exit is obstructed.");
            }
            plugin.log("Portal exit blocked:" + portal.endPoint);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new clearPortalingState(plugin,e),40);
        }
        
        
    }
}