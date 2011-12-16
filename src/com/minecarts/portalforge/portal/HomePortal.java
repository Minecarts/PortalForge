package com.minecarts.portalforge.portal;


import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class HomePortal extends BasePortal {
    @Override
    public void onTouch(Entity e){
        super.onTouch(e);
        if(e instanceof Player){
            ((Player) e).sendMessage(getPlugin().getConfig().getString("messages.NOT_YET_IMPLEMENTED"));
        }
    } 
    
    @Override
    public void onPortal(Entity e){
        super.onPortal(e);
        //Home portals don't do anything
    }
}
