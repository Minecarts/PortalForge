package com.minecarts.portalforge.listener;

import java.text.MessageFormat;

import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityInPortalEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.Portal;
import com.minecarts.portalforge.portal.PortalType;

public class EntityListener extends org.bukkit.event.entity.EntityListener{ 
    
    private PortalForge plugin;
    public EntityListener(PortalForge plugin){
        this.plugin = plugin;
    }
    
    @Override
    public void onEntityInPortal(EntityInPortalEvent e){
        //Fetch the portalId
        try{
            Entity entity = e.getEntity();
            if(plugin.entityPortaling.contains(entity)) return; //If they're already portaling, skip em.
            plugin.entityPortaling.add(entity);

            Portal portal = plugin.dbHelper.getPortalFromBlockLocation(e.getBlock().getLocation());
                if(portal!= null && portal.endPoint != null){
                    if(portal.type == PortalType.GENERIC){
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new teleportLater(plugin,entity,portal));
                    } else if (portal.type == PortalType.HOME){
                        //Check if it's a player and send them to their home point
                        //Send them to their home point
                    } else if (portal.type == PortalType.NETHER){
                        //TO THE NETHER!
                    } else {
                        if(entity instanceof Player){
                            ((Player)entity).sendMessage("Unknown poral type: " + portal.type);
                        }
                    }
                } else {
                    if(entity instanceof Player){
                        Player player = (Player)entity;
                        String data = "";
                        if(player.isOp()){
                            if(portal == null){
                                data = "(Not in DB)";
                            } else if(portal.id != -1){
                                data = MessageFormat.format("(#{0})", portal.id);
                            } else {
                                data = "(No id but in DB?)";
                            }
                        }
                        ((Player)entity).sendMessage(MessageFormat.format("This portal{0} is not linked anywhere", data));
                        //entity.teleport(new Location(Bukkit.getServer().getWorld("world"),-22,72,62));
                        //plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new portalKnockback(plugin,entity,e.getBlock().getLocation()));
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new clearPortalingState(entity),40);
                    }
                   
                }
        } catch (Exception x){
            x.printStackTrace();
        }
        
        //e.setCancelled(true);
    }

    private class teleportLater implements Runnable{
        private Entity e;
        private Portal portal;
        private PortalForge plugin;
        public teleportLater(PortalForge plugin, Entity e, Portal portal){
            this.e = e;
            this.portal = portal;
            this.plugin = plugin;
        }
        public void run() {
            if(e instanceof Player){
                Player p = (Player) e;
                if(plugin.debuggingPortals.contains(p.getName())){
                    p.sendMessage(MessageFormat.format(ChatColor.GRAY + "Portal #{0} ({1}): Source: ({2,number,#.##}, {3,number,#.##}, {4,number,#.##}) to Dest: ({5,number,#.##}, {6,number,#.##}, {7,number,#.##}) [P: {8}, Y: {9}, V: {10}]",
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
            }
            //Check to see if the end point is blocked (this check should be improved)
            if(portal.endPoint.getWorld().getBlockAt(portal.endPoint).getRelative(BlockFace.UP).getType() == Material.AIR){
            //if(portal.endPoint.getWorld().getBlockAt(portal.endPoint.setY(portal.endPoint.getY() + 1)).getType() == Material.AIR){
                //plugin.entityPortaling.remove(e);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new clearPortalingState(e),40);
                e.teleport(portal.endPoint);
                e.setVelocity(e.getLocation().getDirection().normalize().multiply(portal.exitVelocity));
            } else {
                //It was blocked, display a message?
                System.out.println("Portal exit blocked:" + portal.endPoint);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new clearPortalingState(e),40);
            }
            
            
        }
    }
    
    private class clearPortalingState implements Runnable{
        private Entity e;
        public clearPortalingState(Entity e){
            this.e = e;
        }
        public void run() {
            if(plugin.entityPortaling.contains(e)){
                plugin.entityPortaling.remove(e);
            }
        }
    }
}
