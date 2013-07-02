package net.ecsserver.plugins.bukkitdnsbl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerJoinEvent;

//OH GOD WHY
import org.xbill.DNS.*;


public class BukkitDNSBL extends JavaPlugin implements Listener {
	public final static Logger log = Logger.getLogger("Minecraft");
	public static String logPrefix = "[DNSBL]";
	public static String chatPrefix = "&3[DNSBL]&f";
	
	@Override
    public void onEnable() {
        log.info(logPrefix + " now checking player's against know DNSBLs.");
    }
 
    @Override
    public void onDisable() {
        log.info(logPrefix + " Disabling!");
    }
    
	@EventHandler
	void OnPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPermission("dnsbl.ignore") || player.isOp()) {
			InetSocketAddress sockAddr = player.getAddress();
			InetAddress ip = sockAddr.getAddress();
			String[] breakTehIP = ip.getHostAddress().replace("\\","").split(".");
			String reverseIP = breakTehIP[3] + "." + breakTehIP[2]+ "." + breakTehIP[1]+ "." + breakTehIP[0];
			String lookupHostname = reverseIP + ".dnsbl.dronebl.org";	
				
			try {
				Lookup lookup = new Lookup(lookupHostname, Type.A);
				lookup.setResolver(new SimpleResolver("dnsbl.dronebl.org"));
				Record[] records = lookup.run();
				if (records != null) {
					player.kickPlayer("You are listed in a DNSBL. Are you a proxy?");
					event.setJoinMessage(chatPrefix + player.getName() + " was kicked due to a listing in a DNSBL!");
				}
			}
			catch (Exception e) {
				log.severe(logPrefix + " There was an error! Printing stacktrace!");
				System.out.println(e.getStackTrace());
			}
		}
	}
}
