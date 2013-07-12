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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

public class DNSBLThread extends BukkitRunnable {
	
	private static Player player;
	private static Plugin plugin;
	private Logger log;
	private String chatPrefix;
	private String logPrefix;
	private String debugPrefix;
	private ArrayList<String> list;
	
	String DroneBL = "DroneBL";
	String Spamhaus = "Spamhaus";
	String ProxyBL = "ProxyBL";
	String Sectoor = "Sectoor";
	String Sorbs = "Sorbs";
	String Tornevall = "Tornevall";

	public DNSBLThread(Plugin plugin) {
		list = BukkitDNSBL.list;
		log = BukkitDNSBL.log;
		debugPrefix = BukkitDNSBL.debugPrefix;
		chatPrefix = BukkitDNSBL.chatPrefix;
		logPrefix = BukkitDNSBL.logPrefix;
		plugin = DNSBLThread.plugin;
	}
	
	public void run() {
		checkIP(DNSBLThread.player);
	}
	
	public void checkIP(Player player) {
		//player = Bukkit.getPlayer(list.get(0)); // This was bad code.
		//list.clear(); // This made it worse!
		
		Player[] playerList = Bukkit.getOnlinePlayers();
		for (Player p : playerList) {
			BukkitDNSBL.debugLog(debugPrefix + " Checking ArrayList for Player " + p.getName());
			if (list.contains(p.getName())) {
				BukkitDNSBL.debugLog(debugPrefix + " Player " + p.getName() + " is in the list.");
				player = Bukkit.getPlayer(p.getName());
				BukkitDNSBL.debugLog(debugPrefix + " Setting Player " + p.getName() + " to player var.");
				list.remove(p.getName());
				BukkitDNSBL.debugLog(debugPrefix + " Removing Player " + p.getName() + " from the list.");
				break;
			}
		}

		BukkitDNSBL.debugLog(debugPrefix + " Player " + player.getName());
		
		InetSocketAddress sockAddr = player.getAddress();
		InetAddress ip = sockAddr.getAddress();
		
		BukkitDNSBL.debugLog(debugPrefix + " IP is " + ip.getHostAddress());
		String[] splitIP = ip.getHostAddress().split("\\.");
		BukkitDNSBL.debugLog(debugPrefix + " String Array is " + splitIP.length); 
		for (String i : splitIP){
			BukkitDNSBL.debugLog(debugPrefix + i);
		}
		String reverseIP = splitIP[3] + "." + splitIP[2]+ "." + splitIP[1]+ "." + splitIP[0];
		
		// Check DroneBL
		try {
			BukkitDNSBL.debugLog(debugPrefix + " Checking DroneBL for ip " + ip.toString().replace("/", ""));
			Lookup lookup = new Lookup(reverseIP + ".dnsbl.dronebl.org", Type.A);
			Record[] records = lookup.run();
			if (records != null) {
				for (Record record : records) {
					ARecord a = (ARecord) record;
					if (a.getAddress().equals(InetAddress.getByName("127.0.0.8"))) { // socks4/5 proxy = 127.0.0.8
						BukkitDNSBL.debugLog(debugPrefix + " We've found a socks proxy in dronebl! Kicking player " + player.getName());
						BukkitDNSBL.kickPlayer(player, "You are listed in DroneBL. Are you a proxy? http://dronebl.org/lookup_branded?ip=" + ip.toString().replace("/",""), DroneBL);
						return;
					}
					else if (a.getAddress().equals(InetAddress.getByName("127.0.0.9"))) { // http proxy = 127.0.0.9
						BukkitDNSBL.debugLog(debugPrefix + " We've found a http proxy in dronebl! Kicking player " + player.getName());
						if (player.isOnline()) {
							BukkitDNSBL.kickPlayer(player, "You are listed in DroneBL. Are you a proxy? http://dronebl.org/lookup_branded?ip=" + ip.toString().replace("/",""), DroneBL);
						}
						return;
					}
					else if (a.getAddress().equals(InetAddress.getByName("127.0.0.10"))) { // proxy chain = 127.0.0.10
						BukkitDNSBL.debugLog(debugPrefix + " We've found a proxy chain in dronebl! Kicking player " + player.getName());
						if (player.isOnline()) {
							BukkitDNSBL.kickPlayer(player, "You are listed in DroneBL. Are you a proxy? http://dronebl.org/lookup_branded?ip=" + ip.toString().replace("/",""), DroneBL);
						}
						return;
					}
					else if (a.getAddress().equals(InetAddress.getByName("127.0.0.14"))) { // open wingate proxy = 127.0.0.14
						BukkitDNSBL.debugLog(debugPrefix + " We've found a winegate proxy in dronebl! Kicking player " + player.getName());
						if (player.isOnline()) {
							BukkitDNSBL.kickPlayer(player, "You are listed in DroneBL. Are you a proxy? http://dronebl.org/lookup_branded?ip=" + ip.toString().replace("/",""), DroneBL);
						}
						return;
					}
					else { // catch all others (ddos bots and the like and link them to dronebl
						player.sendMessage(chatPrefix + " You are listed in DroneBL. To find out more goto: http://dronebl.org/lookup_branded?ip=" + ip.toString().replace("/",""));
						return;
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
			BukkitDNSBL.debugLog(debugPrefix + " Checking sectoor for ip " + ip.toString().replace("/", ""));
			Lookup lookup = new Lookup(reverseIP + ".tor.dnsbl.sectoor.de", Type.CNAME);
			Record[] records = lookup.run();
			if (records != null) {
				for (Record record : records) {
					CNAMERecord a = (CNAMERecord) record;
					if (a.getName().equals("torserver.tor.dnsbl.sectoor.de")) {
						BukkitDNSBL.debugLog(debugPrefix + " We've found a tor exit node in sectoor.de! Kicking player " + player.getName());
						if (player.isOnline()) {
							BukkitDNSBL.kickPlayer(player, "You are listed in sectoor. Are you a tor exit node?", Sectoor);
						}
						return;
					}
					if (a.getName().equals("tornetwork.tor.dnsbl.sectoor.de")) {
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
			BukkitDNSBL.debugLog(debugPrefix + " Checking Spamhaus for ip " + ip.toString().replace("/", ""));
			Lookup lookup = new Lookup(reverseIP + ".xbl.spamhaus.org", Type.A);
			Record[] records = lookup.run();
			if (records != null) {
				BukkitDNSBL.debugLog(debugPrefix + " We've found a record in spamhaus! Kicking player " + player.getName());
				if (player.isOnline()) {
					BukkitDNSBL.kickPlayer(player, "You are listed in spamhaus' XBL list. Are you a proxy? You might have a virus...", Spamhaus);
				}
				return;
			}
		}
		catch (Exception e) {
			log.severe(logPrefix + " There was an error! Printing stacktrace!");
			System.out.println(e.getStackTrace());
		}
		
		// Check ProxyBL 
		try {
			BukkitDNSBL.debugLog(debugPrefix + " Checking ProxyBL for ip " + ip.toString().replace("/", ""));
			Lookup lookup = new Lookup(reverseIP + ".dnsbl.proxybl.org", Type.A);
			Record[] records = lookup.run();
			if (records != null) {
				BukkitDNSBL.debugLog(debugPrefix + " We've found a proxy in ProxyBL! Kicking player " + player.getName());
				if (player.isOnline()) {
					BukkitDNSBL.kickPlayer(player, "You are listed in ProxyBL. Are you a proxy?", ProxyBL);
				}
				return;
			}
		}
		catch (Exception e) {
			log.severe(logPrefix + " There was an error! Printing stacktrace!");
			System.out.println(e.getStackTrace());
		}
		
		// Check Sorbs' proxy lists 
		try {
			BukkitDNSBL.debugLog(debugPrefix + " Checking Sorbs for ip " + ip.toString().replace("/", ""));
			Lookup lookup = new Lookup(reverseIP + ".proxies.dnsbl.sorbs.net", Type.A);
			Record[] records = lookup.run();
			if (records != null) {
				BukkitDNSBL.debugLog(debugPrefix + " We've found a proxy in sorbs! Kicking player " + player.getName());
				if (player.isOnline()) {
					BukkitDNSBL.kickPlayer(player, "You are listed in Sorbs' Proxy Blacklist. Are you a proxy?", Sorbs);
				}
				return;
			}
		}
		catch (Exception e) {
			log.severe(logPrefix + " There was an error! Printing stacktrace!");
			System.out.println(e.getStackTrace());
		}	
		
		// Check Torenvall list
		try {
			BukkitDNSBL.debugLog(debugPrefix + " Checking Tornevall for ip " + ip.toString().replace("/", ""));
			Lookup lookup = new Lookup(reverseIP + ".dnsbl.tornevall.org", Type.A);
			Record[] records = lookup.run();
			if (records != null) {
				for (Record record : records) {
					ARecord a = (ARecord) record;
					if (a.getAddress().equals(InetAddress.getByName("127.0.0.2"))) { // Found a working proxy
						BukkitDNSBL.debugLog(debugPrefix + " We've found a proxy in Tornevall! Kicking player " + player.getName());
						BukkitDNSBL.kickPlayer(player, "You are listed in Sorbs' Proxy Blacklist. Are you a proxy?", Tornevall);
						return;
					}
					else if (a.getAddress().equals(InetAddress.getByName("127.0.0.32"))) { // Looks like tor
						BukkitDNSBL.debugLog(debugPrefix + " We've found a proxy in Tornevall! Kicking player " + player.getName());
						BukkitDNSBL.kickPlayer(player, "You are listed in Sorbs' Proxy Blacklist. Are you a proxy?", Tornevall);
						return;
					}
					else if (a.getAddress().equals(InetAddress.getByName("127.0.0.64"))) { // Abusive Host
						BukkitDNSBL.debugLog(debugPrefix + " We've found a proxy in Tornevall! Kicking player " + player.getName());
						BukkitDNSBL.kickPlayer(player, "You are listed in Sorbs' Proxy Blacklist. Are you a proxy?", Tornevall);
						return;
					}
					else if (a.getAddress().equals(InetAddress.getByName("127.0.0.2"))) { // Anon-state proxy (anonymouse, etc)
						BukkitDNSBL.debugLog(debugPrefix + " We've found a proxy in Tornevall! Kicking player " + player.getName());
						BukkitDNSBL.kickPlayer(player, "You are listed in Sorbs' Proxy Blacklist. Are you a proxy?", Tornevall);
						return;
					}
					else {
						if (player.isOnline()) {
							player.sendMessage(chatPrefix + " You have a preliminary listing in the Tornevall. Are you running a proxy?");
						}
						return;
					}
				}
			}
		}
		catch (Exception e) {
			log.severe(logPrefix + " There was an error! Printing Stacktrace!");
			System.out.println(e.getStackTrace());
		}
	}	
}
