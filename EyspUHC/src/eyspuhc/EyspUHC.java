package eyspuhc;

import org.bukkit.plugin.java.JavaPlugin;

public class EyspUHC extends JavaPlugin
{
	@Override
	public void onEnable(){
		this.getCommand("begin").setExecutor(new BeginGameCommand());
	}
	@Override
	public void onDisable(){
		
	}
}
