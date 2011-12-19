package com.minecarts.portalforge.portal;


import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class HomePortal extends BasePortal {
    @Override
    public void onTouch(){
        if(portalingEntityIsPlayer()){
            getPortalingPlayer().sendMessage(getPlugin().getConfig().getString("messages.NOT_YET_IMPLEMENTED"));
        }
    }
    
    @Override
    public void onPortal(){
        if(portalingEntityIsPlayer()){
            getPortalingPlayer().sendMessage(getPlugin().getConfig().getString("messages.NOT_YET_IMPLEMENTED"));
        }
    }
}
