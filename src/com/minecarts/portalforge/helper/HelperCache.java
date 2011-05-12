package com.minecarts.portalforge.helper;

import com.minecarts.portalforge.PortalForge;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.block.Block;

import com.minecarts.portalforge.portal.Portal;

public class HelperCache {
    private PortalForge plugin;
    private HashMap<Location,Portal> cacheMap = new HashMap<Location,Portal>();
    public HelperCache(PortalForge plugin){
        this.plugin = plugin;
    }
    
    public void setPortal(Location location,Portal portal){
        this.cacheMap.put(location, portal);
    }
    
    public Portal getPortal(Location location){
        if(this.cacheMap.containsKey(location)){
            return this.cacheMap.get(location);
        } else {
            return null;
        }
    }
    
    public void clearByPortal(int portalId){
        java.util.ArrayList<Block> blocks = plugin.dbHelper.getPortalBlocksFromId(portalId);
        for(Block block : blocks){
            this.clearByLocation(block.getLocation());
        }
    }
    public void clearByLocation(Location location){
        if(this.cacheMap.containsKey(location)){
            this.cacheMap.remove(location);
        }
    }
    
    public void clearAll(){
        this.cacheMap.clear();
    }
}
