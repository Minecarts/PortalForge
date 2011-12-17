package com.minecarts.portalforge.portal;

import org.bukkit.Bukkit;

public class EndPortal extends BasePortal {
    
    //EndPortals do not fire an EntityEnterPortalEvent (yet?), only an OnPlayerPortal
    @Override
    public void onPortal(){
        //TODO, make these worlds a config option
        if(getPortalingEntity().getWorld().getName().equals("world_the_end")){
            setExitLocation(Bukkit.getWorld("new_highridge").getSpawnLocation()); //Send them back
        } else {
            setExitLocation(Bukkit.getWorld("world_the_end").getSpawnLocation());
        }
        super.onPortal(); //Fires the event and checks flags
    }
}
