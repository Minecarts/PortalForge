package com.minecarts.portalforge.helper;

import org.bukkit.entity.Entity;
import com.minecarts.portalforge.PortalForge;

public class clearPortalingState implements Runnable{
    private Entity e;
    private PortalForge plugin;
    
    public clearPortalingState(PortalForge plugin, Entity e){
        this.plugin = plugin;
        this.e = e;
    }
    public void run() {
        if(plugin.entityPortaling.contains(e)){
            plugin.entityPortaling.remove(e);
        }
    }
}
