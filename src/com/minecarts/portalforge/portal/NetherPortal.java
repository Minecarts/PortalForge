package com.minecarts.portalforge.portal;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class NetherPortal extends BasePortal {

    @Override 
    public void onTouch(){
        //Nether portals don't have any onTouch actions
    }
    @Override
    public void onPortal(){
        System.out.println(getPortalingPlayer() + " used NETHER portal " + getId());
        String entityWorld = getPortalingEntity().getWorld().getName();
        //TODO: Make this configurable / database driven
        if(entityWorld.equals("world")){
            getPlugin().doHistoricalTeleport(getPortalingPlayer(),"world_nether",this);
        } else {
            getPlugin().doHistoricalTeleport(getPortalingPlayer(),"world",this);
        }
    }
    
    public void historicalTeleport(Location loc){
        setExitLocation(loc);
        super.onPortal();
    }
}
