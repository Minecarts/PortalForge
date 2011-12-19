package com.minecarts.portalforge.portal;

import com.minecarts.portalforge.portal.internal.PortalType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class EndPortal extends GenericPortal {
    private BlockFace[] faces = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST};
    private ArrayList<Block> foundBlocks = new ArrayList<Block>();

    public EndPortal(){
        setType(PortalType.END);
    }

    //EndPortals do not fire an EntityEnterPortalEvent (yet?), only an OnPlayerPortal
    @Override
    public void onPortal(){
        super.showDebug(); //End portals don't have an onTouch so lets force a debug to show

        //TODO, make these worlds a config option
        if(getPortalingEntity().getWorld().getName().equals("world_the_end")){
            setExitLocation(Bukkit.getWorld("world").getSpawnLocation()); //Send them back
        } else {
            setExitLocation(Bukkit.getWorld("world_the_end").getSpawnLocation());
        }
        super.onPortal(); //Fires the event and checks flags
    }

    @Override
    public void postPortal(){
        //Since we're async, the player can fall into the lava and take some damage
        //  before the portal fires, so lets fix them up afterwards
        //TODO: Consider storing the health before and setting it here to prevent free HP gain
        if(portalingEntityIsPlayer()){
            getPortalingPlayer().setFireTicks(0);
            getPortalingPlayer().setHealth(getPortalingPlayer().getMaxHealth());
        }
    }
    
    @Override
    public void portalCreated(Player player, Block block){
        getPlugin().logAndMessagePlayer(player, "Discovered nether portal (ID: " + getId() + ")");
        //Attempt to locate all the nearby enderblocks and add them to the field
        findNearbyPortalBlocksHorizontal(block);


        //And lets send the player to the end
        this.onPortal(); //Refire the portal event now
    }


    public void createInDB(Block block){
        getPlugin().createPortal(getPortalingPlayer(), this, block);

        //Clear the editing portal X seconds later
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),new Runnable() {
            public void run() {
                getPlugin().clearEditingPortal(getPortalingPlayer()); //Portal has been created
            }
        },20 * 5); //TODO: Make configurable
    }

    private void findNearbyPortalBlocksHorizontal(Block block){
        for(BlockFace face : faces){
            Block relativeBlock = block.getRelative(face);
            if(foundBlocks.contains(relativeBlock)) continue;
            if(relativeBlock.getType() == Material.ENDER_PORTAL){
                foundBlocks.add(relativeBlock);
                getPlugin().addBlockToPortal(getPortalingPlayer(),relativeBlock.getLocation());
                findNearbyPortalBlocksHorizontal(relativeBlock);
            }
        }
        return;
    }
}
