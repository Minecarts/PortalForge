package com.minecarts.portalforge.listener;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import com.minecarts.portalforge.PortalForge;


public class PlayerListener extends org.bukkit.event.player.PlayerListener{
    private PortalForge plugin;
    public PlayerListener(PortalForge plugin){
        this.plugin = plugin;
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent e){
        e.setCancelled(true); //We handle all portal events ourselves
        plugin.entityUsedPortal(e.getPlayer());
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent e){

    }

    @Override
    public void onItemHeldChange(PlayerItemHeldEvent e){

    }
}
