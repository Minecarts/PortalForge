package com.minecarts.portalforge.portal;


import com.minecarts.portalforge.portal.internal.PortalType;
import org.bukkit.ChatColor;

public class UnknownPortal extends GenericPortal {

    public UnknownPortal(){
        setType(PortalType.UNKNOWN);
    }

    @Override
    public void showDebug(){
        if(portalingEntityIsPlayer()){
            getPortalingPlayer().sendMessage(ChatColor.DARK_GRAY + "Touched an unknown portal (not in DB)");
        }
    }

    @Override
    public void onTouch(){
        getPlugin().log(getPortalingEntity() + " - Touched unknown portal @ " + getPortalingEntity().getLocation());
    }
    
    public void onPortal(){
        getPlugin().log(getPortalingEntity() + " - Stood in unknown portal @ " + getPortalingEntity().getLocation());
    }
}
