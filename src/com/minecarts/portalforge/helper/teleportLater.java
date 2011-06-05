package com.minecarts.portalforge.helper;

import java.text.MessageFormat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.minecarts.portalforge.portal.Portal;
import com.minecarts.portalforge.PortalForge;

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
            plugin.logDebug(p,MessageFormat.format("USED: Portal #{0} ({1}): Source: ({2,number,#.##}, {3,number,#.##}, {4,number,#.##}) to Dest: ({5,number,#.##}, {6,number,#.##}, {7,number,#.##}) [P: {8}, Y: {9}, V: {10}]",
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
                        portal.exitVelocity
                        ));
        }
        //Check to see if the end point is blocked (@TODO: this check should be improved)
        Block relativeBlock = portal.endPoint.getWorld().getBlockAt(portal.endPoint).getRelative(BlockFace.UP);
        if(relativeBlock.getType() == Material.AIR || relativeBlock.getType() == Material.PORTAL){
            String entityName = (e instanceof Player) ? ((Player)e).getName() : e.toString() +"["+e.getEntityId()+"]";
            //Display to console log
            //[P:{8,number,#.##},Y:{9,number,#.##},V:{10,number,#.##}] 
            plugin.log(MessageFormat.format("{11} used Portal #{0} ({1}): [{2,number,#.##}, {3,number,#.##}, {4,number,#.##}]->[{5,number,#.##}, {6,number,#.##}, {7,number,#.##}]",
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
                    portal.exitVelocity,
                    entityName
                    ));

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new clearPortalingState(plugin,e),40);
            
            //Load the chunk
            if (!portal.endPoint.getWorld().isChunkLoaded(portal.endPoint.getBlockX() >> 4, portal.endPoint.getBlockZ() >> 4)) {
                portal.endPoint.getWorld().loadChunk(portal.endPoint.getBlockX() >> 4, portal.endPoint.getBlockZ() >> 4);
            }
            
            e.teleport(portal.endPoint);
            e.setVelocity(e.getLocation().getDirection().normalize().multiply(portal.exitVelocity));
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