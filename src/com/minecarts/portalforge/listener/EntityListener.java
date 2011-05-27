package com.minecarts.portalforge.listener;

import java.text.MessageFormat;

import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.minecarts.portalforge.event.*;
import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.Portal;
import com.minecarts.portalforge.portal.PortalType;
import com.minecarts.portalforge.helper.clearPortalingState;

public class EntityListener extends org.bukkit.event.entity.EntityListener{ 
    
    private PortalForge plugin;
    public EntityListener(PortalForge plugin){
        this.plugin = plugin;
    }
    
    @Override
    public void onEntityPortalEnter(EntityPortalEnterEvent e){
        //Fetch the portalId
        try{
            Entity entity = e.getEntity();
            if(plugin.entityPortaling.contains(entity)) return; //If they're already portaling, skip em.
            plugin.entityPortaling.add(entity);

            Location blockLocation = e.getLocation().getBlock().getLocation(); //maybe just e.getLocation()?
            Portal portal = plugin.dbHelper.getPortalFromBlockLocation(blockLocation);
                if(portal!= null && portal.endPoint != null){
                    //Portal should be a success!
                    Bukkit.getServer().getPluginManager().callEvent(new PortalSuccessEvent(entity,portal));
                } else {
                    if(entity instanceof Player){
                        Player player = (Player)entity;
                        String data = "";
                        if(portal == null){
                            data = "(Not in DB)";
                        } else if(portal.id != -1){
                            data = MessageFormat.format("(#{0})", portal.id);
                        } else {
                            data = "(No id but in DB?)";
                        }

                        //It was probably a nether portal that somehow ended up in the DB without being created
                        // should we start tracking this portal somehow?
                        if(portal == null || portal.type == null || portal.type == PortalType.NETHER){
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new clearPortalingState(plugin,entity),200); //clear 10 seconds later
                            return;
                        } else {
                            plugin.log(MessageFormat.format("{0} tried to use unlinked portal: {1}",player.getName(),data));
                            if(!player.isOp()){ data = ""; }
                            player.sendMessage(MessageFormat.format("This portal{0} is not linked anywhere.", data));
                            
                            //And clear their state 2 seconds later
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new clearPortalingState(plugin,entity),40);
                            return;
                        }
                    }
                   
                }
        } catch (Exception x){
            x.printStackTrace();
        }
        
        //e.setCancelled(true);
    }
}
