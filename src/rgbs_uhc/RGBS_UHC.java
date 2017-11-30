package rgbs_uhc;

import org.bukkit.plugin.java.JavaPlugin;

public class RGBS_UHC extends JavaPlugin
{
	@Override
	public void onEnable(){
		this.getCommand("begin").setExecutor(new BeginGame());
	}
	@Override
	public void onDisable(){
		
	}
}
