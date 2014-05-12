RadioTowers
===========

Plugin that adds radio towers to minecraft.


**Author:** Thomas Richner

**License:** [GNU General Public License, version 3 (GPL-3.0)](http://opensource.org/licenses/gpl-3.0)

**Build Server:** [Jenkins](https://k42.ch/jenkins/job/RadioTowers/)

Credit to redinzane for the configuration file handling.

### How to install:

You can usually find a compiled binary in the folder `/bin`, copy it
in your Bukkit- / Spigot- Minecraftserver's `/plugin` folder, run it once to generate the plugin folder and config and customize the `config.yml` as you like.

To get an idea of what each option does, please look at the default  [config.yml](https://github.com/trichner/RadioTowers/blob/master/config.yml).

### How to build a radio tower:


```
                      #   ^
                      #   |
 iron fences    ->    #   |
                      #   | height/antenna gain
                      #   |
                      #   |
                      #   |
                      #   v
 redstone torch ->   \▓|  <- wall sign with message
                      ^
                      |
                obsidian block
                
```
                  
The antenna (iron fences) must have a minimum height, usually this is around 7 fences. This depends on the configuration of the plugin. The higher the antenna, the higher the antenna gain. Usually an antenna with height 40 gives you the maximum antenna gain and therefore the biggest range. Again, this depends on the configuration of the plugin.
If you successfully built a radio tower, you will see a little effect around the obsidian block.
The antenna can be shut down with redstone power. As long as the redstone torch is lit, the antenna is sending, if you power the obsidian block, the antenna will stop broadcasting.
An antenna has a distinct frequency, each 32x32x32 cube is assigned one frequency. So if you build an antenna in the same cube as before, it will have the same frequency.


### How to receive a radio signal:

In order to reveive a signal, you must hold a radio in your hand. A radio is a named vanilla minecraft compass, the name depends on the plugin configuration. If the configuration is set to a colored name, you can not obtain it in vanilla survival mode. If it has no color, you can simply name it with an anvil.
Upon receiving a signal, your radio will display the sending frequency, the signal strenghth in dBm and the broadcasted message.
Per right clicking in the air with the radio in hand, you can tune your radio to a specific tower. If the reception is good enough, the compass will direct you to the antenna.

### Some bonus features:
- autosaves towers to file asynchronously
- checks validity of towers before broadcasting, minimizing risk of glitches (hopefully)
- order in which tower is built doesn't matter
- fancy effect for tower completion
- realistic signal strength based on distance to tower
- based on the Bukkit Event System
- no dependencies to other plugins/libraries
- kinda robust

### Wow, such plugin, much coding
DAthmosSZLtk6LC1wJVcgdXchPXuhb1a9E

