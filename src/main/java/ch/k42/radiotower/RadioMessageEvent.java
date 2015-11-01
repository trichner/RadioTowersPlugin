package ch.k42.radiotower;
/**
 * Event for sending one Message from one Tower
 *
 * @author Thomas Richner
 * @created 07.02.14.
 * @license MIT
 */

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RadioMessageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private RadioTower tower;

    public RadioMessageEvent(RadioTower tower) {
        this.tower = tower;
    }

    public RadioTower getTower() {
        return tower;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
