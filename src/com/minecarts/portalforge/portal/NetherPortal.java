package com.minecarts.portalforge.portal;

import java.text.MessageFormat;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;

import com.minecarts.portalforge.PortalForge;

public class NetherPortal {
    //See if it's a nether portal
    PortalForge plugin;
    
    public NetherPortal(PortalForge plugin){
        this.plugin = plugin;
    }
    
    public boolean destory(Block block){
            //Recursively destory nether portal blocks belonging to a portal
            return true;
    }
    
    public void createPortalInDB(Block block){
        int portalId = plugin.dbHelper.createPortal(PortalType.NETHER);
        
        //Add the 3 portal blocks (including the one passed in) to the DB
        this.addBlocksVertically(block,portalId);

        //Now try and find the portal block next to the one we just added
        BlockFace[] faces = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST};
        for(BlockFace face : faces){
            if(block.getRelative(face).getType() == Material.PORTAL){
                this.addBlocksVertically(block.getRelative(face),portalId);
                break;
            }
        }
    }
        
    private void addBlocksVertically(Block block, int portalId){
        for(int i=0; i<3; i++){
            Location location = new Location(block.getWorld(),block.getX(),block.getY()+i,block.getZ());
            plugin.dbHelper.addBlockToField(location, portalId); 
        }
    }
}
