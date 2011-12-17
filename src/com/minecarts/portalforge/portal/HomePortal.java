package com.minecarts.portalforge.portal;


import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class HomePortal extends BasePortal {
    @Override
    public void onTouch(){
        super.onTouch();
        if(portalingEntityIsPlayer()){
            getPortalingPlayer().sendMessage(getPlugin().getConfig().getString("messages.NOT_YET_IMPLEMENTED"));
        }
    }
    
    @Override
    public void onPortal(){
        super.onPortal();
        //Home portals don't do anything
    }
}
