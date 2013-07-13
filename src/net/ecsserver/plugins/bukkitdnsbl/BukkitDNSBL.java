/*
 *  Copyright (c) 2013 Ex Cinere Surgimus, LLC
 *  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.ecsserver.plugins.bukkitdnsbl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerJoinEvent;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

public class BukkitDNSBL extends JavaPlugin implements Listener {
	private static boolean DEBUG = false; // set to false for release builds!
	final static Logger log = Logger.getLogger("DNSBL");
	static String logPrefix = "[DNSBL]";
	static String debugPrefix = "[DNSBL-DEBUG]";
	static String chatPrefix = "&3[DNSBL]&f";
	Server server = getServer();
	public static ArrayList<String> list = new ArrayList<String>();
    // Plugin Metrics defines.
	Metrics metrics;
    static Graph kicked;
    static Graph dnsblKicks;
    static int playersKicked = 0;
    static int droneblKicks = 0;
    static int proxyblKicks = 0;
    static int spamhausKicks = 0;
    static int sectoorKicks = 0;
    static int sorbsKicks = 0;
    static int tornevallKicks = 0;
    static int unknownKicks = 0; //This should never be used
    
    File metricsDB = new File("plugins" + File.separator + "DNSBL" + File.separator + "metrics.db");
    InputStream stream;
	
	@Override
    public void onEnable() {
		
		int[] integers = null;
		try {
			integers = FlatFileHelper.getFlatFile(metricsDB);
		}
		catch(IOException e) {
			//Treat it as if the file does not exist yet.
			integers = new int[8];
		}
		
		if(integers.length < 7) {
			throw new IllegalStateException("The flatfile is invalid. Consider removal to fix this issue.");
		}
			 	
		playersKicked 	= integers[0];
		droneblKicks 	= integers[1];
		proxyblKicks 	= integers[2];
		spamhausKicks 	= integers[3];
		sectoorKicks 	= integers[4];
		sorbsKicks 		= integers[5];
		tornevallKicks  = integers[6];
		unknownKicks 	= integers[7];
		
		getServer().getPluginManager().registerEvents(this, this);
		
		try {
			metrics = new Metrics(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		kicked = metrics.createGraph("Players kicked for proxys");
		dnsblKicks = metrics.createGraph("IPs listed in DNSBLs");
		metrics.start();
		
		debugLog(debugPrefix + " Sending data to Metrics.");
        log.info(logPrefix + " Now checking players against known DNSBLs.");  
    }
 
    @Override
    public void onDisable() {
    	int[] integers = new int[] {
    			playersKicked,
    			droneblKicks,
    			proxyblKicks,
    			spamhausKicks,
    			sectoorKicks,
    			sorbsKicks,
    			tornevallKicks,
    			unknownKicks
		};
    	
    	boolean hasSaved = FlatFileHelper.saveFlatFile(metricsDB, integers);
    	String logMessage = hasSaved == true ? "Flatfile Database saved." : "FLATFILE DATABASE NOT SAVED!";
    	log.info(logPrefix + logMessage);
    	
        log.info(logPrefix + " Disabling!");
        
    }
    
	public static void debugLog(String string) {
		if (DEBUG == true) {
			log.info(string);
		}
	}
	
	static boolean kickPlayer(Player player, String reason, final String dnsbl) {
		if (player.isOnline()) {
			kicked.addPlotter(new Metrics.Plotter("kicked") {
				@Override
				public int getValue() {
					return playersKicked++;
				}
			});
			dnsblKicks.addPlotter(new Metrics.Plotter(dnsbl) {
				@Override
				public int getValue() {
					if (dnsbl.equals("DroneBL")) {
						return droneblKicks++;
					}
					else if (dnsbl.equals("Spamhaus")) {
						return spamhausKicks++;
					}
					else if (dnsbl.equals("ProxyBL")) {
						return proxyblKicks++;
					}
					else if (dnsbl.equals("Sectoor")) {
						return sectoorKicks++;
					}
					else if (dnsbl.equals("Sorbs")) {
						return sorbsKicks++;
					}
					else if (dnsbl.equals("Tornevall")) {
						return tornevallKicks++;
					}
					else {
						return unknownKicks++;
					}
				}
			});
			log.info(logPrefix + " " + player + "'s IP was found in the " + dnsbl + " DNS Blacklist. Kicking.");
			player.kickPlayer(reason);
			return true;
		}
		else {
			return false;
		}
	}
    
	@EventHandler
	void OnPlayerJoin(PlayerJoinEvent event) {
		debugLog(debugPrefix + " Got login for player " + event.getPlayer().getName());
		Player player = event.getPlayer();
		if (!player.hasPermission("dnsbl.ignore") || !player.isOp()) {
			debugLog(debugPrefix + " Player is not op... Checking player " + player.getName());
			list.add(player.getName());
			new DNSBLThread(this).runTaskAsynchronously(this);
		}
	}
}