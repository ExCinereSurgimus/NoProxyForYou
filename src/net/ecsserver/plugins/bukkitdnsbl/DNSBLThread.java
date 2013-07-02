package net.ecsserver.plugins.bukkitdnsbl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

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

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

public class DNSBLThread extends BukkitRunnable {

	private Player player;
	private Logger log;
	private Plugin plugin;
	private String chatPrefix;
	private String logPrefix;

	public DNSBLThread(Player player, Plugin plugin)
	{
		player = this.player;
		log = BukkitDNSBL.log;
		chatPrefix = BukkitDNSBL.chatPrefix;
		logPrefix = BukkitDNSBL.logPrefix;
		plugin = this.plugin;
		
	}

	@Override
	public void run() {
		InetSocketAddress sockAddr = player.getAddress();
		InetAddress ip = sockAddr.getAddress();
		String[] breakTehIP = ip.getHostAddress().replace("/","").split(".");
		String reverseIP = breakTehIP[3] + "." + breakTehIP[2]+ "." + breakTehIP[1]+ "." + breakTehIP[0];
		
		// Check DroneBL
		try {
			Lookup lookup = new Lookup(reverseIP + ".dnsbl.dronebl.org", Type.A);
			Record[] records = lookup.run();
			if (records != null) {
				for (Record record : records) {
					ARecord a = (ARecord) record;
					if (a.getAddress().equals(InetAddress.getByName("127.0.0.8"))) { // socks4/5 proxy = 127.0.0.8
						player.kickPlayer("You are listed in DroneBL. Are you a proxy? http://dronebl.org/lookup_branded?ip=" + ip.toString().replace("/",""));
					}
					else if (a.getAddress().equals(InetAddress.getByName("127.0.0.9"))) { // http proxy = 127.0.0.9
						player.kickPlayer("You are listed in DroneBL. Are you a proxy? http://dronebl.org/lookup_branded?ip=" + ip.toString().replace("/",""));
					}
					else if (a.getAddress().equals(InetAddress.getByName("127.0.0.10"))) { // proxy chain = 127.0.0.10
						player.kickPlayer("You are listed in DroneBL. Are you a proxy? http://dronebl.org/lookup_branded?ip=" + ip.toString().replace("/",""));
					}
					else if (a.getAddress().equals(InetAddress.getByName("127.0.0.14"))) { // open wingate proxy = 127.0.0.14
						player.kickPlayer("You are listed in DroneBL. Are you a proxy? http://dronebl.org/lookup_branded?ip=" + ip.toString().replace("/",""));
					}
					else { // catch all others (ddos bots and the like and link them to dronebl
						player.sendMessage(chatPrefix + " You are listed in DroneBL. To find out more goto: http://dronebl.org/lookup_branded?ip=" + ip.toString().replace("/",""));
					}
				}
			}
		}
		catch (Exception e) {
			log.severe(logPrefix + " There was an error! Printing stacktrace!");
			System.out.println(e.getStackTrace());
		}
		
		// Check sectoor: Tor exit node BlackList
		try {
			Lookup lookup = new Lookup(reverseIP + ".tor.dnsbl.sectoor.de", Type.A);
			Record[] records = lookup.run();
			if (records != null) {
				for (Record record : records) {
					ARecord a = (ARecord) record;
					if (a.getAddress().equals(InetAddress.getByName("127.0.0.1"))) {
						player.kickPlayer("You are listed in sectoor. Are you a tor exit node?");
					}
					if (a.getAddress().equals(InetAddress.getByName("127.0.0.2"))) {
						// This is a tor exitnode on the network. wut do here?
					}
				}
			}
		}
		catch (Exception e) {
			log.severe(logPrefix + " There was an error! Printing stacktrace!");
			System.out.println(e.getStackTrace());
		}
		
		// Check spamhaus' XBL list (proxies)
		try {
			Lookup lookup = new Lookup(reverseIP + ".xbl.spamhaus.org", Type.A);
			Record[] records = lookup.run();
			if (records != null) {
				player.kickPlayer("You are listed in spamhaus' XBL list. Are you a proxy? You might have a virus...");
			}
		}
		catch (Exception e) {
			log.severe(logPrefix + " There was an error! Printing stacktrace!");
			System.out.println(e.getStackTrace());
		}
		
		// Check ProxyBL 
		try {
			Lookup lookup = new Lookup(reverseIP + ".dnsbl.proxybl.org", Type.A);
			Record[] records = lookup.run();
			if (records != null) {
				player.kickPlayer("You are listed in ProxyBL. Are you a proxy?");
			}
		}
		catch (Exception e) {
			log.severe(logPrefix + " There was an error! Printing stacktrace!");
			System.out.println(e.getStackTrace());
		}
		
		// Check Sorbs' proxy lists 
		try {
			Lookup lookup = new Lookup(reverseIP + ".proxies.dnsbl.sorbs.net", Type.A);
			Record[] records = lookup.run();
			if (records != null) {
				player.kickPlayer("You are listed in Sorbs' Proxy Blacklist. Are you a proxy?");
			}
		}
		catch (Exception e) {
			log.severe(logPrefix + " There was an error! Printing stacktrace!");
			System.out.println(e.getStackTrace());
		}		
	}
	
}
