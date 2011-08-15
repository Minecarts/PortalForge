package com.minecarts.portalforge.listener;

import java.text.MessageFormat;

import com.minecarts.portalforge.portal.Portal;
import com.minecarts.portalforge.portal.PortalType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
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
    public void onBlockPhysics(BlockPhysicsEvent e){
        if(e.isCancelled()) return;
        if(e.getBlock().getType() == Material.PORTAL){
            e.setCancelled(true);
        }
    }


    @Override
    public void onBlockFromTo(BlockFromToEvent e){
        if(e.isCancelled()) return;
        Material sourceBlockType = e.getBlock().getType();
        if(e.getToBlock().getType() == Material.PORTAL && (sourceBlockType == Material.STATIONARY_WATER || sourceBlockType == Material.WATER)){
            e.setCancelled(true);
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e){
        if(e.isCancelled()) return;
        Block b = e.getBlock();
        if(b.getType() == Material.OBSIDIAN){
            World world = b.getWorld();
            int x = b.getX();
            int y = b.getY();
            int z = b.getZ();

            //Try to locate nearby portal blocks and remove them
            removeNearbyPortalBlocks(world,x + 1,y,z);
            removeNearbyPortalBlocks(world,x,y + 1,z);
            removeNearbyPortalBlocks(world,x,y,z + 1);
            removeNearbyPortalBlocks(world,x - 1,y,z);
            removeNearbyPortalBlocks(world,x,y - 1,z);
            removeNearbyPortalBlocks(world,x,y,z - 1);
        }
    }

    private void removeNearbyPortalBlocks(World world, int x, int y, int z){
        Block b = world.getBlockAt(x,y,z);
        if(b.getType() == Material.PORTAL){
            Portal p = plugin.dbHelper.getPortalFromBlockLocation(b.getLocation());
            if(p.type == PortalType.NETHER){
                b.setType(Material.AIR);
                plugin.dbHelper.removeBlockFromUnknownField(b.getLocation());

                removeNearbyPortalBlocks(world,x + 1,y,z);
                removeNearbyPortalBlocks(world,x,y + 1,z);
                removeNearbyPortalBlocks(world,x,y,z + 1);
                removeNearbyPortalBlocks(world,x - 1,y,z);
                removeNearbyPortalBlocks(world,x,y - 1,z);
                removeNearbyPortalBlocks(world,x,y,z - 1);
            }
        }
    }
}
