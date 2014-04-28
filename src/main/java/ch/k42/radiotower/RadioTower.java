package ch.k42.radiotower;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 15.09.13
 * Time: 16:51
 * To change this template use File | Settings | File Templates.
 */
public class RadioTower{

    public static final Material BASE_BLOCK = Material.OBSIDIAN;
    private static final int MAX_POWER = 180000; // 180kW, power of the 'Beromünster Antenna'

    private static final int N_FREQ = 100000; // 1 GHz
    private static final int D_FREQ = 10; // 10 MHz Bands
    private static final int MIN_FREQ = 1000; // 1 MHz

    private static final int ALPHA = 100; // 1 MHz


    private static int MIN_HEIGHT = 6;
    private static int MAX_HEIGHT = 50;
    private static int MAX_RANGE = 10000;

    public final int WORLD_HEIGHT;

    protected String message;
    protected Plugin plugin;
    private final Location location;

    private int maxRange = 0;

    private double antennaGain;
    private int frequency;
    private String frequencyString;

    public static boolean setParameters(int MIN_HEIGHT,int MAX_HEIGHT,int MAX_RANGE) {
        if(MIN_HEIGHT<1) return false;
        if(MAX_HEIGHT<MIN_HEIGHT) return false;
        if(MAX_RANGE<0) return false;

        RadioTower.MIN_HEIGHT = MIN_HEIGHT;
        RadioTower.MAX_HEIGHT = MAX_HEIGHT;
        RadioTower.MAX_RANGE = MAX_RANGE;
        return true;
    }


    public RadioTower(Plugin plugin, Location location) {
        this.location = location;
        this.WORLD_HEIGHT = location.getWorld().getMaxHeight();
        this.plugin = plugin;
        this.maxRange = 0;

        updateFrequency();

        if(update()){
            location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES,0);
        }
    }

    private static final int M32 = 0xFFFFFFE0;
    private void updateFrequency(){
        int hash = ((location.getBlockX() & M32)*31 + (location.getBlockY() & M32))* 53 + (location.getBlockZ() & M32);
        hash = Minions.cryptoHashToInt(hash);
        this.frequency = MIN_FREQ+(hash%N_FREQ)*D_FREQ;
        this.frequencyString = Minions.frequencyToString(frequency);
    }


    public void broadcastMessage() {
        if(!isActive()) return; // not active
        Bukkit.getPluginManager().callEvent(new RadioMessageEvent(message,this));
    }

    private boolean isActive(){
        return this.location.getBlock().getBlockPower()==0;
    }

    private void calculateRange(int height) {
        double linFactor = ((height)/( (double) MAX_HEIGHT));
        if(linFactor<0) linFactor=0;

        int range = (int) (MAX_RANGE*linFactor);
        this.antennaGain = linFactor*MAX_POWER;
        if(range<0) range=0;

        range += 20;
        this.maxRange = range;
    }


    private double invPowerLaw(double r){
        if(r>this.maxRange) return 0;
        if(r<1) r=1;
        return ALPHA/(r);
        //return this.alphaSQ*(r-this.maxRange)*(r-this.maxRange); // p(r)=a*(r-R)^2
    }


    public double getReceptionPower(Location location) {
        if(!this.location.getWorld().equals(location.getWorld())) return 0;
        if(this.location.getBlock().getBlockPower()!=0) return 0; // tower off?

        double distance =this.location.distance(location);
        if(distance>MAX_RANGE) return 0;
        return this.antennaGain* invPowerLaw(distance);
    }

    public double getNormReceptionPower(Location location) {
        if(!this.location.getWorld().equals(location.getWorld())) return 0;
        if(this.location.getBlock().getBlockPower()!=0) return 0; // tower off?

        double distance =this.location.distance(location);
        if(distance>MAX_RANGE) return 0;
        return invPowerLaw(distance);
    }

    public boolean update() {
        if(!this.location.getBlock().getType().equals(BASE_BLOCK)) // check if base is correct
            return false;

        boolean hasRedstoneTorch = false;
        boolean hasSign = false;
        for(int x=-1;x<=1;x++){
            for(int z=-1;z<=1;z++){
                if(Math.abs(x)!=Math.abs(z)){ // check for sign & torch
                    Location l = location.clone();
                    Block b = l.add(x,0,z).getBlock();
                    if(b.getType().equals(Material.REDSTONE_TORCH_ON)||b.getType().equals(Material.REDSTONE_TORCH_OFF)){
                        hasRedstoneTorch = true;
                    }else if(b.getType().equals(Material.WALL_SIGN)){
                        hasSign = true;
                        org.bukkit.block.Sign s = (org.bukkit.block.Sign) b.getState();
                        StringBuffer sb = new StringBuffer();
                        for(String line : s.getLines()){ // read message
                            sb.append(line);
                        }
                        this.message = sb.toString();
                    }
                }
            }
        }
        if(!hasSign || !hasRedstoneTorch) return false; // sign or torch missing

        Location base = location.clone().add(0,1,0); // start of the antenna
        int height = 0;
        for(int i=0;i<MAX_HEIGHT && base.getY()<WORLD_HEIGHT;i++){
            if(base.getBlock().getType().equals(Material.IRON_FENCE)){
                height++;
            }else {
                break;
            }
            base.add(0,1,0);
        }
        if(height<MIN_HEIGHT) return false; // antenna not high enough


        // force air above (sunlight)
        while (base.getBlock().getType().equals(Material.AIR)){
            base.add(0,1,0);
        }

        if(base.getY()!=WORLD_HEIGHT) return false; // no sunlight

        calculateRange(height);
        return true;
    }

    @Override
    public String toString() {
        return location + " : " + message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RadioTower tower = (RadioTower) o;

        if (!location.equals(tower.location)) return false;
        if (message != null ? !message.equals(tower.message) : tower.message != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    public static int getMaxPower() {
        return MAX_POWER;
    }

    public static int getMAX_RANGE() {
        return MAX_RANGE;
    }

    public int getFrequency() {
        return frequency;
    }

    public double getAntennaGain() {
        return antennaGain;
    }

    public Location getLocation() {
        return location;
    }

    public String getFrequencyString() {
        return frequencyString;
    }
}
