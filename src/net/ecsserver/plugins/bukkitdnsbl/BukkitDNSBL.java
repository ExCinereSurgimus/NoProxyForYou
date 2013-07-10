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

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerJoinEvent;
import org.mcstats.Metrics;

public class BukkitDNSBL extends JavaPlugin implements Listener {
	private static boolean DEBUG = false; // set to false for release builds!
	
	final static Logger log = Logger.getLogger("DNSBL");
	static String logPrefix = "[DNSBL]";
	static String debugPrefix = "[DNSBL-DEBUG]";
	static String chatPrefix = "&3[DNSBL]&f";
	Server server = getServer();
	public static ArrayList<String> list = new ArrayList<String>();
	
	@Override
    public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		    debugLog(debugPrefix + " Sending data to Metrics.");
		} catch (IOException e) {
			debugLog(debugPrefix + " Metrics failed to send!");
		    // Failed to submit the stats :-(
		}
        log.info(logPrefix + " Now checking players against known DNSBLs.");
    }
 
    @Override
    public void onDisable() {
        log.info(logPrefix + " Disabling!");
    }
    
	public static void debugLog(String string) {
		if (DEBUG == true) {
			log.info(string);
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