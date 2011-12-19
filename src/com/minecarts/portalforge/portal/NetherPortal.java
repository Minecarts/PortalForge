package com.minecarts.portalforge.portal;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class NetherPortal extends BasePortal {

    private BlockFace[] faces = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST};
    private ArrayList<Block> foundBlocks = new ArrayList<Block>();

    @Override
    public void onTouch(){
        //Nether portals don't have any onTouch actions, but we override to prevent
        //the default from occuring. We don't want to fire any events.
    }
    @Override
    public void onPortal(){
        System.out.println(getPortalingPlayer() + " used NETHER portal " + getId());
        String entityWorld = getPortalingEntity().getWorld().getName();
        //TODO: Make this configurable / database driven
        String targetWorld = "world";
        if(entityWorld.equals("world")){
            targetWorld = "world_nether";
        } else {
            targetWorld = "world";
        }

        //Only players can use a NetherPortal
        if(portalingEntityIsPlayer()){
            getPlugin().doHistoricalTeleport(getPortalingPlayer(),targetWorld,this);
        } else {
            super.onPortal(); //Portal the entity immediately, TODO: Might break shit.
        }
    }

    @Override
    public void portalCreated(final Player player, Block block){
        getPlugin().log(player.getName() + " ignited nether portal " + this.getId());
        if(block != null){
            this.addBlocksVertically(player, block);

            //Now try and find the portal block next to the one we just added
            BlockFace[] faces = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST};
            for(BlockFace face : faces){
                if(block.getRelative(face).getType() == Material.PORTAL){
                    this.addBlocksVertically(player, block.getRelative(face));
                    break;
                }
            }
        }

        //Clear the editing portal X seconds later
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),new Runnable() {
            public void run() {
                getPlugin().clearEditingPortal(player); //Portal has been created
            }
        },20 * 5); //TODO: Make configurable
    }

//These two functions are unique to nether protals and allow for sharing and historical teleports, thusly they do not override
    public void sharedTeleport(String playerName, Location loc){
        setExitLocation(loc);
        getPortalingPlayer().sendMessage(ChatColor.GRAY + "You follow " + playerName + " through the nether portal."); //TODO: Add displayName functionality
        super.onPortal();
    }

    public void historicalTeleport(Location loc){
        setExitLocation(loc);
        getPlugin().setPortalHistory(getPortalingPlayer(),this);
        super.onPortal();
    }

    private void addBlocksVertically(Player player, Block block){
        for(int i=0; i<3; i++){
            Location location = new Location(block.getWorld(),block.getX(),block.getY()+i,block.getZ());
            getPlugin().addBlockToPortal(player, location, true); //ADd the blocks to the field silently
        }
    }
}
