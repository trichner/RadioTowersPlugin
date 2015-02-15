package ch.k42.radiotower;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public final class RadioListener implements Listener{

    private String LOREITEMRADIO;
    private Player player;
    private RadioTowerPlugin plugin;

    // TODO Reference should not be number but tower!
    private RadioTower myTower = null;

    public RadioListener(String LOREITEMRADIO, Player player, RadioTowerPlugin plugin) {
        this.LOREITEMRADIO = LOREITEMRADIO;
        this.player = player;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void receiveMessage(RadioMessageEvent event){
        if(hasRadioInHand(player)){

            CircularList<RadioTower> towers = plugin.getRadioTowerManager().getTowers();
            // maybe towers changed
            if(myTower==null || (!towers.contains(myTower))){
                myTower = towers.head().value();
            }

            if(event.getTower()!=myTower) return; // are we tuned to the right radio?

            String msg = event.getMessageAt(player.getLocation());
            if(msg!=null){
                player.sendMessage(msg); // display message, obfuscate if needed
            }
        }else {
            RadioMessageEvent.getHandlerList().unregister(this); // we no longer have a radio in hand, no need to listen further
        }
    }

    private boolean hasRadioInHand(Player player){
        ItemStack item = player.getItemInHand();
        return Minions.isNamedRadio(item,LOREITEMRADIO);
    }

    public RadioTower tuneNext() {
        myTower = plugin.getRadioTowerManager().getTowers().next(myTower);
        return myTower;
    }
}
