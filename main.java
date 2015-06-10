package BlockyPortals.blockynights;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin {
	
	private static main plugin;
	private PortalCreate portalcreate = new PortalCreate(this);
	
	public static main getPlugin() {
		return plugin;
	}
	public void onEnable() {
		portalcreate = new PortalCreate(this);
		plugin = this;
		Bukkit.getPluginManager().registerEvents(portalcreate, this);
		portalcreate.updateOnEnable();
	}
	public void onDisable() {
		plugin = null;
	}
}
