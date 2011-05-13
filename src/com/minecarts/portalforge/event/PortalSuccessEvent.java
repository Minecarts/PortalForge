package com.minecarts.portalforge.event;

import com.minecarts.portalforge.portal.Portal;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PortalSuccessEvent extends Event implements Cancellable {
    private Entity entity;
    private Portal portal;
    private boolean cancel = false;

    public PortalSuccessEvent(Entity entity, Portal portal) {
      super("PortalSuccessEvent");
      this.entity = entity;
      this.portal = portal;
    }

    public boolean isCancelled() {
      return this.cancel;
    }

    public void setCancelled(boolean cancel) {
      this.cancel = cancel;
    }

    public Entity getEntity() {
      return this.entity;
    }

    public Portal getPortal() {
      return this.portal;
    }
}