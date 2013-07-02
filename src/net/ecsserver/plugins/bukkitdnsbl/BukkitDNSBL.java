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

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerJoinEvent;


public class BukkitDNSBL extends JavaPlugin implements Listener {
	public final static Logger log = Logger.getLogger("DNSBL");
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
