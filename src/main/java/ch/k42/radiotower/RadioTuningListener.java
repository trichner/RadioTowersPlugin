package ch.k42.radiotower;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by Thomas on 07.02.14.
 */
public class RadioTuningListener implements Listener{

    private final String LOREITEMRADIO;
    private final RadioTowerPlugin plugin;
    private Map<Player,RadioListener> receivers = new ConcurrentHashMap<Player, RadioListener>();

    public RadioTuningListener(String LOREITEMRADIO, RadioTowerPlugin plugin) {
        this.LOREITEMRADIO = LOREITEMRADIO;
        this.plugin = plugin;
    }

    @EventHandler
    public void itemChange(PlayerItemHeldEvent event){
        int slot = event.getNewSlot();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(slot);

        if(Minions.isNamedRadio(item, LOREITEMRADIO)){
            if(receivers.containsKey(player)) return; // already listening
            plugin.getLogger().info("Adding listener");
            RadioListener l = new RadioListener(LOREITEMRADIO, event.getPlayer(),plugin);
            receivers.put(player,l);
            Bukkit.getPluginManager().registerEvents(l, plugin);
            return;
        }else if(receivers.containsKey(player)){
            plugin.getLogger().info("Removing listener");
            RadioMessageEvent.getHandlerList().unregister(receivers.remove(player));
        }
    }

    private static final Set<Action> RIGHT_CLICK_ACTIONS = ImmutableSet.of(Action.RIGHT_CLICK_AIR,Action.RIGHT_CLICK_BLOCK);

    @EventHandler
    public void rightClick(PlayerInteractEvent event){
        if(isRightClickRadio(event)){ // yes, clicked with radio
            plugin.getLogger().finest("Radio interact event");
            Player player = event.getPlayer();
            if(!receivers.containsKey(player)) return;
            RadioListener receiver = receivers.get(player);

            CircularList<RadioTower> towers = plugin.getRadioTowerManager().getTowers();

            RadioTower tower;
            int size = towers.size();
            for(int i =0;i<size;i++){
                tower = receiver.tuneNext();
                if(0<tower.getReceptionPower(player.getLocation())){
                    player.sendMessage("Found signal on " + tower.getFrequencyString() +", tuning radio");
                    player.setCompassTarget(tower.getLocation());
                    return;
                }
            }
            player.sendMessage("No signal found.");
        }
    }

    private boolean isRightClickRadio(PlayerInteractEvent event){
        return RIGHT_CLICK_ACTIONS.contains(event.getAction()) && Minions.isNamedRadio(event.getItem(),LOREITEMRADIO);
    }
}
