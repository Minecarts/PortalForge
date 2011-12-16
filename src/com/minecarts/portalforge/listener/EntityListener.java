package com.minecarts.portalforge.listener;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import com.minecarts.portalforge.PortalForge;


public class EntityListener extends org.bukkit.event.entity.EntityListener{

    private PortalForge plugin;
    public EntityListener(PortalForge plugin){
        this.plugin = plugin;
    }

    @Override
    public void onEntityPortalEnter(EntityPortalEnterEvent e){
        Entity entity = e.getEntity();
        if(entity instanceof Player){
            plugin.entityTouchedPortal(entity, e.getLocation()); //This player touched a portal
        } else if(entity instanceof Animals || entity instanceof Monster){
            plugin.entityUsedPortal(entity); //Non player entities should be teleported instantly
        }
    }
}
