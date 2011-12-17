package com.minecarts.portalforge.portal;

import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.event.PortalSuccessEvent;
import com.minecarts.portalforge.portal.internal.PortalActivation;
import com.minecarts.portalforge.portal.internal.PortalFlag;
import com.minecarts.portalforge.portal.internal.PortalType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class BasePortal{
    private PortalType type; 
    private PortalActivation activation;
    private ArrayList<PortalFlag> flags;
    private PortalForge plugin;
    private Entity portalingEntity; //The entity doing the portaling
    private Location exitLocation;
    private Vector exitVector = new Vector(0,0,0);
    private String message;
    private Long id;

    public void onTouch(){
        if(portalingEntityIsPlayer()){
            Player player = getPortalingPlayer();

            if(containsFlag(PortalFlag.REQUIRE_EMPTY_INVENTORY)){
                for(ItemStack stack : player.getInventory().getContents()){
                    if(stack != null && stack.getTypeId() != 0){
                        player.sendMessage(getPlugin().getConfig().getString("REQUIRE_EMPTY_INVENTORY"));
                        return;
                    }
                }
            }
        }

        Bukkit.getServer().getPluginManager().callEvent(new PortalSuccessEvent(this));
    }
    public void onPortal(){
        if(portalingEntityIsPlayer()){
            //Unfortunately we need to check the flags again
            if(containsFlag(PortalFlag.REQUIRE_EMPTY_INVENTORY)){
                return;
            }
        }

        //Fire the portal event
        Bukkit.getServer().getPluginManager().callEvent(new PortalSuccessEvent(this));
    }

    public void postPortal(){
        if(portalingEntityIsPlayer()){
            Player p = getPortalingPlayer();
            if(containsFlag(PortalFlag.CLEAR_INVENTORY)){
                p.getInventory().clear();
            }
            if(containsFlag(PortalFlag.MESSAGE)){
                //TODO
            }
            if(containsFlag(PortalFlag.MODE_CREATIVE)){
                p.setGameMode(GameMode.CREATIVE);
            }
            if(containsFlag(PortalFlag.MODE_SURVIVAL)){
                p.setGameMode(GameMode.SURVIVAL);
            }
        }
    }

    public void teleportEntity(){
        teleportEntity(1);
    }
    public void teleportEntity(int delay){
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,new Runnable() {
            public void run() {
                getPortalingEntity().teleport(getExitLocation());
                getPortalingEntity().setVelocity(getExitVector());
                postPortal();
            }
        },delay); //Teleport delay ticks later
    }


//Getters nad setters    
    public void setId(Long id){
        this.id = id;
    }
    public Long getId(){
        return this.id;
    }
    
    public void setMessage(String msg){
        this.message = msg;
    }
    public String getMessage(){
        return this.message;
    }
    
    public void setExitLocation(Location loc){
        if(loc == null) return;
        this.exitLocation = loc;
    }
    public void setExitLocation(World w, Double x, Double y, Double z, Float pitch, Float yaw){
        if(w == null || x == null || y == null || z == null || pitch == null || yaw == null) return;
        this.exitLocation = new Location(w,x,y,z,yaw,pitch);
    }
    public Location getExitLocation(){
        return this.exitLocation;
    }
    public Location getSafeExitLocation(){
        //TODO: Find a safe exit point..
        return this.exitLocation;
    }
    
    public void setExitVector(Vector v){
        this.exitVector = v;
    }
    public void setExitVector(Float x, Float y, Float z){
        if(x == null) x = 0f;
        if(y == null) y = 0f;
        if(z == null) z = 0f;
        this.exitVector = new Vector(x,y,z);
    }
    public Vector getExitVector(){
        return this.exitVector;
    }
    
    public void setPortalingEntity(Entity e){
        this.portalingEntity = e;
    }
    public Entity getPortalingEntity(){
        return this.portalingEntity;
    }
    public Player getPortalingPlayer(){
        if(this.portalingEntity instanceof Player){
            return (Player)this.portalingEntity;
        } else {
            return null;
        }
    }
    public boolean portalingEntityIsPlayer(){
        return (this.portalingEntity instanceof Player);
    }


    
    public void setPlugin(PortalForge plugin){
        this.plugin = plugin;
    }
    public PortalForge getPlugin(){
        return this.plugin;
    }

    public void setType(PortalType type){
        this.type = type;
    }
    public PortalType getType(){
        return this.type;
    }
    
    public void setActivation(PortalActivation activation){
        this.activation = activation;
    }
    public PortalActivation getActivation(){
        return this.activation;
    }


    public void setFlags(String flags){
        if(flags == null) return;
        for(String flag : flags.split(",")){
            this.flags.add(PortalFlag.valueOf(flag));
        }
    }
    public void setFlags(ArrayList<PortalFlag> flags){
        this.flags = flags;
    }
    public ArrayList<PortalFlag> getFlags(){
        return this.flags;
    }
    public boolean containsFlag(PortalFlag flag){
        if(this.flags == null) return false;
        return this.flags.contains(flag);
    }
    

}
