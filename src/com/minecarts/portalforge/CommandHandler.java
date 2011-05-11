package com.minecarts.portalforge;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class CommandHandler implements CommandExecutor {
    protected final PortalForge plugin;

    public CommandHandler(PortalForge plugin) {
        this.plugin = plugin;
    }
    
    public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);
}
