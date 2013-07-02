package net.ecsserver.plugins.bukkitdnsbl;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerJoinEvent;


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
		if (!player.hasPermission("dnsbl.ignore") || !player.isOp()) {
			new DNSBLThread(player, this).runTaskAsynchronously(this);
		}
	}
}
