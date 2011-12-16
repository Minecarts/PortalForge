package com.minecarts.portalforge.portal;


import org.bukkit.Bukkit;
import org.bukkit.World;

public class NetherPortal extends BasePortal {

    @Override 
    public void onTouch(){
        System.out.println(getPortalingPlayer() + "Touched a nether portal" + getId());
        super.onTouch();
    }
    @Override
    public void onPortal(){
        System.out.println(getPortalingPlayer() + " used NETHER portal " + getId());
        String entityWorld = getPortalingEntity().getWorld().getName();
        //TODO: Make this configurable / database driven
        if(entityWorld.equals("world")){
            setExitLocation(Bukkit.getWorld("world_nether").getSpawnLocation());
        } else {
            setExitLocation(Bukkit.getWorld("world").getSpawnLocation());
        }
        super.onPortal(); //Fires the event and checks flags
    }
}
