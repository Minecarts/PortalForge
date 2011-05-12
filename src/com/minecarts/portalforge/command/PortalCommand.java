package com.minecarts.portalforge.command;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;

import com.minecarts.portalforge.*;
import com.minecarts.portalforge.portal.Portal;
import org.bukkit.entity.Player;

public class PortalCommand extends CommandHandler{
    
    public PortalCommand(PortalForge plugin){
        super(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //TODO Handle the subcommand
        if(sender instanceof Player){
            if(!sender.isOp()) return true;
            if(args.length == 0) return false;
            Player p = (Player) sender;

            //Create a new portal
            if(args[0].equalsIgnoreCase("create")){
                if(plugin.activePortalDesigns.containsKey(p.getName())){
                    p.sendMessage("You already are editing or creating a portal. Type /portal done when finished.");
                    return true;
                }
                //Else, they are creating a new portal
                int portalId = plugin.dbHelper.createPortal();
                plugin.activePortalDesigns.put(p.getName(), portalId);
                p.sendMessage(MessageFormat.format("Created portal #{0}. Place the portal blocks for this portal.",portalId));
                plugin.previousInventory.put(p.getName(), p.getItemInHand());
                p.setItemInHand(new ItemStack(Material.PORTAL,64));
                return true;
            }
            
            //Set a portal destination
            if(args[0].equalsIgnoreCase("dest") || args[0].equalsIgnoreCase("exit")){
                if(plugin.activePortalDesigns.containsKey(p.getName())){
                    plugin.dbHelper.setPortalDestination(p.getLocation(),plugin.activePortalDesigns.get(p.getName()));
                    p.sendMessage("Portal exit location set to your current position.");
                    return true;
                }
            }
          //Set a portal velocity
            if(args[0].equalsIgnoreCase("vel") || args[0].equalsIgnoreCase("velocity")){
                if(plugin.activePortalDesigns.containsKey(p.getName())){
                    if(args.length == 2){
                        plugin.dbHelper.setPortalVelocity(plugin.activePortalDesigns.get(p.getName()), Float.parseFloat(args[1]));
                        p.sendMessage("Portal velocity set to: " + args[1]);
                        return true;
                    } else {
                        p.sendMessage("/portal velocity <speed>");
                        return true;
                    }
                } else {
                    p.sendMessage("You must be editing a portal to set it's velocity!");
                }
                return false;
            }
            
            //Edit an existing portal
            if(args[0].equalsIgnoreCase("edit")){
                if(args.length == 2){
                    plugin.activePortalDesigns.put(p.getName(), Integer.parseInt(args[1]));
                    p.sendMessage("Now editing portal #" + args[1]);
                    return true;
                } else {
                    p.sendMessage("/portal edit #ID");
                    return true;
                }
            }
            
            //Done editing
            if(args[0].equalsIgnoreCase("done") || args[0].equalsIgnoreCase("save")){
                if(plugin.activePortalDesigns.containsKey(p.getName())){
                    int portalId = plugin.activePortalDesigns.remove(p.getName());
                    p.sendMessage(MessageFormat.format("Portal #{0} editing complete",portalId));
                    if(p.getItemInHand().getType() == Material.PORTAL){
                        if(plugin.previousInventory.containsKey(p.getName())){
                            p.setItemInHand(plugin.previousInventory.get(p.getName()));
                            plugin.previousInventory.remove(p.getName());
                        }
                    }
                    return true;
                } else {
                    p.sendMessage("You are not currently creating or editing a portal");
                    return true;
                }
            }
          //Debug output when using portals
            if(args[0].equalsIgnoreCase("debug")){
                if(plugin.debuggingPortals.contains(p.getName())){
                    plugin.debuggingPortals.remove(p.getName());
                    p.sendMessage("You are no longer debugging portals");
                    return true;
                } else {
                    plugin.debuggingPortals.add(p.getName());
                    p.sendMessage("Now you're debugging with portals");
                    return true;
                }
            }
            
            if(args[0].equalsIgnoreCase("info")){
                if(args.length == 2){
                    ArrayList<Block> blocks = plugin.dbHelper.getPortalBlocksFromId(Integer.parseInt(args[1]));
                    p.sendMessage("Blocks for portal #" + args[1] + ":");
                    int i = 0;
                    if(blocks.size() == 0){
                        p.sendMessage("There are no blocks attached to that portal.");
                    }
                    for(Block b : blocks){
                        p.sendMessage(MessageFormat.format("{0}: ({1,number,#.##}, {2,number,#.##}, {3,number,#.##})", ++i, b.getX(), b.getY(), b.getZ()));
                    }
                    return true;
                } else {
                    p.sendMessage("/portal info #ID");
                    return true;
                }
            }
        } else {
            //Command line stuff?
        }
        return false;
    }
}
