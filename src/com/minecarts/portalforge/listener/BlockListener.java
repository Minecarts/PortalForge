package com.minecarts.portalforge.listener;

import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import com.minecarts.portalforge.PortalForge;

public class BlockListener extends org.bukkit.event.block.BlockListener{
    private PortalForge plugin;
    public BlockListener(PortalForge plugin){
        this.plugin = plugin;
    }
    
    @Override 
    public void onBlockPlace(BlockPlaceEvent e){
        if(e.getBlock().getType() == Material.PORTAL){
            if(plugin.activePortalDesigns.containsKey(e.getPlayer().getName())){
                //Add this portal block to the portal in the DB
                int fieldId = plugin.activePortalDesigns.get(e.getPlayer().getName());
                plugin.dbHelper.addBlockToField(e.getBlockPlaced().getLocation(), fieldId);
                e.getPlayer().sendMessage("Added block to field: " + fieldId);
            } else {
                e.getPlayer().sendMessage("You must /portal create (or /portal edit #) before placing portal blocks");
                e.setCancelled(true);
            }
        }
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent e){
        //THIS WILL NOT WORK (YET) BECAUSE SUPERPICK DOES NOT THROW A BREAK EVENT
        if(e.isCancelled()) return;
        if(e.getBlock().getType() == Material.PORTAL){
            //Try and remove it from a field (although it may not have one)
            if(plugin.dbHelper.removeBlockFromUnknownField(e.getBlock().getLocation())){
                e.getPlayer().sendMessage("Portal block removed from it's portal");
            }
        }
    }
}
