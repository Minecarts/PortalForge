package com.minecarts.portalforge.listener;

import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.entity.Player;

import com.minecarts.portalforge.PortalForge;

public class PlayerListener extends org.bukkit.event.player.PlayerListener{ 
    private PortalForge plugin;
    public PlayerListener(PortalForge plugin){
        this.plugin = plugin;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent e){
        if(e.getAction() == Action.LEFT_CLICK_BLOCK && plugin.activePortalDesigns.containsKey(e.getPlayer().getName())){
            if(e.hasBlock() && e.getClickedBlock().getType() == Material.PORTAL){
                //Try and remove it if it's a portal block
                if(plugin.dbHelper.removeBlockFromUnknownField(e.getClickedBlock().getLocation())){
                    e.getPlayer().sendMessage("Removed portal block from its portal");
                    e.getClickedBlock().setType(Material.AIR);
                }
            }
        }
        //e.setCancelled(true);
    }
    
    @Override
    public void onItemHeldChange(PlayerItemHeldEvent e){
        Player p = e.getPlayer();
        if(plugin.activePortalDesigns.containsKey(e.getPlayer().getName())){
            if(p.getItemInHand().getType() == Material.PORTAL){
                p.sendMessage("Portal building complete!");
                if(p.getItemInHand().getType() == Material.PORTAL){
                    if(plugin.previousInventory.containsKey(p.getName())){
                        p.setItemInHand(plugin.previousInventory.get(p.getName()));
                        plugin.previousInventory.remove(p.getName());
                    }
                    
                }
                plugin.activePortalDesigns.remove(p.getName());
            }
        }
    }
}
