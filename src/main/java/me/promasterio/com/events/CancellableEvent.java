package me.promasterio.com.events;

import net.minestom.server.event.Event;

public abstract class CancellableEvent implements Event {
    private boolean cancelled = false;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
