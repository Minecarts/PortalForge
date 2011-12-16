package com.minecarts.portalforge.listener;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import com.minecarts.portalforge.PortalForge;

public class BlockListener extends org.bukkit.event.block.BlockListener{
    private PortalForge plugin;
    public BlockListener(PortalForge plugin){
        this.plugin = plugin;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent e){

    }

    @Override
    public void onBlockPhysics(BlockPhysicsEvent e){

    }


    @Override
    public void onBlockFromTo(BlockFromToEvent e){

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e){


    }
}
