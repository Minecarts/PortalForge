package com.minecarts.portalforge.portal;

import org.bukkit.Bukkit;

public class EndPortal extends BasePortal {
    
    //EndPortals do not fire an EntityEnterPortalEvent (yet?), only an OnPlayerPortal
    @Override
    public void onPortal(){
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
}
