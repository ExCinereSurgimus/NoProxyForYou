package net.ecsserver.plugins.bukkitdnsbl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

//OH GOD WHY
import org.xbill.DNS.*;

public class DNSBLThread extends BukkitRunnable {

	private Player player;
	private Logger log;
	private Plugin plugin;

	public DNSBLThread(Player player, Plugin plugin)
	{
		player = this.player;
		log = BukkitDNSBL.log;
		plugin = this.plugin;
	}

	@Override
	public void run() {
		InetSocketAddress sockAddr = player.getAddress();
		InetAddress ip = sockAddr.getAddress();
		String[] breakTehIP = ip.getHostAddress().replace("/","").split(".");
		String reverseIP = breakTehIP[3] + "." + breakTehIP[2]+ "." + breakTehIP[1]+ "." + breakTehIP[0];
		String lookupHostname = reverseIP + ".dnsbl.dronebl.org";	
			
		try {
			Lookup lookup = new Lookup(lookupHostname, Type.A);
			lookup.setResolver(new SimpleResolver("dnsbl.dronebl.org"));
			Record[] records = lookup.run();
			if (records != null) {
				player.kickPlayer("You are listed in a DNSBL. Are you a proxy?");
			}
		}
		catch (Exception e) {
			log.severe(BukkitDNSBL.logPrefix + " There was an error! Printing stacktrace!");
			System.out.println(e.getStackTrace());
		}
	}
	
}
