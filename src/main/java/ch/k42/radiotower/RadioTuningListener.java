package ch.k42.radiotower;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableSet;

//import sun.net.www.content.text.plain;

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
        }else{
            if(receivers.containsKey(player)){
                plugin.getLogger().info("Removing listener");
                RadioMessageEvent.getHandlerList().unregister(receivers.remove(player));
            }
        }
    }

    private static final Set<Action> RIGHT_CLICK_ACTIONS = ImmutableSet.of(Action.RIGHT_CLICK_AIR,Action.RIGHT_CLICK_BLOCK);

    @EventHandler
    public void rightClick(PlayerInteractEvent event){
        if(!(RIGHT_CLICK_ACTIONS.contains(event.getAction()))) return; // only rightclick

        if(Minions.isNamedRadio(event.getItem(),LOREITEMRADIO)){ // yes, clicked in the air with radio
            plugin.getLogger().finest("Radio interact event");
            Player player = event.getPlayer();
            if(!receivers.containsKey(player)) return;
            RadioListener receiver = receivers.get(player);

            int index = receiver.getRadioNr()+1; // increment index
            List<RadioTower> towers = plugin.getRadioTowerManager().getTowers();
            int size = towers.size();
            if(size==0){
                player.sendMessage("No signal found.");
                return; // no towers
            }
            if(index>=size) index =0; // index out of bounds?

            RadioTower tower;

            for(int i =0;i<size;i++){
                tower = towers.get((index+i)%size);
                if(0<tower.getReceptionPower(player.getLocation())){
                    player.sendMessage("Found signal on " + tower.getFrequencyString() +", tuning radio");
                    player.setCompassTarget(tower.getLocation());
                    receiver.setRadioNr((index+i)%size);
                    return;
                }
            }
            player.sendMessage("No signal found.");
        }
    }
}
