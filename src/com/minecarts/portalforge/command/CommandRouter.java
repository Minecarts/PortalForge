package com.minecarts.portalforge.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.minecarts.portalforge.*;
import org.bukkit.entity.Player;


public class CommandRouter extends CommandHandler{
    public CommandRouter(PortalForge plugin){
        super(plugin);
    }

    private enum Commands{
        CREATE,
        DONE,
        EXIT,
        EDIT,
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) return false;
        if(!(sender.hasPermission("portalforge.admin"))) return true;

        //Unfortunately most of our commands require you to be in game to issue them... we may want to consider to change this
        //  in the future, but for now... lets assume they're a player. Since most of our historial portal editing is done
        //  in game and creating a new portal requires you to be in game

        if(!(sender instanceof Player)){
            sender.sendMessage("You must be a player in game to issue portal commands");
            return true;
        }

        Player player = (Player)sender;

        switch(Commands.valueOf(args[0].toUpperCase())){
            case CREATE:
                return PortalCreate.handleCommand(plugin,player,args);
            case DONE:
                return PortalDone.handleCommand(plugin,player,args);
            case EXIT:
                return PortalExit.handleCommand(plugin,player,args);
            case EDIT:
                return PortalEdit.handleCommand(plugin,player,args);
            default: //This won't ever be reached unless the command is in theCommands enum above
                return false;
        }
    }
}
