package com.minecarts.portalforge.portal;

import com.minecarts.portalforge.portal.internal.PortalType;

public class SkylandPortal extends GenericPortal {

    public SkylandPortal(){
        setType(PortalType.SKYLAND);
    }

    @Override
    public void onTouch(){
        if(portalingEntityIsPlayer()){
            getPortalingPlayer().sendMessage("Skyland portals are no longer functional.");
        }
    }

    @Override
    public void onPortal(){
        if(portalingEntityIsPlayer()){
            getPortalingPlayer().sendMessage("Skyland portals are no longer functional.");
        }
    }
}
