package com.minecarts.portalforge.listener;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import com.minecarts.portalforge.PortalForge;


public class EntityListener implements Listener {

    private PortalForge plugin;
    public EntityListener(PortalForge plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityPortalEnter(EntityPortalEnterEvent e){
        Entity entity = e.getEntity();
        if(entity instanceof Player){
            plugin.entityTouchedPortal(entity, e.getLocation());
        } else if(entity instanceof Tameable){
            plugin.entityUsedPortal(entity);
        } else {
            entity.remove(); //Remove thigns such as items immediately
        }
    }
}
