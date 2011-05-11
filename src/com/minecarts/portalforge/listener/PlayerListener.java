package com.minecarts.portalforge.listener;

import org.bukkit.event.player.PlayerChatEvent;

import com.minecarts.portalforge.PortalForge;

public class PlayerListener extends org.bukkit.event.player.PlayerListener{ 
    private PortalForge plugin;
    public PlayerListener(PortalForge plugin){
        this.plugin = plugin;
    }

    @Override
    public void onPlayerChat(PlayerChatEvent e){
        //e.setCancelled(true);
    }
}
