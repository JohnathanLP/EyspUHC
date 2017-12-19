package eyspuhc;

import org.bukkit.plugin.java.JavaPlugin;

public class EyspUHC extends JavaPlugin
{
	@Override
	public void onEnable(){
		this.getCommand("setup").setExecutor(new SetupGameCommand());
		this.getCommand("begin").setExecutor(new BeginGameCommand());
		this.getCommand("depth").setExecutor(new DepthDispCommand());
		getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
	}
	@Override
	public void onDisable(){
		
	}
}
