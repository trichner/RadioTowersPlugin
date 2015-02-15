package ch.k42.radiotower;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Thomas on 07.02.14.
 */
public class RadioMessageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private String message;
    private RadioTower tower;

    public RadioMessageEvent(String message, RadioTower tower) {
        this.message = message;
        this.tower = tower;
    }

    /**
     *
     * @param location the location of the receiver
     * @return the broadcasted message or null if none reveived
     */
    public String getMessageAt(Location location) {
        // is player in range?
        double reception = tower.getNormReceptionPower(location);
        double receptionAbs = tower.getReceptionPower(location);
        if(reception<=0) return null;

        // assemble the chat message
        String freq = tower.getFrequencyString();
        String power = Minions.powerToString(receptionAbs);

        StringBuffer sb = new StringBuffer(); // [22MHz 22dB]
        sb.append('[').append(freq).append(' ').append(power).append("] ").append(Minions.obfuscateMessage(message, 1 - reception));
        return sb.toString();
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
