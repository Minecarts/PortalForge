package com.minecarts.portalforge.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.text.MessageFormat;

import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.Portal;
import com.minecarts.portalforge.portal.PortalType;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class HelperDB {
    private PortalForge plugin; 
    public HelperDB(PortalForge plugin){
        this.plugin = plugin;
    }
    public int createPortal(){
        int lastInsertedId = -1;
        try{
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO portals(`type`) VALUES (?)",PreparedStatement.RETURN_GENERATED_KEYS);
            if(ps == null){ //Query failed
                conn.close();
                plugin.log.warning("Insert Portal query failed");
            }
            ps.setString(1, "GENERIC");

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
            PreparedStatement ps = conn.prepareStatement("INSERT INTO portal_blocks(portal_id,world,x,y,z) VALUES (?,?,?,?,?)");
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
    
    public Portal getPortalFromBlockLocation(Location location){
        Portal portal = null;
        try{
            System.out.println(location);
            Connection conn = this.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT `type`,`dest_world`,`dest_x`,`dest_y`,`dest_z`,`dest_pitch`,`dest_yaw` FROM `portals`,`portal_blocks` WHERE `portal_blocks`.`portal_id` = `portals`.`id` AND `portal_blocks`.`world` = ? AND `portal_blocks`.`x` = ? AND `portal_blocks`.`y` = ? AND `portal_blocks`.`z` = ? LIMIT 1");
            if(ps == null){
                plugin.log.warning("GetPortalEndPointFromBlock query failed");
                conn.close();
                return portal;
            }
            ps.setString(1, location.getWorld().getName());
            ps.setInt(2, location.getBlockX());
            ps.setInt(3, location.getBlockY());
            ps.setInt(4, location.getBlockZ());
            System.out.println(MessageFormat.format("{0},{1},{2},{3}",location.getWorld(),location.getBlockX(),location.getBlockY(),location.getBlockZ()));
            ResultSet set = ps.executeQuery();
            if (set.next()) {
                if(Bukkit.getServer().getWorld(set.getString("dest_world")) != null){
                    portal = new Portal();
                    portal.endPoint = new Location(Bukkit.getServer().getWorld(set.getString("dest_world")),set.getInt("dest_x"),set.getInt("dest_y"),set.getInt("dest_z"),set.getFloat("dest_yaw"),set.getFloat("dest_pitch"));
                    portal.type = PortalType.valueOf(set.getString("type")); 
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
