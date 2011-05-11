package com.minecarts.portalforge.command;

import java.text.MessageFormat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.minecarts.portalforge.*;
import org.bukkit.entity.Player;

public class PortalCommand extends CommandHandler{
    
    public PortalCommand(PortalForge plugin){
        super(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //TODO Handle the subcommand
        if(sender instanceof Player){
            Player p = (Player) sender;
            if(plugin.activePortalDesigns.containsKey(p.getName())){
                p.sendMessage("You already are editing or creating a portal. Type /portal done when finished.");
                return true;
            }
            //Else, they are creating a new portal
            int portalId = plugin.dbHelper.createPortal();
            plugin.activePortalDesigns.put(p.getName(), portalId);
            p.sendMessage(MessageFormat.format("Created portal #{0}. Place the portal blocks for this portal.",portalId));
        } else {
            //Command line stuff?
        }
        return true;
    }
}
