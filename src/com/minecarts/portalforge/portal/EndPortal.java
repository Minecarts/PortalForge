package com.minecarts.portalforge.portal;

import com.minecarts.portalforge.PortalForge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;

public class EndPortal {
    PortalForge plugin;
    BlockFace[] faces = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST};
    ArrayList<Block> foundBlocks = new ArrayList<Block>();
    
    
    public EndPortal(PortalForge plugin){
        this.plugin = plugin;
    }

    public boolean destory(Block block){
            //Recursively destory nether portal blocks belonging to a portal
            return true;
    }

    public int createPortalInDB(Block block){
        int portalId = plugin.dbHelper.createPortal(PortalType.END,PortalActivation.INSTANT);
        this.findNearbyPortalBlocksHorizontal(block,portalId);
        return portalId;
    }

    private void findNearbyPortalBlocksHorizontal(Block block, int portalId){
        for(BlockFace face : faces){
            Block relativeBlock = block.getRelative(face);
            if(foundBlocks.contains(relativeBlock)) continue;
            if(relativeBlock.getType() == Material.ENDER_PORTAL){
                foundBlocks.add(relativeBlock);
                //System.out.println("Adding block at: " + relativeBlock.getLocation());
                plugin.dbHelper.addBlockToField(relativeBlock.getLocation(), portalId);
                findNearbyPortalBlocksHorizontal(relativeBlock,portalId);
            }
        }
        return;
    }
}
