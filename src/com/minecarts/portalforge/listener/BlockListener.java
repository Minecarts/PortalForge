package com.minecarts.portalforge.listener;

import java.text.MessageFormat;

import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.NetherPortal;

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
                plugin.log(e.getPlayer().getName() + "Added block to field: " + fieldId);
            } else {
                //Player ignited a portal?, which is weird because they didn't place a block!
                if(e.getPlayer().getItemInHand().getType() == Material.FLINT_AND_STEEL){
                    //They lighted a portal
                    org.bukkit.block.Block block = e.getBlock();
                    plugin.log(MessageFormat.format("{0} ignited nether portal at ({1},{2},{3})", e.getPlayer().getName(),block.getX(),block.getY(),block.getZ()));
                    plugin.netherPortal.createPortalInDB(block);
                } else {
                    if(e.getPlayer().isOp()){
                        e.getPlayer().sendMessage("You must /portal create (or /portal edit #) before placing portal blocks");
                    }
                    e.setCancelled(true);
                }
            }
        }
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent e){
        //THIS WILL NOT WORK (YET) BECAUSE SUPERPICK DOES NOT THROW A BREAK EVENT
        if(e.isCancelled()) return;
        if(e.getBlock().getType() == Material.PORTAL){
            e.setCancelled(true);
            /*
            //Try and remove it from a field (although it may not have one)
            if(plugin.dbHelper.removeBlockFromUnknownField(e.getBlock().getLocation())){
                e.getPlayer().sendMessage("Block removed from portal");
                plugin.log(MessageFormat.format("{0} removed portal block ({1},{2},{3})",
                        e.getPlayer().getName(),
                        e.getBlock().getX(),
                        e.getBlock().getY(),
                        e.getBlock().getZ()));
            }
            */
        }
    }
}
