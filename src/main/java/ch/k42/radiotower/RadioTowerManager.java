package ch.k42.radiotower;
/**
 * Event for sending one Message from one Tower
 *
 * @author Thomas Richner
 * @created 07.02.14.
 * @license MIT
 */
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.RedstoneTorch;
import org.bukkit.material.Sign;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/**
 * Created by Thomas on 07.02.14.
 */
public class RadioTowerManager implements Listener, Runnable {

    private ConcurrentNavigableMap<Integer, RadioTower> towers = new ConcurrentSkipListMap<>();
    private Plugin plugin;

    public RadioTowerManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerTower(Location location) {
        if (RadioTower.validate(location)) {
            RadioTower tower = new RadioTower(location);
            towers.put(tower.getFrequency(), tower);
            plugin.getLogger().fine("tower registered");
        }
    }

    public Set<Location> getTowerLocations() {
        return towers.values().stream().map(t -> t.getLocation()).collect(Collectors.<Location>toSet());
    }

    public final ConcurrentNavigableMap<Integer, RadioTower> getTowers() {
        return towers;
    }

    private static final int MAX_BLOCKCHECK = 10;
    private static final Material BASE_BLOCK = Material.OBSIDIAN;

    @EventHandler
    public void blockPlaced(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Location location = block.getLocation();
        if (block.getType().equals(BASE_BLOCK)) {
            registerTower(location);
        } else if (block.getType().equals(Material.IRON_FENCE)) {
            for (int i = 0; i < MAX_BLOCKCHECK; i++) {
                location.add(0, -1, 0);
                if (!location.getBlock().getType().equals(Material.IRON_FENCE)) {
                    break;
                }
            }
            if (location.getBlock().getType().equals(BASE_BLOCK)) {
                registerTower(location);
            }

        } else if (block.getType().equals(Material.WALL_SIGN)) {
            Sign sign = (Sign) block.getState().getData();
            if (sign.isWallSign()) {
                location.add(sign.getAttachedFace().getModX(), sign.getAttachedFace().getModY(),
                        sign.getAttachedFace().getModZ());
                if (location.getBlock().getType().equals(BASE_BLOCK)) {
                    registerTower(location);
                }
            }
        } else if (block.getType().equals(Material.REDSTONE_TORCH_ON)) {
            RedstoneTorch torch = (RedstoneTorch) block.getState().getData();
            location.add(torch.getAttachedFace().getModX(), torch.getAttachedFace().getModY(),
                    torch.getAttachedFace().getModZ());
            if (location.getBlock().getType().equals(BASE_BLOCK)) {
                registerTower(location);
            }
        }
    }

    @Override
    public void run() {
        plugin.getLogger().fine("Broadcasting at " + towers.size() + " towers");
        plugin.getLogger()
                .fine("Receivers: " + RadioMessageEvent.getHandlerList().getRegisteredListeners().length);

        for (RadioTower tower : towers.values()) {
            if (tower.update()) { // tower still valid?
                tower.broadcastMessage();
            } else {
                towers.remove(tower.getFrequency());
            }
        }
    }
}
