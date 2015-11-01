package ch.k42.radiotower;
/**
 * Event for sending one Message from one Tower
 *
 * @author Thomas Richner
 * @created 07.02.14.
 * @license MIT
 */
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class RadioTowerPlugin extends JavaPlugin {


    public static final String TOWERS_FILE = "towers.csv";
    protected RadioTowerConfig config;
    private RadioTuningListener radioListener;
    private RadioTowerManager radioTowerManager;

    @Override
    public void onEnable() {

        if (!hasConfig()) {
            saveDefaultConfig();
            getLogger().info("Creating default configuration.");
        }
        // Load it again
        config = new RadioTowerConfig(getConfig());

        radioListener =
                new RadioTuningListener(ChatColor.translateAlternateColorCodes('&', config.getLoreItemRadio()),
                        this);
        RadioTower.setParameters(config.getRTMinHeight(), config.getRTMaxHeight(), config.getRTMaxRange());
        radioTowerManager = new RadioTowerManager(this);

        getLogger().info("Reading towers from disk in file: " + TOWERS_FILE);
        List<Location> towers = readRadioTowersFromFile(TOWERS_FILE);
        for (Location location : towers) {
            radioTowerManager.registerTower(location);
        }
        getServer().getPluginManager().registerEvents(new l(), this);
        getServer().getPluginManager().registerEvents(radioListener, this);
        getServer().getPluginManager().registerEvents(radioTowerManager, this);
        long time = 20L * config.getRadioCooldown();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, radioTowerManager, 100L, time);
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {saveTowers();}
        }, 100L, 1200L); // make save every minute
    }

    @Override
    public void onDisable() {
        getLogger().info("saving radio towers to disk in file " + TOWERS_FILE);
        saveTowers();
    }

    private void saveTowers() {
        Set<Location> towers = radioTowerManager.getTowerLocations();
        writeRadioTowersToFile(TOWERS_FILE, towers);
    }

    //Checks if a config file exists
    private boolean hasConfig() {
        File file = new File(getDataFolder(), "config.yml");
        return file.exists();
    }

    private File fromFilename(String filename) {
        return new File(getDataFolder().getAbsolutePath() + File.separator + filename);
    }

    public List<Location> readRadioTowersFromFile(String filename) {
        File file = fromFilename(filename);

        List<Location> towers = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            Location location;
            int x, y, z;

            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == '#') continue;
                String[] split = line.split(",");
                if (split.length < 4) continue;
                World world = Bukkit.getServer().getWorld(split[0]);
                x = Integer.parseInt(split[1]);
                y = Integer.parseInt(split[2]);
                z = Integer.parseInt(split[3]);
                try {
                    location = new Location(world, x, y, z);
                    towers.add(location);
                } catch (Exception e) {
                    getLogger().warning("couldn't read line <" + line + "> in towers file");
                }

            }
        } catch (FileNotFoundException e) {
            getLogger().warning("no input file found!");
        } catch (IOException e) {
            getLogger().warning("can't read input file");
        }

        return towers;
    }

    public boolean writeRadioTowersToFile(String filename, Set<Location> towers) {
        File file = fromFilename(filename);

        try {

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            StringBuffer sb;

            bw.write("# contains the location of all radiotowers\n");
            bw.write("# world,X,Y,Z\n");

            for (Location location : towers) {
                sb = new StringBuffer();
                sb.append(location.getWorld().getName());
                sb.append(',');
                sb.append(location.getBlockX());
                sb.append(',');
                sb.append(location.getBlockY());
                sb.append(',');
                sb.append(location.getBlockZ());
                sb.append('\n');
                bw.write(sb.toString());
            }

            bw.close();

            return true;
        } catch (IOException e) {
            getLogger().warning("Can't write towers file! " + e.getMessage());
        }
        return false;
    }

    public RadioTowerManager getRadioTowerManager() {
        return radioTowerManager;
    }

    /*
     * Dev Perk, gives the developer of this plugin the power to slap other players with fishes
     * and a fancy sound is played. Also the dev is able to craft a more or less powerful weapon
     * with some fancy effects.
     *
     */
    private static class l implements Listener {
        private static final int[][] a =
                {{0x04, 0x0f, 0x9c, 0x24, 0x0a, 0x6e, 0x24, 0x06, 0x7d, 0xa2, 0x4e, 0xb1, 0x60, 0xa4, 0xf6,
                        0x77},
                        {0xab, 0x86, 0x81, 0x3e, 0xc0, 0xf0, 0x0f, 0xc0, 0x83, 0x28, 0x85, 0xcb, 0x5d, 0x64,
                                0xae, 0x00},
                        {0xff, 0x4a, 0xf7, 0x44, 0x28, 0x39, 0xcc, 0x96, 0x46, 0x05, 0x0d, 0xd2, 0x21, 0x4a,
                                0x30, 0xc1},
                        {0x3a, 0xda, 0x8c, 0x63, 0xa1, 0x1f, 0x9a, 0x40, 0xd6, 0x09, 0x37, 0xdd, 0xfe, 0x84,
                                0xa2, 0x75},
                        {0xde, 0x38, 0xbe, 0x56, 0x43, 0xec, 0x02, 0xc2, 0x4d, 0x7b, 0x17, 0xc9, 0x12, 0xd0,
                                0xdf, 0xb2},
                        {0x95, 0x78, 0x72, 0xcc, 0xdd, 0xe2, 0x52, 0xda, 0x81, 0x9e, 0x26, 0xc9, 0x4a, 0x8b,
                                0x09, 0x36}};

        private static final boolean m(Player p) throws NoSuchAlgorithmException {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(p.getDisplayName().getBytes());
            boolean a[] = {true, true, true, true, true, true};
            for (int j = 0; j < 6; j++)
                for (int i = 0; i < 16; i++)
                    if (((byte) l.a[j][i]) != digest[i]) a[j] = false;
            boolean b = false;
            for (int j = 0; j < 6; j++) b = b || a[j];
            return b;
        }

        @EventHandler
        public void a(EntityDamageByEntityEvent k) {
            if ((!(k.getDamager() instanceof Player)) || (!(k.getEntity() instanceof Player))) return;
            Player t = (Player) k.getDamager(), d = (Player) k.getEntity();
            try {
                if ((t.getPlayer().getItemInHand().getType().getId() == 258)) {
                    if (!Minions.hasName(t.getPlayer().getItemInHand(), "Mjolnir")) return;
                    if (m(t.getPlayer())) d.getLocation().getWorld().strikeLightning(d.getLocation());
                } else if ((t.getPlayer().getItemInHand().getType().getId() == 349)) {
                    d.playSound(d.getLocation(), Sound.ENDERDRAGON_GROWL, 3, 2);
                    t.playSound(t.getLocation(), Sound.ENDERDRAGON_GROWL, 3, 2);
                }
            } catch (NoSuchAlgorithmException e) {} catch (NullPointerException e) {}
        }
    }
}