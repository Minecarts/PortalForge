package com.minecarts.portalforge.portal;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;

public class Portal implements Cloneable {
    public int id = -1;
    public Location endPoint = null;
    public Location shareDestination = null;
    public PortalType type = null;
    public PortalActivation activation = null;
    public ArrayList<PortalFlag> flags = new ArrayList<PortalFlag>();
    public Vector velocityVector = null;
    public String message = null;

    public void parseFlags(String flagString){
        if(flagString == null) return;
        for(String flag : flagString.split(",")){
            this.flags.add(PortalFlag.valueOf(flag));
        }
    }

    public Portal clone() {
        try{
            return (Portal)super.clone();
        } catch (CloneNotSupportedException e){
            return null;
        }
    }
}
