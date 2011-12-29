package com.minecarts.portalforge.listener;

import com.minecarts.portalforge.portal.GenericPortal;
import com.minecarts.portalforge.portal.NetherPortal;
import com.minecarts.portalforge.portal.internal.PortalActivation;
import com.minecarts.portalforge.portal.internal.PortalType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import com.minecarts.portalforge.PortalForge;

public class BlockListener extends org.bukkit.event.block.BlockListener{
    private PortalForge plugin;
    private BlockFace[] faces = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST, BlockFace.DOWN, BlockFace.UP};
    public BlockListener(PortalForge plugin){
        this.plugin = plugin;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent e){
        if(e.getBlock().getType() == Material.PORTAL || e.getBlock().getType() == Material.ENDER_PORTAL){
            //See if this player is editing a portal
            if(plugin.getEditingPortal(e.getPlayer()) != null){
                plugin.addBlockToPortal(e.getPlayer(),e.getBlock().getLocation());
            } else {
                Player player = e.getPlayer();
                if(player.getItemInHand().getType() == Material.FLINT_AND_STEEL){
                    //They ignited a nether portal
                    // Create a new portal
                    NetherPortal portal = new NetherPortal();
                    portal.setActivation(PortalActivation.DELAYED);
                    plugin.createPortal(player,portal,e.getBlock());
                }
            }
        }
    }

    @Override
    public void onBlockPhysics(BlockPhysicsEvent e){
        //Prevent portal blocks from updating
        if(e.getBlock().getType() == Material.PORTAL){
            e.setCancelled(true);
        }
    }


    @Override
    public void onBlockFromTo(BlockFromToEvent e){
        if(e.getToBlock().getType() == Material.PORTAL || e.getToBlock().getType() == Material.ENDER_PORTAL){
            e.setCancelled(true);
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e){
        if(e.getBlock().getType() == Material.PORTAL || e.getBlock().getType() == Material.ENDER_PORTAL){
            GenericPortal portal = plugin.getEditingPortal(e.getPlayer());
            if(portal != null){
                //Remove this block from the field
                plugin.removeBlockFromPortal(e.getPlayer(),portal,e.getBlock());
            } else {
                e.setCancelled(true); //Otherwise portal blocks cannot be broken
            }
            return;
        }
        
        
        if(e.getBlock().getType() == Material.OBSIDIAN){
            for(BlockFace face : faces){
                Block checkBlock = e.getBlock().getRelative(face);
                if(checkBlock.getType() == Material.PORTAL){
                    Player player = e.getPlayer();
                    //We found a portal block,
                    plugin.editPortalFromBlock(e.getPlayer(),checkBlock);
                    GenericPortal portal = plugin.getEditingPortal(player);

                    if(portal.getType() != PortalType.NETHER) return; //Safety check, only remove nether portals

                    plugin.getAllBlocksFromPortal(e.getPlayer(),portal); //Find all the blocks in this portla

                    for(Block block : portal.getBlocks()){
                        plugin.removeBlockFromPortal(e.getPlayer(),portal,block); //And remove them
                        block.setType(Material.AIR);
                    }

                    plugin.clearEditingPortal(player); //This player is done, clear the editing
                    break; //Stop looping, we're done
                }
            }
        }
    }
}
