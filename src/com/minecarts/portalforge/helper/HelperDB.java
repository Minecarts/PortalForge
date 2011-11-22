package com.minecarts.portalforge.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

import java.util.ArrayList;

import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.Portal;
import com.minecarts.portalforge.portal.PortalFlag;
import com.minecarts.portalforge.portal.PortalType;
import com.minecarts.portalforge.portal.PortalActivation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class HelperDB {
    private PortalForge plugin; 
    public HelperDB(PortalForge plugin){
        this.plugin = plugin;
    }
    
    public int createPortal(){
        return createPortal(PortalType.GENERIC, PortalActivation.INSTANT);
    }
    public int createPortal(PortalType type, PortalActivation activation){
        int lastInsertedId = -1;
        try{
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO portals(`type`,`activation`) VALUES (?,?)",PreparedStatement.RETURN_GENERATED_KEYS);
            if(ps == null){ //Query failed
                conn.close();
                plugin.log.warning("Insert Portal query failed");
            }
            ps.setString(1, type.name());
            ps.setString(2, activation.name());

            ps.execute();
            ResultSet rskey = ps.getGeneratedKeys();
            if (rskey != null && rskey.next()) {
                 lastInsertedId = rskey.getInt(1);
            }
            ps.close();
            conn.close();
        } catch (SQLException e) {
           e.printStackTrace();
        }
        return lastInsertedId;
    }
    public void addBlockToField(Location location, int fieldId){
        try{
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO portal_blocks(portal_id,world,x,y,z) VALUES (?,?,?,?,?)");
            if(ps == null){ //Query failed
                conn.close();
                plugin.log.warning("Insert Portal block query failed");
                return;
            }
            ps.setInt(1, fieldId);
            ps.setString(2, location.getWorld().getName());
            ps.setInt(3, location.getBlockX());
            ps.setInt(4, location.getBlockY());
            ps.setInt(5, location.getBlockZ());

            ps.execute();

            ps.close();
            conn.close();
        } catch (SQLException e) {
           e.printStackTrace();
        }
    }
    
    public void removePortalRecord(int portalId){
        try{
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM portals WHERE `id`=? LIMIT 1");
            if(ps == null){ //Query failed
                conn.close();
                plugin.log.warning("Delete Portal record failed");
            }
            ps.setInt(1, portalId);
            ps.executeUpdate();
            ps.close();
            conn.close();
        } catch (SQLException e) {
           e.printStackTrace();
        }
    }
    
    public boolean removeBlockFromUnknownField(Location location){
        try{
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM portal_blocks WHERE `world`=? AND `x`=? AND `y`=? AND `z`=? LIMIT 1");
            if(ps == null){ //Query failed
                conn.close();
                plugin.log.warning("Delete Portal from unknown field failed");
                return false;
            }
            ps.setString(1, location.getWorld().getName());
            ps.setInt(2, location.getBlockX());
            ps.setInt(3, location.getBlockY());
            ps.setInt(4, location.getBlockZ());

            int deleteCount = ps.executeUpdate();

            ps.close();
            conn.close();
            if(deleteCount > 0) return true;
        } catch (SQLException e) {
           e.printStackTrace();
        }
        return false;
    }
    
    public void setPortalDestination(Location location, int portalId){
        try{
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE `portals` SET `dest_world` =?,`dest_x`=?,`dest_y`=?,`dest_z`=?,`dest_pitch`=?,`dest_yaw`=? WHERE `id`=? LIMIT 1");
            if(ps == null){ //Query failed
                conn.close();
                plugin.log.warning("Set portal destination query failed");
                return;
            }
            ps.setString(1, location.getWorld().getName());
            ps.setDouble(2, location.getX());
            ps.setDouble(3, location.getY());
            ps.setDouble(4, location.getZ());
            ps.setFloat(5, location.getPitch());
            ps.setFloat(6, location.getYaw());
            
            ps.setInt(7, portalId);

            ps.execute();

            ps.close();
            conn.close();
        } catch (SQLException e) {
           e.printStackTrace();
        }
    }
    
    public void setPortalVelocity(int portalId, float v){
        try{
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE `portals` SET `dest_vel`=? WHERE `id`=? LIMIT 1");
            if(ps == null){ //Query failed
                conn.close();
                plugin.log.warning("Set portal velocity query failed");
                return;
            }
            ps.setFloat(1, v);
            ps.setInt(2, portalId);

            ps.execute();

            ps.close();
            conn.close();
        } catch (SQLException e) {
           e.printStackTrace();
        }
    }
    public ArrayList<Block> getPortalBlocksFromId(int portalId){
        ArrayList<Block> portalBlocks = new ArrayList<Block>();
        try{
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM `portal_blocks` WHERE `portal_id` = ?");
            if(ps == null){
                plugin.log.warning("GetPortalEndPointFromBlock query failed");
                conn.close();
                return portalBlocks;
            }
            ps.setInt(1, portalId);
            ResultSet set = ps.executeQuery();
            while(set.next()) {
                org.bukkit.World world = Bukkit.getServer().getWorld(set.getString("world"));
                if(world != null){
                    Block block = world.getBlockAt(set.getInt("x"), set.getInt("y"), set.getInt("z"));
                    if(block != null && block.getType() == Material.PORTAL){
                        portalBlocks.add(block);
                    }
                }
            }
            set.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return portalBlocks;
    }
    public Portal getPortalFromBlockLocation(Location location){
        //See if it's in the cache first
        Portal portal = plugin.cache.getPortal(location);
        if(portal != null){ return portal; }

        try{
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT `portals`.* FROM `portals`,`portal_blocks` WHERE `portal_blocks`.`portal_id` = `portals`.`id` AND `portal_blocks`.`world` = ? AND `portal_blocks`.`x` = ? AND `portal_blocks`.`y` = ? AND `portal_blocks`.`z` = ? LIMIT 1");
            if(ps == null){
                plugin.log.warning("GetPortalEndPointFromBlock query failed");
                conn.close();
                return portal;
            }
            ps.setString(1, location.getWorld().getName());
            ps.setInt(2, location.getBlockX());
            ps.setInt(3, location.getBlockY());
            ps.setInt(4, location.getBlockZ());
            ResultSet set = ps.executeQuery();
            if (set.next()) {
                portal = new Portal();
                portal.id = set.getInt("id");
                portal.type = PortalType.valueOf(set.getString("type"));
                portal.activation = PortalActivation.valueOf(set.getString("activation"));
                portal.parseFlags(set.getString("flags"));
                portal.message = set.getString("message");
                if(set.getString("dest_world")!= null && Bukkit.getServer().getWorld(set.getString("dest_world")) != null){
                    portal.endPoint = new Location(Bukkit.getServer().getWorld(set.getString("dest_world")),set.getDouble("dest_x"),set.getDouble("dest_y"),set.getDouble("dest_z"),set.getFloat("dest_yaw"),set.getFloat("dest_pitch"));
                    portal.velocityVector = new Vector(set.getFloat("dest_vel_x"),set.getFloat("dest_vel_y"),set.getFloat("dest_vel_z"));
                }
                set.close();
            }
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        plugin.cache.setPortal(location, portal);
        return portal;
    }
    
    public Portal getPortalById(int portalId){
        Portal portal = null;
        try{
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT `portals`.* FROM `portals` WHERE `portals`.`id` = ? LIMIT 1");
            if(ps == null){
                plugin.log.warning("GetPortalEndPointFromBlock query failed");
                conn.close();
                return portal;
            }
            ps.setInt(1, portalId);
            ResultSet set = ps.executeQuery();
            if (set.next()) {
                portal = new Portal();
                portal.id = set.getInt("id");
                portal.type = PortalType.valueOf(set.getString("type"));
                portal.activation = PortalActivation.valueOf(set.getString("activation"));
                portal.parseFlags(set.getString("flags"));
                portal.message = set.getString("message");
                if(set.getString("dest_world")!= null && Bukkit.getServer().getWorld(set.getString("dest_world")) != null){
                    portal.endPoint = new Location(Bukkit.getServer().getWorld(set.getString("dest_world")),set.getDouble("dest_x"),set.getDouble("dest_y"),set.getDouble("dest_z"),set.getFloat("dest_yaw"),set.getFloat("dest_pitch"));
                    portal.velocityVector = new Vector(set.getFloat("dest_vel_x"),set.getFloat("dest_vel_y"),set.getFloat("dest_vel_z"));
                }
                set.close();
            }
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return portal;
    }

    private Connection getConnection(){
        return plugin.dbc.getConnection("minecarts");
    }
}
