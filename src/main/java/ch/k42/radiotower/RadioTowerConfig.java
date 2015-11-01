package ch.k42.radiotower;
/**
 * Event for sending one Message from one Tower
 *
 * @author Thomas Richner
 * @created 07.02.14.
 * @license MIT
 */

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class RadioTowerConfig {
    private final Configuration config;

    private static final String SECTION_COOLDOWNS = "cooldowns";
    private static final String SECTION_LOREITEMS = "loreitems";
    private static final String SECTION_RADIOTOWER = "radiotower";

    private static final String RT_MINHEIGHT_KEY = "minimumHeight";
    private static final String RT_MAXHEIGHT_KEY = "maximumHeight";
    private static final String RT_MAXRANGE_KEY = "maximumRange";
    private static final String RT_CUTOFFRANGE_KEY = "cutoffRange";


    private static final String RADIOCOOLDOWN_KEY = "radiomessagecooldown";

    private static final String LOREITEMRADIO_KEY = "radio";

    private static final int DEFAULT_RADIOCOOLDOWN = 2000;

    public static final int DEFAULT_RT_MIN_HEIGHT = 6;
    public static final int DEFAULT_RT_MAX_HEIGHT = 50;
    public static final int DEFAULT_RT_MAX_RANGE = 10000;
    public static final double DEFAULT_RT_CUTOFFRANGE = 0.3;

    private static final String DEFAULT_LOREITEMRADIO = "Radio";

    public RadioTowerConfig(Configuration config) {
        this.config = config;
    }

    private ConfigurationSection getSectionOrDefault(String name) {
        ConfigurationSection section = config.getConfigurationSection(name);

        if (section != null)
            return section;
        else
            return config.createSection(name);
    }

    //Section with Radio settings

    /**
     * Retrieve the cooldown in milliseconds.
     *
     * @return Cooldown in milliseconds.
     */
    public int getRadioCooldown() {
        return getSectionOrDefault(SECTION_COOLDOWNS).getInt(RADIOCOOLDOWN_KEY);
    }

    //Lore settings

    /**
     * Retrieve the lore item name.
     *
     * @return Distance in milliseconds.
     */
    public String getLoreItemRadio() {
        return getSectionOrDefault(SECTION_LOREITEMS).getString(LOREITEMRADIO_KEY);
    }

    //==== Radio Tower Settings
    public int getRTMinHeight() {
        int value = getSectionOrDefault(SECTION_RADIOTOWER).getInt(RT_MINHEIGHT_KEY);

        if (value < 0) {
            value = DEFAULT_RT_MIN_HEIGHT;
        }
        return value;
    }

    public int getRTMaxHeight() {
        int value = getSectionOrDefault(SECTION_RADIOTOWER).getInt(RT_MAXHEIGHT_KEY);

        if (value < 0) {
            value = DEFAULT_RT_MAX_HEIGHT;
        }
        return value;
    }

    public int getRTMaxRange() {
        int value = getSectionOrDefault(SECTION_RADIOTOWER).getInt(RT_MAXRANGE_KEY);

        if (value < 0) {
            value = DEFAULT_RT_MAX_RANGE;
        }
        return value;
    }
}
