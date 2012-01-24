package com.minecarts.portalforge.portal;

import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.event.PortalSuccessEvent;
import com.minecarts.portalforge.portal.internal.PortalActivation;
import com.minecarts.portalforge.portal.internal.PortalFlag;
import com.minecarts.portalforge.portal.internal.PortalType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class GenericPortal {
    private PortalType type; 
    private PortalActivation activation;
    private ArrayList<PortalFlag> flags = new ArrayList<PortalFlag>();
    private PortalForge plugin;
    private Entity portalingEntity; //The entity doing the portaling
    private Location exitLocation;
    private Location enterLocation;
    private Vector exitVector = new Vector(0,0,0);
    private String message;
    private Long id;
    private ArrayList<Block> blocks = new ArrayList<Block>();

    public GenericPortal(){
        setType(PortalType.GENERIC);
    }

//Below are the functions that other protal types MAY want to consider overriding
//  for some customized special features
    public void onTouch(){
        //Check to see if this portal is linked
        if(getExitLocation() == null){
            if(portalingEntityIsPlayer()){ getPortalingPlayer().sendMessage(getPlugin().getConfig().getString("messages.PORTAL_NOT_LINKED")); }
            return;
        }

        //Attempt to verify the flags
        if(!checkPrePortalFlags()) return;


        Bukkit.getServer().getPluginManager().callEvent(new PortalSuccessEvent(this));
    }
    public void onPortal(){
        if(getExitLocation() == null){
            if(portalingEntityIsPlayer()){ getPortalingPlayer().sendMessage(getPlugin().getConfig().getString("messages.PORTAL_NOT_LINKED")); }
            return;
        }

        if(!checkPrePortalFlags()) return;

        //Fire the portal event
        Bukkit.getServer().getPluginManager().callEvent(new PortalSuccessEvent(this));
    }

    public void postPortal(){
        if(portalingEntityIsPlayer()){
            Player p = getPortalingPlayer();
            if(containsFlag(PortalFlag.CLEAR_INVENTORY)){
                p.getInventory().clear();
                p.getInventory().setBoots(null);
                p.getInventory().setHelmet(null);
                p.getInventory().setLeggings(null);
                p.getInventory().setChestplate(null);
            }
            if(containsFlag(PortalFlag.MESSAGE)){
                p.sendMessage(getMessage());
            }
            if(containsFlag(PortalFlag.MODE_CREATIVE)){
                p.setGameMode(GameMode.CREATIVE);
            }
            if(containsFlag(PortalFlag.MODE_SURVIVAL)){
                p.setGameMode(GameMode.SURVIVAL);
            }
        }
    }
    
    public void showDebug(){
        if(portalingEntityIsPlayer() && getPortalingPlayer().hasPermission("portalforge.debug")){
            getPortalingPlayer().sendMessage(ChatColor.DARK_GRAY + "Touched portal: " + getId());
        }
    }
    
    public void portalCreated(Player player, Block block){
        getPlugin().logAndMessagePlayer(player, "Created portal " + getId());
    }


    //Called in both the onTouch() and onPortal() cases
    //  May want to override to create or alter specific flag checks for certain portals
    public boolean checkPrePortalFlags(){
        if(portalingEntityIsPlayer()){
            Player player = getPortalingPlayer();

            if(containsFlag(PortalFlag.REQUIRE_EMPTY_INVENTORY)){
                //Check the armor
                for(ItemStack stack : player.getInventory().getArmorContents()){
                    if(stack != null && stack.getTypeId() != 0){
                        player.sendMessage(getPlugin().getConfig().getString("messages.REQUIRE_EMPTY_INVENTORY"));
                        return false;
                    }
                }
                //Check the inventory
                for(ItemStack stack : player.getInventory().getContents()){
                    if(stack != null && stack.getTypeId() != 0){
                        player.sendMessage(getPlugin().getConfig().getString("messages.REQUIRE_EMPTY_INVENTORY"));
                        return false;
                    }
                }
            }

            if(containsFlag(PortalFlag.SUBSCRIBER)){
                if(!player.hasPermission("subscriber")){
                    player.sendMessage(getPlugin().getConfig().getString("messages.SUBSCRIBER_ONLY_PORTAL"));
                    return false;
                }
            }
        }
        return true;
    }
//END OVERRIDE FLAGS

//The functions listed below should only  be called internally by PortalForge
    public void teleportEntity(){
        teleportEntity(1);
    }
    public void teleportEntity(int delay){
        final Location exitLocation = getSafeExitLocation();
        if(exitLocation == null){
            getPortalingPlayer().sendMessage("The portal exit appears to be blocked");
            getPlugin().log("Portal exit is blocked @ " + getExitLocation());
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,new Runnable() {
            public void run() {
                getPortalingEntity().teleport(exitLocation);
                getPortalingEntity().setVelocity(getExitVector());
                postPortal();
            }
        },delay); //Teleport delay ticks later
    }
    
//Check the exit location for any blockage
    //TODO: Improve this logic to make it more.. robust, needs to take into account players getting stuck and dying
    //TODO:  becuase a single block check wont get a player stuck with the push out of block code
    public boolean isLocationEmpty(Location location){
        Block exitBlock = location.getBlock();
        switch(exitBlock.getType()){
            case AIR:
            case VINE:
            case TORCH:
            case WATER:
            case STATIONARY_WATER:
            case STONE_BUTTON:
            case STONE_PLATE:
            case WOOD_PLATE:
            case DEAD_BUSH:
            case LONG_GRASS:
            case SIGN:
            case SIGN_POST:
            case SNOW:
            case REDSTONE_TORCH_ON:
            case REDSTONE_TORCH_OFF:
            //case PORTAL:
            //case ENDER_PORTAL:
                return true;
        }
        return false;
    }


//Getters and setters
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
    
    public void addBlock(Block block){
        if(!blocks.contains(block)){
            blocks.add(block);
        }
    }
    public ArrayList<Block> getBlocks(){
        return blocks;
    }
    
    public void setEnterLocation(Location loc){
        if(loc == null) return;
        this.enterLocation = loc;
    }
    public Location getEnterLocation(){
        return this.enterLocation;
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
        //Check the exact exit point
        if(isLocationEmpty(this.exitLocation)){
            plugin.log("Exact location was safe: " + this.exitLocation);
            return this.exitLocation;
        } else {
            if(plugin.getConfig().getBoolean("debug")){
                plugin.log("Exact location was not safe: " + this.exitLocation);
            }
        }

        //Search for any air blocks around the exit point
        for(int xOffset=-1; xOffset <= 1; xOffset++){
            for(int zOffset=-1; zOffset <= 1; zOffset++){
                Location testLoc = new Location(this.exitLocation.getWorld(), this.exitLocation.getBlockX() + xOffset, this.exitLocation.getBlockY(), this.exitLocation.getBlockZ() + zOffset).subtract(new Vector(0.5,0,0.5)); //Substract 0.5 from the loc to get the midpoint of the block
                if(plugin.getConfig().getBoolean("debug")){
                    plugin.log("Checking: " + testLoc);
                    plugin.log("\tFound y: " + testLoc.getBlock().getType());
                    plugin.log("\tFound y+1: " + (new Location(testLoc.getWorld(),testLoc.getX(), testLoc.getY() + 1, testLoc.getZ())).getBlock().getType());
                }
                if(isLocationEmpty(testLoc) && isLocationEmpty(new Location(testLoc.getWorld(),testLoc.getX(), testLoc.getY() + 1, testLoc.getZ()))){
                    plugin.log("\tSafe location found: " + testLoc);
                    return testLoc;
                }
            }
        }
        return null;
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

    protected void setType(PortalType type){
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
        if(flags == null || flags.equals("")) return;
        if(this.flags == null){
            this.flags = new ArrayList<PortalFlag>();
        }
        for(String flag : flags.split(",")){
            this.flags.add(PortalFlag.valueOf(flag));
        }
    }
    public void setFlags(ArrayList<PortalFlag> flags){
        this.flags = flags;
    }
    public ArrayList<PortalFlag> getFlags(){
        if(this.flags.size() == 0) return null;
        return this.flags;
    }
    public boolean containsFlag(PortalFlag flag){
        if(this.flags == null) return false;
        return this.flags.contains(flag);
    }
    

}
