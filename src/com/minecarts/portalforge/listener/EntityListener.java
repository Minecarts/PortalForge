package com.minecarts.portalforge.listener;

import org.bukkit.event.entity.EntityInPortalEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
            
            Portal portal = plugin.dbHelper.getPortalFromBlockLocation(e.getBlock().getLocation());
                if(portal!= null){
                    plugin.entityPortaling.add(entity);
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
                        ((Player)entity).sendMessage("This portal is not linked anywhere");
                        entity.teleport(new Location(Bukkit.getServer().getWorld("world"),-22,72,62));
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
            plugin.entityPortaling.remove(e);
            e.teleport(portal.endPoint);
            e.setVelocity(e.getLocation().getDirection().normalize().multiply(0.5));
        }
    }
}
