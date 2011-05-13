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
            if(!sender.isOp()) return true; //OpOnly(tm)!
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
                plugin.log(MessageFormat.format("{0} created portal #{1}",p.getName(),portalId));
                plugin.previousInventory.put(p.getName(), p.getItemInHand());
                p.setItemInHand(new ItemStack(Material.PORTAL,64));
                return true;
            }

            //Set a portal destination
            if(args[0].equalsIgnoreCase("dest") || args[0].equalsIgnoreCase("exit")){
                if(plugin.activePortalDesigns.containsKey(p.getName())){
                    int portalId = plugin.activePortalDesigns.get(p.getName());
                    plugin.dbHelper.setPortalDestination(p.getLocation(),portalId);
                    plugin.log(MessageFormat.format("{0} set destination of #{1} to ({2,number,#.##},{3,number,#.##},{4,number,#.##})",
                            p.getName(),
                            portalId,
                            p.getLocation().getX(),
                            p.getLocation().getY(),
                            p.getLocation().getZ()
                    ));
                    p.sendMessage("Portal #"+portalId+" exit location set to your current position.");
                } else {
                    p.sendMessage("You must be editing a portal to set the exit point");
                }
                return true;
            }

            //Set a portal velocity
            if(args[0].equalsIgnoreCase("vel") || args[0].equalsIgnoreCase("velocity")){
                if(plugin.activePortalDesigns.containsKey(p.getName())){
                    if(args.length == 2){
                        int portalId = plugin.activePortalDesigns.get(p.getName());
                        plugin.dbHelper.setPortalVelocity(portalId, Float.parseFloat(args[1]));
                        plugin.log(MessageFormat.format("{0} set velocity of Portal #{1} to {2}", p.getName(),portalId,args[1]));
                        p.sendMessage("Portal #"+portalId+" velocity set to: " + args[1]);
                    } else {
                        p.sendMessage("/portal velocity <speed>");
                    }
                } else {
                    p.sendMessage("You must be editing a portal to set it's velocity!");
                }
                return true;
            }

            //Edit an existing portal
            if(args[0].equalsIgnoreCase("edit")){
                if(args.length == 2){
                    plugin.activePortalDesigns.put(p.getName(), Integer.parseInt(args[1]));
                    p.sendMessage("Now editing portal #" + args[1]);
                    plugin.log(p.getName() + " started editing Portal #" + args[1]);
                } else {
                    p.sendMessage("/portal edit #ID");
                }
                return true;
            }

            //Done editing
            if(args[0].equalsIgnoreCase("done") || args[0].equalsIgnoreCase("save")){
                if(plugin.activePortalDesigns.containsKey(p.getName())){
                    int portalId = plugin.activePortalDesigns.remove(p.getName());
                    plugin.log(p.getName() + " finished editing Portal #"+portalId);
                    p.sendMessage(MessageFormat.format("Portal #{0} editing complete",portalId));
                    plugin.cache.clearByPortal(portalId); //Clear the cache because we made changes
                    if(p.getItemInHand().getType() == Material.PORTAL){
                        if(plugin.previousInventory.containsKey(p.getName())){
                            p.setItemInHand(plugin.previousInventory.get(p.getName()));
                            plugin.previousInventory.remove(p.getName());
                        }
                    }
                } else {
                    p.sendMessage("You are not currently creating or editing a portal");
                }
                return true;
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

            //All the portal blocks that make up a portal
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
                } else {
                    p.sendMessage("/portal info #ID");
                }
                return true;
            }

            if(args[0].equalsIgnoreCase("clear")){
                if(args.length == 2){
                    plugin.cache.clearByPortal(Integer.parseInt(args[1]));
                    p.sendMessage("Cache cleared for Portal #" + args[1]);
                    plugin.log("Cache cleared for Portal #" + args[1]);
                } else {
                    p.sendMessage("/portal clear #ID");
                }
                return true;
            }
        } else {
            //Command line stuff?
        }
        return false;
    }
}
