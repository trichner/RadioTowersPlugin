package ch.k42.radiotower;
/**
 * Event for sending one Message from one Tower
 *
 * @author Thomas Richner
 * @created 07.02.14.
 * @license MIT
 */

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collector;

public final class RadioListener implements Listener {

    private String LOREITEMRADIO;
    private Player player;
    private RadioTowerPlugin plugin;

    private int mFrequency = 0;

    public RadioListener(String LOREITEMRADIO, Player player, RadioTowerPlugin plugin) {
        this.LOREITEMRADIO = LOREITEMRADIO;
        this.player = player;
        this.plugin = plugin;
    }

    private boolean broadcastMessage(RadioTower tower) {
        if (tower == null || mFrequency != tower.getFrequency()) return false;
        String msg = tower.getMessageAt(player.getLocation());
        if (msg == null) return false;
        player.sendMessage(msg); // display message, obfuscate if needed
        return true;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void receiveMessage(RadioMessageEvent event) {
        if (hasRadioInHand(player)) {
            broadcastMessage(event.getTower());
        } else {
            RadioMessageEvent.getHandlerList()
                    .unregister(this); // we no longer have a radio in hand, no need to listen further
        }
    }

    private boolean hasRadioInHand(Player player) {
        ItemStack item = player.getItemInHand();
        return Minions.isNamedRadio(item, LOREITEMRADIO);
    }

    public void tuneNext() {
        NavigableMap<Integer, RadioTower> towers = plugin.getRadioTowerManager().getTowers();

        Collector<RadioTower, NavigableMap<Integer, RadioTower>, NavigableMap<Integer, RadioTower>>
                nmCollector = Collector.of(
                ConcurrentSkipListMap<Integer, RadioTower>::new, (a, t) -> a.put(t.getFrequency(), t),
                (l, r) -> {
                    l.putAll(r);
                    return l;
                });

        NavigableMap<Integer, RadioTower> available =
                towers.values().stream().filter(t -> 0 < t.getReceptionPower(player.getLocation()))
                        .collect(nmCollector);

        Integer newFreq = null;
        if (available.size() > 0) {
            newFreq = towers.higherKey(mFrequency);
            if (newFreq == null) {
                Entry<Integer, RadioTower> e = towers.firstEntry();
                if (e == null) {
                    newFreq = null;
                } else {
                    newFreq = e.getKey();
                }
            }
        }

        if (newFreq == null) {
            player.sendMessage("No signal found.");
        } else {
            mFrequency = newFreq.intValue();
            RadioTower tower = towers.get(mFrequency);
            player.sendMessage("Found signal on " + tower.getFrequencyString() + ", tuning radio");
            player.setCompassTarget(tower.getLocation());
        }
    }
}
