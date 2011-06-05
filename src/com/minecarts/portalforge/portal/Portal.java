package com.minecarts.portalforge.portal;

import org.bukkit.Location; 

public class Portal implements Cloneable {
    public int id = -1;
    public Location endPoint = null;
    public PortalType type = null;
    public PortalActivation activation = null;
    public float exitVelocity = 0;
    public Portal clone() {
        try{
            return (Portal)super.clone();
        } catch (CloneNotSupportedException e){
            return null;
        }
    }
}
