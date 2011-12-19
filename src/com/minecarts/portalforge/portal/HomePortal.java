package com.minecarts.portalforge.portal;


import com.minecarts.portalforge.portal.internal.PortalType;

public class HomePortal extends GenericPortal {

    public HomePortal(){
        setType(PortalType.HOME);
    }

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
