package com.minecarts.portalforge.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import com.minecarts.portalforge.PortalForge;


public class PlayerListener implements Listener {
    private PortalForge plugin;
    public PlayerListener(PortalForge plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPortal(PlayerPortalEvent e){
        e.setCancelled(true); //We handle all portal events ourselves
        plugin.entityUsedPortal(e.getPlayer());
    }
}
