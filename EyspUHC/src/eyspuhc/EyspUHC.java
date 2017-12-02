package eyspuhc;

import org.bukkit.plugin.java.JavaPlugin;

public class EyspUHC extends JavaPlugin
{
	@Override
	public void onEnable(){
		this.getCommand("setup").setExecutor(new SetupGameCommand());
	}
	@Override
	public void onDisable(){
		
	}
}
