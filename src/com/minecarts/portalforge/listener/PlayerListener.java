package com.minecarts.portalforge.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.event.PortalSuccessEvent;
import com.minecarts.portalforge.portal.Portal;

public class PlayerListener extends org.bukkit.event.player.PlayerListener{ 
    private PortalForge plugin;
    public PlayerListener(PortalForge plugin){
        this.plugin = plugin;
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent e){
        if(e.isCancelled()) return;
        //They're portaling, so lets handle it ourselves
        e.setCancelled(true);

        
        Location blockLocation = e.getPlayer().getLocation().getBlock().getLocation();
        //Find the nearest portal block they're touching because of rounding issues with getBlock()
        org.bukkit.block.Block block = blockLocation.getBlock();
        if(block.getType() != Material.PORTAL){
            if(block.getRelative(BlockFace.NORTH).getType() == Material.PORTAL)
                blockLocation = block.getRelative(BlockFace.NORTH).getLocation(); 
            else if(block.getRelative(BlockFace.EAST).getType() == Material.PORTAL)
                blockLocation = block.getRelative(BlockFace.EAST).getLocation();
            else if(block.getRelative(BlockFace.SOUTH).getType() == Material.PORTAL)
                blockLocation = block.getRelative(BlockFace.SOUTH).getLocation();
            else if(block.getRelative(BlockFace.WEST).getType() == Material.PORTAL)
                blockLocation = block.getRelative(BlockFace.WEST).getLocation();
        }
            
        Portal portal = plugin.dbHelper.getPortalFromBlockLocation(blockLocation);
        if(portal != null){
            Bukkit.getServer().getPluginManager().callEvent(new PortalSuccessEvent(e.getPlayer(),portal)); //Portal should be a success!
        } else { 
            //This portal is unknown.
            plugin.log("onPlayerPortal to an unknown portal: " + blockLocation);
        }

    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent e){
        if(e.getAction() == Action.LEFT_CLICK_BLOCK && plugin.activePortalDesigns.containsKey(e.getPlayer().getName())){
            if(e.hasBlock() && e.getClickedBlock().getType() == Material.PORTAL){
                //Try and remove it if it's a portal block
                if(plugin.dbHelper.removeBlockFromUnknownField(e.getClickedBlock().getLocation())){
                    e.getPlayer().sendMessage("Removed portal block from its portal");
                    plugin.log(e.getPlayer().getName() + " removed block from portal" + e.getClickedBlock().getLocation());
                    e.getClickedBlock().setType(Material.AIR);
                }
            }
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().getItemInHand().getType() == Material.FLINT_AND_STEEL){
            org.bukkit.block.Block block = e.getClickedBlock();
            if(block.getType() == Material.OBSIDIAN){
            }
            //See if this player is trying to ignite a portal
        }
        
        //e.setCancelled(true);
    }

    @Override
    public void onItemHeldChange(PlayerItemHeldEvent e){
        /*
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
        */
    }
}
