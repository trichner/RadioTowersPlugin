package ch.k42.radiotower;
/**
 * Event for sending one Message from one Tower
 *
 * @author Thomas Richner
 * @created 07.02.14.
 * @license MIT
 */

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.nio.ByteBuffer;
import java.util.Set;

public class RadioTower {

    public static final Material BASE_BLOCK = Material.OBSIDIAN;
    private static final int MAX_POWER = 180000; // 180kW, power of the 'Beromünster Antenna'

    private static final int N_FREQ = 100000; // 1 GHz
    private static final int D_FREQ = 10; // 10 MHz Bands
    private static final int MIN_FREQ = 1000; // 1 MHz

    private static final int ALPHA = 100; // Magic Value

    private static int MIN_HEIGHT = 6;
    private static int MAX_HEIGHT = 50;
    private static int MAX_RANGE = 10000;

    private String message;
    private final Location location;

    private int maxRange = 0;

    private double antennaGain;
    private int frequency;
    private String frequencyString;

    public static boolean setParameters(int MIN_HEIGHT, int MAX_HEIGHT, int MAX_RANGE) {
        if (MIN_HEIGHT < 1) return false;
        if (MAX_HEIGHT < MIN_HEIGHT) return false;
        if (MAX_RANGE < 0) return false;

        RadioTower.MIN_HEIGHT = MIN_HEIGHT;
        RadioTower.MAX_HEIGHT = MAX_HEIGHT;
        RadioTower.MAX_RANGE = MAX_RANGE;
        return true;
    }


    public RadioTower(Location location) {
        this.location = location;
        this.maxRange = 0;
        updateFrequency();
        if (update()) {
            location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
        }
    }

    private void updateFrequency() {
        ByteBuffer buf = ByteBuffer.allocate(3 * 4);
        buf.putInt(location.getBlockX());
        buf.putInt(location.getBlockY());
        buf.putInt(location.getBlockZ());
        frequency = Minions.cryptoHashToInt(buf.array());
        int readableFrequency = MIN_FREQ + (frequency % N_FREQ) * D_FREQ;
        this.frequencyString = Minions.frequencyToString(readableFrequency);
    }


    public void broadcastMessage() {
        if (!isActive()) return; // not active
        Bukkit.getPluginManager().callEvent(new RadioMessageEvent(this));
    }

    private boolean isActive() {
        return this.location.getBlock().getBlockPower() == 0;
    }

    private void calculateRange(int height) {
        double linFactor = ((height) / ((double) MAX_HEIGHT));
        if (linFactor < 0) linFactor = 0;

        int range = (int) (MAX_RANGE * linFactor);
        this.antennaGain = linFactor * MAX_POWER;
        if (range < 0) range = 0;

        range += 20;
        this.maxRange = range;
    }

    /**
     * Calculates the power at a radius r from the antenna.
     * This is based on the fact that an antenna farfield decreases
     * with ~1/r, alpha is a magic value
     *
     * @param r radius from antenna
     * @return power at a distance of r
     */
    private double antennaField(double r) {
        if (r > this.maxRange) return 0;
        if (r < 1) r = 1;
        return ALPHA / (r);
    }


    /**
     * Returns the received power in a physical unit (Watt)
     *
     * @param location location of the listener
     * @return reception power in Watt
     */
    public double getReceptionPower(Location location) {
        return this.antennaGain * getNormReceptionPower(location);
    }

    /**
     * Returns the received power normed between [0,1]
     *
     * @param location location of receiver
     * @return value in [0,1], depending on distance
     */
    public double getNormReceptionPower(Location location) {
        if (!this.location.getWorld().equals(location.getWorld())) return 0;
        if (this.location.getBlock().getBlockPower() != 0) return 0; // tower off?

        double distance = this.location.distance(location);
        if (distance > MAX_RANGE) return 0;
        return antennaField(distance);
    }

    public boolean update() {
        int height = verify(location);
        if (height == 0) return false;
        this.message = assembleMessage();
        calculateRange(height);
        return true;
    }

    private static final Set<Material> REDSTONE_TORCH =
            ImmutableSet.of(Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF);

    /**
     * @param location location of the base block
     * @return the height of the tower or 0 if it's not valid
     */
    public static int verify(Location location) {
        if (location == null) return 0;

        if (!location.getBlock().getType().equals(BASE_BLOCK)) // check if base is correct
            return 0;

        boolean hasRedstoneTorch = false;
        boolean hasSign = false;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (Math.abs(x) != Math.abs(z)) { // check for sign & torch
                    Location l = location.clone();
                    Block b = l.add(x, 0, z).getBlock();
                    if (REDSTONE_TORCH.contains(b.getType())) {
                        hasRedstoneTorch = true;
                    } else if (b.getType().equals(Material.WALL_SIGN)) {
                        hasSign = true;
                    }
                }
            }
        }

        if (!hasSign || !hasRedstoneTorch) return 0; // sign or torch missing

        int height = getHeight(location);

        if (height < MIN_HEIGHT) return 0; // antenna not high enough

        Location base = location.clone().add(0, height + 1, 0);
        if (!hasSunlight(base)) return 0;

        return height;
    }

    public static boolean validate(Location location) {
        return verify(location) > 0;
    }

    private static int getHeight(Location location) {
        Location base = location.clone().add(0, 1, 0); // start of the antenna
        final int WORLD_HEIGHT = location.getWorld().getMaxHeight();
        int height = 0;
        while (base.getY() < WORLD_HEIGHT) {
            if (base.getBlock().getType().equals(Material.IRON_FENCE)) {
                height++;
            } else {
                break;
            }
            base.add(0, 1, 0);
        }
        return height;
    }

    private String assembleMessage() {
        StringBuffer sb = new StringBuffer();
        // check for sign & torch
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (Math.abs(x) != Math.abs(z)) {
                    Location l = location.clone();
                    Block b = l.add(x, 0, z).getBlock();
                    if (b.getType().equals(Material.WALL_SIGN)) {
                        Sign s = (org.bukkit.block.Sign) b.getState();
                        for (String line : s.getLines()) { // read message
                            sb.append(line);
                        }
                    }
                }
            }
        }
        return sb.toString();
    }


    private static boolean hasSunlight(Location location) {
        return location.getY() >= location.getWorld().getHighestBlockYAt(location);
    }

    public String getMessageAt(Location location) {
        // is player in range?
        double reception = getNormReceptionPower(location);
        double receptionAbs = getReceptionPower(location);
        if (reception <= 0) return null;

        // assemble the chat message
        String freq = getFrequencyString();
        String power = Minions.powerToString(receptionAbs);

        StringBuffer sb = new StringBuffer(); // [22MHz 22dB]
        sb.append('[').append(freq).append(' ').append(power).append("] ")
                .append(Minions.obfuscateMessage(message, 1 - reception));
        return sb.toString();
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

    public Location getLocation() {
        return location;
    }

    public String getFrequencyString() {
        return frequencyString;
    }

    public int getFrequency() {
        return frequency;
    }
}
