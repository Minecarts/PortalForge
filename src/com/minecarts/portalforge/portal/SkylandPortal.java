package com.minecarts.portalforge.portal;

public class SkylandPortal extends BasePortal {
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
